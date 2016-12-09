package com.iot.newServiceImpl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.iot.socketUtil.OutPutSocketMessage;
import com.iot.mapper.*;
import com.iot.newService.NewSceneService;
import com.iot.pojo.*;
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
public class NewSceneServiceImpl implements NewSceneService {

    private static Logger logger= Logger.getLogger(NewSceneServiceImpl.class);

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

    //添加场景
    public int insertScene(TableScene tableScene) {

        int n= tableSceneMapper.insertSelective(tableScene);
        return n;
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
    public int deleteScene(TableScene tableScene) {
//        //查询区域场景
//        TableRegionScene tableRegionScene=new TableRegionScene();
//        tableRegionScene.setGateway_id(tableScene.getGateway_id());
//        tableRegionScene.setScene_addr(tableScene.getScene_addr());
//        tableRegionScene.setAccount_id(tableScene.getAccount_id());
//        TableRegionScene regionScene=tableRegionSceneMapper.findRegionSceneByAccountIdAndSceneaddrAndGatewayId(tableRegionScene);
        //查询场景成员
//        TableSceneMembers tableSceneMembers=new TableSceneMembers();
//        tableSceneMembers.setAccount_id(tableScene.getAccount_id());
//        tableSceneMembers.setscene_addr(tableScene.getScene_addr());
//        tableSceneMembers.setGateway_id(tableScene.getGateway_id());
//        List<TableSceneMembers> list=tableSceneMembersMapper.selectSceneMemberBySceneaddrAndGatewayIdAndAccountId(tableSceneMembers);
//        int flag=0;
//        if (regionScene!= null){
//            int a=tableRegionSceneMapper.deleteSceneBySence_addrAndGateway_idAndAccount_id(tableRegionScene);
//            if (a<1){
//                return flag;
//            }
//
//        }
//            if (list.size()>0){
//            int b=tableSceneMembersMapper.deleteByAccountIdAndSenceaddrAndGatewayId(tableSceneMembers);
//            if (b<1){
//                return flag;
//            }
//        }
        int n=tableSceneMapper.deleteByAccountIdAndSceneaddrAndGatewayId(tableScene);
//        if (n>0){
//            flag=1;
//        }else {
//            return flag;
//        }
        return n;
    }

    //添加场景成员
    public int insertSceneMembers(TableSceneMembers tableSceneMembers) {

        //TableSceneMembers tableSceneMembers1=tableSceneMembersMapper.selectSceneMemberByDeviceguidAndGatewayIdAndAccountId(tableSceneMembers);
//        if (tableSceneMembers1!=null){
//            return 0;
//        }
        int n=tableSceneMembersMapper.insertSelective(tableSceneMembers);
        return n;
    }



    //删除场景成员
    public int deleteSceneMembers(TableSceneMembers tableSceneMembers) {
        int n=tableSceneMembersMapper.deleteByAccountIdAndScene_addrAndGatewayIdAndDevice_addr(tableSceneMembers);
        return n;
    }

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
}
