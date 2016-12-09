package com.iot.serviceImp;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.iot.socketUtil.OutPutSocketMessage;
import com.iot.mapper.TableDeviceMapper;
import com.iot.message.Message;
import com.iot.pojo.TableDevice;
import com.iot.service.DeviceService;
import com.iot.sqlControlUtil.SqlControlUtil;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;

/**
 * Created by Jacob on 16/4/6.
 */


@Service
public class DeviceServiceImpl implements DeviceService {


    private static Logger logger =  Logger.getLogger(DeviceServiceImpl.class);
    @Resource
    private TableDeviceMapper tableDeviceMapper;

    //修改设备
    public Message modifyDevice(JSONObject jsonObject, String DestinationID, String SourceID, int packegType) {
        Set<String> keyset = jsonObject.keySet();
        Message message = new Message();
        for (String key: keyset) {
            String[] result = SqlControlUtil.controlObjects(key, (JSONArray) jsonObject.get(key));
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
                String status = String.valueOf(jsonResult.get("Status"));
                logger.error("Sql Command : " +result[i] + " Gateway_Statue : "+status);
                if (!"0".equals(status)) {

                    if ("2".equals(status)){
                        message.setCode("-1");
                        message.setContent(new JSONArray());
                        message.setMessage("Gateway Error : Device is offline!");
                    }else {
                        message.setCode("-1");
                        message.setContent("[]");
                        message.setMessage("failed to controll the device");
                    }

                    return message;
                }
            }
        }
        message.setCode("0");
        message.setContent("[]");
        message.setMessage("controll the device successfully!");
        return message;
    }

    //查找设备
    public Message findDevice(JSONObject jsonObject, String DestinationID, String SourceID, int packegType) {
        Message message = new Message();
        String result = "";
        JSONArray list =null;
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

            //发送数据包
            JSONObject jsonResult = outPutSocketMessage.sendMessag(SourceID);

            String status = String.valueOf(jsonResult.get("Status")) ;
            logger.error("Sql Command : " +result + " Gateway_Statue : "+status);
            if(!"0".equals(status)){
                message.setCode("-1");
                message.setContent("[]");
                message.setMessage("Device value search failed!");
                return message;
            }
            list=(JSONArray) jsonResult.get("List");
        }

        message.setCode("0");
        message.setMessage("Search Device successfully!");
        message.setContent(list);
        return message;
    }

    //添加设备
    public Message addDevice(JSONObject jsonObject, String DestinationID, String SourceID, int packegType) {
        Message message = new Message();
        Set<String> keyset = jsonObject.keySet();
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

                //发送数据包
                JSONObject jsonResult = outPutSocketMessage.sendMessag(SourceID);
                String status = (String) jsonResult.get("Status");
                if(!"0".equals(status)){
                    message.setCode("-1");
                    message.setContent(jsonResult);
                    message.setMessage("设备添加失败");
                }
            }
        }
        message.setCode("0");
        message.setContent("[]");
        message.setMessage("设备添加成功");
        return message;
    }

    //删除设备
    public Message deleteDevice(JSONObject jsonObject, String DestinationID, String SourceID, int packegType) {
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
                //发送数据包
                JSONObject jsonResult = outPutSocketMessage.sendMessag(SourceID);
                String status = (String) jsonResult.get("Status");
                if(!"0".equals(status)){
                    message.setCode("-1");
                    message.setContent(jsonResult);
                    message.setMessage("设备删除失败");
                }
            }
        }
        message.setCode("0");
        message.setContent("[]");
        message.setMessage("设备删除成功");
        return message;
    }

    public Message findDevice(String account_id) {
        Message message = new Message();
        String result = "";

        List<TableDevice> tableDeviceList=tableDeviceMapper.selectByAccountId(account_id);

        message.setCode("0");
        message.setMessage("设备查询成功");
        message.setContent(tableDeviceList);
        return message;
    }
}
