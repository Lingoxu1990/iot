package com.iot.newController;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.iot.exception.BussinessException;
import com.iot.message.Message;

import com.iot.newService.NewDeviceService;
import com.iot.newService.NewRegionDeviceService;
import com.iot.newService.NewRegionService;
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
import java.util.*;


/**
 * Created by adminchen on 16/6/3.
 */
@Controller
@RequestMapping()
public class NewRegionDeviceController {

    private static Logger logger = Logger.getLogger(NewRegionDeviceController.class);

    @Resource
    private UserService userService;
    @Resource
    private NewRegionDeviceService newRegionDeviceService;
    @Resource
    private NewDeviceService newDeviceService;
    @Resource
    private NewRegionService newRegionService;




    //删除区域设备
    @RequestMapping(value = "/region/device/old/old/old/",method = RequestMethod.DELETE)
    @ResponseBody
    public  Object DeleteRegionDevice(HttpServletRequest httpServletRequest){
        Message message=new Message();
        try {
            JSONObject jsonObject = ParamUtils.getJsonObjectFromRequest(httpServletRequest);

            JSONArray jsonArray=(JSONArray) jsonObject.get("table_region_device");
            JSONObject jsonObject1=(JSONObject) jsonArray.get(0);
            String gateway_id=(String) jsonObject1.get("gateway_id");
            String user_id=(String) jsonObject.get("user_id");

            String region_addr=(String) jsonObject1.get("region_addr");
            String device_addr=(String) jsonObject1.get("device_addr");
            String region_guid=(String) jsonObject1.get("region_guid");

            List<UserGateway> list=userService.selectGatewayByUserId(user_id);
            if (list.size()<1){
                message.setCode("-1");
                message.setMessage("user does not exist");
                message.setContent(new JSONArray());
                return message;
            }
            String account_id=list.get(0).getAccount_id();
            String SourceId = account_id + Param.WEBAPPID + String.valueOf(System.currentTimeMillis() / 1000000000);

            TableRegionDevice tableRegionDevice=new TableRegionDevice();
            tableRegionDevice.setDevice_addr(device_addr);
            tableRegionDevice.setRegion_addr(region_addr);
            tableRegionDevice.setAccount_id(account_id);
            tableRegionDevice.setGateway_id(gateway_id);
            tableRegionDevice.setRegion_guid(region_guid);


            jsonObject.remove("user_id");
            jsonObject1.remove("gateway_id");

            JSONObject socketResult = newRegionDeviceService.socketDeleteRegionDevice(jsonObject,gateway_id,SourceId,2);

            String stauts = String.valueOf(socketResult.get("Status"));

            if (stauts.equals("1")){
                message.setCode("-1");
                message.setMessage("Gateway return the status code '1' means sub-gateway failed to remove the data");
                message.setContent(new JSONArray());
                return message;
            }
            if (stauts.equals("2")){
                message.setCode("-1");
                message.setMessage("Gateway return the status code '2' means the device is off line,please try later!");
                message.setContent(new JSONArray());
                return message;
            }

            int n= newRegionDeviceService.deleteByRegion_AddrIdAndDevice_Addr(tableRegionDevice);

            if (n <1) {
                message.setCode("-1");
                message.setMessage("Failed to remove the data out from database");
                message.setContent(new JSONArray());
                return message;
            }
            List<Map> regionDeviceList=newRegionDeviceService.fingRegionDevice(tableRegionDevice);
            if (regionDeviceList.size()<1){
                TableRegion tableRegion=new TableRegion();
                tableRegion.setAccount_id(account_id);
                tableRegion.setGateway_id(gateway_id);
                tableRegion.setRegion_addr(region_addr);
                tableRegion.setRegion_value("null");
                int p=newRegionService.mysqlControlRegion(tableRegion);
                if (p<1){
                    message.setCode("-1");
                    message.setMessage("Region value modification failed ");
                    message.setContent(new JSONArray());
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        message.setCode("0");
        message.setMessage("RegionDevice delete success");
        message.setContent(new JSONArray());
        return message;
    }

    //查询区域设备
    @RequestMapping(value ="/region/device",method = RequestMethod.GET)
    @ResponseBody
    public  Object FindRegionDevice(HttpServletRequest httpServletRequest){
        Message message =new Message();
        try {
            JSONObject jsonObject = ParamUtils.getJsonObjectFromRequest(httpServletRequest);

            JSONObject temp=(JSONObject) jsonObject.get("device");

            String user_id=(String) temp.get("user_id");

            List<UserGateway> userGateways=userService.selectGatewayByUserId(user_id);
            if(userGateways.size()<1){
//                message.setCode("-1");
//                message.setMessage("user does not exist");
//                message.setContent(new JSONArray());
//                return message;
                throw new BussinessException("-1","user does not exist");
            }
            String account_id=userGateways.get(0).getAccount_id();

            String region_guid=(String) temp.get("region_guid");

            String gateway_id=(String) temp.get("gateway_id");

            TableRegionDevice tableRegionDevice=new TableRegionDevice();

            tableRegionDevice.setAccount_id(account_id);

            tableRegionDevice.setRegion_guid(region_guid);

            tableRegionDevice.setGateway_id(gateway_id);

            List<Map> list= newRegionDeviceService.fingRegionDevice(tableRegionDevice);


            for (Map map:list){

               if("sensor".equals(map.get("device_type"))){

                   map.put("channel_value","ff");
                   continue;

               }

                String device_value=(String) map.get("device_value");
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

                String channel_guid=(String) map.get("channel_guid");

                TableChannel tableChannel=new TableChannel();
                tableChannel.setGateway_id(gateway_id);
                tableChannel.setAccount_id(account_id);
                tableChannel.setChannel_guid(channel_guid);

                TableChannel channelsOne=newRegionDeviceService.selectChannelsOne(tableChannel);


                String str_chnanel_num =  channelsOne.getChannel_number();


                int intChannelValue=Integer.parseInt(devices[Integer.parseInt(str_chnanel_num)-1],16);

                String strChannelValue = String.valueOf(intChannelValue);


                map.put("channel_value",strChannelValue);
                map.put("channel_number",channelsOne.getChannel_number());
                System.out.println("channel_number:"+map.get("channel_number"));



            }
            if (!list.isEmpty()){

                message.setCode("0");

                message.setMessage("Query RegionDevice success");

                HashMap<String,JSONObject> devices =new HashMap<String, JSONObject>();


                //去重复
                for (int i=0;i<list.size();i++){

                    JSONObject deviceData = new JSONObject();

                    deviceData.put("device_name", list.get(i).get("device_name"));
                    deviceData.put("table_device_guid", list.get(i).get("table_device_guid"));
                    deviceData.put("gateway_id", list.get(i).get("gateway_id"));
                    deviceData.put("region_name", list.get(i).get("region_name"));
                    deviceData.put("region_guid", list.get(i).get("region_guid"));
                    deviceData.put("device_addr", list.get(i).get("device_addr"));
                    deviceData.put("device_type", list.get(i).get("device_type"));

                    String deviceGuid = (String) list.get(i).get("table_device_guid");
//                    JSONObject deviceInfo  = (JSONObject) JSON.toJSON(list.get(i));
                    deviceData.put("channel",new JSONArray());
                    devices.put(deviceGuid, deviceData);

                }
                //写入通道信息
                for (int i=0;i<list.size();i++){

                    if ("sensor".equals(list.get(i).get("device_type"))){

                        continue;
                    }

                    JSONObject channel =new JSONObject();
                    channel.put("channel_number",(String) list.get(i).get("channel_number"));
                    //System.out.println("ddd"+list.get(i).get("channel_number"));
                    channel.put("channel_value",(String) list.get(i).get("channel_value"));
                    channel.put("channel_name",(String)list.get(i).get("channel_name"));

                    String deviceGuid = (String) list.get(i).get("table_device_guid");
                    JSONObject deviceInfo = devices.get(deviceGuid);
                    JSONArray channels = (JSONArray) deviceInfo.get("channel");
                    channels.add(channel);
                }
                Set<Map.Entry<String,JSONObject>> entrySet = devices.entrySet();

                JSONArray result  = new JSONArray();

                for (Map.Entry<String,JSONObject> entry:entrySet) {

                    result.add( entry.getValue());

                }

                message.setContent(result);

            }else {

                throw new   BussinessException("0","No devices int the region");
            }

        }catch (Exception e){

            e.printStackTrace();


        }

        return message;

    }

    //区域设备修改
    @RequestMapping(value = "/new/RegionDevice",method = RequestMethod.PUT)
    @ResponseBody
    public Message Modify(HttpServletRequest httpServletRequest){
        Message message=new Message();
        try {

            JSONObject jsonObject = ParamUtils.getJsonObjectFromRequest(httpServletRequest);
            JSONArray j_array=(JSONArray) jsonObject.get("table_region_device");
            String user_id=(String) jsonObject.get("user_id");
            List<UserGateway> list=userService.selectGatewayByUserId(user_id);
            String account_id=list.get(0).getAccount_id();
            List <TableRegionDevice> list1=new ArrayList<TableRegionDevice>();
            for (int i=0;i<j_array.size();i++){
                JSONObject jsonObject1=(JSONObject) j_array.get(i);
                String region_guid=jsonObject1.get("region_guid").toString();
                String device_guid=jsonObject1.get("table_device_guid").toString();
                String device_name=jsonObject1.get("device_name").toString();
                String device_addr=jsonObject1.get("device_addr").toString();
                String region_name=jsonObject1.get("region_name").toString();
                String region_addr=jsonObject1.get("region_addr").toString();
                TableRegionDevice tableRegionDevice=new TableRegionDevice();
                tableRegionDevice.setAccount_id(account_id);
                tableRegionDevice.setRegion_guid(region_guid);
                tableRegionDevice.setRegion_name(region_name);
                tableRegionDevice.setDevice_name(device_name);
                tableRegionDevice.setDevice_addr(device_addr);
                tableRegionDevice.setTable_device_guid(device_guid);
                list1.add(tableRegionDevice);
                //设置Device
                TableDevice tableDevice=new TableDevice();
                tableDevice.setDevice_addr(device_addr);
                tableDevice.setDevice_guid(device_guid);
                tableDevice.setAccount_id(account_id);

            }
            message= newRegionDeviceService.ModifyByDrviceOfname(list1);
        }catch (Exception e){
            e.printStackTrace();
        }
        return message;
    }


}
