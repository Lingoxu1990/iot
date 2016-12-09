package com.iot.newController;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.iot.exception.BussinessException;
import com.iot.exception.ParameterException;
import com.iot.message.Message;
import com.iot.message.MessageNoContent;
import com.iot.newService.NewDeviceService;
import com.iot.newService.NewGroupService;
import com.iot.newService.NewRegionDeviceService;
import com.iot.pojo.*;
import com.iot.service.UserService;
import com.iot.spitUtil.Param;
import com.iot.utils.ParamUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by adminchen on 16/6/21.
 */

@Controller
@RequestMapping()
public class NewDeviceController {

    private static Logger logger = Logger.getLogger(NewDeviceController.class);

    JSONObject jsonObject;
    @Resource
    private UserService userService;
    @Resource
    private NewDeviceService newDeviceService;

    @Resource
    private  NewRegionDeviceService newRegionDeviceService;

    @Resource
    private NewGroupService newGroupService;

    //查询用户所有设备
    @RequestMapping(value = "/table_device/old/old",method = RequestMethod.GET)
    @ResponseBody
    public Message findDevice(HttpServletRequest httpServletRequest) {
        Message message = new Message();
        try {
            JSONObject jsonObject = ParamUtils.getJsonObjectFromRequest(httpServletRequest);
//            System.out.println(jsonObject.toString());
//            System.out.println(jsonObject.toString());
            JSONObject devicess = (JSONObject) jsonObject.get("table_device");
            String user_id = (String) devicess.get("user_id");
            String region_guid=(String) devicess.get("region_guid");
            String group_guid=(String) devicess.get("table_gruop_guid");
            String scene_guid=(String) devicess.get("table_scene_guid");
            String gateway_id=(String) devicess.get("gateway_id");
            List<UserGateway> list = userService.selectGatewayByUserId(user_id);
            if (list.size()<1){
//                message.setCode("-1");
//                message.setMessage("user does not exist");
//                message.setContent(new JSONArray());
//                return message;
                throw new BussinessException("-1","user does not exist");
            }
            String account_id = list.get(0).getAccount_id();

            //实体类
            TableDevice tableDevice = new TableDevice();
            tableDevice.setAccount_id(account_id);
            List<TableDevice> list1 = newDeviceService.findDevice(tableDevice);


            //复制所有设备
            List<TableDevice> tableDeviceList=new ArrayList<TableDevice>(list1);
            //如果区域设备有添加设备
            if (region_guid!=null||!region_guid.equals("")){
                //实体类
                TableRegionDevice tableRegionDevice=new TableRegionDevice();
                tableRegionDevice.setAccount_id(account_id);
                tableRegionDevice.setGateway_id(gateway_id);
                tableRegionDevice.setRegion_guid(region_guid);
                List<Map> tableRegionDeviceList=newRegionDeviceService.fingRegionDevice(tableRegionDevice);
                //去掉区域设备重复
                List<String> RegionDeviceList1=new ArrayList<String>();
                for (int a=0;a<tableRegionDeviceList.size();a++){
                    String deviceaddr=(String) tableRegionDeviceList.get(a).get("device_addr");
                    if (!RegionDeviceList1.contains(deviceaddr)){
                        RegionDeviceList1.add(deviceaddr);
                    }
                }

                for (TableDevice tableDevice1:list1){
                    for (int i=0;i<RegionDeviceList1.size();i++){
                        if (RegionDeviceList1.get(i).toString().equals(tableDevice1.getDevice_addr())){
                            tableDeviceList.remove(tableDevice1);
                        }
                    }
                }
            }

            //如果组有添加组成员
            if (group_guid!=null||!group_guid.equals("")){
                //组成员实体类
                TableGroupMembers tableGroupMembers=new TableGroupMembers();
                tableGroupMembers.setAccount_id(account_id);
                tableGroupMembers.setGateway_id(gateway_id);
                tableGroupMembers.setTable_group_guid(group_guid);
                List<TableGroupMembers> tableGroupMembersList =newGroupService.findGroupMember(tableGroupMembers);
                for (TableDevice tableDevice1:list1){
                    for (TableGroupMembers tableGroupMembers1:tableGroupMembersList){
                        if (tableDevice1.getDevice_addr().equals(tableGroupMembers1.getDevice_addr())){
                            tableDeviceList.remove(tableDevice1);
                        }
                    }
                }
            }

            //如果有添加场景成员
            if (scene_guid!=null||!scene_guid.equals("")){
                //场景成员实体类
                TableSceneMembers tableSceneMembers=new TableSceneMembers();
                tableSceneMembers.setGateway_id(gateway_id);
            }


            JSONArray deviceObject = new JSONArray();
            deviceObject = (JSONArray) JSONArray.toJSON(tableDeviceList);
            JSONArray channelListObject=new JSONArray();
            //
            int p=0;//循环语句递增变量
            for (TableDevice tableDevice1:tableDeviceList){

                String device_value=tableDevice1.getDevice_value();
                int num=0;
                if (device_value.equals(null)){
                    num=0;
                }

                if ("sensor".equals(tableDevice1.getDevice_type())){

                    JSONObject temDevices=(JSONObject) deviceObject.get(p);
                    temDevices.put("channel",new JSONArray());
                    p+=1;
                    continue;

                }
                //System.out.println("device_value:"+device_value);
                 num=device_value.length()/2;

                System.out.println("num:"+num);
                String[] deviceValue=null;
                if (num==0){
                    deviceValue=new String[1];
                }else {
                    deviceValue=new String[num];
                }
                for (int i=0;i<deviceValue.length;i++){
                    StringBuilder sb=new StringBuilder();
                    sb.append(device_value);
                    System.out.println(sb);
                    if (i==(deviceValue.length-1)){
                        sb.delete(0,i*2);
                        deviceValue[i]=sb.toString();
                    }else {
                        deviceValue[i]=sb.substring(i*2,i*2+2).toString();
                    }
                }

                TableChannel tableChannel=new TableChannel();
                tableChannel.setAccount_id(account_id);
                tableChannel.setGateway_id(tableDevice1.getGateway_id());
                tableChannel.setTable_device_guid(tableDevice1.getDevice_guid());
                List<TableChannel> channelList=newDeviceService.findChannelInfo(tableChannel);
                for (TableChannel tableChannel1:channelList){
                    int  channel_num=Integer.parseInt(tableChannel1.getChannel_number());
                    String channel_value_str=deviceValue[channel_num-1];
                    int channel_values=Integer.parseInt(channel_value_str,16);
                    String channel_value=String.valueOf(channel_values);
                    tableChannel1.setChannel_value(channel_value);
                }
                channelListObject=(JSONArray) JSONArray.toJSON(channelList);
                //去除多余通道字段
                for (int j=0;j<channelListObject.size();j++){
                    JSONObject temChannels=(JSONObject)channelListObject.get(j);
                    temChannels.remove("device_value");
                }
                //System.out.println("channelListObject:"+channelListObject.toString());
                JSONObject temDevices=(JSONObject) deviceObject.get(p);
                temDevices.put("channel",channelListObject);
                p+=1;
            }
            //去除设备多余的字段

            for (int i=0;i<deviceObject.size();i++){
                JSONObject temDevices=(JSONObject) deviceObject.get(i);
                temDevices.remove("id");
                temDevices.remove("region_bunding");
                temDevices.remove("account_id");
            }
            //System.out.println("deviceObject:"+deviceObject.toString());
            //deviceObject.add(channelListObject);

            if (list1.size() > 0) {
                message.setCode("0");
                message.setMessage("User Device query success");
                message.setContent(deviceObject);
            } else {
//                message.setCode("-1");
//                message.setMessage("User Device query is empty");
//                message.setContent(new JSONArray());
                throw new BussinessException("-1","User Device query is empty");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return message;
    }

    //设备通道值获取
    @RequestMapping(value = "/new/device/channel", method = RequestMethod.GET)
    @ResponseBody
    public Message findDeviceOfChannelValue(HttpServletRequest httpServletRequest) {
        Message message = new Message();
        try {
            JSONObject jsonObject = ParamUtils.getJsonObjectFromRequest(httpServletRequest);
            JSONObject jsonObject1 = (JSONObject) jsonObject.get("channel");
            String user_id = (String) jsonObject1.get("user_id");
            String gateway_id = (String) jsonObject1.get("gateway_id");
            String device_guid = (String) jsonObject1.get("table_device_guid");
            String device_type = (String) jsonObject1.get("device_type");
            List<UserGateway> list = userService.selectGatewayByUserId(user_id);
            if (list.size()<1){
//                message.setCode("-1");
//                message.setMessage("user does not exist");
//                message.setContent(new JSONArray());
//                return message;
                throw new BussinessException("-1","user does not exist");
            }
            String account_id = list.get(0).getAccount_id();
            //
            TableChannel tableChannel = new TableChannel();
            tableChannel.setGateway_id(gateway_id);
            tableChannel.setAccount_id(account_id);
            tableChannel.setTable_device_guid(device_guid);
            if (device_type.equals("sensor")) {
//                message.setCode("-1");
//                message.setMessage("Device is sensor");
//                message.setContent(new JSONArray());
//                return message;
                throw new BussinessException("-1","Device is sensor");
            }
            List<TableChannel> list1 = newDeviceService.findDeviceChannelValue(tableChannel);
            if (list1.size() > 0) {
                message.setCode("0");
                message.setMessage("DeviceChannel value for success");
                JSONArray jsonArray = new JSONArray();
                jsonArray = (JSONArray) JSONArray.toJSON(list1);
                message.setContent(jsonArray);

            } else {
//                message.setCode("-1");
//                message.setMessage("DeviceChannel value for failed");
//                message.setContent(new JSONArray());
                throw new BussinessException("-1","DeviceChannel value for failed");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return message;
    }

    //设备控制
    @RequestMapping(value = "/table_device", method = RequestMethod.PUT)
    @ResponseBody
    public MessageNoContent DeviceController(HttpServletRequest httpServletRequest) {
        MessageNoContent message = new MessageNoContent();

        try {
            JSONObject jsonObject = ParamUtils.getAttributess(httpServletRequest);
            String user_id = (String) jsonObject.get("user_id");
            jsonObject.remove("user_id");

            JSONArray devices = (JSONArray) jsonObject.get("table_device");

            JSONObject device = (JSONObject) devices.get(0);

            String gateway_id = (String) device.get("gateway_id");
            String device_addr = (String) device.get("device_addr");

            JSONObject jsonDeviceValue = null;
            Object o = device.get("device_value");
            if (o != null) {
                jsonDeviceValue = (JSONObject) device.get("device_value");
            }

            String device_switch = (String) device.get("device_switch");
            String device_delay = (String) device.get("device_delay");
            String deviceGuid = (String) device.get("device_guid");

            List<UserGateway> list = userService.selectGatewayByUserId(user_id);
            if (list.size() < 1) {
//                message.setCode("-1");
//                message.setMessage("user does not exit");
//                message.setContent(new JSONArray());
//                return message;
                throw new BussinessException("-1","user does not exit");
            }
            String account_id = list.get(0).getAccount_id();
            String SourceId = account_id + Param.WEBAPPID + String.valueOf(System.currentTimeMillis() / 1000000000);
            //System.out.println("源:"+SourceId);


            if (gateway_id == null) {
//                message.setCode("-1");
//                message.setMessage("GatwayId Missing");
//                message.setContent(new JSONArray());
//                return message;
                throw new ParameterException("-1","GatwayId Missing");
            }
//        System.out.println("开关或设备值为空");

            if (device_switch != null && jsonDeviceValue!=null ) {
//                message.setCode("-1");
//                message.setMessage("can not controll the switch and device value at one moment");
//                message.setContent(new JSONArray());
//                return message;
                throw new ParameterException("-1","can not controll the switch and device value at one moment");
            }

            if (device_switch == null && jsonDeviceValue == null) {
//                message.setCode("-1");
//                message.setMessage("Missing Controll Param ");
//                message.setContent(new JSONArray());
//                return message;
                throw new ParameterException("-1","Missing Controll Param ");
            }

            if (device_switch == null && jsonDeviceValue != null) {

                TableChannel channelParam = new TableChannel();
                channelParam.setAccount_id(account_id);
                channelParam.setGateway_id(gateway_id);
                channelParam.setTable_device_guid(deviceGuid);

                //获取其他通道的值
                List<TableChannel> channelList = newDeviceService.findDeviceChannelValue(channelParam);
                if (channelList.size() < 1) {
//                    message.setCode("-1");
//                    message.setMessage("The device has no channel");
//                    message.setContent(new JSONArray());
//                    return message;
                    throw new BussinessException("-1","The device has no channel");
                }
                String[] channelValuesStr = new String[channelList.size()];

                for (TableChannel channelinfo : channelList) {
                    String strChannelNumber = channelinfo.getChannel_number();
                    int index = Integer.parseInt(strChannelNumber) - 1;

//                    String strChannelValue = channelinfo.getChannel_value();
//                    int intChannelVaule = Integer.parseInt(strChannelValue);
//
//                    String hexChannelValue = Integer.toHexString(intChannelVaule);

                    String hexChannelValue = channelinfo.getChannel_value();
                    channelValuesStr[index] = hexChannelValue;
                }

                String strControllNumber = (String) jsonDeviceValue.get("channel_number");
                //System.out.println("通道数:"+strControllNumber);
                if (strControllNumber == null || strControllNumber.equals("")) {
//                    message.setCode("-1");
//                    message.setMessage("Get no channel number or channel  value");
//                    message.setContent(new JSONArray());
//                    return message;
                    throw new BussinessException("-1","Get no channel number or channel  value");
                }
                int intControllNumber = Integer.parseInt(strControllNumber) - 1;

                String strControllValue = (String) jsonDeviceValue.get("value");
                int intControllvalue = Integer.parseInt(strControllValue);

                String hexControllValue = "";
                if (intControllvalue < 16) {
                    hexControllValue = "0" + Integer.toHexString(intControllvalue);
                } else {
                    hexControllValue = Integer.toHexString(intControllvalue);
                }

                channelValuesStr[intControllNumber] = hexControllValue;


                String finnalValue = "";
                for (int i = 0; i < channelValuesStr.length; i++) {
                    finnalValue += channelValuesStr[i];
                }
                //修改入参时的device_value字段的值
                device.remove("device_value");
                device.put("device_value", finnalValue);
                //移除入参的网关字段
                device.remove("gateway_id");


                //发送socket指令控制设备
                JSONObject socketResult = newDeviceService.socketControllDevice(jsonObject, gateway_id, SourceId, 2);

                String status = String.valueOf(socketResult.get("Status"));
                if (status.equals("1")) {
//                    message.setCode("-1");
//                    message.setMessage("SubGateway return the status '1' means the command can't be executed!");
//                    message.setContent(new JSONArray());
//                    return message;
                    throw new BussinessException("-1","SubGateway return the status '1' means the command can't be executed!");
                }
                if (status.equals("2")) {
//                    message.setCode("-1");
//                    message.setMessage("SubGateway return the status '2' means the device is off line");
//                    message.setContent(new JSONArray());
//                    return message;
                    throw new BussinessException("-1","SubGateway return the status '2' means the device is off line");
                }


                //实体类
                TableDevice tableDevice = new TableDevice();
                tableDevice.setAccount_id(account_id);
                tableDevice.setDevice_delay(device_delay);
                tableDevice.setGateway_id(gateway_id);
                tableDevice.setDevice_addr(device_addr);
                tableDevice.setDevice_value(finnalValue);

                int n = newDeviceService.deviceController(tableDevice);
                if (n > 0) {

                    message.setCode("0");
                    message.setMessage("Device data update success");

                } else {
//                    message.setCode("-1");
//                    message.setMessage("Device data update failed");
//                    message.setContent(new JSONArray());
                    throw new BussinessException("-1","Device data update failed");
                }
            }

            if (device_switch != null && jsonDeviceValue == null) {

                String deviceSwitch = (String) device.get("device_switch");
                device.remove("gateway_id");
                device.remove("device_delay");


                //发送socket指令控制设备
                JSONObject socketResult = newDeviceService.socketControllDevice(jsonObject, gateway_id, SourceId, 2);


                String status = String.valueOf(socketResult.get("Status"));
                if (status.equals("1")) {
//                    message.setCode("-1");
//                    message.setMessage("SubGateway return the status '1' means the command can't be executed!");
//                    message.setContent(new JSONArray());
//                    return message;
                    throw new BussinessException("-1","SubGateway return the status '1' means the command can't be executed!");
                }
                if (status.equals("2")) {
//                    message.setCode("-1");
//                    message.setMessage("SubGateway return the status '2' means the device is off line");
//                    message.setContent(new JSONArray());
//                    return message;
                    throw new BussinessException("-1","SubGateway return the status '2' means the device is off line");
                }

                //实体类
                TableDevice tableDevice = new TableDevice();
                tableDevice.setAccount_id(account_id);
                tableDevice.setGateway_id(gateway_id);
                tableDevice.setDevice_addr(device_addr);
                tableDevice.setDevice_switch(deviceSwitch);

                int n = newDeviceService.deviceController(tableDevice);
                if (n > 0) {
                    message.setCode("0");
                    message.setMessage("Device data update success");

                } else {
//                    message.setCode("-1");
//                    message.setMessage("Device data update failed");
//                    message.setContent(new JSONArray());
                    throw new BussinessException("-1","Device data update failed");
                }

            }
        }catch (Exception e){
            e.printStackTrace();
        }


        return message;
    }


}
