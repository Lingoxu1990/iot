package com.iot.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.iot.requestUtil.RequestCaseUtil;
import com.iot.socketUtil.OutPutSocketMessage;
import com.iot.dbUtil.PropsUtil;
import com.iot.message.Message;
import com.iot.pojo.AccountDataInfo;
import com.iot.pojo.UserGateway;
import com.iot.service.DeviceService;
import com.iot.service.UserService;
import com.iot.spitUtil.Param;
import com.iot.utils.ParamUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * 该类为设备服务的控制层,设备的查询/修改/删除/控制
 * Created by Jacob on 16/4/6.
 */
/*
* 设备控制
* */
@Controller
@RequestMapping()
public class DeviceController {

    private static Logger logger = Logger.getLogger(DeviceController.class);

    @Resource
    private DeviceService deviceService;
    @Resource
    private UserService userService;

    /**
     * 设备添加 (由系统初始化,本接口不开放使用)
     * @return
     */
    @RequestMapping(value = "/table_device",method = RequestMethod.POST)
    @ResponseBody
    public Object addDevice(HttpServletRequest httpServletRequest) throws IOException {

        JSONObject jsonObject = ParamUtils.getAttributess(httpServletRequest);

        Message message = deviceService.addDevice(jsonObject,"","",-1);

        return  message;
    }


    /**
     * 修改设备接口
     * 1 该接口目前用语设备控制
     *   控制逻辑
     *   UPDATE table_device SET column='value' WHERE device_addr = 'ff15::xxxx'
     *   利用参数及sql映射,生成上述SQL语句,封装后使用socket交互
     *
     * {
     * "user_id":"12345667",
     * "table_group":[{
     * "group_guid":"468df2c790714a72a7c54646c87dc315",
     * "group_addr":"ff15::1028",
     * "group_value":[{
     * "channel_number":"1",
     * "value":"255"
     * },{
     * "channel_number":"2",
     * "value":"255"
     * },{
     * "channel_number":"3",
     * "value":"255"
     * },{
     * "channel_number":"4",
     * "value":"255"
     * }],
     * "group_delay":"0"
     * }]
     * }
     *   1.1 根据用户user_id获取用户帐号id,提取web/app的本地4位字符串,加上随机生成的4位字符串,拼接生成SourceId
     *   1.2 根据入参中的value,做16进制数转化,生成设备的device_value,删除value字段,添加device_value字段.
     *   1.3 将修正好的入参传入sql工具的生成方法中,产生sql控制语句
     *   1.4 调用Socket通信封装包,获取响应,处理响应结果,返回给调用者
     *
     * 2 原则上说,该接口的开放的功能应该是修改设备名称
     *
     *   2.1 要注意设备名称修改,就必需同步修改区域下的设备名称
     *
     *
     * @return
     */
    @RequestMapping(value = "/table_device/old",method = RequestMethod.PUT)
    @ResponseBody
    public Object modifyDevice(HttpServletRequest httpServletRequest) throws IOException {

        JSONObject jsonObject = ParamUtils.getJsonObjectFromRequest(httpServletRequest);
        logger.error("service : table_device"  +" action : control device" +" \nparam : "+jsonObject.toString());

        String user_id = (String) jsonObject.get("user_id");

        List<UserGateway> tableDeviceList = userService.selectGatewayByUserId(user_id);

        String account_id =tableDeviceList.get(0).getAccount_id();
        String SourceId= account_id+ Param.WEBAPPID+String.valueOf(System.currentTimeMillis()/1000000000);

        jsonObject.remove("user_id");

        JSONArray jsonArray = (JSONArray) jsonObject.get("table_device");
        JSONObject tmpj = (JSONObject) jsonArray.get(0);
        String DestinationId = (String) tmpj.get("gateway_id");

        logger.error("SourceId : "+SourceId+ " DestinationId : "+DestinationId );


        for (int i = 0; i <jsonArray.size() ; i++) {
            JSONObject json = (JSONObject)jsonArray.get(i);
            JSONObject device_value = (JSONObject) json.get("device_value");
            String device_guid = (String) json.get("device_guid");
            String gateway_id =(String)json.get("gateway_id");
            String device_switch =(String) json.get("device_switch");
            if (device_switch!=null){
                json.remove("gateway_id");
                json.remove("device_delay");
                continue;
            }

            String host = "";

            PropsUtil configProps = new PropsUtil("config.properties");

            try {
                host = configProps.get("host");
            } catch (IOException e) {
                e.printStackTrace();
            }

            host+="table_device/channel_value?user_id="+user_id+"&device_guid="+device_guid+"&gateway_id="+gateway_id;

            String result = RequestCaseUtil.requestGetCase(host);

            JSONObject jresult = JSONObject.parseObject(result);

            JSONArray jsonArray1 = (JSONArray) jresult.get("content");
            logger.error("Channel info : "+jsonArray1.toString());

            for (int j = 0; j <jsonArray1.size() ; j++) {
                JSONObject temp = (JSONObject) jsonArray1.get(j);
                String channel_number  = (String) temp.get("channel_number");
                String value =(String) temp.get("value");

                if (channel_number.equals(device_value.get("channel_number"))){
                    temp.remove("value");
                    temp.put("value",device_value.get("value"));
                }

            }

            String [] values = new String[jsonArray1.size()];
            for (int j = 0; j <jsonArray1.size() ; j++) {
                JSONObject temp = (JSONObject) jsonArray1.get(j);
                int aa =Integer.parseInt((String) temp.get("channel_number"))-1;

                int a =  Integer.parseInt((String) temp.get("value"));
                String bb ="";

                if (a<16){
                   bb="0"+Integer.toHexString(a);
                }else {
                    bb=Integer.toHexString(a);
                }
                values[aa]=bb;
            }
            String R="";
            for (int j = 0; j <values.length ; j++) {
                R+=values[j];
            }
            logger.error("device_value : "+R);
            json.remove("device_value");
            json.put("device_value",R);
            json.remove("gateway_id");
        }
         
        Message message = deviceService.modifyDevice(jsonObject,DestinationId,SourceId,2);

        return message;
    }

    /**
     *  该接口用语查询用户的设备
     *  如果入参gateway_id 如果不指定,应该返回用户所有网关下的设备信息,目前固定获取第一个网关地址
     *  二期计划(添加缓存库,实现数据同步)
     *
     *  http://localhost:8080/table_device?user_id=12345667&gateway_id=158d000052c779
     *
     *  入参形式如上所述
     *
     *  接口内主要逻辑
     *  1 根据用户user_id获取用户帐号id,提取web/app的本地4位字符串,加上随机生成的4位字符串,拼接生成SourceId
     *  2 根据第一步的结果,同样能输出用户的网关信息
     *  3 调整入参形式以符合sql生成工具的入参要求,调用说sql语句生成方法
     *  4 调用socket通信查询设备
     *
     *
     *
     * @return
     */
    @RequestMapping(value = "/table_device/old",method = RequestMethod.GET)
    @ResponseBody
    public Object findDevice(HttpServletRequest httpServletRequest) throws IOException {
        JSONObject jsonObject = ParamUtils.getJsonObjectFromRequest(httpServletRequest);

        logger.error("service : table_device"  +" action : get all devices" +" \nparam : "+jsonObject.toString());

        JSONObject a = (JSONObject) jsonObject.get("table_device");

        String user_id = (String) a.get("user_id");

        String region_guid = (String)a.get("region_guid");
        String gateway_id =(String)a.get("gateway_id");

        List<UserGateway> tableDeviceList = userService.selectGatewayByUserId(user_id);

        String account_id =tableDeviceList.get(0).getAccount_id();
        String SourceId= account_id+ Param.WEBAPPID+String.valueOf(System.currentTimeMillis()/1000000000);

        List<AccountDataInfo> tableDeviceList1 = userService.getALLagateway(account_id);

        logger.error("SourceId : "+SourceId +" Number Of Gateway : "+tableDeviceList1.size());

        JSONArray rj = new JSONArray();
        int index = 0;
        for (AccountDataInfo accountinfo: tableDeviceList1) {
            index+=1;

            String DestinationId = accountinfo.getGateway_id();
            JSONObject jj = new JSONObject();
            jj.put("gateway_id",DestinationId);
            JSONObject result = new JSONObject();
            result.put("table_device",jj);
            logger.error("Gatewy_id"+index+" : "+DestinationId);

            Message temp = deviceService.findDevice(result,DestinationId, SourceId, 2);

            if (temp.getCode().equals("0")){
                JSONArray tempdata = (JSONArray) temp.getContent();
                for (int i = 0; i <tempdata.size() ; i++) {
                    JSONObject device = (JSONObject) tempdata.get(i);
                    String device_type = (String) device.get("device_type");
                    if (!device_type.toUpperCase().equals("GATEWAY")){
                        rj.add(tempdata.get(i));
                    }
                }
            }
        }

        if (rj.size()>0){


            for (int i = 0; i <rj.size() ; i++) {

                JSONObject jsonobject = (JSONObject) rj.get(i);
                String device_guid = (String) jsonobject.get("device_guid");
                String device_gateway_id = (String)jsonobject.get("gateway_id");
                String device_value =(String) jsonobject.get("device_value");
                String device_type = (String) jsonobject.get("device_type");

                int len = device_value.length()/2;

                String[]  devices=null;
                if (len==0){
                    devices = new String[1];
                }else {
                    devices = new String[len];
                }

                if (device_type.equals("sensor")){
                    continue;
                }

                for (int j = 0; j <devices.length ; j++) {
                    StringBuilder SB = new StringBuilder();
                    SB.append(device_value);
                    if (j==(devices.length-1)){
                        SB.delete(0,j*2);
                        devices[j]=SB.toString();
                    }else {
                        devices[j]=SB.substring(j*2,j*2+2).toString();
                    }
                }

                String sql1 = "select * from table_channel where table_device_guid = '"+device_guid+"'";
                OutPutSocketMessage outPutSocketMessage = new OutPutSocketMessage();
                outPutSocketMessage.setPackegType(2);//包类型，app为2，web端为-1
                outPutSocketMessage.setDestinationID(device_gateway_id);//app客户可以为任意的16个字符串，web填写目标网关地址
                outPutSocketMessage.setType("NULL");//查询就填写表明，非查询就填写“NULL”
                outPutSocketMessage.setMessage("table_channel");//消息用于网关返回消息，下发命令时可以填写表明
                outPutSocketMessage.setSourceID(SourceId);//源ID
                outPutSocketMessage.setSql(sql1);//下发sql指令

                JSONObject r = outPutSocketMessage.sendMessag(SourceId);

                JSONArray list = (JSONArray) r.get("List");


                if (list.size()>0){

                    for (int j = 0; j <list.size() ; j++) {

                        JSONObject channel = (JSONObject) list.get(j);
                        String channel_num= (String)channel.get("channel_number");
                        int int_channel_num = Integer.parseInt(channel_num);
                        String hex_value = devices[int_channel_num-1];
                        channel.put("value",Integer.parseInt(hex_value,16));
                    }
                }
                jsonobject.put("channel",list);
            }

        }



        if (region_guid!=null && gateway_id!=null){

            String sql = "select * from table_region_device where region_guid = '"+region_guid+"' group by table_device_guid";
            OutPutSocketMessage outPutSocketMessage = new OutPutSocketMessage();
            outPutSocketMessage.setPackegType(2);//包类型，app为2，web端为-1
            outPutSocketMessage.setDestinationID(gateway_id);//app客户可以为任意的16个字符串，web填写目标网关地址
            outPutSocketMessage.setType("NULL");//查询就填写表明，非查询就填写“NULL”
            outPutSocketMessage.setMessage("table_region_device");//消息用于网关返回消息，下发命令时可以填写表明
            outPutSocketMessage.setSourceID(SourceId);//源ID
            outPutSocketMessage.setSql(sql);//下发sql指令



            JSONObject result  =outPutSocketMessage.sendMessag(SourceId);

            JSONArray list = (JSONArray) result.get("List");


            if (list.size()>0){
                List<Integer> indexD = new LinkedList<Integer>();
                for (int i = 0; i <list.size() ; i++) {
                    JSONObject temp_region_device = (JSONObject) list.get(i);
                    String table_device_guid = (String) temp_region_device.get("table_device_guid");

                    for (int j = 0; j <rj.size() ; j++) {
                        JSONObject temp_device = (JSONObject) rj.get(j);
                        String device_guid = (String) temp_device.get("device_guid");
                        if (table_device_guid.equals(device_guid)){
                            indexD.add(j);
                        }

                    }
                }

                for (Integer temp_index : indexD) {
                    int tt= temp_index;
                    rj.remove(tt);
                }

            }
        }




        Message message = new Message();
        message.setCode("0");
        message.setContent(rj);
        message.setMessage("successfully!");

        return message;
    }

    /**
     * 设备删除接口
     * 目前不开放这个接口的调用
     *
     * @return
     */
    @RequestMapping(value = "/table_device",method = RequestMethod.DELETE)
    @ResponseBody
    public Object deleteDevice(HttpServletRequest httpServletRequest) throws IOException {

        JSONObject jsonObject = ParamUtils.getAttributess(httpServletRequest);

        Message message = deviceService.deleteDevice(jsonObject,"","",-1);

        return message;
    }

    /**
     * 获取设备下的各通道详情
     * 主要业务逻辑
     * 1 根据用户id获取用户的网关列表
     * 2 生成SourceId与DestinationId
     * 3 根据入参传入的设备主键,获取设备的device_value
     * 4 按照通道规则 拆分device_value,返回给调用者
     * @return
     */
    @RequestMapping(value = "/table_device/channel_value",method = RequestMethod.GET)
    @ResponseBody
    public Object findDeviceValue(HttpServletRequest httpServletRequest) throws IOException {

        JSONObject jsonObject = ParamUtils.getJsonObjectFromRequest(httpServletRequest);
        logger.error("service : table_device"  +" action : get channel_value" +" \n param : "+jsonObject.toString());

        JSONObject a = (JSONObject) jsonObject.get("channel_value");

        String user_id = (String) a.get("user_id");
        String device = (String)a.get("device_guid");
        String channel_number =(String)a.get("channel_number");
        String DestinationId = (String) a.get("gateway_id");
        List<UserGateway> tableDeviceList = userService.selectGatewayByUserId(user_id);

        String account_id =tableDeviceList.get(0).getAccount_id();
        String SourceId= account_id+ Param.WEBAPPID+String.valueOf(System.currentTimeMillis()/1000000000);

        JSONObject jj = new JSONObject();
        jj.put("gateway_id",DestinationId);
        jj.put("device_guid",device);
        JSONObject result = new JSONObject();
        result.put("table_device",jj);

        String result_string="";
        Message message1 = deviceService.findDevice(result,DestinationId, SourceId, 2);

        JSONArray r = new JSONArray();
        if (!message1.getCode().equals("0")){
            return message1;
        }else {

            JSONArray jsonArray = (JSONArray) message1.getContent();
            JSONObject json = (JSONObject) jsonArray.get(0);
            String device_value = (String) json.get("device_value");
            System.out.println("device_value :"+device_value);

            int len = device_value.length()/2;

            String[]  devices=null;
            if (len==0){
                devices = new String[1];
            }else {
                 devices = new String[len];
            }


            for (int i = 0; i <devices.length ; i++) {
                StringBuilder SB = new StringBuilder();
                SB.append(device_value);
                if (i==(devices.length-1)){
                    SB.delete(0,i*2);
                    devices[i]=SB.toString();
                }else {
                    devices[i]=SB.substring(i*2,i*2+2).toString();
                }
            }

            for (int i = 0; i <devices.length ; i++) {
                JSONObject t= new JSONObject();
                t.put("channel_number",String.valueOf(i+1));
                t.put("value",String.valueOf(Integer.parseInt(devices[i],16)));
                r.add(t);
            }
        }

        Message message = new Message();
        message.setCode("0");
        message.setMessage("get the value successfully!");
        message.setContent(r);
        return message;
    }


}
