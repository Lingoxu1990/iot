package com.iot.serviceImp;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.iot.socketUtil.OutPutSocketMessage;
import com.iot.mapper.*;
import com.iot.message.Message;
import com.iot.pojo.*;
import com.iot.service.RegionService;
import com.iot.spitUtil.Param;
import com.iot.sqlControlUtil.SqlControlUtil;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * Created by xulingo on 16/4/5.
 */
@Service
public class RegionServiceImpl implements RegionService {

    private static Logger logger =Logger.getLogger(RegionServiceImpl.class);
    @Resource
    private TableDeviceMapper tabledevicemapper;
    @Resource
    private TableRegionMapper tableRegionMapper;
    @Resource
    private TableChannelMapper tableChannelMapper;
    @Resource
    private TableRegionDeviceMapper tableRegionDeviceMapper;
    @Resource
    private TableRegionGroupMapper tableRegionGroupMapper;
    @Resource
    private TableRegionSceneMapper tableRegionSceneMapper;
    @Resource
    private TableCdtsListMapper tableCdtsListMapper;


    public Message modifyRegion(JSONObject jsonObject, String DestinationID, String SourceID, int packegType) {

        Set<String> keyset = jsonObject.keySet();
        Message message = new Message();

        for (String key : keyset) {
            String[] result = SqlControlUtil.putObjects(key, (JSONArray) jsonObject.get(key));

            for (int i = 0; i < result.length; i++) {
                OutPutSocketMessage outPutSocketMessage = new OutPutSocketMessage();
                outPutSocketMessage.setPackegType(packegType);//包类型,app写2,web写-1
                outPutSocketMessage.setDestinationID(DestinationID);//app客户端可以写为任意的16个字符串,web填写目标网关地址
                outPutSocketMessage.setType("NULL");//查询就填表名,非查询填写NULL
                outPutSocketMessage.setMessage(key);//消息用于网关返回消息,下发命令的时候,可以写表名
                outPutSocketMessage.setSourceID(SourceID);//源ID
                outPutSocketMessage.setSql(result[i]);//下发的指令(sql语句)
                //发送数据包
                JSONObject reslut = outPutSocketMessage.sendMessag(SourceID);
                String status = (String) reslut.get("Status");
                if (!"0".equals(status)) {
                    message.setCode("-1");
                    message.setContent(reslut);
                    message.setMessage("区域修改失败");
                }
            }
        }
        message.setCode("0");
        message.setContent("[]");
        message.setMessage("区域修改成功");
        return message;
    }

    public JSONArray findRegion(JSONObject jsonObject, String DestinationID, String SourceID, int packegType) {
        String result = "";
        Message message = new Message();
        JSONArray list =null;
        Set<String> keyset = jsonObject.keySet();

        for (String key : keyset) {
            result = SqlControlUtil.selectObjects(key, (JSONObject) jsonObject.get(key));
            OutPutSocketMessage outPutSocketMessage = new OutPutSocketMessage();
            outPutSocketMessage.setPackegType(packegType);//包类型,app写2,web写-1
            outPutSocketMessage.setDestinationID(DestinationID);//app客户端可以写为任意的16个字符串,web填写目标网关地址
            outPutSocketMessage.setType(key);//查询就填表名,非查询填写NULL
            outPutSocketMessage.setMessage(key);//消息用于网关返回消息,下发命令的时候,可以写表名
            outPutSocketMessage.setSourceID(SourceID);//源ID
            outPutSocketMessage.setSql(result);//下发的指令(sql语句)
//
//            JSONObject region = (JSONObject) jsonObject.get(key);
//
//            TableRegion tableRegion = JSONObject.parseObject(region.toString(),TableRegion.class);
//
//            tableRegion.setAccount_id(SourceID.substring(0,10));
//
//            List<TableRegion> tableRegions = tableRegionMapper.selectByGatewayId(tableRegion);



            //发送数据包
            JSONObject reslut = outPutSocketMessage.sendMessag(SourceID);

            System.out.println(reslut.toString());
            String status = String.valueOf(reslut.get("Status")) ;
            if (!"0".equals(status)) {
               continue;
            }
            list=(JSONArray) reslut.get("List");
        }
//        message.setCode("0");
//        message.setMessage("region search successfully!");
//        message.setContent(list);
//        return message;
        return list;

    }

    public Message addRegion(JSONObject jsonObject, String DestinationID, String SourceID, int packegType) {

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

                System.out.println(result[i]);

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
//                json.put("account_id",SourceID.substring(0,10));
//                json.put("id",UUID.randomUUID().toString());
                list.add(json);
//
//                TableRegion tableRegion =  JSONObject.parseObject(json.toString(),TableRegion.class);
//
//                int num = tableRegionMapper.insertSelective(tableRegion);
//
//                if (num!=1){
//                    message.setCode("-1");
//                    message.setContent(json);
//                    message.setMessage("region add failed!");
//                    return message;
//                }

                //发送数据包
                JSONObject reslut = outPutSocketMessage.sendMessag(SourceID);


                String status = String.valueOf(reslut.get("Status"));

                if (!"0".equals(status)) {
                    message.setCode("-1");
                    message.setContent("[]");
                    message.setMessage("区域创建失败");
                    return message;
                }
            }

        }

        message.setCode("0");
        message.setContent(list);
        message.setMessage("region add successfully!");
        return message;
    }

    public Message deleteRegion(JSONObject jsonObject, String DestinationID, String SourceID, int packegType) {
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
                JSONObject reslut = outPutSocketMessage.sendMessag(SourceID);
                String status = (String) reslut.get("Status");
                if (!"0".equals(status)) {
                    message.setCode("-1");
                    message.setContent(reslut);
                    message.setMessage("区域删除失败");
                }
            }
        }
        message.setCode("0");
        message.setMessage("区域删除成功");
        message.setContent("[]");
        return message;
    }

    public Message addRegionDevice(JSONObject jsonObject, String DestinationID, String SourceID, int packegType) {
        Message message = new Message();
        Set<String> keyset = jsonObject.keySet();

        JSONArray list= new JSONArray();
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
//                System.out.println(result[i]);
//                StringBuilder sb= new StringBuilder();
//                sb.append(result[i]);
//                StringBuilder SB1 =new StringBuilder();
//                SB1.append(result[i]);
//
//                sb.delete(0,sb.lastIndexOf("("));
//                sb.deleteCharAt(sb.lastIndexOf("("));
//                sb.deleteCharAt(sb.lastIndexOf(")"));
//                String []  values = sb.toString().split(",");
//                String [] keys = SB1.substring(SB1.indexOf("(")+1,SB1.indexOf(")")).split(",");
//
//                JSONObject json = new JSONObject();
//
//                for (int j = 0; j <values.length ; j++) {
//                    json.put(keys[j].trim(),values[j].substring(values[j].indexOf("'")+1,values[j].lastIndexOf("'")));
//                }
//                json.put("id",UUID.randomUUID().toString());
//                json.put("account_id","0000123477");
//
//                TableRegionDevice tableRegionDevice = JSONObject.parseObject(json.toJSONString(),TableRegionDevice.class);
//                list.add(json);
//
//                tableRegionDeviceMapper.insertSelective(tableRegionDevice);

//                //发送数据包
//                System.out.println(outPutSocketMessage.test().toString());
                JSONObject reslut = outPutSocketMessage.sendMessag(SourceID);
                System.out.println(reslut.toString());
                Integer SS = (Integer)reslut.get("Status");
                JSONArray LL =(JSONArray)reslut.get("List");
                String status = String.valueOf(SS);
                if (!"0".equals(status)) {
                    if ("2".equals(status)){
                        message.setCode("0");
                        message.setContent(LL);
                        message.setMessage("Gateway Error : Device is offline!");
                    }else {
                        message.setCode("-1");
                        message.setContent(LL);
                        message.setMessage("Failed to add the device into the region!");
                    }

                    return message;
                }

            }

        }
        message.setCode("0");
        message.setContent(list);
        message.setMessage("devices has been added into the region");
        return message;

    }

    public Map<String,JSONObject> specialAction(JSONArray alldevies,String sourceId,String tablename,String region_gateway_id){
        int device_num =alldevies.size();
        Map<String,JSONArray> tempGateway_id = new HashMap<String, JSONArray>();

        //将设备按照 网关进行分组
        for (int i = 0; i <device_num ; i++) {

            JSONObject jsonobject=(JSONObject) alldevies.get(i);

            String gateway_id = (String) jsonobject.get("gateway_id");

            JSONArray entities= tempGateway_id.get(gateway_id);

            if (entities==null){
                entities=new JSONArray();
                entities.add(jsonobject);
                tempGateway_id.put(gateway_id,entities);
            }else {
                entities.add(jsonobject);
                tempGateway_id.put(gateway_id,entities);
            }

        }
        //获得按照网关分组的设备群
        Set<Map.Entry<String,JSONArray>> entries = tempGateway_id.entrySet();
        Map<String,JSONObject> result = new HashMap<String, JSONObject>();

        if (!"table_region_device".equals(tablename)){
            for (Map.Entry<String,JSONArray> entry: entries) {
                JSONObject J = new JSONObject();
                J.put(tablename,entry.getValue());
                result.put(entry.getKey(),J);
            }
            return result;
        }

        for (Map.Entry<String,JSONArray> entry: entries) {

            //key为gateway_id
            String key=  entry.getKey();
            //value 为设备列表
            JSONArray value =  entry.getValue();

            int size = value.size();

            JSONArray region_devices_channels = new JSONArray();

            for (int i = 0; i <size ; i++) {

                JSONObject region_deviece = (JSONObject) value.get(i);

                String guid = (String) region_deviece.get("table_device_guid");

//                JSONObject jj = main.singoTest("select * from table_device where device_guid = '"+guid+"'");


                String sql1 = "select * from table_device where device_guid = '"+guid+"'";
                OutPutSocketMessage outPutSocketMessage1 = new OutPutSocketMessage();
                outPutSocketMessage1.setPackegType(2);//包类型，app为2，web端为-1
                outPutSocketMessage1.setDestinationID(key);//app客户可以为任意的16个字符串，web填写目标网关地址
                outPutSocketMessage1.setType("NULL");//查询就填写表明，非查询就填写“NULL”
                outPutSocketMessage1.setMessage("table_device");//消息用于网关返回消息，下发命令时可以填写表明
                outPutSocketMessage1.setSourceID(sourceId);//源ID
                outPutSocketMessage1.setSql(sql1);//下发sql指令

                JSONObject r = outPutSocketMessage1.sendMessag(sourceId);


                JSONArray ll = (JSONArray) r.get("List");

                List<JSONObject> list = new ArrayList<JSONObject>();
                for (int j = 0; j <ll.size() ; j++) {
                    list.add( (JSONObject) ll.get(i));
                }

                //根据主键查询出的设备详细信息
                JSONObject device = list.get(0);
                String device_type = (String) device.get("device_type");

                if (device_type.equals(Param.DEVICETYPE)){
                    String region_guid = (String) region_deviece.get("region_guid");


//                    TableRegion tableRegion = tableRegionMapper.selectByRegionGuid(region_guid);

                    if(!region_gateway_id.equals(key)){
                        device.remove("device_valid");
                        device.put("device_valid","A");
                        device.remove("id");
                        device.remove("account_id");
                        device.remove("region_bunding");
                        JSONArray specialDevics = new JSONArray();
                        specialDevics.add(device);
                        String [] sql = SqlControlUtil.addObjects("table_device", specialDevics);
                        OutPutSocketMessage outPutSocketMessage= new OutPutSocketMessage();
                        outPutSocketMessage.setDestinationID(region_gateway_id);
                        outPutSocketMessage.setPackegType(2);
                        outPutSocketMessage.setSql(sql[0]);
                        outPutSocketMessage.setSourceID(sourceId);
                        outPutSocketMessage.setMessage("Sensors copy");
                        outPutSocketMessage.setType("NULL");

                        System.out.println(outPutSocketMessage.test().toString());


                        JSONObject resp =outPutSocketMessage.sendMessag(sourceId);
                        Integer integer = (Integer) resp.get("Status");
                        String rr = String.valueOf(integer);
                        if (!rr.equals("0")){
                            Map<String,JSONObject> map= new HashMap<String, JSONObject>();
                            return map;
                        }
                    }
                }
                String channel_sql = "select * from table_channel where table_device_guid = '"+guid+"'";
                OutPutSocketMessage outPutSocketMessage= new OutPutSocketMessage();
                outPutSocketMessage.setPackegType(2);//包类型，app为2，web端为-1
                outPutSocketMessage.setDestinationID(key);//app客户可以为任意的16个字符串，web填写目标网关地址
                outPutSocketMessage.setType("NULL");//查询就填写表明，非查询就填写“NULL”
                outPutSocketMessage.setMessage("table_channel");//消息用于网关返回消息，下发命令时可以填写表明
                outPutSocketMessage.setSourceID(sourceId);//源ID
                outPutSocketMessage.setSql(channel_sql);//下发sql指令

                JSONObject channel_result = outPutSocketMessage.sendMessag(sourceId);
                JSONArray channelList = (JSONArray) channel_result.get("List");

                for (int j = 0; j <channelList.size() ; j++) {

                    JSONObject channel =(JSONObject) channelList.get(j);
                    JSONObject json= new JSONObject();

                    json.put("region_device_guid", UUID.randomUUID().toString());
                    json.put("region_guid",region_deviece.get("region_guid"));
                    json.put("region_addr",region_deviece.get("region_addr"));
                    json.put("region_name",region_deviece.get("region_name"));
                    json.put("table_device_guid",region_deviece.get("table_device_guid"));
                    json.put("gateway_id",region_deviece.get("gateway_id"));
                    json.put("device_addr",region_deviece.get("device_addr"));
                    json.put("device_name",region_deviece.get("device_name"));
                    json.put("channel_class",channel.get("channel_class"));
                    json.put("channel_guid",channel.get("channel_guid"));
                    json.put("channel_name",channel.get("channel_name"));
                    json.put("channel_type",channel.get("channel_type"));
                    json.put("channel_bit_num",channel.get("channel_number"));

                    region_devices_channels.add(json);

                }




//                List<TableChannel> channelList = tableChannelMapper.selectByDeviceGuid(guid);

                // 设备通道扩展
//                for (TableChannel channel: channelList) {
//                    JSONObject json= new JSONObject();
//
//                    json.put("region_device_guid", UUID.randomUUID().toString());
//                    json.put("region_guid",region_deviece.get("region_guid"));
//                    json.put("region_addr",region_deviece.get("region_addr"));
//                    json.put("region_name",region_deviece.get("region_name"));
//                    json.put("table_device_guid",region_deviece.get("table_device_guid"));
//                    json.put("gateway_id",region_deviece.get("gateway_id"));
//                    json.put("device_addr",region_deviece.get("device_addr"));
//                    json.put("device_name",region_deviece.get("device_name"));
//                    json.put("channel_class",channel.getChannel_class());
//                    json.put("channel_guid",channel.getChannel_guid());
//                    json.put("channel_name",channel.getChannel_name());
//                    json.put("channel_type",channel.getChannel_type());
//                    json.put("channel_bit_num",channel.getChannel_number());
//
//                    region_devices_channels.add(json);
//                }
            }

            JSONObject temp = new JSONObject();
            temp.put("table_region_device",region_devices_channels);
            System.out.println(temp.toString());
            result.put(key,temp);
        }


        return  result;
    }

    public Message findRegionDevice(JSONObject jsonObject, String DestinationID, String SourceID, int packegType) {
        String result = "";
        Message message = new Message();
        JSONArray list =null;
        Set<String> keyset = jsonObject.keySet();

        for (String key : keyset) {
            result = SqlControlUtil.selectObjects(key, (JSONObject) jsonObject.get(key));
            OutPutSocketMessage outPutSocketMessage = new OutPutSocketMessage();
            outPutSocketMessage.setPackegType(packegType);//包类型,app写2,web写-1
            outPutSocketMessage.setDestinationID(DestinationID);//app客户端可以写为任意的16个字符串,web填写目标网关地址
            outPutSocketMessage.setType(key);//查询就填表名,非查询填写NULL
            outPutSocketMessage.setMessage(key);//消息用于网关返回消息,下发命令的时候,可以写表名
            outPutSocketMessage.setSourceID(SourceID);//源ID
            outPutSocketMessage.setSql(result);//下发的指令(sql语句)

            //发送数据包
            JSONObject reslut = outPutSocketMessage.sendMessag(SourceID);
            System.out.println(reslut.toString());
            String status = String.valueOf(reslut.get("Status")) ;
            if (!"0".equals(status)) {

                message.setContent(reslut.get("List"));
                if ("3".equals(status)){
                    message.setCode("0");
                    message.setMessage("No result!");
                }else {
                    message.setCode("-1");
                    message.setMessage("region search failed!");
                }
                return message;
            }
            list=(JSONArray) reslut.get("List");
        }
        message.setCode("0");
        message.setMessage("region search successfully!");
        message.setContent(list);
        return message;
    }

//    public JSONArray findRegionDevice(TableRegionDevice tableRegionDevice) {
//
//        List<TableRegionDevice> tableRegionDeviceList = tableRegionDeviceMapper.selectByAccountIdAndRegiongGuid(tableRegionDevice);
//
//        JSONObject jresult = new JSONObject();
//
//        for (TableRegionDevice ta: tableRegionDeviceList) {
//
//            if(jresult.get(ta.getTable_device_guid())==null){
//                JSONArray jsonArray = new JSONArray();
//                jsonArray.add(JSONObject.toJSON(ta));
//                jresult.put(ta.getTable_device_guid(),jsonArray);
//            }else {
//                JSONArray jsonArray = (JSONArray) jresult.get(ta.getTable_device_guid());
//                jsonArray.add(JSONObject.toJSON(ta));
//            }
//
//        }
//
//
//        JSONObject result = new JSONObject();
//        JSONArray new_arr = new JSONArray();
//
//        for (String key : jresult.keySet()) {
//            JSONArray arr = (JSONArray) jresult.get(key);
//
//            JSONObject tempJ = (JSONObject) arr.get(0);
//
//            String id = (String) tempJ.get("id");
//            String region_device_guid= (String) tempJ.get("region_device_guid");
//            String region_guid= (String) tempJ.get("region_guid");
//            String region_addr= (String) tempJ.get("region_addr");
//            String table_device_guid= (String) tempJ.get("table_device_guid");
//            String gateway_id= (String) tempJ.get("gateway_id");
//            String device_addr= (String) tempJ.get("device_addr");
//            String device_name= (String) tempJ.get("device_name");
//            String account_id= (String) tempJ.get("account_id");
//
//            JSONObject json  = new JSONObject();
//
//            json.put("region_device_guid",region_device_guid);
//            json.put("region_guid",region_guid);
//            json.put("region_addr",region_addr);
//            json.put("table_device_guid",table_device_guid);
//            json.put("gateway_id",gateway_id);
//            json.put("device_addr",device_addr);
//            json.put("device_name",device_name);
//            json.put("account_id",account_id);
//            JSONArray channels = new JSONArray();
//            for (int i = 0; i <arr.size() ; i++) {
//
//                JSONObject jj = (JSONObject) arr.get(i);
//                JSONObject channel = new JSONObject();
//
//                channel.put("channel_name",jj.get("channel_name"));
//                channel.put("channel_guid",jj.get("channel_guid"));
//                channel.put("channel_bit_num",jj.get("channel_bit_num"));
//                channel.put("channel_class",jj.get("channel_class"));
//                channel.put("channel_type",jj.get("channel_type"));
//                channels.add(channel);
//            }
//            json.put("channels",channels);
//            new_arr.add(json);
//
//        }
//
//        return new_arr;
//
//    }

    public Message deleteRegionDevice(JSONObject jsonObject,String DestinationID,String SourceID,int packegType) {
        Message message =new Message();

        Set<String> keys = jsonObject.keySet();

        JSONArray list= new JSONArray();
        for (String key:keys) {

            String [] sqls = SqlControlUtil.deleteObjects("table_region_device", (JSONArray) jsonObject.get(key));

            for (int i = 0; i<sqls.length;i++){
                OutPutSocketMessage outPutSocketMessage = new OutPutSocketMessage();
                outPutSocketMessage.setPackegType(packegType);//包类型,app写2,web写-1
                outPutSocketMessage.setDestinationID(DestinationID);//app客户端可以写为任意的16个字符串,web填写目标网关地址
                outPutSocketMessage.setType("NULL");//查询就填表名,非查询填写NULL
                outPutSocketMessage.setMessage(key);//消息用于网关返回消息,下发命令的时候,可以写表名
                outPutSocketMessage.setSourceID(SourceID);//源ID
                outPutSocketMessage.setSql(sqls[i]);//下发的指令(sql语句)

                System.out.println(sqls[i]);

            JSONObject jsonResult = outPutSocketMessage.sendMessag(SourceID);
            String status = String.valueOf(jsonResult.get("Status"));
            if(!"0".equals(status)){

                if ("2".equals(status)){
                    message.setCode("0");
                    message.setContent(new JSONArray());
                    message.setMessage("Gateway Error : Device is offline!");
                }else {
                    message.setCode("-1");
                    message.setContent(jsonResult);
                    message.setMessage("failed to remove the device from the region!");
                }

                return message;
            }
            }


        }
        message.setCode("0");
        message.setContent("[]");
        message.setMessage("device has been removed from the region!");

        return message;
    }

    public Message addRegionGroup(JSONObject jsonObject, String DestinationID, String SourceID, int packegType) {

        Message message = new Message();
        Set<String> keyset = jsonObject.keySet();

        JSONArray list= new JSONArray();
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
//
//                TableRegionGroup tableRegionGroup = JSONObject.parseObject(json.toJSONString(),TableRegionGroup.class);
//                list.add(json);
//
//                tableRegionGroupMapper.insertSelective(tableRegionGroup);


                //发送数据包
//                System.out.println(outPutSocketMessage.test().toString());
                JSONObject reslut = outPutSocketMessage.sendMessag(SourceID);
                String status = String.valueOf(reslut.get("Status"));
                if (!"0".equals(status)) {
                    message.setCode("-1");
                    message.setContent("[]");
                    message.setMessage("Failed to add group to the region!");
                    return message;
                }
            }

        }
        message.setCode("0");
        message.setContent(list);
        message.setMessage("groups has been added into the region!");
        return message;
    }
    //根据区域与用户id查找对应的组

    public Message findRegionGroupByAccountIdAndRegionId(JSONObject jsonObject, String DestinationID, String SourceID, int packegType) {
        String result = "";
        Message message = new Message();
        JSONArray list =null;
        Set<String> keyset = jsonObject.keySet();

        for (String key : keyset) {
            result = SqlControlUtil.selectObjects(key, (JSONObject) jsonObject.get(key));
            OutPutSocketMessage outPutSocketMessage = new OutPutSocketMessage();
            outPutSocketMessage.setPackegType(packegType);//包类型,app写2,web写-1
            outPutSocketMessage.setDestinationID(DestinationID);//app客户端可以写为任意的16个字符串,web填写目标网关地址
            outPutSocketMessage.setType(key);//查询就填表名,非查询填写NULL
            outPutSocketMessage.setMessage(key);//消息用于网关返回消息,下发命令的时候,可以写表名
            outPutSocketMessage.setSourceID(SourceID);//源ID
            outPutSocketMessage.setSql(result);//下发的指令(sql语句)

            System.out.println();
            //发送数据包
            JSONObject reslut = outPutSocketMessage.sendMessag(SourceID);

            String status = String.valueOf(reslut.get("Status")) ;
            if (!"0".equals(status)) {

                message.setContent(reslut.get("List"));
                if ("3".equals(status)){
                    message.setCode("0");
                    message.setMessage("No result!");
                }else {
                    message.setCode("-1");
                    message.setMessage("region search failed!");
                }

                return message;
            }
            list=(JSONArray) reslut.get("List");
        }
        message.setCode("0");
        message.setMessage("region search successfully!");
        message.setContent(list);
        return message;
    }
    public JSONArray findRegionGroupByAccountIdAndRegionId(TableRegionGroup tableRegionGroup) {
        List<TableRegionGroup> list = tableRegionGroupMapper.findRegionGroupByAccountIdAndRegionId(tableRegionGroup);
        JSONArray jresult = new JSONArray();

        for (TableRegionGroup ta: list) {
            jresult.add(JSONObject.toJSON(ta));
        }

        return jresult;

    }
    //根据组guid与用户id查找对应的组
    public JSONObject findRegionGroupByAccountIdAndGroupId(TableRegionGroup tableRegionGroup) {

        TableRegionGroup tableRegionGroup1 = tableRegionGroupMapper.findRegionGroupByAccountIdAndGroupId(tableRegionGroup);


        JSONObject jresult = (JSONObject) JSONObject.toJSON(tableRegionGroup1);

        return jresult;

    }
    //删除区域组
    public Message deleteRegionGroup(JSONObject jsonObject, String DestinationID, String SourceID, int packegType) {

        Message message = new Message();
        Set<String> keyset = jsonObject.keySet();

        JSONArray list= new JSONArray();
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

//                System.out.println(result[i]);
//                StringBuilder sb= new StringBuilder();
//                sb.append(result[i]);
//                StringBuilder SB1 =new StringBuilder();
//                SB1.append(result[i]);
//
//                sb.delete(0,sb.indexOf("WHERE")+6);
//
//                SB1.delete(0,SB1.lastIndexOf("region_guid"));
//
//                String [] kv1=SB1.toString().split("=");
//                String [] kv2=sb.substring(0,sb.indexOf("AND")-1).toString().split("=");
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
//                list.add(json);
//
//
//                TableRegionGroup tableRegionGroup= JSONObject.parseObject(json.toString(),TableRegionGroup.class);
//
//                int num =tableRegionGroupMapper.deleteGroup(tableRegionGroup);
//
//                if (num<1){
//                    message.setCode("-1");
//                    message.setMessage("delete failed!");
//                    message.setContent("[]");
//                }

                System.out.println(result[i]);
                //发送数据包
                JSONObject reslut = outPutSocketMessage.sendMessag(SourceID);
                String status = String.valueOf(reslut.get("Status")) ;
                if (!"0".equals(status)) {
                    message.setCode("-1");
                    message.setContent(reslut);
                    message.setMessage("failed to remove the group from the region");
                    return message;
                }
            }
        }
        message.setCode("0");
        message.setMessage("the group in the selected region has been deleted!");
        message.setContent("[]");
        return message;
    }
    //修改区域组
    public Message modifyRegionGroup(JSONObject jsonObject, String DestinationID, String SourceID, int packegType) {

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

                TableRegionGroup tableGroup = JSONObject.parseObject(json.toString(),TableRegionGroup.class);

                //发送数据包
                JSONObject jsonResult = outPutSocketMessage.sendMessag(SourceID);
                String status = String.valueOf(jsonResult.get("Status"));
                if (!"0".equals(status)) {
                    logger.error("Commond : "+result[i]+" Gateway Statue : "+status);
                    message.setCode("-1");
                    message.setContent(jsonResult);
                    message.setMessage("Failed to modify the region_group name");
                    return message;
                }
                int num = tableRegionGroupMapper.updateByAccountIdAndGroupGuid(tableGroup);

                if (num!=1){
                    logger.error("Commond : "+result[i]+" web Statue : Data Updata error!");
                    message.setCode("-1");
                    message.setContent(json);
                    message.setMessage("Failed to update the region group name!");
                    return message;
                }
            }
        }
        message.setCode("0");
        message.setContent("[]");
        message.setMessage("Update the region group name successfully!");
        return message;
    }
    //增加区域场景
    public Message addRegionScene(JSONObject jsonObject, String DestinationID, String SourceID, int packegType) {
        Message message = new Message();
        Set<String> keyset = jsonObject.keySet();

        JSONArray list= new JSONArray();
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
//
//                JSONObject json = new JSONObject();
//
//                for (int j = 0; j <values.length ; j++) {
//                    json.put(keys[j].trim(),values[j].substring(values[j].indexOf("'")+1,values[j].lastIndexOf("'")));
//                }
//                json.put("id",UUID.randomUUID().toString());
//                json.put("account_id",SourceID.substring(0,10));
//
//                TableRegionScene tableRegionScene = JSONObject.parseObject(json.toJSONString(),TableRegionScene.class);
//                list.add(json);
//
//                tableRegionSceneMapper.insertSelective(tableRegionScene);

                //发送数据包
//                System.out.println(outPutSocketMessage.test().toString());
                JSONObject reslut = outPutSocketMessage.sendMessag(SourceID);
                String status = String.valueOf(reslut.get("Status"));
                if (!"0".equals(status)) {
                    message.setCode("-1");
                    message.setContent(reslut);
                    message.setMessage("failed to add the scene to the region");
                    return message;
                }
            }

        }
        System.out.println(list.toString());
        message.setCode("0");
        message.setContent(list);
        message.setMessage("groups has been added into the region!");
        return message;
    }
    //查找区域场景的主键
    public JSONObject findRegionSceneByAccountIdAndSceneId(TableRegionScene tableRegionScene) {

        TableRegionScene tableRegionScene1 = tableRegionSceneMapper.findRegionSceneByAccountIdAndSceneId(tableRegionScene);

        JSONObject jresult = (JSONObject) JSONObject.toJSON(tableRegionScene1);

        return jresult;
    }
    //修改区域场景名称
    public Message modifyRegionScene(JSONObject jsonObject, String DestinationID, String SourceID, int packegType) {

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

                TableRegionScene tableRegionScene = JSONObject.parseObject(json.toString(),TableRegionScene.class);

                int num = tableRegionSceneMapper.updateByAccountIdAndSceneGuid(tableRegionScene);

                if (num!=1){
                    message.setCode("-1");
                    message.setContent(json);
                    message.setMessage("Update the region scene name failed!");
                    return message;
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
        message.setMessage("Update the region scene name successfully!");
        return message;
    }
    //删除区域下的场景
    public Message deleteRegionScene(JSONObject jsonObject, String DestinationID, String SourceID, int packegType) {
        Message message = new Message();
        Set<String> keyset = jsonObject.keySet();

        JSONArray list= new JSONArray();
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

                System.out.println(result[i]);
//                StringBuilder sb= new StringBuilder();
//                sb.append(result[i]);
//
//                sb.delete(0,sb.indexOf("WHERE")+6);
//
//                String [] kv2=sb.toString().trim().split("=");
//
//                JSONObject json  =new JSONObject();
//
//                StringBuilder stringBuilder2 = new StringBuilder();
//                stringBuilder2.append(kv2[1].trim());
//                stringBuilder2.deleteCharAt(0);
//                stringBuilder2.deleteCharAt(stringBuilder2.length()-1);
//
//                json.put(kv2[0],stringBuilder2.toString());
//                json.put("account_id",SourceID.substring(0,10));
//
//                list.add(json);
//
//
//                TableRegionScene tableRegionScene= JSONObject.parseObject(json.toString(),TableRegionScene.class);
//
//                int num =tableRegionSceneMapper.deleteScene(tableRegionScene);
//
//                if (num<1){
//                    message.setCode("-1");
//                    message.setMessage("delete failed!");
//                    message.setContent("[]");
//                }
//
//                //发送数据包
//                System.out.println(outPutSocketMessage.test().toString());


//                //发送数据包
                JSONObject reslut = outPutSocketMessage.sendMessag(SourceID);
                String status = String.valueOf(reslut.get("Status"));
                if (!"0".equals(status)) {
                    message.setCode("-1");
                    message.setContent(reslut);
                    message.setMessage("区域删除失败");
                    return message;
                }
            }
        }
        message.setCode("0");
        message.setMessage("the scene in the selected region has been deleted!");
        message.setContent("[]");
        return message;
    }
    //查找区域下的场景
    public Message findRegionSceneByAccountIdAndRegionId(JSONObject jsonObject, String DestinationID, String SourceID, int packegType) {
        String result = "";
        Message message = new Message();
        JSONArray list =null;
        Set<String> keyset = jsonObject.keySet();

        for (String key : keyset) {
            result = SqlControlUtil.selectObjects(key, (JSONObject) jsonObject.get(key));
            OutPutSocketMessage outPutSocketMessage = new OutPutSocketMessage();
            outPutSocketMessage.setPackegType(packegType);//包类型,app写2,web写-1
            outPutSocketMessage.setDestinationID(DestinationID);//app客户端可以写为任意的16个字符串,web填写目标网关地址
            outPutSocketMessage.setType(key);//查询就填表名,非查询填写NULL
            outPutSocketMessage.setMessage(key);//消息用于网关返回消息,下发命令的时候,可以写表名
            outPutSocketMessage.setSourceID(SourceID);//源ID
            outPutSocketMessage.setSql(result);//下发的指令(sql语句)

            //发送数据包
            JSONObject reslut = outPutSocketMessage.sendMessag(SourceID);
            System.out.println(reslut.toString());
            String status = String.valueOf(reslut.get("Status")) ;
            if (!"0".equals(status)) {

                message.setContent(reslut.get("List"));
                if ("3".equals(status)){
                    message.setCode("0");
                    message.setMessage("No result!");
                }else {
                    message.setCode("-1");
                    message.setMessage("Failed to search the region scene!");
                }
                return message;
            }
            list=(JSONArray) reslut.get("List");
        }
        message.setCode("0");
        message.setMessage("region scene search successfully!");
        message.setContent(list);
        return message;
    }
    //添加区域下的条件控制集
    public Message addRegionCdts(JSONObject jsonObject, String DestinationID, String SourceID, int packegType) {

        Message message = new Message();
        Set<String> keyset = jsonObject.keySet();

        JSONArray list= new JSONArray();
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
                json.put("id",UUID.randomUUID().toString());
                json.put("account_id",SourceID.substring(0,10));

                 TableCdtsList tableCdtsList = JSONObject.parseObject(json.toJSONString(),TableCdtsList.class);
                list.add(json);

//                int num = tableCdtsListMapper.insertSelective(tableCdtsList);
//
//                if(num!=1){
//                    message.setCode("0-1");
//                    message.setContent(json);
//                    message.setMessage("Conditions and controls failed to added into the region!");
//                    return message;
//                }


                //发送数据包
                System.out.println(outPutSocketMessage.test().toString());
                JSONObject reslut = outPutSocketMessage.sendMessag(SourceID);
                String status = String.valueOf(reslut.get("Status"));
                if (!"0".equals(status)) {
                    message.setCode("-1");
                    message.setContent(reslut);
                    message.setMessage("区域创建失败");
                    return message;
                }
            }

        }
        System.out.println(list.toString());
        message.setCode("0");
        message.setContent(list);
        message.setMessage("Conditions and controls have been added into the region!");
        return message;
    }

    public Message controlReion(JSONObject jsonObject, String DestinationID, String SourceID, int packegType) {
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
                        message.setMessage("Failed to control the region");
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

