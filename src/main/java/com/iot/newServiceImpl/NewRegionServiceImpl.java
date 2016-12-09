package com.iot.newServiceImpl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.iot.socketUtil.OutPutSocketMessage;
import com.iot.mapper.*;

import com.iot.newService.NewRegionService;
import com.iot.pojo.*;
import com.iot.sqlControlUtil.SqlControlUtil;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by adminchen on 16/6/1.
 */
@Service
public class NewRegionServiceImpl implements NewRegionService {

    private static Logger logger= Logger.getLogger(NewRegionServiceImpl.class);
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
    private RegionStatusMapper regionStatusMapper;

    //区域添加
    public int  addRegion(TableRegion tableRegion) {
        //a添加次数
        int a =tableRegionMapper.insert(tableRegion);
        return a;

    }

    //删除区域
    public int deleteRegion(TableRegion tableRegion) {
        //flag=-3删除区域设备失败;-4删除区域组失败;-5删除区域场景失败;;-6删除组成员;-7删除场景成员
        int flag=0;
        //实体类区域设备
        TableRegionDevice tableRegionDevice=new TableRegionDevice();
        tableRegionDevice.setAccount_id(tableRegion.getAccount_id());
        tableRegionDevice.setRegion_addr(tableRegion.getRegion_addr());//
        tableRegionDevice.setGateway_id(tableRegion.getGateway_id());
        tableRegionDevice.setRegion_guid(tableRegion.getRegion_guid());
        //判断区域设备是否存在
        List<TableRegionDevice>  list=tableRegionDeviceMapper.selectByAccountIdAndRegiongGuidGateway_id(tableRegionDevice);
        //System.out.println("区域设备数:"+list.size());
        //判断该区域是否存在
        //n,a
       if (list.size()>0){
            int n=tableRegionDeviceMapper.deleteByRegion_addrAndAccount_idGateway_id(tableRegionDevice);
           if (n<1){
               return flag=-3;
           }
       }
        //实体类区域组
        TableRegionGroup tableRegionGroup=new TableRegionGroup();
        tableRegionGroup.setGateway_id(tableRegion.getGateway_id());
        tableRegionGroup.setRegion_guid(tableRegion.getRegion_guid());
        tableRegionGroup.setAccount_id(tableRegion.getAccount_id());

        //判断区域组是否存在
        List<TableRegionGroup> groupList=tableRegionGroupMapper.findRegionGroupByAccountIdAndRegionguidAndGatewayid(tableRegionGroup);
        if (groupList.size()>0){
            //准备删除组成员,判断是否有组成员
            List<TableGroupMembers> membersList=new ArrayList<TableGroupMembers>();
            TableGroupMembers tableGroupMembers=new TableGroupMembers();
            tableGroupMembers.setAccount_id(tableRegion.getAccount_id());
            tableGroupMembers.setGateway_id(tableRegion.getGateway_id());
            for (TableRegionGroup tableRegionGroup1:groupList){
                tableGroupMembers.setTable_group_guid(tableRegionGroup1.getTable_group_guid());
                tableGroupMembers.setGroup_addr(tableRegionGroup1.getGroup_addr());
                membersList.add(tableGroupMembers);
            }
            for (TableGroupMembers tableGroupMembers1:membersList){
                List<TableGroupMembers> groupMembersList=tableGroupMembersMapper.selectByGroupGuidAndAccount_idAndGateway_id(tableGroupMembers1);
                if (groupMembersList.size()>0){
                    int gm=tableGroupMembersMapper.deleteByAccountIdAndGatewayAndGroup_addr(tableGroupMembers1);
                    if (gm<1){
                        return flag=-6;
                    }
                    //System.out.println("删除组成员成功");
                }
            }
            int p=tableRegionGroupMapper.deleteRegionGroupOfAccount_idAndregion_guidAndgatewayId(tableRegionGroup);

            if (p<1){
                return flag=-4;
            }
        }

        //实体类区域场景
        TableRegionScene tableRegionScene=new TableRegionScene();
        tableRegionScene.setAccount_id(tableRegion.getAccount_id());
        tableRegionScene.setGateway_id(tableRegion.getGateway_id());
        tableRegionScene.setRegion_guid(tableRegion.getRegion_guid());
        //判断区域场景是否存在
        List<TableRegionScene> regionSceneList=tableRegionSceneMapper.findRegionSceneByAccountIdAndRegionguidAndGatewayId(tableRegionScene);
        if (regionSceneList.size()>0){
            //判断场景成员
            List<TableSceneMembers> membersList=new ArrayList<TableSceneMembers>();
            TableSceneMembers tableSceneMembers=new TableSceneMembers();
            tableSceneMembers.setAccount_id(tableRegion.getAccount_id());
            tableSceneMembers.setGateway_id(tableRegion.getGateway_id());
            for (TableRegionScene tableRegionScene1:regionSceneList){
                tableSceneMembers.setscene_addr(tableRegionScene1.getScene_addr());
                tableSceneMembers.setTable_scene_guid(tableRegionScene1.getTable_scene_guid());
                membersList.add(tableSceneMembers);
            }
            for (TableSceneMembers tableSceneMembers1:membersList){
                List<TableSceneMembers> sceneMembersList=tableSceneMembersMapper.selectSceneMemberByScene_guidAndGatewayIdAndAccountId(tableSceneMembers1);
                if (sceneMembersList.size()>0){
                    int sm=tableSceneMembersMapper.deleteByAccountIdAndSenceaddrAndGatewayId(tableSceneMembers1);
                    if (sm<1){
                        return flag-7;
                    }
                    //System.out.println("删除场景成员成功");
                }
            }
            int q=tableRegionSceneMapper.deleteRegionSceneByRegionGuidAndGateway_idAndAccount_id(tableRegionScene);
            if (q<1){
                return flag=-5;
            }
        }


        int a=tableRegionMapper.deleteByRegion_addrAndAccountIdAndGateway_id(tableRegion);
        return a;
//
    }

    //区域修改
    public int  modify(TableRegion tableRegion) {
        //实体类
        TableRegionDevice tableRegionDevice=new TableRegionDevice();
        tableRegionDevice.setAccount_id(tableRegion.getAccount_id());
        tableRegionDevice.setRegion_guid(tableRegion.getRegion_guid());
        tableRegionDevice.setGateway_id(tableRegion.getGateway_id());
        tableRegionDevice.setRegion_name(tableRegion.getRegion_name());
        //实体类
        TableRegionCdtsCtrl tableRegionCdtsCtrl=new TableRegionCdtsCtrl();
        tableRegionCdtsCtrl.setRegion_name(tableRegion.getRegion_name());
        tableRegionCdtsCtrl.setAccount_id(tableRegion.getAccount_id());
        tableRegionCdtsCtrl.setGateway_id(tableRegion.getGateway_id());
        tableRegionCdtsCtrl.setRegion_guid(tableRegion.getRegion_guid());

        //查询区域设备是否有当前这个设备
        List<TableRegionDevice> list=tableRegionDeviceMapper.selectByAccountIdAndRegiongGuidGateway_id(tableRegionDevice);
        if (list.size()>0){
            //a更新次数
            int a=tableRegionDeviceMapper.updateRegionNameByAccountIdAndGatewayIdAndRegionId(tableRegionDevice);
            if (a<1){
                return 0;
            }
        }
        //判断区域条件控制是否存在
        List<TableRegionCdtsCtrl> list1=tableRegionCdtsCtrlMapper.selectByAccoutIdAndGatewayIdAndRegionGuid(tableRegionCdtsCtrl);
            if (list1.size()>0){
                int c=tableRegionCdtsCtrlMapper.updateRegionNameByAccountIdAndGatewayIdAndRegionGuid(tableRegionCdtsCtrl);
                if (c<1){
                    return 0;
                }
            }
        //n更新次数
        int n=tableRegionMapper.updateByUidAndAccountIdAndGatewayID(tableRegion);
        return n;
    }

    //查询用户所有区域
    public List<TableRegion> findAllRegion(TableRegion tableRegion) {

        List<TableRegion> list =tableRegionMapper.selectAccount_id(tableRegion);
        //查询区域是否异常
        for (TableRegion tableRegion1:list){
            RegionStatus regionStatus=regionStatusMapper.selectByPrimaryKey(tableRegion1.getRegion_guid());
            String regionStatuses ="";
            if (regionStatus==null){
                regionStatuses="0";
            }else {
                regionStatuses=regionStatus.getRegion_status();
            }
            tableRegion1.setStatus(regionStatuses);

        }

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
    public int addRegionScene(TableRegionScene tableRegionScene) {
        //n指添加次数
        int n=0;
//        TableRegionScene tableRegionScene1=tableRegionSceneMapper.findRegionSceneByAccountIdAndSceneguidAndGatewayId(tableRegionScene);
//        if (tableRegionScene1!=null){
//            return n;
//        }
        n=tableRegionSceneMapper.insertSelective(tableRegionScene);
        return n;
    }

    //删除区域场景
    public int deleteRegionScene(TableRegionScene tableRegionScene) {
        TableSceneMembers tableSceneMembers=new TableSceneMembers();
        tableSceneMembers.setTable_scene_guid(tableRegionScene.getTable_scene_guid());
        tableSceneMembers.setAccount_id(tableRegionScene.getAccount_id());
        tableSceneMembers.setscene_addr(tableRegionScene.getScene_addr());
        tableSceneMembers.setGateway_id(tableRegionScene.getGateway_id());
        List<TableSceneMembers> list=tableSceneMembersMapper.selectSceneMemberByScene_guidAndGatewayIdAndAccountId(tableSceneMembers);
        if (list.size()>0){
            int a=tableSceneMembersMapper.deleteByAccountIdAndSenceaddrAndGatewayId(tableSceneMembers);
            if (a<1){
                //-1删除失败
                return -1;
            }
            //System.out.println("删除成功");
        }
        int n=tableRegionSceneMapper.deleteSceneBySence_addrAndGateway_idAndAccount_id(tableRegionScene);
        return n;
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
    public int addRegionGroup(TableRegionGroup tableRegionGroup) {
        //n指添加次数
        int n=0;
        List<TableRegionGroup> list=tableRegionGroupMapper.findRegionGroupByAccountIdAndGroupGuidAndGatewayid(tableRegionGroup);
        if (list.size()>0){
            return n;
        }
        n=tableRegionGroupMapper.insertSelective(tableRegionGroup);
        return n;
    }

    //删除区域组
    public int deleteRegionGroup(TableRegionGroup tableRegionGroup) {
        int n=tableRegionGroupMapper.deleteGroupOfAccount_idAndgateway_idAndAddr(tableRegionGroup);
        return n;
    }

    //查找区域组
    public List<TableRegionGroup> findRegionGroup(TableRegionGroup tableRegionGroup) {
        List<TableRegionGroup> list=tableRegionGroupMapper.findRegionGroupByAccountIdAndRegionguidAndGatewayid(tableRegionGroup);
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

    public JSONObject socketAdd(JSONObject jsonObject, String DestinationID, String SourceID, int packegType) {

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

                String status = String.valueOf(result.get("Status"));
                logger.debug("Command : "+sqls[i]+" gateway status : "+ status);

                System.out.println("Command : "+sqls[i]+" gateway status : "+ status);
                if (!"0".equals(status)) {
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

    public TableRegionGroup selectRegionGroupByGroupGuid(TableRegionGroup regionGroup) {

        return tableRegionGroupMapper.findRegionGroupByGroupGuid(regionGroup);
    }
}
