package com.iot.newEditionServiceImpl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.iot.socketUtil.OutPutSocketMessage;
import com.iot.exception.BussinessException;
import com.iot.mapper.*;
import com.iot.newEditionService.NewEditionSceneService;
import com.iot.pojo.*;
import com.iot.spitUtil.Param;
import com.iot.sqlControlUtil.SqlControlUtil;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;

/**
 * Created by adminchen on 16/6/16.
 */
@Service
public class NewEditionSceneServiceImpl implements NewEditionSceneService{

    private static Logger logger= Logger.getLogger(NewEditionSceneServiceImpl.class);

    @Resource
    private TableSceneMapper tableSceneMapper;

    @Resource
    private TableRegionSceneMapper tableRegionSceneMapper;

    @Resource
    private TableSceneMembersMapper tableSceneMembersMapper;

    @Resource
    private TableDeviceMapper tableDeviceMapper;

    @Resource
    private TableChannelMapper tableChannelMapper;

    @Resource
    private UserGatewayMapper userGatewayMapper;

    @Resource
    private  AccountDataInfoMapper accountDataInfoMapper;

    //添加场景
    public TableScene insertScene(String user_id,TableScene tableScene) {
        List<UserGateway> list=userGatewayMapper.selectByUserId(user_id);
        if (list.size()<1){
            throw new BussinessException("-1","user does not exist");
        }

        String account_id=list.get(0).getAccount_id();

        //获取场景地址
        AccountDataInfo accountDataInfo=new AccountDataInfo();
        accountDataInfo.setAccount_id(account_id);
        accountDataInfo.setGateway_id(tableScene.getGateway_id());
        AccountDataInfo accountDataInfo1 =accountDataInfoMapper.selectByAccountIdAndGatewayId(accountDataInfo);
        String scene_addr=accountDataInfo1.getSence_adde();
        String sceneAddr=Integer.toHexString(Integer.parseInt(scene_addr,16)+1);

        //实体类
        tableScene.setAccount_id(account_id);
        tableScene.setScene_addr("ff15::"+sceneAddr);

        int n= tableSceneMapper.insertSelective(tableScene);
        if (n<1){
            throw new BussinessException("-1","scene add failed");
        }

        //更新用户表
        accountDataInfo.setSence_adde(sceneAddr);
        int a=accountDataInfoMapper.updateByAccountIdAndGatewayId(accountDataInfo);
        if (a<1){
            throw new BussinessException("-1","accountDataInfo update failed");
        }
        return tableScene;

    }

    //修改场景
    public int updateScene(TableScene tableScene) {

            int a=tableSceneMapper.updateByAccountIdAndSceneGuidAndGatewayId(tableScene);

        return a;
    }

    //更新区域场景
    public int  updateRegionScene(TableRegionScene tableRegionScene){
        int n=tableRegionSceneMapper.updateByAccountIdAndTableSceneGuidAndGatewayId(tableRegionScene);
        return n;
    }

    //查询场景
    public List<TableScene> findScene(TableScene tableScene) {
        List<TableScene> list=tableSceneMapper.selectScene(tableScene);
        return list;
    }

    //查询场景


    //查询区域下场景单个
    public TableRegionScene findRegionSceneone(TableRegionScene tableRegionScene){
        TableRegionScene tableRegionScene1=tableRegionSceneMapper.findRegionSceneByAccountIdAndSceneguidAndGatewayId(tableRegionScene);
        return tableRegionScene1;
    }

    //查询区域场景
    public TableRegionScene findRegionScene(TableRegionScene tableRegionScene){

        TableRegionScene regionScene=tableRegionSceneMapper.findRegionSceneByAccountIdAndSceneguidAndGatewayId(tableRegionScene);
        return regionScene;
    }

    //删除区域场景
    public int deleteRegionScene(TableRegionScene tableRegionScene){
        int a=tableRegionSceneMapper.deleteSceneBySence_addrAndGateway_idAndAccount_id(tableRegionScene);
        return a;
    }

    //删除场景
    public void deleteScene(String user_id,TableScene tableScene,JSONObject jsonObject) {
        List<UserGateway> list=userGatewayMapper.selectByUserId(user_id);
        if (list.size()<1){
            throw new BussinessException("-1","user does not exist");
        }

        String account_id=list.get(0).getAccount_id();
        String SourceId = account_id + Param.WEBAPPID + String.valueOf(System.currentTimeMillis() / 1000000000);
        tableScene.setAccount_id(account_id);
        jsonObject.remove("user_id");

        //实体类场景成员
        TableSceneMembers tableSceneMembers=new TableSceneMembers();
        tableSceneMembers.setAccount_id(account_id);
        tableSceneMembers.setGateway_id(tableScene.getGateway_id());
        tableSceneMembers.setscene_addr(tableScene.getScene_addr());
        tableSceneMembers.setTable_scene_guid(tableScene.getScene_guid());
        //删除场景成员
        List<TableSceneMembers> tableSceneMembersList=tableSceneMembersMapper.selectSceneMemberByScene_guidAndGatewayIdAndAccountId(tableSceneMembers);
        if (tableSceneMembersList.size()>0){
            JSONArray table_scene_members=new JSONArray();
            JSONObject table_scene_m=new JSONObject();
            table_scene_m.put("scene_addr",tableScene.getScene_addr());
            table_scene_m.put("gateway_id",tableScene.getGateway_id());
            table_scene_members.add(table_scene_m);
            JSONObject jsonMembers=new JSONObject();
            jsonMembers.put("table_scene_members",table_scene_members);

            JSONObject socketResult=socketDeleteRegionScene(jsonMembers,tableScene.getGateway_id(),SourceId,2);
            if (socketResult==null){
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

        //删除场景
        List<TableScene> tableSceneList =tableSceneMapper.selectSceneAccount_idAndGateway_idAndSceneGuid(tableScene);
        if (tableSceneList.size()<1){
            throw new BussinessException("-1","The scene does not exist");
        }

        JSONObject socketResult1=socketDeleteRegionScene(jsonObject,tableScene.getGateway_id(),SourceId,2);
        if (socketResult1==null){
            throw new BussinessException("-1","Gateway socket read time out!");
        }

        String status1=String.valueOf(socketResult1.get("Status"));
        if (status1.equals("1")){
            throw new BussinessException("-1","Sub-gateway retrun status '1' means command can't be executed");
        }
        if (status1.equals("2")){
            throw new BussinessException("-1","Sub-gateway return status '2' means deivce is offline");
        }

        int a=tableSceneMapper.deleteByAccountIdAndSceneaddrAndGatewayId(tableScene);
        if (a<1){
            throw new BussinessException("-1","scene delete failed");
        }

        //实体类区域场景
        TableRegionScene tableRegionScene=new TableRegionScene();
        tableRegionScene.setAccount_id(account_id);
        tableRegionScene.setTable_scene_guid(tableScene.getScene_guid());
        tableRegionScene.setGateway_id(tableScene.getGateway_id());
        tableRegionScene.setScene_addr(tableScene.getScene_addr());

        // 删除区域场景
        TableRegionScene regionScenees =tableRegionSceneMapper.findRegionSceneByAccountIdAndSceneguidAndGatewayId(tableRegionScene);
        if (regionScenees!=null){
            JSONArray table_region_scene=new JSONArray();
            JSONObject region_sceness=new JSONObject();
            region_sceness.put("scene_addr",tableScene.getScene_addr());
            region_sceness.put("gateway_id",tableScene.getGateway_id());
            table_region_scene.add(region_sceness);
            JSONObject regionScenesjson=new JSONObject();
            regionScenesjson.put("table_region_scene",table_region_scene);

            JSONObject socketResult2=socketDeleteRegionScene(regionScenesjson,tableScene.getGateway_id(),SourceId,2);
            if (socketResult2==null){
                throw new BussinessException("-1","Gateway socket read time out!");
            }

            String status2=String.valueOf(socketResult2.get("Status"));
            if (status2.equals("1")){
                throw new BussinessException("-1","Sub-gateway retrun status '1' means command can't be executed");
            }
            if (status2.equals("2")){
                throw new BussinessException("-1","Sub-gateway return status '2' means deivce is offline");
            }

            int p=tableRegionSceneMapper.deleteSceneBySence_addrAndGateway_idAndAccount_id(tableRegionScene);
            if (p<1){
                throw new BussinessException("-1","regionScene delete failed");
            }
        }

    }

    //添加场景成员
    public void insertSceneMembers(String user_id, TableSceneMembers tableSceneMembers, JSONObject jsonObject, JSONArray jsonArray) {
        List<UserGateway> list=userGatewayMapper.selectByUserId(user_id);
        if (list.size()<1){
            throw new BussinessException("-1","user does not exist");
        }

        String account_id=list.get(0).getAccount_id();
        String SourceId = account_id + Param.WEBAPPID + String.valueOf(System.currentTimeMillis() / 1000000000);
        //实体类
        tableSceneMembers.setAccount_id(account_id);

        //是否有该成员
        List<TableSceneMembers> tableSceneMembersList=tableSceneMembersMapper.selectSceneMemberByScene_guidAndGatewayIdAndAccountIdAndDeviceGuid(tableSceneMembers);
        if (tableSceneMembersList.size()>0){
            throw new BussinessException("-1","SceneMembers already exists.");
        }

        //查看该成员通道数
        TableChannel channel=new TableChannel();
        channel.setAccount_id(account_id);
        channel.setGateway_id(tableSceneMembers.getGateway_id());
        channel.setTable_device_guid(tableSceneMembers.getDevice_guid());
        List<TableChannel> channelList=tableChannelMapper.selectByDevice_guidAndAccout_idAndGateway_id(channel);
        if (channelList.size()<1){
            throw new BussinessException("-1","SceneMembers channel does not exist");
        }

        String[] values=new String[channelList.size()];
        for (int j=0;j<jsonArray.size();j++){
            JSONObject channels=(JSONObject) jsonArray.get(j);
            String channel_numbers=(String) channels.get("channel_number");
            String channel_value=(String) channels.get("value");

            String channelValue="";
            if ((Integer.parseInt(channel_value)<16)){
                channelValue="0"+Integer.toHexString(Integer.parseInt(channel_value));
            }else {
                channelValue=Integer.toHexString(Integer.parseInt(channel_value));
            }

            //System.out.println("channelValue:"+channelValue);
            int channelNumber=Integer.parseInt(channel_numbers)-1;
            //System.out.println("channelNumber:"+channelNumber);
            values[channelNumber]=channelValue;
        }

        //获取设备值
        String device_value="";
        for (int p=0;p<values.length;p++){
            device_value+=values[p];
        }

        tableSceneMembers.setDevice_value(device_value);

        //设置json值
        jsonObject.remove("user_id");
        JSONArray table_scene_members=(JSONArray)jsonObject.get("table_scene_members");
        table_scene_members.remove(0);
        JSONObject scene_member=(JSONObject)JSONObject.toJSON(tableSceneMembers);
        //System.out.println("tableSceneMembers:"+tableSceneMembers.getscene_addr());
        scene_member.remove("id");
        scene_member.remove("account_id");
        scene_member.put("scene_addr",tableSceneMembers.getscene_addr());
        System.out.println("scene_member:"+scene_member.toString());
        table_scene_members.add(scene_member);

        JSONObject socketResult = socketAddSceneMembers(jsonObject, tableSceneMembers.getGateway_id(), SourceId, 2);
        if (socketResult == null) {
                throw new BussinessException("-1","Geteway socket read time out!");
        }
        String status = String.valueOf(socketResult.get("Status"));
        if (status.equals("1")) {
            throw new BussinessException("-1","Sub-gateway retrun status '1' means command can't be executed");
        }
        if (status.equals("2")) {
            throw new BussinessException("-1","Sub-gateway return status '2' means deivce is offline");
        }

        int n=tableSceneMembersMapper.insertSelective(tableSceneMembers);
        if (n<1){
            throw new BussinessException("-1","sceneMembers add failed");
        }

    }

    //删除场景成员
    public void deleteSceneMembers(String user_id, TableSceneMembers tableSceneMembers, JSONObject jsonObject) {
        List<UserGateway> list = userGatewayMapper.selectByUserId(user_id);
        if (list.size()<1){
            throw  new BussinessException("-1","user does not exist");
        }

        //实体类
        String account_id=list.get(0).getAccount_id();
        tableSceneMembers.setAccount_id(account_id);

        String SourceId = account_id + Param.WEBAPPID + String.valueOf(System.currentTimeMillis() / 1000000000);

        //
        jsonObject.remove("user_id");
        JSONObject socketResult = socketDeleteSceneMembers(jsonObject, tableSceneMembers.getGateway_id(), SourceId, 2);

        if (socketResult == null) {
            // todo
            throw  new BussinessException("-1","Geteway socket read time out!");
        }

        String status = String.valueOf(socketResult.get("Status"));
        if (status.equals("1")) {
            throw  new BussinessException("-1","Sub-gateway retrun status '1' means command can't be executed");
        }

        if (status.equals("2")) {
            throw  new BussinessException("-1","Sub-gateway return status '2' means deivce is offline");
        }

        int n=tableSceneMembersMapper.deleteByAccountIdAndScene_addrAndGatewayIdAndDevice_addr(tableSceneMembers);

        if (n<1){
            throw  new BussinessException("-1","SceneMembers delete failed!");
        }
    }


//    //删除场景成员
//    public int deleteSceneMembers(TableSceneMembers tableSceneMembers) {
//        int n=tableSceneMembersMapper.deleteByAccountIdAndScene_addrAndGatewayIdAndDevice_addr(tableSceneMembers);
//        return n;
//    }

    //删除场景下的所有成员
    public int deleteSceneByMembers(TableSceneMembers tableSceneMembers) {
        int n=tableSceneMembersMapper.deleteByAccountIdAndSenceaddrAndGatewayId(tableSceneMembers);
        return n;
    }

    //查询场景成员
    public List<TableSceneMembers> selectSceneMembers(TableSceneMembers tableSceneMembers) {
        List<TableSceneMembers> list=tableSceneMembersMapper.selectSceneMemberByScene_guidAndGatewayIdAndAccountId(tableSceneMembers);
        return list;
    }

    //查询场景成员(该成员)
    public TableSceneMembers  selectSceneMemberByDevice(TableSceneMembers tableSceneMembers){
        TableSceneMembers tableSceneMembers1=tableSceneMembersMapper.selectSceneMemberByDeviceguidAndGatewayIdAndAccountIdAndSceneGuid(tableSceneMembers);
        return tableSceneMembers1;
    }

    //查询场景成员通道
    public List<TableChannel> selectSceneMembersByChannel(TableChannel tableChannel){
        List<TableChannel> list=tableChannelMapper.selectByDevice_guidAndAccout_idAndGateway_id(tableChannel);
        return list;
    }

    //修改场景成员
    public int  updateSceneMembers(TableSceneMembers tableSceneMembers) {
        int n=tableSceneMembersMapper.updateByAccountIdAndSceneMemberGuidAndGatewayId(tableSceneMembers);
        return n;
    }

    //场景应用
    public int sceneApplication(TableScene tableScene){

        int n=tableSceneMapper.updateByAccountIdAndSceneaddrAndGatewayId(tableScene);

        return n;
    }

    //添加场景socket
    public JSONObject socketAddScene(JSONObject jsonObject, String DestinationID, String SourceID, int packegType) {
        JSONObject result= new JSONObject();

        Set<String> keyset = jsonObject.keySet();

        JSONArray list = new JSONArray();
        for (String key : keyset){
            String [] sqls = SqlControlUtil.addObjects(key, (JSONArray) jsonObject.get(key));
            for (int i = 0; i<sqls.length;i++){
                OutPutSocketMessage outPutSocketMessage = new OutPutSocketMessage();
                outPutSocketMessage.setPackegType(packegType);
                outPutSocketMessage.setSourceID(SourceID);
                outPutSocketMessage.setDestinationID(DestinationID);
                outPutSocketMessage.setType("NULL");
                outPutSocketMessage.setMessage(key);
                outPutSocketMessage.setSql(sqls[i]);
                //System.out.println("插入罔顾数据"+sqls[i]);

                result = outPutSocketMessage.sendMessag(SourceID);
                String status = String.valueOf(result.get("Status")) ;

                logger.error("Sql Command : " +sqls[i] + " Gateway_Statue : "+status);
                if (!"0".equals(status)){
                   return result;
                }
            }
        }

        return result;

    }

    //添加场景成员socket
    public JSONObject socketAddSceneMembers(JSONObject jsonObject,String DestinationID,String SourceID,int packegType){
        JSONObject result=new JSONObject();
        Set<String> keyset=jsonObject.keySet();
        //JSONArray list=new JSONArray();
        for (String key:keyset){
            String[] sqls= SqlControlUtil.addObjects(key, (JSONArray) jsonObject.get(key));
            for (int i=0;i<sqls.length;i++){
                OutPutSocketMessage outPutSocketMessage=new OutPutSocketMessage();
                outPutSocketMessage.setMessage(key);
                outPutSocketMessage.setDestinationID(DestinationID);
                outPutSocketMessage.setType("NULL");
                outPutSocketMessage.setSourceID(SourceID);
                outPutSocketMessage.setPackegType(packegType);
                outPutSocketMessage.setSql(sqls[i]);
                System.out.println(sqls[i]);

                result=outPutSocketMessage.sendMessag(SourceID);
                String status=String.valueOf(result.get("Status"));
                logger.error("Sql Command:"+sqls[i]+"Gateway_Status:"+status);
                if (!"0".equals(status)){
                    return result;
                }
            }
        }
        return result;


    }

    //删除场景成员socket
    public JSONObject socketDeleteSceneMembers(JSONObject jsonObject,String DestinationID,String SourceID,int packegType){
        JSONObject result=new JSONObject();
        Set<String> keyset=jsonObject.keySet();
        for (String key:keyset){
            String[] sqls= SqlControlUtil.deleteObjects(key, (JSONArray) jsonObject.get(key));
            for (int i=0;i<sqls.length;i++){
                OutPutSocketMessage outPutSocketMessage=new OutPutSocketMessage();
                outPutSocketMessage.setMessage(key);
                outPutSocketMessage.setSql(sqls[i]);
                outPutSocketMessage.setPackegType(packegType);
                outPutSocketMessage.setSourceID(SourceID);
                outPutSocketMessage.setDestinationID(DestinationID);
                outPutSocketMessage.setType("NULL");
                System.out.println(sqls[i]);
                result=outPutSocketMessage.sendMessag(SourceID);
                String status=String.valueOf(result.get("Status"));
                logger.error("Sql Command:"+sqls[i]+"Gateway_Statue:"+status);
                if (!"0".equals(status)){
                    return result;
                }
            }
        }
        return result;
    }

    //删除场景成员socket
    public JSONObject socketDeleteScene(JSONObject jsonObject,String DestiantionID,String SourceID, int packegType){

        JSONObject result=new JSONObject();
        Set<String> keyset=jsonObject.keySet();
        for (String key:keyset){
            String[] sqls= SqlControlUtil.deleteObjects(key, (JSONArray) jsonObject.get(key));
            for (int i=0;i<sqls.length;i++){
                OutPutSocketMessage outPutSocketMessage=new OutPutSocketMessage();
                outPutSocketMessage.setMessage(key);
                outPutSocketMessage.setType("NULL");
                outPutSocketMessage.setDestinationID(DestiantionID);
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

    //删除区域场景socket
    public JSONObject socketDeleteRegionScene(JSONObject jsonObject,String DestiantionID,String SourceID, int packegType){

        JSONObject result=new JSONObject();
        Set<String> keyset=jsonObject.keySet();
        for (String key:keyset){
            String[] sqls= SqlControlUtil.deleteObjects(key, (JSONArray) jsonObject.get(key));
            for (int i=0;i<sqls.length;i++){
                OutPutSocketMessage outPutSocketMessage=new OutPutSocketMessage();
                outPutSocketMessage.setMessage(key);
                outPutSocketMessage.setType("NULL");
                outPutSocketMessage.setDestinationID(DestiantionID);
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

    //修改场景成员socket
    public JSONObject socketModifySceneMembers(JSONObject jsonObject,String DestiantionID,String SourceID,int packegType){

        JSONObject result=new JSONObject();
        Set<String> keyset=jsonObject.keySet();
        for (String key:keyset){
            System.out.println(jsonObject.get(key));
            String[] sqls= SqlControlUtil.putObjects(key, (JSONArray) jsonObject.get(key));
            for (int i=0;i<sqls.length;i++){
                OutPutSocketMessage outPutSocketMessage=new OutPutSocketMessage();
                outPutSocketMessage.setMessage(key);
                outPutSocketMessage.setSql(sqls[i]);
                outPutSocketMessage.setPackegType(packegType);
                outPutSocketMessage.setSourceID(SourceID);
                outPutSocketMessage.setDestinationID(DestiantionID);
                outPutSocketMessage.setType("NULL");
                System.out.println(sqls[i]);
                result=outPutSocketMessage.sendMessag(SourceID);
                String status=String.valueOf(result.get("Status"));
                logger.error("Sql Command:"+sqls[i]+"Gateway_Statue:"+status);
                if (!"0".equals(status)){
                    return result;
                }
            }
        }
        return result;
    }

    //修改场景Socket
    public JSONObject socketmodifyScene(JSONObject jsonObject,String DestiantionID,String SourceID,int packegType){

        JSONObject result=new JSONObject();
        Set<String> keyset=jsonObject.keySet();
        for (String key:keyset){
            System.out.println(jsonObject.get(key));
            String[] sqls= SqlControlUtil.putObjects(key, (JSONArray) jsonObject.get(key));
            for (int i=0;i<sqls.length;i++){
                OutPutSocketMessage outPutSocketMessage=new OutPutSocketMessage();
                outPutSocketMessage.setMessage(key);
                outPutSocketMessage.setSql(sqls[i]);
                outPutSocketMessage.setPackegType(packegType);
                outPutSocketMessage.setSourceID(SourceID);
                outPutSocketMessage.setDestinationID(DestiantionID);
                outPutSocketMessage.setType("NULL");
                System.out.println(sqls[i]);
                result=outPutSocketMessage.sendMessag(SourceID);
                String status=String.valueOf(result.get("Status"));
                logger.error("Sql Command:"+sqls[i]+"Gateway_Statue:"+status);
                if (!"0".equals(status)){
                    return result;
                }
            }
        }
        return result;
    }

    //修改区域场景
    //修改场景Socket
    public JSONObject socketmodifyRegionScene(JSONObject jsonObject,String DestiantionID,String SourceID,int packegType){


        JSONObject result=new JSONObject();
        Set<String> keyset=jsonObject.keySet();
        for (String key:keyset){
            //System.out.println(jsonObject.get(key));
            String[] sqls= SqlControlUtil.putObjects(key, (JSONArray) jsonObject.get(key));
            //System.out.println(sqls);
            for (int i=0;i<sqls.length;i++){
                OutPutSocketMessage outPutSocketMessage=new OutPutSocketMessage();
                outPutSocketMessage.setMessage(key);
                outPutSocketMessage.setSql(sqls[i]);
                outPutSocketMessage.setPackegType(packegType);
                outPutSocketMessage.setSourceID(SourceID);
                outPutSocketMessage.setDestinationID(DestiantionID);
                outPutSocketMessage.setType("NULL");
                System.out.println(sqls[i]);
                result=outPutSocketMessage.sendMessag(SourceID);
                String status=String.valueOf(result.get("Status"));
                logger.error("Sql Command:"+sqls[i]+"Gateway_Statue:"+status);
                if (!"0".equals(status)){
                    return result;
                }
            }
        }
        return result;
    }

    //场景控制socket
    public JSONObject socketApplication(JSONObject jsonObject,String DestiantionID,String SourceID,int packegType){
        System.out.println("num:"+jsonObject.size());
        JSONObject result=new JSONObject();
        Set<String> keyset=jsonObject.keySet();

        for (String key:keyset){
            System.out.println(key);
            String[] sqls= SqlControlUtil.controlObjects(key, (JSONArray) jsonObject.get(key));
            for (int i=0;i<sqls.length;i++){
                OutPutSocketMessage outPutSocketMessage=new OutPutSocketMessage();
                outPutSocketMessage.setMessage(key);
                outPutSocketMessage.setType("NULL");
                outPutSocketMessage.setDestinationID(DestiantionID);
                outPutSocketMessage.setSourceID(SourceID);
                outPutSocketMessage.setPackegType(packegType);
                outPutSocketMessage.setSql(sqls[i]);
                System.out.println(sqls[i]);
                result=outPutSocketMessage.sendMessag(SourceID);
                String status=String.valueOf(result.get("Status"));
                logger.error("Sql Command:"+sqls[i]+"Gateway_Statue:"+status);
                if ("0".equals(status)){
                    return result;
                }
            }
        }
        return  result;
    }

    //查询该设备
    public TableDevice selectDevice(TableDevice tableDevice){
        TableDevice tableDevice1=tableDeviceMapper.selectByDevice_guidAndAccount_idAndGateway_id(tableDevice);
        return tableDevice1;
    }

//    public int deleteByMembers(TableSceneMembers tableSceneMembers) {
//        return 0;
//    }
}
