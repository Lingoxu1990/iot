package com.iot.serviceImp;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.iot.socketUtil.OutPutSocketMessage;
import com.iot.mapper.TableCdtsListMapper;
import com.iot.mapper.TableConditonsMapper;
import com.iot.mapper.TableControlMapper;
import com.iot.mapper.TableCtrlSequenceMapper;
import com.iot.message.Message;
import com.iot.service.TermService;
import com.iot.sqlControlUtil.SqlControlUtil;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Jacob on 2016-04-07.
 */
@Service
public class TermServiceImpl implements TermService {

    private static Logger logger = Logger.getLogger(TermServiceImpl.class);

    @Resource
    private TableCdtsListMapper tableCdtsListMapper;
    @Resource
    private TableConditonsMapper tableConditonsMapper;
    @Resource
    private TableCtrlSequenceMapper tableCtrlSequenceMapper;
    @Resource
    private TableControlMapper tableControlMapper;

    //修改条件
    public Message modifyTerm(JSONObject jsonObject, String DestinationID, String SourceID, int packegType) {
        Message message = new Message();
        Set<String> keyset = jsonObject.keySet();
        for (String key : keyset) {
            String[] result = SqlControlUtil.putObjects(key, (JSONArray) jsonObject.get(key));
            for (int i = 0; i < result.length; i++) {
                OutPutSocketMessage outPutSocketMessage = new OutPutSocketMessage();
                outPutSocketMessage.setPackegType(packegType);//包类型,app写2,web写-1
                outPutSocketMessage.setDestinationID(DestinationID);//app客户端可以写为任意的16个字符串,web填写目标网关地址
                outPutSocketMessage.setSourceID(SourceID);//源ID
                outPutSocketMessage.setType("NULL");//查询就填表名,非查询填写NULL
                outPutSocketMessage.setMessage(key);//消息用于网关返回消息,下发命令的时候,可以写表名
                outPutSocketMessage.setSql(result[i]);//下发的指令(sql语句)


                //发送数据包
                JSONObject jsonResult = outPutSocketMessage.sendMessag(SourceID);
                String status = String.valueOf(jsonResult.get("Status"));
                logger.error("Command : " + result[i] + " Gateway Status : " + status);
                if (!"0".equals(status)) {
                    message.setCode("-1");
                    message.setContent(jsonResult);
                    message.setMessage("Failed to modify the condition list");
                    return message;
                }
            }
        }
        message.setCode("0");
        message.setContent("[]");
        message.setMessage("Modify the condition list successfully!");
        return message;
    }
    //查找条件
    public Message findTerm(JSONObject jsonObject, String DestinationID, String SourceID, int packegType) {
        String result = "";
        Message message = new Message();
        JSONArray list = new JSONArray();
        Set<String> keyset = jsonObject.keySet();

        Object Obj_cdts_list_guid =  jsonObject.get("cdts_list_guid");


        for (String key:keyset) {
            result = SqlControlUtil.selectObjects(key, (JSONObject) jsonObject.get(key));
            OutPutSocketMessage outPutSocketMessage = new OutPutSocketMessage();
            outPutSocketMessage.setPackegType(packegType);//包类型,app写2,web写-1
            outPutSocketMessage.setDestinationID(DestinationID);//app客户端可以写为任意的16个字符串,web填写目标网关地址
            outPutSocketMessage.setType(key);//查询就填表名,非查询填写NULL
            outPutSocketMessage.setMessage(key);//消息用于网关返回消息,下发命令的时候,可以写表名
            outPutSocketMessage.setSourceID(SourceID);//源ID
            outPutSocketMessage.setSql(result);//下发的指令(sql语句)

            //发送数据包
            JSONObject jsonResult = outPutSocketMessage.sendMessag(SourceID);
            String status = String.valueOf(jsonResult.get("Status"));
            if (!"0".equals(status)) {
                message.setCode("-1");
                message.setContent(new JSONArray());
                message.setMessage("No result!");
                return message;
            }
            JSONArray jsonarr = (JSONArray) jsonResult.get("List");

            String str_cdts_list_guid="";
            if (Obj_cdts_list_guid!=null){
                str_cdts_list_guid = (String)Obj_cdts_list_guid;

                for (int i = 0; i <jsonarr.size() ; i++) {

                    JSONObject temp_cdts = (JSONObject) jsonarr.get(i);

                    if (!str_cdts_list_guid.equals((String) temp_cdts.get("cdts_list_guid"))){

                        jsonarr.remove(i);

                    }

                }

            }

            for (int i = 0; i <jsonarr.size() ; i++) {
                JSONObject cdts_list = (JSONObject) jsonarr.get(i);
                String cdts_list_guid = (String) cdts_list.get("cdts_list_guid");
                String sql_for_conditions = "select * from table_conditons where cdts_list_guid='"+cdts_list_guid+"'";
                OutPutSocketMessage outPutSocketMessage1 = new OutPutSocketMessage();
                outPutSocketMessage1.setPackegType(packegType);//包类型,app写2,web写-1
                outPutSocketMessage1.setDestinationID(DestinationID);//app客户端可以写为任意的16个字符串,web填写目标网关地址
                outPutSocketMessage1.setType("table_conditions");//查询就填表名,非查询填写NULL
                outPutSocketMessage1.setMessage("table_conditions");//消息用于网关返回消息,下发命令的时候,可以写表名
                outPutSocketMessage1.setSourceID(SourceID);//源ID
                outPutSocketMessage1.setSql(sql_for_conditions);//下发的指令(sql语句)

                JSONObject jsonResult1 = outPutSocketMessage1.sendMessag(SourceID);


                String status1 = String.valueOf(jsonResult1.get("Status"));
                if ("2".equals(status1)) {
                    cdts_list.put("conditions",new JSONArray()) ;
                    message.setCode("0");
                    message.setContent(jsonarr);
                    message.setMessage("this term has no conditions!");
                    return message;
                }

                cdts_list.put("conditions",jsonResult1.get("List")) ;

                String sql_for_seq = "select * from table_ctrl_sequence where cdts_list_guid= '"+cdts_list_guid+"'";
                OutPutSocketMessage outPutSocketMessage2 = new OutPutSocketMessage();
                outPutSocketMessage2.setPackegType(packegType);//包类型,app写2,web写-1
                outPutSocketMessage2.setDestinationID(DestinationID);//app客户端可以写为任意的16个字符串,web填写目标网关地址
                outPutSocketMessage2.setType("table_ctrl_sequence");//查询就填表名,非查询填写NULL
                outPutSocketMessage2.setMessage("table_ctrl_sequence");//消息用于网关返回消息,下发命令的时候,可以写表名
                outPutSocketMessage2.setSourceID(SourceID);//源ID
                outPutSocketMessage2.setSql(sql_for_seq);//下发的指令(sql语句)

                JSONObject jsonResult2 = outPutSocketMessage2.sendMessag(SourceID);
                String status2 = String.valueOf(jsonResult2.get("Status"));
                if ("2".equals(status2)) {
                    cdts_list.put("ctrl_sequence",new JSONArray());
                    message.setCode("0");
                    message.setContent(jsonarr);
                    message.setMessage("this term has no sequence!");
                    return message;
                }

                cdts_list.put("ctrl_sequence",jsonResult2.get("List"));


                JSONArray ctrl_sequences = (JSONArray) cdts_list.get("ctrl_sequence");

                for (int j = 0; j <ctrl_sequences.size() ; j++) {
                    JSONObject ctrl_sequence = (JSONObject) ctrl_sequences.get(j);

                    String ctrl_sqn_guid = (String) ctrl_sequence.get("ctrl_sqn_guid");
                    String sql_for_control = "select * from table_control where ctrl_sqn_guid = '"+ctrl_sqn_guid+"'";
                    OutPutSocketMessage outPutSocketMessage3 = new OutPutSocketMessage();
                    outPutSocketMessage3.setPackegType(packegType);//包类型,app写2,web写-1
                    outPutSocketMessage3.setDestinationID(DestinationID);//app客户端可以写为任意的16个字符串,web填写目标网关地址
                    outPutSocketMessage3.setType("table_ctrl_sequence");//查询就填表名,非查询填写NULL
                    outPutSocketMessage3.setMessage("table_ctrl_sequence");//消息用于网关返回消息,下发命令的时候,可以写表名
                    outPutSocketMessage3.setSourceID(SourceID);//源ID
                    outPutSocketMessage3.setSql(sql_for_control);//下发的指令(sql语句)

                    JSONObject jsonResult3 = outPutSocketMessage3.sendMessag(SourceID);
                    String status3 = String.valueOf(jsonResult3.get("Status"));
                    if ("2".equals(status3)) {
                        ctrl_sequence.put("control",new JSONArray());
                        message.setCode("0");
                        message.setContent(jsonarr);
                        message.setMessage("this term has no controller!");
                        return message;
                    }
                    ctrl_sequence.put("control",jsonResult3.get("List"));

                }

            }
            list=jsonarr;

        }
        message.setCode("0");
        message.setMessage("get the cdt_list successfully!");
        message.setContent(list);
        return message;
    }
    //添加条件集合
    public Message addTerm(JSONObject jsonObject, String DestinationID, String SourceID, int packegType) {
        Message message = new Message();
        Set<String> keyset = jsonObject.keySet();
        JSONArray list = new JSONArray();

        for (String key : keyset) {
            String[] result = SqlControlUtil.addObjects(key, (JSONArray) jsonObject.get(key));
            for (int i = 0; i < result.length; i++) {
                OutPutSocketMessage outPutSocketMessage = new OutPutSocketMessage();
                outPutSocketMessage.setPackegType(packegType);//包类型,app写2,web写-1
                outPutSocketMessage.setDestinationID(DestinationID);//app客户端可以写为任意的16个字符串,web填写目标网关地址
                outPutSocketMessage.setType("NULL");//查询就填表名,非查询填写NULL
                outPutSocketMessage.setMessage(key);//消息用于网关返回消息,下发命令的时候,可以写表名
                outPutSocketMessage.setSourceID(SourceID);//源ID
                outPutSocketMessage.setSql(result[i]);//下发的指令(sql语句)


                StringBuilder sb = new StringBuilder();
                sb.append(result[i]);
                StringBuilder SB1 = new StringBuilder();
                SB1.append(result[i]);

                sb.delete(0, sb.lastIndexOf("("));
                sb.deleteCharAt(sb.lastIndexOf("("));
                sb.deleteCharAt(sb.lastIndexOf(")"));
                String[] values = sb.toString().split(",");

                String[] keys = SB1.substring(SB1.indexOf("(") + 1, SB1.indexOf(")")).split(",");


                JSONObject json = new JSONObject();

                for (int j = 0; j < values.length; j++) {
                    json.put(keys[j].trim(), values[j].substring(values[j].indexOf("'") + 1, values[j].lastIndexOf("'")));
                }
                json.put("account_id",SourceID.substring(0,10));
                json.put("id", UUID.randomUUID().toString());
                list.add(json);
//
//                TableCdtsList tableCdtsList = JSON.parseObject(json.toString(),TableCdtsList.class);
//
//                int num = tableCdtsListMapper.insertSelective(tableCdtsList);
//
//                if (num!=1){
//                    message.setCode("-1");
//                    message.setContent("[]");
//                    message.setMessage("Conditions list add failed!");
//                    return message;
//                }
//
//                System.out.println(outPutSocketMessage.test().toString());

                //发送数据包
                JSONObject jsonResult = outPutSocketMessage.sendMessag(SourceID);
                String status = String.valueOf(jsonResult.get("Status"));
                if (!"0".equals(status)) {
                    message.setCode("-1");
                    message.setContent("[]");
                    message.setMessage("Failed to add conditions!");
                    return message;
                }
            }
        }
        message.setCode("0");
        message.setContent(list);
        message.setMessage("Conditions list add successfully!");
        return message;
    }
    //删除条件
    public Message deleteTerm(JSONObject jsonObject, String DestinationID, String SourceID, int packegType) {
        Message message = new Message();
        Set<String> keyset = jsonObject.keySet();
        for (String key : keyset) {
            String[] result = SqlControlUtil.deleteObjects(key, (JSONArray) jsonObject.get(key));
            for (int i = 0; i < result.length; i++) {
                OutPutSocketMessage outPutSocketMessage = new OutPutSocketMessage();
                outPutSocketMessage.setPackegType(packegType);//包类型,app写2,web写-1
                outPutSocketMessage.setDestinationID(DestinationID);//app客户端可以写为任意的16个字符串,web填写目标网关地址
                outPutSocketMessage.setType("NULL");//查询就填表名,非查询填写NULL
                outPutSocketMessage.setMessage(key);//消息用于网关返回消息,下发命令的时候,可以写表名
                outPutSocketMessage.setSourceID(SourceID);//源ID
                outPutSocketMessage.setSql(result[i]);//下发的指令(sql语句)

                //发送数据包
                JSONObject jsonResult = outPutSocketMessage.sendMessag(SourceID);
                String status = String.valueOf(jsonResult.get("Status")) ;

                logger.error("Command : " + result[i] + " Gateway Status : " + status);

                if (!"0".equals(status)) {
                    message.setCode("-1");
                    message.setContent(jsonResult);
                    message.setMessage("failed to delete the ctrl_list");
                    return message;
                }
            }
        }
        message.setCode("0");
        message.setContent("[]");
        message.setMessage("delete the ctrl_list successfully!");
        return message;
    }

    //增加条件至条件集合
    public Message addConditions(JSONObject jsonObject, String DestinationID, String SourceID, int packegType) {
        Message message = new Message();
        Set<String> keyset = jsonObject.keySet();
        JSONArray list = new JSONArray();

        for (String key : keyset) {
            String[] result = SqlControlUtil.addObjects(key, (JSONArray) jsonObject.get(key));
            for (int i = 0; i < result.length; i++) {
                OutPutSocketMessage outPutSocketMessage = new OutPutSocketMessage();
                outPutSocketMessage.setPackegType(packegType);//包类型,app写2,web写-1
                outPutSocketMessage.setDestinationID(DestinationID);//app客户端可以写为任意的16个字符串,web填写目标网关地址
                outPutSocketMessage.setType("NULL");//查询就填表名,非查询填写NULL
                outPutSocketMessage.setMessage(key);//消息用于网关返回消息,下发命令的时候,可以写表名
                outPutSocketMessage.setSourceID(SourceID);//源ID
                outPutSocketMessage.setSql(result[i]);//下发的指令(sql语句)

                StringBuilder sb = new StringBuilder();
                sb.append(result[i]);
                StringBuilder SB1 = new StringBuilder();
                SB1.append(result[i]);

                sb.delete(0, sb.lastIndexOf("("));
                sb.deleteCharAt(sb.lastIndexOf("("));
                sb.deleteCharAt(sb.lastIndexOf(")"));
                String[] values = sb.toString().split(",");

                String[] keys = SB1.substring(SB1.indexOf("(") + 1, SB1.indexOf(")")).split(",");


                JSONObject json = new JSONObject();

                for (int j = 0; j < values.length; j++) {

                    json.put(keys[j].trim(), values[j].substring(values[j].indexOf("'") + 1, values[j].lastIndexOf("'")));
                }
                json.put("account_id", SourceID.substring(0, 8));
                json.put("id", UUID.randomUUID().toString());
                list.add(json);
//
//                TableConditons tableConditons = JSON.parseObject(json.toString(), TableConditons.class);
//
//                int num = tableConditonsMapper.insertSelective(tableConditons);
//
//                if (num != 1) {
//                    message.setCode("-1");
//                    message.setContent("[]");
//                    message.setMessage("Conditions add failed!");
//                    return message;
//                }


                //发送数据包
                JSONObject jsonResult = outPutSocketMessage.sendMessag(SourceID);
                String status = String.valueOf(jsonResult.get("Status"));
                if(!"0".equals(status)){
                    message.setCode("-1");
                    message.setContent(jsonResult);
                    message.setMessage("faield to add the conditions");
                    return message;
                }
            }
        }
        message.setCode("0");
        message.setContent(list);
        message.setMessage("Conditions add successfully!");
        return message;
    }
    public Message modifyConditions(JSONObject jsonObject, String DestinationID, String SourceID, int packegType){

        Message message = new Message();
        Set<String> keyset = jsonObject.keySet();
        for (String key : keyset) {
            String[] result = SqlControlUtil.putObjects(key, (JSONArray) jsonObject.get(key));
            for (int i = 0; i < result.length; i++) {
                OutPutSocketMessage outPutSocketMessage = new OutPutSocketMessage();
                outPutSocketMessage.setPackegType(packegType);//包类型,app写2,web写-1
                outPutSocketMessage.setDestinationID(DestinationID);//app客户端可以写为任意的16个字符串,web填写目标网关地址
                outPutSocketMessage.setSourceID(SourceID);//源ID
                outPutSocketMessage.setType("NULL");//查询就填表名,非查询填写NULL
                outPutSocketMessage.setMessage(key);//消息用于网关返回消息,下发命令的时候,可以写表名
                outPutSocketMessage.setSql(result[i]);//下发的指令(sql语句)


                //发送数据包
                JSONObject jsonResult = outPutSocketMessage.sendMessag(SourceID);
                String status = String.valueOf(jsonResult.get("Status"));
                logger.error("Command : " + result[i] + " Gateway Status : " + status);
                if (!"0".equals(status)) {
                    message.setCode("-1");
                    message.setContent(jsonResult);
                    message.setMessage("Failed to modify the conditions");
                    return message;
                }
            }
        }
        message.setCode("0");
        message.setContent("[]");
        message.setMessage("Modify the condition list successfully!");
        return message;

    }
    public Message deleteConditions(JSONObject jsonObject, String DestinationID, String SourceID, int packegType) {
        Message message = new Message();
        Set<String> keyset = jsonObject.keySet();
        for (String key : keyset) {
            String[] result = SqlControlUtil.deleteObjects(key, (JSONArray) jsonObject.get(key));
            for (int i = 0; i < result.length; i++) {
                OutPutSocketMessage outPutSocketMessage = new OutPutSocketMessage();
                outPutSocketMessage.setPackegType(packegType);//包类型,app写2,web写-1
                outPutSocketMessage.setDestinationID(DestinationID);//app客户端可以写为任意的16个字符串,web填写目标网关地址
                outPutSocketMessage.setType("NULL");//查询就填表名,非查询填写NULL
                outPutSocketMessage.setMessage(key);//消息用于网关返回消息,下发命令的时候,可以写表名
                outPutSocketMessage.setSourceID(SourceID);//源ID
                outPutSocketMessage.setSql(result[i]);//下发的指令(sql语句)

                //发送数据包
                JSONObject jsonResult = outPutSocketMessage.sendMessag(SourceID);
                String status = String.valueOf(jsonResult.get("Status")) ;

                logger.error("Command : " + result[i] + " Gateway Status : " + status);

                if (!"0".equals(status)) {
                    message.setCode("-1");
                    message.setContent(jsonResult);
                    message.setMessage("failed to delete the conditions");
                    return message;
                }
            }
        }
        message.setCode("0");
        message.setContent("[]");
        message.setMessage("条件删除成功");
        return message;
    }

    //增加动作序列至条件集合
    public Message addSequence(JSONObject jsonObject, String DestinationID, String SourceID, int packegType) {

        Message message = new Message();
        Set<String> keyset = jsonObject.keySet();
        JSONArray list = new JSONArray();

        for (String key : keyset) {
            String[] result = SqlControlUtil.addObjects(key, (JSONArray) jsonObject.get(key));
            for (int i = 0; i < result.length; i++) {
                OutPutSocketMessage outPutSocketMessage = new OutPutSocketMessage();
                outPutSocketMessage.setPackegType(packegType);//包类型,app写2,web写-1
                outPutSocketMessage.setDestinationID(DestinationID);//app客户端可以写为任意的16个字符串,web填写目标网关地址
                outPutSocketMessage.setType("NULL");//查询就填表名,非查询填写NULL
                outPutSocketMessage.setMessage(key);//消息用于网关返回消息,下发命令的时候,可以写表名
                outPutSocketMessage.setSourceID(SourceID);//源ID
                outPutSocketMessage.setSql(result[i]);//下发的指令(sql语句)

                StringBuilder sb = new StringBuilder();
                sb.append(result[i]);
                StringBuilder SB1 = new StringBuilder();
                SB1.append(result[i]);

                sb.delete(0, sb.lastIndexOf("("));
                sb.deleteCharAt(sb.lastIndexOf("("));
                sb.deleteCharAt(sb.lastIndexOf(")"));
                String[] values = sb.toString().split(",");

                String[] keys = SB1.substring(SB1.indexOf("(") + 1, SB1.indexOf(")")).split(",");


                JSONObject json = new JSONObject();

                for (int j = 0; j < values.length; j++) {
                    json.put(keys[j].trim(), values[j].substring(values[j].indexOf("'") + 1, values[j].lastIndexOf("'")));
                }
                json.put("account_id", SourceID.substring(0, 8));
                json.put("id", UUID.randomUUID().toString());
                list.add(json);

//                TableCtrlSequence tableCtrlSequence = JSON.parseObject(json.toString(), TableCtrlSequence.class);
//
//                int num = tableCtrlSequenceMapper.insertSelective(tableCtrlSequence);
//
//                if (num != 1) {
//                    message.setCode("-1");
//                    message.setContent("[]");
//                    message.setMessage("Control sequence add failed!");
//                    return message;
//                }


                //发送数据包
                JSONObject jsonResult = outPutSocketMessage.sendMessag(SourceID);
                String status = String.valueOf(jsonResult.get("Status"));
                logger.error("Command : " + result[i] + " Gateway Status : " + status);
                if(!"0".equals(status)){
                    message.setCode("-1");
                    message.setContent(jsonResult);
                    message.setMessage("failed to add the sequence");
                    return message;
                }
            }
        }
        message.setCode("0");
        message.setContent(list);
        message.setMessage("Control sequence add successfully!");
        return message;
    }
    public Message modifySequence(JSONObject jsonObject, String DestinationID, String SourceID, int packegType){

        Message message = new Message();
        Set<String> keyset = jsonObject.keySet();
        for (String key : keyset) {
            String[] result = SqlControlUtil.putObjects(key, (JSONArray) jsonObject.get(key));
            for (int i = 0; i < result.length; i++) {
                OutPutSocketMessage outPutSocketMessage = new OutPutSocketMessage();
                outPutSocketMessage.setPackegType(packegType);//包类型,app写2,web写-1
                outPutSocketMessage.setDestinationID(DestinationID);//app客户端可以写为任意的16个字符串,web填写目标网关地址
                outPutSocketMessage.setSourceID(SourceID);//源ID
                outPutSocketMessage.setType("NULL");//查询就填表名,非查询填写NULL
                outPutSocketMessage.setMessage(key);//消息用于网关返回消息,下发命令的时候,可以写表名
                outPutSocketMessage.setSql(result[i]);//下发的指令(sql语句)


                //发送数据包
                JSONObject jsonResult = outPutSocketMessage.sendMessag(SourceID);
                String status = String.valueOf(jsonResult.get("Status"));
                logger.error("Command : " + result[i] + " Gateway Status : " + status);
                if (!"0".equals(status)) {
                    message.setCode("-1");
                    message.setContent(jsonResult);
                    message.setMessage("Failed to modify the conditions");
                    return message;
                }
            }
        }
        message.setCode("0");
        message.setContent("[]");
        message.setMessage("Modify the condition list successfully!");
        return message;

    }
    public Message deleteSequence(JSONObject jsonObject, String DestinationID, String SourceID, int packegType) {
        Message message = new Message();
        Set<String> keyset = jsonObject.keySet();
        for (String key : keyset) {
            String[] result = SqlControlUtil.deleteObjects(key, (JSONArray) jsonObject.get(key));
            for (int i = 0; i < result.length; i++) {
                OutPutSocketMessage outPutSocketMessage = new OutPutSocketMessage();
                outPutSocketMessage.setPackegType(packegType);//包类型,app写2,web写-1
                outPutSocketMessage.setDestinationID(DestinationID);//app客户端可以写为任意的16个字符串,web填写目标网关地址
                outPutSocketMessage.setType("NULL");//查询就填表名,非查询填写NULL
                outPutSocketMessage.setMessage(key);//消息用于网关返回消息,下发命令的时候,可以写表名
                outPutSocketMessage.setSourceID(SourceID);//源ID
                outPutSocketMessage.setSql(result[i]);//下发的指令(sql语句)

                //发送数据包
                JSONObject jsonResult = outPutSocketMessage.sendMessag(SourceID);
                String status = String.valueOf(jsonResult.get("Status")) ;

                logger.error("Command : " + result[i] + " Gateway Status : " + status);

                if (!"0".equals(status)) {
                    message.setCode("-1");
                    message.setContent(jsonResult);
                    message.setMessage("failed to delete the Sequence!");
                    return message;
                }
            }
        }
        message.setCode("0");
        message.setContent("[]");
        message.setMessage("delete the Sequence successfully!");
        return message;
    }

    //添加控制动作至条件集合
    public Message addControls(JSONObject jsonObject, String DestinationID, String SourceID, int packegType) {
        Message message = new Message();
        Set<String> keyset = jsonObject.keySet();
        JSONArray list = new JSONArray();

        for (String key : keyset) {
            String[] result = SqlControlUtil.addObjects(key, (JSONArray) jsonObject.get(key));
            for (int i = 0; i < result.length; i++) {
                OutPutSocketMessage outPutSocketMessage = new OutPutSocketMessage();
                outPutSocketMessage.setPackegType(packegType);//包类型,app写2,web写-1
                outPutSocketMessage.setDestinationID(DestinationID);//app客户端可以写为任意的16个字符串,web填写目标网关地址
                outPutSocketMessage.setType("NULL");//查询就填表名,非查询填写NULL
                outPutSocketMessage.setMessage(key);//消息用于网关返回消息,下发命令的时候,可以写表名
                outPutSocketMessage.setSourceID(SourceID);//源ID
                outPutSocketMessage.setSql(result[i]);//下发的指令(sql语句)

                StringBuilder sb = new StringBuilder();
                sb.append(result[i]);
                StringBuilder SB1 = new StringBuilder();
                SB1.append(result[i]);

                sb.delete(0, sb.lastIndexOf("("));
                sb.deleteCharAt(sb.lastIndexOf("("));
                sb.deleteCharAt(sb.lastIndexOf(")"));
                String[] values = sb.toString().split(",");

                String[] keys = SB1.substring(SB1.indexOf("(") + 1, SB1.indexOf(")")).split(",");


                JSONObject json = new JSONObject();

                for (int j = 0; j < values.length; j++) {
                    json.put(keys[j].trim(), values[j].substring(values[j].indexOf("'") + 1, values[j].lastIndexOf("'")));
                }
                json.put("account_id", SourceID.substring(0, 8));
                json.put("id", UUID.randomUUID().toString());
                list.add(json);
//                System.out.println(json.toString());
//
//                TableControl tableControl = JSON.parseObject(json.toString(), TableControl.class);
//
//                int num = tableControlMapper.insertSelective(tableControl);
//
//                if (num != 1) {
//                    message.setCode("-1");
//                    message.setContent("[]");
//                    message.setMessage("Control add failed!");
//                    return message;
//                }


//                发送数据包
                JSONObject jsonResult = outPutSocketMessage.sendMessag(SourceID);
                String status = String.valueOf(jsonResult.get("Status"));
                logger.error("Command : " + result[i] + " Gateway Status : " + status);
                if(!"0".equals(status)){
                    message.setCode("-1");
                    message.setContent(jsonResult);
                    message.setMessage("failed to add the control");
                    return message;
                }
            }
        }
        message.setCode("0");
        message.setContent(list);
        message.setMessage("Control add successfully!");
        return message;
    }
    public Message modifyControls(JSONObject jsonObject, String DestinationID, String SourceID, int packegType){

        Message message = new Message();
        Set<String> keyset = jsonObject.keySet();
        for (String key : keyset) {
            String[] result = SqlControlUtil.putObjects(key, (JSONArray) jsonObject.get(key));
            for (int i = 0; i < result.length; i++) {
                OutPutSocketMessage outPutSocketMessage = new OutPutSocketMessage();
                outPutSocketMessage.setPackegType(packegType);//包类型,app写2,web写-1
                outPutSocketMessage.setDestinationID(DestinationID);//app客户端可以写为任意的16个字符串,web填写目标网关地址
                outPutSocketMessage.setSourceID(SourceID);//源ID
                outPutSocketMessage.setType("NULL");//查询就填表名,非查询填写NULL
                outPutSocketMessage.setMessage(key);//消息用于网关返回消息,下发命令的时候,可以写表名
                outPutSocketMessage.setSql(result[i]);//下发的指令(sql语句)


                //发送数据包
                JSONObject jsonResult = outPutSocketMessage.sendMessag(SourceID);
                String status = String.valueOf(jsonResult.get("Status"));
                logger.error("Command : " + result[i] + " Gateway Status : " + status);
                if (!"0".equals(status)) {
                    message.setCode("-1");
                    message.setContent(jsonResult);
                    message.setMessage("Failed to modify the conditions");
                    return message;
                }
            }
        }
        message.setCode("0");
        message.setContent("[]");
        message.setMessage("Modify the condition list successfully!");
        return message;

    }
    public Message deleteControls(JSONObject jsonObject, String DestinationID, String SourceID, int packegType) {
        Message message = new Message();
        Set<String> keyset = jsonObject.keySet();
        for (String key : keyset) {
            String[] result = SqlControlUtil.deleteObjects(key, (JSONArray) jsonObject.get(key));
            for (int i = 0; i < result.length; i++) {
                OutPutSocketMessage outPutSocketMessage = new OutPutSocketMessage();
                outPutSocketMessage.setPackegType(packegType);//包类型,app写2,web写-1
                outPutSocketMessage.setDestinationID(DestinationID);//app客户端可以写为任意的16个字符串,web填写目标网关地址
                outPutSocketMessage.setType("NULL");//查询就填表名,非查询填写NULL
                outPutSocketMessage.setMessage(key);//消息用于网关返回消息,下发命令的时候,可以写表名
                outPutSocketMessage.setSourceID(SourceID);//源ID
                outPutSocketMessage.setSql(result[i]);//下发的指令(sql语句)

                //发送数据包
                JSONObject jsonResult = outPutSocketMessage.sendMessag(SourceID);
                String status = String.valueOf(jsonResult.get("Status")) ;

                logger.error("Command : " + result[i] + " Gateway Status : " + status);

                if (!"0".equals(status)) {
                    message.setCode("-1");
                    message.setContent(jsonResult);
                    message.setMessage("failed to delete the Controls");
                    return message;
                }
            }
        }
        message.setCode("0");
        message.setContent("[]");
        message.setMessage("delete the Controls successfully!");
        return message;
    }
}
