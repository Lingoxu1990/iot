package com.iot.serviceImp;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.iot.socketUtil.OutPutSocketMessage;
import com.iot.mapper.TableSceneMapper;
import com.iot.mapper.TableSceneMembersMapper;
import com.iot.message.Message;
import com.iot.pojo.*;
import com.iot.service.SceneService;
import com.iot.sqlControlUtil.SqlControlUtil;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * Created by Jacob on 16/4/6.
 */
@Service
public class SceneServiceImpl implements SceneService {

    private static Logger logger =Logger.getLogger(SceneServiceImpl.class);
    @Resource
    private TableSceneMapper tableSceneMapper;
    @Resource
    private TableSceneMembersMapper tableSceneMembersMapper;
    //修改场景

    public Message modifyScene(JSONObject jsonObject, String DestinationID, String SourceID, int packegType) {
        Message message = new Message();
        Set<String> keyset = jsonObject.keySet();
        for (String key : keyset) {
            String [] result = SqlControlUtil.putObjects(key, (JSONArray) jsonObject.get(key));
            for (int i = 0;i<result.length;i++){
                OutPutSocketMessage outPutSocketMessage = new OutPutSocketMessage();
                outPutSocketMessage.setPackegType(packegType);
                outPutSocketMessage.setDestinationID(DestinationID);
                outPutSocketMessage.setType("NULL");
                outPutSocketMessage.setMessage(key);
                outPutSocketMessage.setSourceID(SourceID);
                outPutSocketMessage.setSql(result[i]);


                JSONObject json = new JSONObject();
                StringBuilder sb= new StringBuilder();
                sb.append(result[i]);
                StringBuilder SB1 =new StringBuilder();
                SB1.append(result[i]);

                sb.delete(0,sb.indexOf("SET")+4);
                sb.delete(sb.indexOf("WHERE")-1,sb.lastIndexOf("'"));
                sb.deleteCharAt(sb.lastIndexOf("'"));

                String [] kvs = sb.toString().split(",");

                for (int j = 0; j <kvs.length ; j++) {
                    String [] kv = kvs[j].split("=");
                    StringBuilder s = new StringBuilder();
                    s.append(kv[1]);
                    s.deleteCharAt(s.indexOf("'"));
                    s.deleteCharAt(s.lastIndexOf("'"));
                    json.put(kv[0],s.toString());
                }
                SB1.delete(0,SB1.indexOf("WHERE")+6);

                String[] kv1 = SB1.toString().split("=");
                StringBuilder ss = new StringBuilder();
                ss.append(kv1[1]);
                ss.deleteCharAt(ss.indexOf("'"));
                ss.deleteCharAt(ss.lastIndexOf("'"));
                json.put(kv1[0],ss.toString());
                json.put("account_id",SourceID.substring(0,10));

                TableScene tableScene = JSONObject.parseObject(json.toString(),TableScene.class);

                int num = tableSceneMapper.updateByAccountIdAndSceneGuid(tableScene);

                if (num!=1){
                    message.setCode("-1");
                    message.setContent(json);
                    message.setMessage("Update the scene failed!");
                }


                System.out.println(outPutSocketMessage.test().toString());

//                JSONObject jsonResult = outPutSocketMessage.sendMessag(SourceID);
//                String status = (String) jsonResult.get("Status");
//                if(!"0".equals(status)){
//                    message.setCode("-1");
//                    message.setContent(jsonResult);
//                    message.setMessage("场景修改失败");
//                }
            }
        }
        message.setCode("0");
        message.setContent("[]");
        message.setMessage("Update the scene successfully!");
        return message;
    }

    //查找场景

    public Message findScene(JSONObject jsonObject, String DestinationID, String SourceID, int packegType) {
        Message message = new Message();
        String result = "";
        JSONArray list= new JSONArray();

        Set<String> keyset = jsonObject.keySet();
        for (String key : keyset){
            result = SqlControlUtil.selectObjects(key, (JSONObject) jsonObject.get(key));
            OutPutSocketMessage outPutSocketMessage = new OutPutSocketMessage();
            outPutSocketMessage.setPackegType(packegType);
            outPutSocketMessage.setDestinationID(DestinationID);
            outPutSocketMessage.setMessage(key);
            outPutSocketMessage.setType(key);
            outPutSocketMessage.setSourceID(SourceID);
            outPutSocketMessage.setSql(result);


//            JSONObject group = (JSONObject) jsonObject.get(key);
//
//            TableScene tableScene = JSONObject.parseObject(group.toString(),TableScene.class);
//
//            tableScene.setAccount_id(SourceID.substring(0,10));
//
//            List<TableScene> tableGroups = tableSceneMapper.selectScene(tableScene);
//
//            if (tableGroups.size()<1) {
//                message.setCode("-1");
//                message.setMessage("No result!");
//                message.setContent(list.toString());
//                return message;
//            }
//            for (TableScene r: tableGroups) {
//                JSONObject rr = (JSONObject) JSONObject.toJSON(r);
//                list.add(rr);
//
//            }
//
//            System.out.println(outPutSocketMessage.test().toString());

            JSONObject jsonResult = outPutSocketMessage.sendMessag(SourceID);
            String status = String.valueOf(jsonResult.get("Status"));
            logger.error("Sql Command : " +result + " Gateway_Statue : "+status);
            if(!"0".equals(status)){
                message.setCode("-1");
                message.setContent("[]");
                message.setMessage("Search Scene failed!");
                return message;
            }
            list.add(jsonResult.get("List"));
        }
        message.setCode("0");
        message.setMessage("Search successfully!");
        message.setContent(list);
        return message;
    }

    //添加场景

    public Message addScene(JSONObject jsonObject, String DestinationID, String SourceID, int packegType) {
        Message message = new Message();
        Set<String> keyset = jsonObject.keySet();

        JSONArray  list = new JSONArray();
        for (String key : keyset){
            String [] result = SqlControlUtil.addObjects(key, (JSONArray) jsonObject.get(key));
            for (int i = 0; i<result.length;i++){
                OutPutSocketMessage outPutSocketMessage = new OutPutSocketMessage();
                outPutSocketMessage.setPackegType(packegType);
                outPutSocketMessage.setSourceID(SourceID);
                outPutSocketMessage.setDestinationID(DestinationID);
                outPutSocketMessage.setType("NULL");
                outPutSocketMessage.setMessage(key);
                outPutSocketMessage.setSql(result[i]);

                StringBuilder sb= new StringBuilder();
                sb.append(result[i]);
                StringBuilder SB1 =new StringBuilder();
                SB1.append(result[i]);

                sb.delete(0,sb.lastIndexOf("("));
                sb.deleteCharAt(sb.lastIndexOf("("));
                sb.deleteCharAt(sb.lastIndexOf(")"));
                String []  values = sb.toString().split(",");

                String [] keys = SB1.substring(SB1.indexOf("(")+1,SB1.indexOf(")")).split(",");

                JSONObject json = new JSONObject();

                for (int j = 0; j <values.length ; j++) {
                    json.put(keys[j].trim(),values[j].substring(values[j].indexOf("'")+1,values[j].lastIndexOf("'")));
                }
//                json.put("id",UUID.randomUUID().toString());
//                json.put("account_id",SourceID.substring(0,10));
                list.add(json);

//                TableScene tableScene = JSONObject.parseObject(json.toString(),TableScene.class);
//
//                int num = tableSceneMapper.insertSelective(tableScene);
//
//                if(num!=1){
//                    message.setCode("-1");
//                    message.setMessage("Scene create failed!");
//                    message.setContent("[]");
//                }
//                System.out.println(outPutSocketMessage.test().toString());


                JSONObject jsonResult = outPutSocketMessage.sendMessag(SourceID);
                String status = String.valueOf(jsonResult.get("Status")) ;
                logger.error("Sql Command : " +result[i] + " Gateway_Statue : "+status);
                if (!"0".equals(status)){
                    message.setCode("-1");
                    message.setMessage("Failed to create scene");
                    message.setContent("[]");
                    return message;
                }
            }
        }
        message.setCode("0");
        message.setMessage("Scene create successfully!");
        message.setContent(list);
        return message;
    }

    //删除场景
    public Message deleteScene(JSONObject jsonObject, String DestinationID, String SourceID, int packegType) {
        Message message = new Message();
        Set<String> keyset = jsonObject.keySet();
        for (String key : keyset){
            String [] result = SqlControlUtil.deleteObjects(key, (JSONArray) jsonObject.get(key));
            for (int i = 0;i<result.length;i++){
                OutPutSocketMessage outPutSocketMessage = new OutPutSocketMessage();
                outPutSocketMessage.setPackegType(packegType);
                outPutSocketMessage.setSourceID(SourceID);
                outPutSocketMessage.setDestinationID(DestinationID);
                outPutSocketMessage.setType("NULL");
                outPutSocketMessage.setMessage(key);
                outPutSocketMessage.setSql(result[i]);


//                StringBuilder sb= new StringBuilder();
//                sb.append(result[i]);
//                StringBuilder SB1 =new StringBuilder();
//                SB1.append(result[i]);
//
//                sb.delete(0,sb.indexOf("WHERE")+6);
//
//                SB1.delete(0,SB1.lastIndexOf("scene_guid"));
//
//                String [] kv1=SB1.toString().split("=");
//
//
//                JSONObject json  =new JSONObject();
//
//                StringBuilder stringBuilder1 = new StringBuilder();
//                stringBuilder1.append(kv1[1].trim());
//                stringBuilder1.deleteCharAt(0);
//                stringBuilder1.deleteCharAt(stringBuilder1.length()-1);
//
//
//                json.put(kv1[0],stringBuilder1.toString());
//                json.put("account_id",SourceID.substring(0,10));
//                System.out.println(json.toString());
//
//                TableScene tableScene = JSONObject.parseObject(json.toString(),TableScene.class);
//
//                int num =tableSceneMapper.deleteByAccountIdAndSceneGuid(tableScene);
//
//                if (num!=1){
//                    message.setCode("-1");
//                    message.setContent("[]");
//                    message.setMessage("Scene delete failed!");
//                    return message;
//                }
//                System.out.println(outPutSocketMessage.test().toString());


                JSONObject jsonResult = outPutSocketMessage.sendMessag(SourceID);
                String status = String.valueOf(jsonResult.get("Status"));
                if(!"0".equals(status)){
                    message.setCode("-1");
                    message.setMessage("Failed to delete the region scene!");
                    message.setContent(jsonResult);
                    return message;
                }
            }
        }
        message.setCode("0");
        message.setMessage("Delete from region scene successfully!");
        message.setContent("[]");
        return message;
    }

    public Message controlScene(JSONObject jsonObject, String DestinationID, String SourceID, int packegType) {
        Set<String> keyset = jsonObject.keySet();
        Message message = new Message();

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
//
//                JSONObject json = new JSONObject();
//
//                StringBuilder sb= new StringBuilder();
//                sb.append(result[i]);
//                StringBuilder SB1 =new StringBuilder();
//                SB1.append(result[i]);
//
//                sb.delete(0,sb.indexOf("SET")+4);
//                sb.delete(sb.indexOf("WHERE")-1,sb.lastIndexOf("'"));
//                sb.deleteCharAt(sb.lastIndexOf("'"));
//
//                String [] kvs = sb.toString().split(",");
//
//                for (int j = 0; j <kvs.length ; j++) {
//                    String [] kv = kvs[j].split("=");
//                    StringBuilder s = new StringBuilder();
//                    s.append(kv[1]);
//                    s.deleteCharAt(s.indexOf("'"));
//                    s.deleteCharAt(s.lastIndexOf("'"));
//                    json.put(kv[0],s.toString());
//                }
//                SB1.delete(0,SB1.indexOf("WHERE")+6);
//
//                String[] kv1 = SB1.toString().split("=");
//                StringBuilder ss = new StringBuilder();
//                ss.append(kv1[1]);
//                ss.deleteCharAt(ss.indexOf("'"));
//                ss.deleteCharAt(ss.lastIndexOf("'"));
//                json.put(kv1[0],ss.toString());
//                json.put("account_id",SourceID.substring(0,10));
//
//                TableGroup tableGroup = JSONObject.parseObject(json.toString(),TableGroup.class);
//
//                int num = tableGroupMapper.updateByAccountId(tableGroup);
//
//                if (num!=1){
//                    message.setCode("-1");
//                    message.setContent(json);
//                    message.setMessage("Update the group failed!");
//                }
//
//                System.out.println(outPutSocketMessage.test().toString());
                //发送数据包
                JSONObject jsonResult = outPutSocketMessage.sendMessag(SourceID);
                String status = String.valueOf(jsonResult.get("Status")) ;
                if (!"0".equals(status)) {

                    if ("2".equals(status)){
                        message.setCode("-1");
                        message.setContent(new JSONArray());
                        message.setMessage("Gateway Error : Device is offline!");
                    }else {
                        message.setCode("-1");
                        message.setContent("[]");
                        message.setMessage("Failed to control the scene");
                    }

                    return message;
                }
            }
        }
        message.setCode("0");
        message.setContent("[]");
        message.setMessage("control the scene successfully!");
        return message;
    }

    public Message addSceneMember(JSONObject jsonObject, String DestinationID, String SourceID, int packegType) {

        Message message = new Message();
        Set<String> keyset = jsonObject.keySet();

        JSONArray list = new JSONArray();
        for (String key : keyset) {
            String [] result = SqlControlUtil.addObjects(key, (JSONArray) jsonObject.get(key));
            for (int i = 0 ; i < result.length ; i ++){
                OutPutSocketMessage outPutSocketMessage = new OutPutSocketMessage();
                outPutSocketMessage.setPackegType(packegType);//包类型，app为2,web为-1
                outPutSocketMessage.setDestinationID(DestinationID);//app客户端可以写任意的16个字符串，web填写相关网关地址
                outPutSocketMessage.setType("NULL");//查询填写表明，非查询就填写“NULL”
                outPutSocketMessage.setMessage("NULL");//消息同于网关返回消息，下发命令的时候可以填写表明
                outPutSocketMessage.setSourceID(SourceID);//源ID
                outPutSocketMessage.setSql(result[i]);//下发sql语句指令
                System.out.println(result[i]);

//                StringBuilder sb= new StringBuilder();
//                sb.append(result[i]);
//                StringBuilder SB1 =new StringBuilder();
//                SB1.append(result[i]);
//
//                sb.delete(0,sb.lastIndexOf("("));
//                sb.deleteCharAt(sb.lastIndexOf("("));
//                sb.deleteCharAt(sb.lastIndexOf(")"));
//                String []  values = sb.toString().split(",");
//
//                String [] keys = SB1.substring(SB1.indexOf("(")+1,SB1.indexOf(")")).split(",");
//
//                JSONObject json = new JSONObject();
//
//                for (int j = 0; j <values.length ; j++) {
//                    json.put(keys[j].trim(),values[j].substring(values[j].indexOf("'")+1,values[j].lastIndexOf("'")));
//                }
//                json.put("id",UUID.randomUUID().toString());
//                json.put("account_id",SourceID.substring(0,10));
//                list.add(json);
//
//                TableSceneMembers tableSceneMembers = JSONObject.parseObject(json.toString(),TableSceneMembers.class);
//
//                int num  = tableSceneMembersMapper.insertSelective(tableSceneMembers);
//                if (num!=1){
//                    message.setCode("-1");
//                    message.setContent(json);
//                    message.setMessage("Scene member add failed!");
//                    return message;
//                }
//
//                //发送数据包
//                System.out.println(outPutSocketMessage.test().toString());
//                发送数据包
                JSONObject jsonResult = outPutSocketMessage.sendMessag(SourceID);
                String status = String.valueOf(jsonResult.get("Status")) ;
                if(!"0".equals(status)){

                    if ("2".equals(status)){
                        message.setCode("0");
                        message.setContent(new JSONArray());
                        message.setMessage("Gateway Error : Device is offline!");
                    }else {
                        message.setCode("-1");
                        message.setContent("[]");
                        message.setMessage("Failed to add device into the scene");
                    }

                    return message;
                }
            }
        }
        message.setCode("0");
        message.setContent(list);
        message.setMessage("Scene member add successfully!");
        return message;
    }

    public Message modifySceneMembers(JSONObject jsonObject, String DestinationID, String SourceID, int packegType) {

        Set<String> keyset = jsonObject.keySet();
        Message message = new Message();

        JSONArray list = new JSONArray();
        for (String key : keyset){
            String[] result = SqlControlUtil.putObjects(key, (JSONArray) jsonObject.get(key));
            for (int i = 0 ; i<result.length;i++){
                OutPutSocketMessage outPutSocketMessage = new OutPutSocketMessage();
                outPutSocketMessage.setPackegType(packegType);//包类型,app写2,web写-1
                outPutSocketMessage.setDestinationID(DestinationID);//app客户端可以写为任意的16个字符串,web填写目标网关地址
                outPutSocketMessage.setType("NULL");//查询就填表名,非查询填写NULL
                outPutSocketMessage.setMessage(key);//消息用于网关返回消息,下发命令的时候,可以写表名
                outPutSocketMessage.setSourceID(SourceID);//源ID
                outPutSocketMessage.setSql(result[i]);//下发的指令(sql语句)


                JSONObject json = new JSONObject();

                StringBuilder sb= new StringBuilder();
                sb.append(result[i]);
                StringBuilder SB1 =new StringBuilder();
                SB1.append(result[i]);

                sb.delete(0,sb.indexOf("SET")+4);
                sb.delete(sb.indexOf("WHERE")-1,sb.lastIndexOf("'"));
                sb.deleteCharAt(sb.lastIndexOf("'"));

                String [] kvs = sb.toString().split(",");

                for (int j = 0; j <kvs.length ; j++) {
                    String [] kv = kvs[j].split("=");
                    StringBuilder s = new StringBuilder();
                    s.append(kv[1]);
                    s.deleteCharAt(s.indexOf("'"));
                    s.deleteCharAt(s.lastIndexOf("'"));
                    json.put(kv[0],s.toString());
                }
                SB1.delete(0,SB1.indexOf("WHERE")+6);

                String[] kv1 = SB1.toString().split("=");
                StringBuilder ss = new StringBuilder();
                ss.append(kv1[1]);
                ss.deleteCharAt(ss.indexOf("'"));
                ss.deleteCharAt(ss.lastIndexOf("'"));
                json.put(kv1[0],ss.toString());
                json.put("account_id",SourceID.substring(0,10));

                TableSceneMembers tableSceneMembers = JSONObject.parseObject(json.toString(),TableSceneMembers.class);

                int num = tableSceneMembersMapper.updateByAccountIdAndSceneMemberGuid(tableSceneMembers);

                if (num!=1){
                    message.setCode("-1");
                    message.setContent(json);
                    message.setMessage("Update the scene members failed!");
                }

                System.out.println(outPutSocketMessage.test().toString());
                //发送数据包
                JSONObject jsonResult = outPutSocketMessage.sendMessag(SourceID);
                String status = (String) jsonResult.get("Status");
                if (!"0".equals(status)) {
                    message.setCode("-1");
                    message.setContent(jsonResult);
                    message.setMessage("设备修改失败");
                }
            }
        }
        message.setCode("0");
        message.setContent("[]");
        message.setMessage("Update the scene members successfully!");
        return message;
    }

    public Message findSceneMembers(JSONObject jsonObject, String DestinationID, String SourceID, int packegType) {

        Message message = new Message();
        String result = "";
        JSONArray list= new JSONArray();

        Set<String> keyset = jsonObject.keySet();
        for (String key: keyset) {
            result= SqlControlUtil.selectObjects(key, (JSONObject) jsonObject.get(key));
            OutPutSocketMessage outPutSocketMessage = new OutPutSocketMessage();
            outPutSocketMessage.setPackegType(packegType);//包类型,app写2,web写-1
            outPutSocketMessage.setDestinationID(DestinationID);//app客户端可以写为任意的16个字符串,web填写目标网关地址
            outPutSocketMessage.setMessage(key);//查询就填表名,非查询填写NULL
            outPutSocketMessage.setType(key);//消息用于网关返回消息,下发命令的时候,可以写表名
            outPutSocketMessage.setSourceID(SourceID);//源ID
            outPutSocketMessage.setSql(result);//下发的指令(sql语句)


//            JSONObject group = (JSONObject) jsonObject.get(key);
//
//            TableSceneMembers tableSceneMembers = JSONObject.parseObject(group.toString(),TableSceneMembers.class);
//            tableSceneMembers.setAccount_id(SourceID.substring(0,10));
//            List<TableSceneMembers> tableGroups = tableSceneMembersMapper.selectSceneMember(tableSceneMembers);
//
//            if (tableGroups.size()<1){
//                message.setCode("-1");
//                message.setContent(group);
//                message.setMessage("search scene members failed!");
//                return message;
//            }
//            for (TableSceneMembers r: tableGroups) {
//                JSONObject rr = (JSONObject) JSONObject.toJSON(r);
//                list.add(rr);
//            }
//
//            JSONObject jsonObject1 =outPutSocketMessage.test();
//            System.out.println(jsonObject1.toString());
            JSONObject jsonResult = outPutSocketMessage.sendMessag(SourceID);
            String status = String.valueOf(jsonResult.get("Status"));
            if(!"0".equals(status)){
                message.setCode("-1");
                message.setContent(jsonResult);
                message.setMessage("No members in the Scnene");
            }
            list.add(jsonResult.get("List"));

        }
        message.setCode("0");
        message.setMessage("Scene member search success!");
        message.setContent(list);
        return message;

    }

    public Message deleteSceneMember(JSONObject jsonObject, String DestinationID, String SourceID, int packegType) {
        Message message = new Message();
        Set<String> keyset = jsonObject.keySet();
        for (String key : keyset) {
            String [] result = SqlControlUtil.deleteObjects(key, (JSONArray) jsonObject.get(key));
            for (int i = 0 ; i<result.length ; i ++){
                OutPutSocketMessage outPutSocketMessage = new OutPutSocketMessage();
                outPutSocketMessage.setPackegType(packegType);//包类型，app为2，web端为-1
                outPutSocketMessage.setDestinationID(DestinationID);//app客户可以为任意的16个字符串，web填写目标网关地址
                outPutSocketMessage.setType("NULL");//查询就填写表明，非查询就填写“NULL”
                outPutSocketMessage.setMessage(key);//消息用于网关返回消息，下发命令时可以填写表明
                outPutSocketMessage.setSourceID(SourceID);//源ID
                outPutSocketMessage.setSql(result[i]);//下发sql指令

                System.out.println(result[i]);
//                StringBuilder sb= new StringBuilder();
//                sb.append(result[i]);
//                StringBuilder SB1 =new StringBuilder();
//                SB1.append(result[i]);
//
//                sb.delete(0,sb.indexOf("WHERE")+6);
//                SB1.delete(0,SB1.lastIndexOf("scene_members_guid"));
//
//                String [] kv1=SB1.toString().split("=");
//                String [] kv2=sb.substring(0,sb.indexOf("AND")-1).toString().split("=");
//
//
//                JSONObject json  =new JSONObject();
//
//                StringBuilder stringBuilder1 = new StringBuilder();
//                stringBuilder1.append(kv1[1].trim());
//                stringBuilder1.deleteCharAt(0);
//                stringBuilder1.deleteCharAt(stringBuilder1.length()-1);
//
//
//                StringBuilder stringBuilder2 = new StringBuilder();
//                stringBuilder2.append(kv2[1].trim());
//                stringBuilder2.deleteCharAt(0);
//                stringBuilder2.deleteCharAt(stringBuilder2.length()-1);
//
//
//                json.put(kv1[0],stringBuilder1.toString());
//                json.put(kv2[0],stringBuilder2.toString());
//                json.put("account_id",SourceID.substring(0,10));
//                System.out.println(json.toString());
//
//                TableSceneMembers tableSceneMembers = JSONObject.parseObject(json.toString(),TableSceneMembers.class);
//
//                int num =tableSceneMembersMapper.deleteByAccountIdAndDeviceGuid(tableSceneMembers);
//
//                if (num!=1){
//                    message.setCode("-1");
//                    message.setContent(json);
//                    message.setMessage("Scene member remove failed!");
//                    return message;
//                }
//                System.out.println(outPutSocketMessage.test().toString());
                //发送数据包
                JSONObject jsonResult = outPutSocketMessage.sendMessag(SourceID);
                System.out.println(jsonResult.toString());
                String status = String.valueOf(jsonResult.get("Status")) ;
                if(!"0".equals(status)){
                    if ("2".equals(status)){
                        message.setCode("0");
                        message.setContent(new JSONArray());
                        message.setMessage("Gateway Error : Device is offline!");
                    }else {
                        message.setCode("-1");
                        message.setContent(jsonResult);
                        message.setMessage("Failed to remove scene member ");
                    }
                    return message;
                }
            }
        }
        message.setCode("0");
        message.setContent("[]");
        message.setMessage("Scene member remove successfully!");
        return message;
    }
}
