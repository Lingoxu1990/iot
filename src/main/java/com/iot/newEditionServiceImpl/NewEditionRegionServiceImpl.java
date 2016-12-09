package com.iot.newEditionServiceImpl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.iot.socketUtil.OutPutSocketMessage;
import com.iot.exception.BussinessException;
import com.iot.mapper.*;
import com.iot.newEditionService.NewEditionRegionService;
import com.iot.pojo.*;
import com.iot.spitUtil.Param;
import com.iot.sqlControlUtil.SqlControlUtil;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;

/**
 * Created by adminchen on 16/6/1.
 */
@Service
public class NewEditionRegionServiceImpl implements NewEditionRegionService {

    private static Logger logger= Logger.getLogger(NewEditionSceneServiceImpl.class);

    @Resource
    private TableRegionMapper tableRegionMapper ;

    @Resource
    private TableRegionDeviceMapper tableRegionDeviceMapper;

    @Resource
    private TableRegionSceneMapper tableRegionSceneMapper;

    @Resource
    private TableRegionGroupMapper tableRegionGroupMapper;

    @Resource
    private TableRegionCdtsCtrlMapper tableRegionCdtsCtrlMapper;

    @Resource
    private TableGroupMembersMapper tableGroupMembersMapper;

    @Resource
    private TableSceneMembersMapper tableSceneMembersMapper;

    @Resource
    private UserGatewayMapper userGatewayMapper;

    @Resource
    private AccountDataInfoMapper accountDataInfoMapper;

    @Resource
    private  TableSceneMapper tableSceneMapper;

    @Resource
    private TableGroupMapper tableGroupMapper;
    @Resource
    private DispatcherLocationMapper dispatcherLocationMapper;
    @Resource
    private RegionStatusMapper regionStatusMapper;


    //添加区域
    public void addRegion(String user_id, TableRegion tableRegion) {
        List<UserGateway> list=userGatewayMapper.selectByUserId(user_id);

        if (list.size()<1){
            throw new BussinessException("-1","user_id does not exist");
        }

        String account_id=list.get(0).getAccount_id();

        //获取区域地址
        AccountDataInfo accountDataInfo=new AccountDataInfo();
        accountDataInfo.setAccount_id(account_id);
        accountDataInfo.setGateway_id(tableRegion.getGateway_id());
        AccountDataInfo accountDataInfo1 =accountDataInfoMapper.selectByAccountIdAndGatewayId(accountDataInfo);

        if (accountDataInfo1==null){
            throw new BussinessException("-1","Failed to obtain user information");
        }

        int regionaddr = Integer.parseInt(accountDataInfo1.getRegion_addr(), 16) + 1;
        String region_addr=Integer.toHexString(regionaddr);

        //实体类
        tableRegion.setAccount_id(account_id);
        tableRegion.setRegion_addr("ff15::"+region_addr);

        //添加区域
        int a =tableRegionMapper.insert(tableRegion);
        if (a<1){
            throw new BussinessException("-1","region add failed");
        }

        //更新用户信息表
        accountDataInfo.setRegion_addr(region_addr);
        int n=accountDataInfoMapper.updateByAccountIdAndGatewayId(accountDataInfo);
        if (n<1){
            throw new BussinessException("-1","update accountDataInfo failed");
        }

    }

    //删除区域
    public void deleteRegion(String user_id,TableRegion tableRegion,JSONObject jsonObject){
        List<UserGateway> list=userGatewayMapper.selectByUserId(user_id);
        if (list.size()<1){
            throw new BussinessException("-1","user does not exist");
        }

        String account_id=list.get(0).getAccount_id();
        String SourceId=account_id+Param.WEBAPPID+String.valueOf(System.currentTimeMillis()/1000000000);
        tableRegion.setAccount_id(account_id);

        //删除区域组
        //实体类区域组
        TableRegionGroup tableRegionGroup=new TableRegionGroup();
        tableRegionGroup.setGateway_id(tableRegion.getGateway_id());
        tableRegionGroup.setRegion_guid(tableRegion.getRegion_guid());
        tableRegionGroup.setAccount_id(tableRegion.getAccount_id());

        //判断区域组是否存在
        List<TableRegionGroup> groupList=tableRegionGroupMapper.findRegionGroupByAccountIdAndRegionguidAndGatewayid(tableRegionGroup);
        if (groupList.size()>0){
            for (TableRegionGroup regionGroup:groupList){
                tableRegionGroup.setGroup_addr(regionGroup.getGroup_addr());
                //实体类
                TableGroup tableGroup=new TableGroup();
                tableGroup.setAccount_id(account_id);
                tableGroup.setGateway_id(tableRegion.getGateway_id());
                tableGroup.setGroup_addr(regionGroup.getGroup_addr());
                tableGroup.setGroup_guid(regionGroup.getTable_group_guid());
                List<TableGroup> tableGroupList =tableGroupMapper.selectGroup(tableGroup);
                if (tableGroupList.size()>0){
                    for (TableGroup tableGroup1:tableGroupList){
                        //删除组成员
                        TableGroupMembers tableGroupMembers=new TableGroupMembers();
                        tableGroupMembers.setAccount_id(account_id);
                        tableGroupMembers.setGroup_addr(tableGroup1.getGroup_addr());
                        tableGroupMembers.setGateway_id(tableGroup1.getGateway_id());
                        tableGroupMembers.setTable_group_guid(tableGroup1.getGroup_guid());
                        List<TableGroupMembers> groupMembersList=tableGroupMembersMapper.selectByGroupGuidAndAccount_idAndGateway_id(tableGroupMembers);
                        if (groupMembersList.size()>0){
                            for (TableGroupMembers groupMembers:groupMembersList){
                                tableGroupMembers.setDevice_addr(groupMembers.getDevice_addr());

                                JSONArray table_group_members=new JSONArray();
                                JSONObject groupMemberJs=new JSONObject();
                                groupMemberJs.put("device_addr",groupMembers.getDevice_addr());
                                groupMemberJs.put("group_addr",groupMembers.getGroup_addr());
                                //groupMemberJs.put("gateway_id",tableGroup1.getGateway_id());
                                table_group_members.add(groupMemberJs);
                                JSONObject groupMembersJson=new JSONObject();
                                groupMembersJson.put("table_group_members",table_group_members);
                                System.out.println(groupMembersJson.toString());

                                JSONObject socketResult=socketDelete(groupMembersJson,tableGroupMembers.getGateway_id(),SourceId,2);
                                if(socketResult==null){
                                    throw new BussinessException("-1","Gateway socket read time out!");
                                }
                                String status=String.valueOf(socketResult.get("Status"));
                                if (status.equals("1")){
                                    throw new BussinessException("-1","Sub-gateway retrun status '1' means command can't be executed");
                                }
                                if (status.equals("2")){
                                    throw new BussinessException("-1","Sub-gateway return status '2' means deivce is offline");
                                }

                                int n=tableGroupMembersMapper.deleteByAccountIdAndDeviceaddrAndGroup_addrAndGatewayId(tableGroupMembers);
                                if (n<1){
                                    throw new BussinessException("-1","groupMembers delete failed");
                                }
                            }

                        }
                        //删除组
                        tableGroup.setGroup_addr(tableGroup1.getGroup_addr());

//

                        int m=tableGroupMapper.deleteByAccountIdAndGroupaddrAndGateway_id(tableGroup);
                        if (m<1){
                            throw new BussinessException("-1","group delete failed");
                        }
                    }

                }
                //删除区域组
                tableRegionGroup.setGroup_addr(regionGroup.getGroup_addr());

//

                int p=tableRegionGroupMapper.deleteGroupOfAccount_idAndgateway_idAndAddr(tableRegionGroup);
                if(p<1){
                    throw new BussinessException("-1","regionGroup delete failed");
                }
            }
        }

        //删除区域场景
        TableRegionScene tableRegionScene=new TableRegionScene();
        tableRegionScene.setAccount_id(account_id);
        tableRegionScene.setGateway_id(tableRegion.getGateway_id());
        tableRegionScene.setRegion_guid(tableRegion.getRegion_guid());

        List<TableRegionScene> regionSceneList=tableRegionSceneMapper.findRegionSceneByAccountIdAndRegionguidAndGatewayId(tableRegionScene);
        if (regionSceneList.size()>0){
            for (TableRegionScene regionScene:regionSceneList){
                TableScene tableScene=new TableScene();
                tableScene.setGateway_id(tableRegion.getGateway_id());
                tableScene.setAccount_id(account_id);
                tableScene.setScene_addr(regionScene.getScene_addr());
                tableScene.setScene_guid(regionScene.getTable_scene_guid());
                List<TableScene> sceneList=tableSceneMapper.selectSceneAccount_idAndGateway_idAndSceneGuid(tableScene);
                if (sceneList.size()>0){
                    for (TableScene scene:sceneList){
                        //删除场景成员
                        TableSceneMembers tableSceneMembers=new TableSceneMembers();
                        tableSceneMembers.setscene_addr(scene.getScene_addr());
                        tableSceneMembers.setTable_scene_guid(scene.getScene_guid());
                        tableSceneMembers.setAccount_id(account_id);
                        tableSceneMembers.setGateway_id(scene.getGateway_id());
                        List<TableSceneMembers> sceneMembersList=tableSceneMembersMapper.selectSceneMemberByScene_guidAndGatewayIdAndAccountId(tableSceneMembers);
                        if (sceneMembersList.size()>0){
                            for (TableSceneMembers sceneMembers:sceneMembersList){
                                tableSceneMembers.setDevice_addr(sceneMembers.getDevice_addr());

                                JSONArray table_scene_members=new JSONArray();
                                JSONObject sceneMembersJs=new JSONObject();
                                sceneMembersJs.put("device_addr",sceneMembers.getDevice_addr());
                                sceneMembersJs.put("scene_addr",sceneMembers.getscene_addr());
                                //sceneMembersJs.put("gateway_id",sceneMembers.getGateway_id());
                                table_scene_members.add(sceneMembersJs);
                                JSONObject sceneMembersJson=new JSONObject();
                                sceneMembersJson.put("table_scene_members",table_scene_members);

                                JSONObject socketResult3=socketDelete(sceneMembersJson,regionScene.getGateway_id(),SourceId,2);
                                if(socketResult3==null){
                                    throw new BussinessException("-1","Gateway socket read time out!");
                                }
                                String status3=String.valueOf(socketResult3.get("Status"));
                                if (status3.equals("1")){
                                    throw new BussinessException("-1","Sub-gateway retrun status '1' means command can't be executed");
                                }
                                if (status3.equals("2")){
                                    throw new BussinessException("-1","Sub-gateway return status '2' means deivce is offline");
                                }
                                int n=tableSceneMembersMapper.deleteByAccountIdAndScene_addrAndGatewayIdAndDevice_addr(tableSceneMembers);
                                if (n<1){
                                    throw new BussinessException("-1","sceneMembers delete failed");
                                }
                            }

                        }
                        //删除场景
                        tableScene.setScene_addr(scene.getScene_addr());

//

                        int m=tableSceneMapper.deleteByAccountIdAndSceneaddrAndGatewayId(tableScene);
                        if (m<1){
                            throw new BussinessException("-1","scene delete failed");
                        }
                    }

                }
                //删除区域场景
                tableRegionScene.setScene_addr(regionScene.getScene_addr());

//
                int p=tableRegionSceneMapper.deleteSceneBySence_addrAndGateway_idAndAccount_id(tableRegionScene);
                if (p<1){
                    throw new BussinessException("-1","regionScene delete failed");
                }
            }

        }
        //删除区域设备
        TableRegionDevice tableRegionDevice=new TableRegionDevice();
        tableRegionDevice.setAccount_id(account_id);
        tableRegionDevice.setGateway_id(tableRegion.getGateway_id());
        tableRegionDevice.setRegion_guid(tableRegion.getRegion_guid());
        tableRegionDevice.setRegion_addr(tableRegion.getRegion_addr());
        List<TableRegionDevice> regionDeviceList=tableRegionDeviceMapper.selectByAccountIdAndRegiongGuidGateway_id(tableRegionDevice);
        if (regionDeviceList.size()>0){
            for (TableRegionDevice regionDevice:regionDeviceList){
                JSONArray table_region_device=new JSONArray();
                JSONObject regionDeviceJs=new JSONObject();
                regionDeviceJs.put("device_addr",regionDevice.getDevice_addr());
                regionDeviceJs.put("region_addr",regionDevice.getRegion_addr());
                //regionDeviceJs.put("gateway_id",tableRegion.getGateway_id());
                table_region_device.add(regionDeviceJs);
                JSONObject regionSceneJson=new JSONObject();
                regionSceneJson.put("table_region_device",table_region_device);

                JSONObject socketResult6=socketDelete(regionSceneJson,tableRegion.getGateway_id(),SourceId,2);
                if(socketResult6==null){
                    throw new BussinessException("-1","Gateway socket read time out!");
                }
                String status6=String.valueOf(socketResult6.get("Status"));
                if (status6.equals("1")){
                    throw new BussinessException("-1","Sub-gateway retrun status '1' means command can't be executed");
                }
                if (status6.equals("2")){
                    throw new BussinessException("-1","Sub-gateway return status '2' means deivce is offline");
                }
            }

            int n=tableRegionDeviceMapper.deleteByRegion_addrAndAccount_idGateway_id(tableRegionDevice);
            if (n<1){
                throw new BussinessException("-1","regionDevice delete failed");
            }
        }
        //删除区域
        TableRegion tableRegion1=tableRegionMapper.selectByRegionGuidAndGateway_idAndAccount_id(tableRegion);
        if (tableRegion1==null){
            throw new BussinessException("-1","the region does not exist");
        }

//
        int m=tableRegionMapper.deleteByRegion_addrAndAccountIdAndGateway_id(tableRegion);
        if (m<1){
            throw new BussinessException("-1","region delete failed");
        }

    }

    //区域名称修改
    public void  modifyRegionName(TableRegion tableRegion,String userId) {
        List<UserGateway> list=userGatewayMapper.selectByUserId(userId);
        if (list.size()<1){
            throw new BussinessException("-1","user does not exist");
        }
        String accountId=list.get(0).getAccount_id();

        //实体类,修改区域设备
        TableRegionDevice tableRegionDevice=new TableRegionDevice();
        tableRegionDevice.setAccount_id(accountId);
        tableRegionDevice.setRegion_guid(tableRegion.getRegion_guid());
        tableRegionDevice.setGateway_id(tableRegion.getGateway_id());
        tableRegionDevice.setRegion_name(tableRegion.getRegion_name());
        //查询区域设备是否有当前这个设备
        List<TableRegionDevice> tableRegionDeviceList=tableRegionDeviceMapper.selectByAccountIdAndRegiongGuidGateway_id(tableRegionDevice);
        if (tableRegionDeviceList.size()>0){
            //a更新次数
            int a=tableRegionDeviceMapper.updateRegionNameByAccountIdAndGatewayIdAndRegionId(tableRegionDevice);
            if (a<1){
                throw new BussinessException("-1","regionDevice modify region name  failed");
            }
        }
        //n更新次数
        tableRegion.setAccount_id(accountId);
        int n=tableRegionMapper.updateByUidAndAccountIdAndGatewayID(tableRegion);
        if (n<1){
            throw new BussinessException("-1","update region name failed");
        }

    }

    //查询用户所有区域
    public List<TableRegion> findAllRegion(TableRegion tableRegion) {

        List<TableRegion> list =tableRegionMapper.selectAccount_id(tableRegion);

        return list;
    }

    //
    public List<TableRegionDevice> findTheDeleteDevice(TableRegion tableRegion) {

        TableRegionDevice tableRegionDevice=new TableRegionDevice();
        tableRegionDevice.setAccount_id(tableRegion.getAccount_id());
        tableRegionDevice.setRegion_guid(tableRegion.getRegion_guid());//
        tableRegionDevice.setGateway_id(tableRegion.getGateway_id());
        List<TableRegionDevice> list = tableRegionDeviceMapper.selectByAccountIdAndRegiongGuidGateway_id(tableRegionDevice);
        return list;
    }

    //添加区域场景
    public void addRegionScene(String user_id, TableRegionScene tableRegionScene) {
        List<UserGateway> list=userGatewayMapper.selectByUserId(user_id);
        if (list.size()<1){
            throw new BussinessException("-1","user_id does not exist");
        }

        String account_id=list.get(0).getAccount_id();

        //实体类
        tableRegionScene.setAccount_id(account_id);

        //判断区域里是否有该场景
        TableRegionScene tableRegionScene1=tableRegionSceneMapper.findRegionSceneByAccountIdAndSceneguidAndGatewayIdAndRegionGuid(tableRegionScene);
        if (tableRegionScene1!=null){
            throw new BussinessException("-1","The scene already exists ");
        }

        //添加区域场景
         int n=tableRegionSceneMapper.insertSelective(tableRegionScene);
        if(n<1){
            throw new BussinessException("-1","regionScene add failed");
        }

    }



    //删除区域场景
    public void deleteRegionScene(String user_id,TableRegionScene tableRegionScene,JSONObject jsonObject) {
        List<UserGateway> list=userGatewayMapper.selectByUserId(user_id);
        if (list.size()<1){
            throw new BussinessException("-1","user does not exist");
        }

        String account_id=list.get(0).getAccount_id();
        String SourceID=account_id+ Param.WEBAPPID+String.valueOf(System.currentTimeMillis()/1000000000);

        tableRegionScene.setAccount_id(account_id);

        TableSceneMembers tableSceneMembers=new TableSceneMembers();
        tableSceneMembers.setAccount_id(account_id);
        tableSceneMembers.setGateway_id(tableRegionScene.getGateway_id());
        tableSceneMembers.setTable_scene_guid(tableRegionScene.getTable_scene_guid());
        tableSceneMembers.setscene_addr(tableRegionScene.getScene_addr());

        //删除场景成员
        List<TableSceneMembers> tableSceneMembersList=tableSceneMembersMapper.selectSceneMemberByScene_guidAndGatewayIdAndAccountId(tableSceneMembers);
        if (tableSceneMembersList.size()>0){

            JSONArray table_scene_members=new JSONArray();
            JSONObject tableSceneMemberjson=new JSONObject();
            tableSceneMemberjson.put("scene_addr",tableRegionScene.getScene_addr());
            tableSceneMemberjson.put("gateway_id",tableRegionScene.getGateway_id());
            table_scene_members.add(tableSceneMemberjson);
            JSONObject membersJson=new JSONObject();
            membersJson.put("table_scene_members",table_scene_members);

            JSONObject socketResult=socketDelete(membersJson,tableRegionScene.getGateway_id(),SourceID,2);
            if(socketResult==null){
                throw new BussinessException("-1","Gateway socket read time out!");
            }
            String status=String.valueOf(socketResult.get("Status"));
            if (status.equals("1")){
                throw new BussinessException("-1","Sub-gateway retrun status '1' means command can't be executed");
            }
            if (status.equals("2")){
                throw new BussinessException("-1","Sub-gateway return status '2' means deivce is offline");
            }

            int n=tableSceneMembersMapper.deleteByAccountIdAndSenceaddrAndGatewayId(tableSceneMembers);
            if (n<1){
                throw new BussinessException("-1","sceneMembers delete failed");
            }
        }

        //实体类
        TableScene tableScene=new TableScene();
        tableScene.setAccount_id(account_id);
        tableScene.setScene_addr(tableRegionScene.getScene_addr());
        tableScene.setScene_guid(tableRegionScene.getTable_scene_guid());
        tableScene.setGateway_id(tableRegionScene.getGateway_id());
        //删除场景
        List<TableScene> tableSceneList=tableSceneMapper.selectSceneAccount_idAndGateway_idAndSceneGuid(tableScene);
        if (tableSceneList.size()>0){
//            JSONArray table_scene=new JSONArray();
//            JSONObject scenees=new JSONObject();
//            scenees.put("scene_addr",tableScene.getScene_addr());
//            scenees.put("gateway_id",tableScene.getGateway_id());
//            table_scene.add(scenees);
//            JSONObject sceneJson=new JSONObject();
//            sceneJson.put("table_scene",table_scene);
//
//            JSONObject socketResult1=socketDelete(sceneJson,tableScene.getGateway_id(),SourceID,2);
//            if (socketResult1==null){
//                throw new BussinessException("-1","Gateway socket read time out!");
//            }
//
//            String status1=String.valueOf(socketResult1.get("Status"));
//            if (status1.equals("1")){
//                throw new BussinessException("-1","Sub-gateway retrun status '1' means command can't be executed");
//            }
//            if (status1.equals("2")){
//                throw new BussinessException("-1","Sub-gateway return status '2' means deivce is offline");
//            }

            int a=tableSceneMapper.deleteByAccountIdAndSceneaddrAndGatewayId(tableScene);
            if (a<1){
                throw new BussinessException("-1","scene delete failed");
            }
        }

        TableRegionScene tableRegionScene1=tableRegionSceneMapper.findRegionSceneByAccountIdAndSceneguidAndGatewayId(tableRegionScene);
        if (tableRegionScene1==null){
            throw new BussinessException("-1","the regionScene  does not exist");
        }

//        jsonObject.remove("user_id");
//
//        JSONObject socketResult2=socketDelete(jsonObject,tableScene.getGateway_id(),SourceID,2);
//        if (socketResult2==null){
//            throw new BussinessException("-1","Gateway socket read time out!");
//        }
//
//        String status2=String.valueOf(socketResult2.get("Status"));
//        if (status2.equals("1")){
//            throw new BussinessException("-1","Sub-gateway retrun status '1' means command can't be executed");
//        }
//        if (status2.equals("2")){
//            throw new BussinessException("-1","Sub-gateway return status '2' means deivce is offline");
//        }
        int p=tableRegionSceneMapper.deleteSceneBySence_addrAndGateway_idAndAccount_id(tableRegionScene);
        if (p<1){
            throw new BussinessException("-1","regionScene delete failed");
        }
//
    }

    //查找区域场景
    public List<TableRegionScene> findRegionScene(TableRegionScene tableRegionScene) {
        List<TableRegionScene> list=tableRegionSceneMapper.findRegionSceneByAccountIdAndRegionguidAndGatewayId(tableRegionScene);
        return list;
    }

    //查找区域场景(查单个)
    public TableRegionScene findRegionSceneOne(TableRegionScene tableRegionScene){
        TableRegionScene tableRegionScene1=tableRegionSceneMapper.findRegionSceneByAccountIdAndSceneIdAndGatewayId(tableRegionScene);
        return tableRegionScene1;
    }


    //添加区域组
    public void addRegionGroup(String user_id,TableRegionGroup tableRegionGroup) {
        List<UserGateway> list=userGatewayMapper.selectByUserId(user_id);
        if (list.size()<1){
            throw new BussinessException("-1","user does not exist");
        }

        String account_id=list.get(0).getAccount_id();
        //实体类
        tableRegionGroup.setAccount_id(account_id);

        TableRegionGroup regionGroup=tableRegionGroupMapper.findRegionGroupByAccountIdAndGroupGuidAndGatewayidAndRegionGuid(tableRegionGroup);
        if (regionGroup!=null){
            throw new BussinessException("-1","The group already exists ");
        }

        //添加区域组
        int n=tableRegionGroupMapper.insertSelective(tableRegionGroup);
        if (n<1){
            throw new BussinessException("-1","regionGroup add failed");
        }

    }
    //查找区域下的组
//    public List<TableRegionGroup> findRegionGroup(TableRegionGroup tableRegionGroup){
//        List<TableRegionGroup> list=tableRegionGroupMapper.findRegionGroupByAccountIdAndGroupGuidAndGatewayid(tableRegionGroup);
//        return list;
//    }

    //删除区域组
    public void deleteRegionGroup(String user_id,TableRegionGroup tableRegionGroup,JSONObject jsonObject) {
        List<UserGateway> list=userGatewayMapper.selectByUserId(user_id);
        if (list.size()<1){
            throw new BussinessException("-1","user does not exist");
        }

        String account_id=list.get(0).getAccount_id();
        String SourceID=account_id+Param.WEBAPPID+String.valueOf(System.currentTimeMillis()/1000000000);
        tableRegionGroup.setAccount_id(account_id);

        //实体类
        TableGroupMembers tableGroupMembers=new TableGroupMembers();
        tableGroupMembers.setAccount_id(account_id);
        tableGroupMembers.setTable_group_guid(tableRegionGroup.getTable_group_guid());
        tableGroupMembers.setGateway_id(tableRegionGroup.getGateway_id());
        tableGroupMembers.setGroup_addr(tableRegionGroup.getGroup_addr());

        //删除组成员
        List<TableGroupMembers> tableGroupMembersList=tableGroupMembersMapper.selectByGroupGuidAndAccount_idAndGateway_id(tableGroupMembers);
        if (tableGroupMembersList.size()>0){

            JSONObject memberses=new JSONObject();
            memberses.put("group_addr",tableRegionGroup.getGroup_addr());
            memberses.put("gateway_id",tableRegionGroup.getGateway_id());
            JSONArray table_group_members=new JSONArray();
            table_group_members.add(memberses);
            JSONObject membersJson=new JSONObject();
            membersJson.put("table_group_members",table_group_members);

            JSONObject socketResult=socketDelete(membersJson,tableRegionGroup.getGateway_id(),SourceID,2);
            if(socketResult==null){
                throw new BussinessException("-1","Gateway socket read time out!");
            }
            String status=String.valueOf(socketResult.get("Status"));
            if (status.equals("1")){
                throw new BussinessException("-1","Sub-gateway retrun status '1' means command can't be executed");
            }
            if (status.equals("2")){
                throw new BussinessException("-1","Sub-gateway return status '2' means deivce is offline");
            }

            int n=tableGroupMembersMapper.deleteByAccountIdAndGatewayAndGroup_addr(tableGroupMembers);
            if (n<1){
                throw new BussinessException("-1","groupMembers delete failed");
            }
        }

        TableGroup tableGroup=new TableGroup();
        tableGroup.setAccount_id(account_id);
        tableGroup.setGateway_id(tableRegionGroup.getGateway_id());
        tableGroup.setGroup_addr(tableRegionGroup.getGroup_addr());
        tableGroup.setGroup_guid(tableRegionGroup.getTable_group_guid());
        List<TableGroup> groupList=tableGroupMapper.selectGroup(tableGroup);
        if (groupList.size()>0){

//

            int a=tableGroupMapper.deleteByAccountIdAndGroupaddrAndGateway_id(tableGroup);
            if (a<1){
                throw new BussinessException("-1","group delete failed");
            }
        }

        List<TableRegionGroup>tableRegionGroupList=tableRegionGroupMapper.findRegionGroupByAccountIdAndGroupGuidAndGatewayid(tableRegionGroup);
        if (tableRegionGroupList.size()<1){
            throw new BussinessException("-1","the regionGroup does not exist");
        }


        //删除区域组
        int p=tableRegionGroupMapper.deleteGroupOfAccount_idAndgateway_idAndAddr(tableRegionGroup);
        if (p<1){
            throw new BussinessException("-1","regionGroup delete failed");
        }

    }

    //查找区域组
    public List<TableRegionGroup> findRegionGroup(TableRegionGroup tableRegionGroup,String userId) {
        List<UserGateway>  userGatewayList=userGatewayMapper.selectByUserId(userId);
        if (userGatewayList.size()<1){
            throw new BussinessException("-1","user does not exist");
        }
        String accountId=userGatewayList.get(0).getAccount_id();
        tableRegionGroup.setAccount_id(accountId);
        List<TableRegionGroup> list=tableRegionGroupMapper.findRegionGroupByAccountIdAndRegionguidAndGatewayid(tableRegionGroup);
        if (list.size()<1){
            throw new BussinessException("0","Query data is empty");
        }
        return list;
    }
    //查找区域值 region_value
    public TableRegion findTheValueOftheRegion(TableRegion tableRegion) {

        return tableRegionMapper.selectRegionValue(tableRegion);

    }

    public JSONObject socketControlReion(JSONObject jsonObject, String DestinationID, String SourceID, int packegType) {
        Set<String> keyset = jsonObject.keySet();
        JSONObject resultJson =null;

        for (String key : keyset){
            String[] result = SqlControlUtil.controlObjects(key, (JSONArray) jsonObject.get(key));
            for (int i = 0 ; i<result.length;i++){
                OutPutSocketMessage outPutSocketMessage = new OutPutSocketMessage();
                outPutSocketMessage.setPackegType(packegType);//包类型,app写2,web写-1
                outPutSocketMessage.setDestinationID(DestinationID);//app客户端可以写为任意的16个字符串,web填写目标网关地址
                outPutSocketMessage.setType("NULL");//查询就填表名,非查询填写NULL
                outPutSocketMessage.setMessage(key);//消息用于网关返回消息,下发命令的时候,可以写表名
                outPutSocketMessage.setSourceID(SourceID);//源ID
                outPutSocketMessage.setSql(result[i]);//下发的指令(sql语句)
                System.out.println(result[i]);

                //发送数据包

                JSONObject jsonResult = outPutSocketMessage.sendMessag(SourceID);
                resultJson =jsonResult;

            }
        }
        return resultJson;
    }

    public int mysqlControlRegion(TableRegion tableRegion) {


        return tableRegionMapper.mysqlControll(tableRegion);

    }

    public JSONObject socketUpdateRegion(String region_value,String region_guid, String DestinationID, String SourceID, int packegType) {

        String sql = "UPDATE table_region SET region_value = '"+region_value+"' WHERE region_guid='"+region_guid+"'";

        OutPutSocketMessage outPutSocketMessage = new OutPutSocketMessage();
        outPutSocketMessage.setPackegType(packegType);//包类型,app写2,web写-1
        outPutSocketMessage.setDestinationID(DestinationID);//app客户端可以写为任意的16个字符串,web填写目标网关地址
        outPutSocketMessage.setType("NULL");//查询就填表名,非查询填写NULL
        outPutSocketMessage.setMessage("table_region");//消息用于网关返回消息,下发命令的时候,可以写表名
        outPutSocketMessage.setSourceID(SourceID);//源ID
        outPutSocketMessage.setSql(sql);//下发的指令(sql语句)

        return  outPutSocketMessage.sendMessag(SourceID);

    }

    public JSONObject socketAddRegion(JSONObject jsonObject, String DestinationID, String SourceID, int packegType) {

        Set<String> keyset = jsonObject.keySet();
        JSONObject result = new JSONObject();

        for (String key : keyset) {
            String[] sqls = SqlControlUtil.addObjects(key, (JSONArray) jsonObject.get(key));

            for (int i = 0; i < sqls.length; i++) {
                OutPutSocketMessage outPutSocketMessage = new OutPutSocketMessage();
                outPutSocketMessage.setPackegType(packegType);//包类型,app写2,web写-1
                outPutSocketMessage.setDestinationID(DestinationID);//app客户端可以写为任意的16个字符串,web填写目标网关地址
                outPutSocketMessage.setType("NULL");//查询就填表名,非查询填写NULL
                outPutSocketMessage.setMessage(key);//消息用于网关返回消息,下发命令的时候,可以写表名
                outPutSocketMessage.setSourceID(SourceID);//源ID
                outPutSocketMessage.setSql(sqls[i]);//下发的指令(sql语句)

                //发送数据包
                JSONObject reslut = outPutSocketMessage.sendMessag(SourceID);

                String status = String.valueOf(reslut.get("Status"));

                if (!"0".equals(status)) {
                    return reslut;
                }

            }

        }


        return result;
    }

    //删除区域
    public JSONObject socketDeleteRegion(JSONObject jsonObject, String DestinationID, String SourceID, int packegType) {
        JSONObject result = new JSONObject();

        Set<String> keyset = jsonObject.keySet();

        for (String key : keyset) {
            String[] sqls = SqlControlUtil.deleteObjects(key, (JSONArray) jsonObject.get(key));
            for (int i = 0; i < sqls.length; i++) {
                OutPutSocketMessage outPutSocketMessage = new OutPutSocketMessage();
                outPutSocketMessage.setPackegType(packegType);//包类型,app写2,web写-1
                outPutSocketMessage.setDestinationID(DestinationID);//app客户端可以写为任意的16个字符串,web填写目标网关地址
                outPutSocketMessage.setType("NULL");//查询就填表名,非查询填写NULL
                outPutSocketMessage.setMessage(key);//消息用于网关返回消息,下发命令的时候,可以写表名
                outPutSocketMessage.setSourceID(SourceID);//源ID
                outPutSocketMessage.setSql(sqls[i]);//下发的指令(sql语句)
                //发送数据包
                result = outPutSocketMessage.sendMessag(SourceID);


                String status = (String) result.get("Status");
                if (!"0".equals(status)) {
                    return result;
                }
            }
        }

        return result;
    }

    public JSONObject socketDelete(JSONObject jsonObject, String DestinationID, String SourceID, int packegType) {
        JSONObject result = new JSONObject();

        Set<String> keyset = jsonObject.keySet();

        for (String key:keyset){
            String[] sqls= SqlControlUtil.deleteObjects(key, (JSONArray) jsonObject.get(key));
            for (int i=0;i<sqls.length;i++){
                OutPutSocketMessage outPutSocketMessage=new OutPutSocketMessage();
                outPutSocketMessage.setMessage(key);
                outPutSocketMessage.setType("NULL");
                outPutSocketMessage.setDestinationID(DestinationID);
                outPutSocketMessage.setSourceID(SourceID);
                outPutSocketMessage.setPackegType(packegType);
                outPutSocketMessage.setSql(sqls[i]);
                System.out.println(sqls[i]);
                result=outPutSocketMessage.sendMessag(SourceID);
                String status=String.valueOf(result.get("Status"));
                logger.error("Sql Command : " +sqls[i] + " Gateway_Statue : "+status);
                if (!"0".equals(status)){
                    return result;
                }

            }
        }

        return result;
    }

    public List<TableRegionDevice> findDistRegionDevice(TableRegion tableRegion) {
        TableRegionDevice tableRegionDevice=new TableRegionDevice();
        tableRegionDevice.setAccount_id(tableRegion.getAccount_id());
        tableRegionDevice.setRegion_addr(tableRegion.getRegion_addr());//
        tableRegionDevice.setGateway_id(tableRegion.getGateway_id());
        tableRegionDevice.setRegion_guid(tableRegion.getRegion_guid());
        //判断区域设备是否存在
        List<TableRegionDevice>  list=tableRegionDeviceMapper.getDisticRegionDevice(tableRegionDevice);
        return list;
    }

    //添加区域组
    public JSONObject socketAddRegionGroup(JSONObject jsonObject, String DestinationID, String SourceID, int packegType) {
       JSONObject  result = new JSONObject();
        Set<String> keyset = jsonObject.keySet();
        for (String key : keyset) {
            String[] sqls = SqlControlUtil.addObjects(key, (JSONArray) jsonObject.get(key));

            for (int i = 0; i < sqls.length; i++) {
                OutPutSocketMessage outPutSocketMessage = new OutPutSocketMessage();
                outPutSocketMessage.setPackegType(packegType);//包类型,app写2,web写-1
                outPutSocketMessage.setDestinationID(DestinationID);//app客户端可以写为任意的16个字符串,web填写目标网关地址
                outPutSocketMessage.setType("NULL");//查询就填表名,非查询填写NULL
                outPutSocketMessage.setMessage(key);//消息用于网关返回消息,下发命令的时候,可以写表名
                outPutSocketMessage.setSourceID(SourceID);//源ID
                outPutSocketMessage.setSql(sqls[i]);//下发的指令(sql语句)

                System.out.println(sqls[i]);
                //发送数据包
                result = outPutSocketMessage.sendMessag(SourceID);
                String status = String.valueOf(result.get("Status"));
                if (!"0".equals(status)) {
                    return result;
                }
            }

        }
        return result;
    }

    //添加区域场景socket
    public JSONObject socketAddRegionScene(JSONObject jsonObject, String DestinationID, String SourceId, int PackegType) {
        JSONObject result=new JSONObject();
        Set<String> keyset=jsonObject.keySet();
        for (String key:keyset){
            String[] sqls= SqlControlUtil.addObjects(key, (JSONArray) jsonObject.get(key));
            for (int i=0;i<sqls.length;i++){
                OutPutSocketMessage outPutSocketMessage=new OutPutSocketMessage();
                outPutSocketMessage.setMessage(key);
                outPutSocketMessage.setDestinationID(DestinationID);//网关目标网关web
                outPutSocketMessage.setPackegType(PackegType);//包类
                outPutSocketMessage.setSourceID(SourceId);//源id
                outPutSocketMessage.setType("NULL");
                outPutSocketMessage.setSql(sqls[i]);
                System.out.println(sqls[i]);
                //发送数据包
                result=outPutSocketMessage.sendMessag(SourceId);
                String status=String.valueOf(result.get("Status"));
                if (!"0".equals(status)){
                    return result;
                }
            }
        }
        return result;
    }

    //查询区域组
    public TableRegionGroup selectRegionGroupByGroupGuid(TableRegionGroup regionGroup) {
        return tableRegionGroupMapper.findRegionGroupByGroupGuid(regionGroup);
    }

    public int deleteRegionDevice(TableRegionDevice tableRegionDevice){
        return tableRegionDeviceMapper.deleteByRegion_addrAndAccount_idGateway_id(tableRegionDevice);
    }

    public void initRegion(TableRegion tableRegion, String account_id) {

        DispatcherLocation dispatcherLocation = dispatcherLocationMapper.selectByGatewayId(tableRegion.getGateway_id());
        String sourceId = account_id + Param.WEBAPPID + String.valueOf(System.currentTimeMillis() / 1000000000);


        OutPutSocketMessage outPutSocketMessage = new OutPutSocketMessage();
        outPutSocketMessage.setPackegType(Param.PACKEGTYPE);//包类型，int型2
        outPutSocketMessage.setDestinationID(dispatcherLocation.getDispatcher_gateway());//调度者地址（字符串）
        outPutSocketMessage.setType("NULL");//填写“NULL”

        outPutSocketMessage.setMessage("init");//下发的具体数据（拼接后的字符串）
        outPutSocketMessage.setSourceID(sourceId);//消息发送者的ID（前8位与调度者地址相同）
        outPutSocketMessage.setSql(tableRegion.getRegion_guid());//配方操作指令 apply/cancel

        JSONObject result = outPutSocketMessage.sendMessag(sourceId);

        String status = String.valueOf(result.get("Status"));

        if (!status.equals("0")) {
            throw new BussinessException("-1","gateway error");
        }
    }

    //修改区域场景名称
    public void modifyRegionSceneName(TableRegionScene tableRegionScene, String userId) {

        List<UserGateway> list=userGatewayMapper.selectByUserId(userId);
        if (list.size()<1){
            throw new BussinessException("-1","user does not exist");
        }

        String accountId=list.get(0).getAccount_id();
        //查询修改场景名称
        TableScene tableScene=new TableScene();
        tableScene.setAccount_id(accountId);
        tableScene.setScene_guid(tableRegionScene.getTable_scene_guid());
        tableScene.setScene_name(tableRegionScene.getScene_name());
        tableScene.setGateway_id(tableRegionScene.getGateway_id());
        List<TableScene>tableSceneList=tableSceneMapper.selectSceneAccount_idAndGateway_idAndSceneGuid(tableScene);
        if (tableSceneList.size()>0){
            int n=tableSceneMapper.updateByAccountIdAndSceneGuidAndGatewayId(tableScene);
            if (n<1){
                throw new BussinessException("-1","update scene name failed");
            }
        }

        tableRegionScene.setAccount_id(accountId);
        int k=tableRegionSceneMapper.updateByAccountIdAndTableSceneGuidAndGatewayId(tableRegionScene);
        if (k<1){
            throw new BussinessException("-1","update regionScene scene name failed");
        }

    }

    //修改区域组名称
    public void modifyRegionGroupName(TableRegionGroup tableRegionGroup, String userId) {

        List<UserGateway> list=userGatewayMapper.selectByUserId(userId);
        if (list.size()<1){
            throw new BussinessException("-1","user does not exist");
        }
        String accountId= list.get(0).getAccount_id();

        //查询修改组名称
        TableGroup tableGroup=new TableGroup();
        tableGroup.setAccount_id(accountId);
        tableGroup.setGateway_id(tableRegionGroup.getGateway_id());
        tableGroup.setGroup_name(tableRegionGroup.getGroup_name());
        tableGroup.setGroup_guid(tableRegionGroup.getTable_group_guid());
        TableGroup tableGroup1=tableGroupMapper.selectByGroupGuidAndAccountIdAndGatewayId(tableGroup);
        if (tableGroup1!=null){
            int n=tableGroupMapper.updateByGroup_guidAndAccount_idAndGateway_id(tableGroup);
            if (n<1){
                throw new BussinessException("-1","update group name failed");
            }
        }

        tableRegionGroup.setAccount_id(accountId);
        int p=tableRegionGroupMapper.updateByAccountIdAndGroupGuidAndGatewayId(tableRegionGroup);
        if (p<1){
            throw new BussinessException("-1","update regionGroup group name failed");
        }

    }

    public int removeRegionStatus(RegionStatus regionStatus) {

        return regionStatusMapper.deleteByPrimaryKey(regionStatus.getRegion_guid());

    }

    public int insertRegionStatus(RegionStatus regionStatus) {

        return regionStatusMapper.insertSelective(regionStatus);
    }
}
