package com.iot.serviceImp;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.iot.socketUtil.OutPutSocketMessage;
import com.iot.mapper.TableGroupMapper;
import com.iot.mapper.TableGroupMembersMapper;
import com.iot.message.Message;
import com.iot.pojo.TableGroup;
import com.iot.service.GroupService;
import com.iot.sqlControlUtil.SqlControlUtil;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * Created by Jacob on 16/4/7.
 */
@Service
public class GroupServiceImpl implements GroupService {


    private static Logger logger =Logger.getLogger(GroupServiceImpl.class);

    @Resource
    private TableGroupMapper tableGroupMapper;
    @Resource
    private TableGroupMembersMapper tableGroupMembersMapper;

    //修改设备组信息
    public Message modifyGroup(JSONObject jsonObject, String DestinationID, String SourceID, int packegType) {
        Set<String> keyset = jsonObject.keySet();
        Message message = new Message();

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

                TableGroup tableGroup = JSONObject.parseObject(json.toString(),TableGroup.class);

                int num = tableGroupMapper.updateByAccountId(tableGroup);

                if (num!=1){
                    message.setCode("-1");
                    message.setContent(json);
                    message.setMessage("Update the group failed!");
                }

                System.out.println(outPutSocketMessage.test().toString());
                //发送数据包
//                JSONObject jsonResult = outPutSocketMessage.sendMessag();
//                String status = (String) jsonResult.get("Status");
//                if (!"0".equals(status)) {
//                    message.setCode("-1");
//                    message.setContent(jsonResult);
//                    message.setMessage("设备修改失败");
//                }
            }
        }
        message.setCode("0");
        message.setContent("[]");
        message.setMessage("Update the group successfully!");
        return message;
    }

    //查找设备组信息
    public Message findGroup(JSONObject jsonObject, String DestinationID, String SourceID, int packegType) {
        Message message = new Message();
        String result = "";
        JSONArray list= null;

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
//
//
//            JSONObject group = (JSONObject) jsonObject.get(key);
//
//            TableGroup tableGroup = JSONObject.parseObject(group.toString(),TableGroup.class);
//
//            List<TableGroup> tableGroups = tableGroupMapper.selectGroup(tableGroup);
//
//            for (TableGroup r: tableGroups) {
//                JSONObject rr = (JSONObject) JSONObject.toJSON(r);
//                list.add(rr);
//
//            }


//            JSONObject jsonObject1 =outPutSocketMessage.test();
            JSONObject jsonResult = outPutSocketMessage.sendMessag(SourceID);
            String status = String.valueOf(jsonResult.get("Status"));
            if(!"0".equals(status)){
                message.setCode("-1");
                message.setContent(new JSONArray());
                message.setMessage("Failed to search the group");
                return message;
            }
            list=(JSONArray) jsonResult.get("List");

        }
        System.out.println(list.toString());
        message.setCode("0");
        message.setMessage("Group search success!");
        message.setContent(list);
        return message;
    }

    //添加设备组
    public Message addGroup(JSONObject jsonObject, String DestinationID, String SourceID, int packegType) {
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
                outPutSocketMessage.setMessage(key);//消息同于网关返回消息，下发命令的时候可以填写表明
                outPutSocketMessage.setSourceID(SourceID);//源ID
                outPutSocketMessage.setSql(result[i]);//下发sql语句指令

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
//
//                TableGroup tableGroup = JSONObject.parseObject(json.toString(),TableGroup.class);
//
//                tableGroupMapper.insertSelective(tableGroup);
//
//                //发送数据包
////                System.out.println(outPutSocketMessage.test().toString());
//                发送数据包
                JSONObject jsonResult = outPutSocketMessage.sendMessag(SourceID);
                String status = String.valueOf(jsonResult.get("Status"));
                logger.error("Sql Command : " +result[i] + " Gateway_Statue : "+status);
                if(!"0".equals(status)){
                    message.setCode("-1");
                    message.setContent("[]");
                    message.setMessage("Failed to create a group");
                    return message;
                }
            }
        }
        message.setCode("0");
        message.setContent(list);
        message.setMessage("Group create successfully!");
        return message;
    }

    //删除设备组
    public Message deleteGroup(JSONObject jsonObject, String DestinationID, String SourceID, int packegType) {
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

                StringBuilder sb= new StringBuilder();
                sb.append(result[i]);
                StringBuilder SB1 =new StringBuilder();
                SB1.append(result[i]);

                sb.delete(0,sb.indexOf("WHERE")+6);

                SB1.delete(0,SB1.lastIndexOf("group_guid"));

                String [] kv1=SB1.toString().split("=");


                JSONObject json  =new JSONObject();

                StringBuilder stringBuilder1 = new StringBuilder();
                stringBuilder1.append(kv1[1].trim());
                stringBuilder1.deleteCharAt(0);
                stringBuilder1.deleteCharAt(stringBuilder1.length()-1);


                json.put(kv1[0],stringBuilder1.toString());
                json.put("account_id",SourceID.substring(0,10));

                TableGroup tableGroup = JSONObject.parseObject(json.toString(),TableGroup.class);

                int num =tableGroupMapper.deleteByAccountIdAndGroupId(tableGroup);

                if (num!=1){
                    message.setCode("-1");
                    message.setContent("[]");
                    message.setMessage("Group delete failed!");
                    return message;
                }
                //发送数据包
                JSONObject jsonResult = outPutSocketMessage.sendMessag(SourceID);
                String status = String.valueOf(jsonResult.get("Status"));
                if(!"0".equals(status)){
                    message.setCode("-1");
                    message.setContent(jsonResult);
                    message.setMessage("failed to delete the group!");
                    return message;
                }
            }
        }
        message.setCode("0");
        message.setContent("[]");
        message.setMessage("group has been deleted successfully!");
        return message;
    }

    public Message addGroupDevice(JSONObject jsonObject, String DestinationID, String SourceID, int packegType) {

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
                outPutSocketMessage.setMessage(key);//消息同于网关返回消息，下发命令的时候可以填写表明
                outPutSocketMessage.setSourceID(SourceID);//源ID
                outPutSocketMessage.setSql(result[i]);//下发sql语句指令


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

//                TableGroupMembers tableGrou = JSONObject.parseObject(json.toString(),TableGroupMembers.class);
//
//                int num  = tableGroupMembersMapper.insertSelective(tableGrou);
//                if (num!=1){
//                    message.setCode("-1");
//                    message.setContent(json);
//                    message.setMessage("Group member add failed!");
//                    return message;
//                }
//
//                //发送数据包
//                System.out.println(outPutSocketMessage.test().toString());
                //发送数据包
                JSONObject jsonResult = outPutSocketMessage.sendMessag(SourceID);
                String status = String.valueOf(jsonResult.get("Status")) ;
                logger.error("Sql Command : " +result[i] + " Gateway_Statue : "+status);
                if(!"0".equals(status)){

                    if ("2".equals(status)){
                        message.setCode("0");
                        message.setContent(new JSONArray());
                        message.setMessage("Gateway Error : Device is offline!");
                    }else {
                        message.setCode("-1");
                        message.setContent("[]");
                        message.setMessage("Failed to add the member into the group!");
                    }

                    return message;
                }
            }
        }
        message.setCode("0");
        message.setContent(list);
        message.setMessage("Group member add successfully!");
        return message;

    }

    public Message deleteGroupMember(JSONObject jsonObject, String DestinationID, String SourceID, int packegType){
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

//                StringBuilder sb= new StringBuilder();
//                sb.append(result[i]);
//                StringBuilder SB1 =new StringBuilder();
//                SB1.append(result[i]);
//
//                sb.delete(0,sb.indexOf("WHERE")+6);
//                SB1.delete(0,SB1.lastIndexOf("table_group_guid"));
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
//
//
//                TableGroupMembers tableGroupMembers = JSONObject.parseObject(json.toString(),TableGroupMembers.class);


                //发送数据包
                JSONObject jsonResult = outPutSocketMessage.sendMessag(SourceID);
                String status = String.valueOf(jsonResult.get("Status"));

                if(!"0".equals(status)){
                    logger.error("Sql Command : " +result[i] + " Gateway_Statue : "+status);

                    if ("2".equals(status)){
                        message.setCode("0");
                        message.setContent(new JSONArray());
                        message.setMessage("Gateway Error : Device is offline!");
                    }else {
                        message.setCode("-1");
                        message.setContent(jsonResult);
                        message.setMessage("Failed to remove the Group member");
                    }

                    return message;
                }

//                int num =tableGroupMembersMapper.deleteByAccountIdAndDeviceGuid(tableGroupMembers);
//
//                if (num!=1){
//                    logger.error("Sql Command : " +result[i] + " web statue : "+status);
//                    message.setCode("-1");
//                    message.setContent(json);
//                    message.setMessage("Group member remove failed!");
//                    return message;
//                }

            }
        }
        message.setCode("0");
        message.setContent("[]");
        message.setMessage("Group member remove successfully!");
        return message;
    }

    public Message modifyGroupMembers(JSONObject jsonObject, String DestinationID, String SourceID, int packegType) {

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
//                TableGroupMembers tableGroup = JSONObject.parseObject(json.toString(),TableGroupMembers.class);
//
//                int num = tableGroupMembersMapper.updateByAccountIdAndGroupMemberGuid(tableGroup);
//
//                if (num!=1){
//                    message.setCode("-1");
//                    message.setContent(json);
//                    message.setMessage("Update the group members failed!");
//                }
//
//                System.out.println(outPutSocketMessage.test().toString());
//                发送数据包
                JSONObject jsonResult = outPutSocketMessage.sendMessag(SourceID);

                String status = (String) jsonResult.get("Status");
                logger.error("Sql Command : " +result[i] + " Gateway_Statue : "+status);
                if (!"0".equals(status)) {
                    message.setCode("-1");
                    message.setContent(jsonResult);
                    message.setMessage("Failed to modify the group members");
                    return message;
                }
            }
        }
        message.setCode("0");
        message.setContent("[]");
        message.setMessage("Update the group members successfully!");
        return message;
    }

    public Message findGroupMembers(JSONObject jsonObject, String DestinationID, String SourceID, int packegType) {

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
//            TableGroupMembers tableGroup = JSONObject.parseObject(group.toString(),TableGroupMembers.class);
//            tableGroup.setAccount_id(SourceID.substring(0,10));
//            List<TableGroupMembers> tableGroups = tableGroupMembersMapper.selectGroupMember(tableGroup);
//
//            if (tableGroups.size()<1){
//                message.setCode("-1");
//                message.setContent(group);
//                message.setMessage("search group members failed!");
//                return message;
//            }
//            for (TableGroupMembers r: tableGroups) {
//                JSONObject rr = (JSONObject) JSONObject.toJSON(r);
//                list.add(rr);
//            }
//
//            JSONObject jsonObject1 =outPutSocketMessage.test();
//            System.out.println(jsonObject1.toString());
            JSONObject jsonResult = outPutSocketMessage.sendMessag(SourceID);
            String status = String.valueOf(jsonResult.get("Status")) ;
            logger.error("Command : "+result +" Gateway Status : "+status);
            if(!"0".equals(status)){
                message.setCode("-1");
                message.setContent(jsonResult);
                message.setMessage("设备查询失败");
            }
            list.add(jsonResult.get("List"));

        }
        message.setCode("0");
        message.setMessage("Group search success!");
        message.setContent(list);
        return message;

    }

    public Message controlGroup(JSONObject jsonObject, String DestinationID, String SourceID, int packegType) {
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
                        message.setMessage("Failed to control the group");
                    }
                    return message;
                }
            }
        }
        message.setCode("0");
        message.setContent("[]");
        message.setMessage("control the group successfully!");
        return message;
    }
}
