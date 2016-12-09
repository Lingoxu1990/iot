package com.iot.newEditionController;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.iot.exception.ParameterException;
import com.iot.message.Message;
import com.iot.message.MessageNoContent;
import com.iot.newEditionService.NewEditionDeviceService;
import com.iot.newEditionService.NewEditionRegionDeviceService;
import com.iot.newEditionService.NewEditionRegionService;
import com.iot.pojo.*;
import com.iot.service.UserService;
import com.iot.utils.ParamUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;


/**
 * Created by adminchen on 16/6/3.
 */
@Controller
@RequestMapping()
public class NewEditionRegionDeviceController {

    private static Logger logger = Logger.getLogger(NewEditionRegionDeviceController.class);

    @Resource
    private UserService userService;
    @Resource
    private NewEditionRegionDeviceService newRegionDeviceService;
    @Resource
    private NewEditionDeviceService newDeviceService;
    @Resource
    private NewEditionRegionService newRegionService;

    //添加区域设备
    @RequestMapping(value = "/region/device", method = RequestMethod.POST)
    @ResponseBody
    public Object addRegionDrivice(HttpServletRequest httpServletRequest) throws IOException {

        JSONObject jsonObject = ParamUtils.getAttributess(httpServletRequest);

        JSONArray regionDevicess = (JSONArray) jsonObject.get("table_region_device");
        JSONObject regionDevice = (JSONObject) regionDevicess.get(0);
        String ragion_guid = (String) regionDevice.get("region_guid");
        String region_addr = (String) regionDevice.get("region_addr");
        String region_name = (String) regionDevice.get("region_name");
        String table_device_guid = (String) regionDevice.get("table_device_guid");
        String gateway_id = (String) regionDevice.get("gateway_id");
        String device_name = (String) regionDevice.get("device_name");
        String device_addr = (String) regionDevice.get("device_addr");
        String user_id = (String) jsonObject.get("user_id");

        if (user_id==null||user_id.equals("")){
            throw new ParameterException("-1","user_id does not exist");
        }

        if (device_addr==null||device_addr.equals("")){
            throw new ParameterException("-1","device_addr does not exist");
        }

        if (device_name==null||device_name.equals("")){
            throw new ParameterException("-1","device_name does not exist");
        }

        if (gateway_id==null||gateway_id.equals("")){
            throw new ParameterException("-1","gateway_id does not exist");
        }

        if (table_device_guid==null||table_device_guid.equals("")){
            throw new ParameterException("-1","table_device_guid does not exist");
        }

        if (region_name==null||region_name.equals("")){
            throw new ParameterException("-1","region_name does not exist");
        }

        if (region_addr==null||region_addr.equals("")){
            throw new ParameterException("-1","region_addr does not exist");
        }

        if (ragion_guid==null||ragion_guid.equals("")){
            throw new ParameterException("-1","ragion_guid does not exist");
        }

        //实体类
        TableRegionDevice tableRegionDevice=new TableRegionDevice();
        tableRegionDevice.setRegion_guid(ragion_guid);
        tableRegionDevice.setTable_device_guid(table_device_guid);
        tableRegionDevice.setGateway_id(gateway_id);
        tableRegionDevice.setDevice_name(device_name);
        tableRegionDevice.setRegion_addr(region_addr);
        tableRegionDevice.setDevice_addr(device_addr);
        tableRegionDevice.setRegion_name(region_name);

        //添加区域设备插入
        JSONObject insertRegionDevice=newRegionDeviceService.insertRegionDevice(user_id, tableRegionDevice,jsonObject);
        //返回值
        JSONArray object=new JSONArray();
        object.add(insertRegionDevice);
        MessageNoContent message=new MessageNoContent();
        message.setCode("0");
        message.setMessage("RegionDevice add success");

        return message;




//

    }


    //删除区域设备
    @RequestMapping(value = "/region/device", method = RequestMethod.DELETE)
    @ResponseBody
    public MessageNoContent DeleteRegionDevice(HttpServletRequest httpServletRequest) throws IOException {

        JSONObject jsonObject = ParamUtils.getAttributess(httpServletRequest);
        JSONArray jsonArray = (JSONArray) jsonObject.get("table_region_device");
        JSONObject regionDevices = (JSONObject) jsonArray.get(0);
        String gateway_id = (String) regionDevices.get("gateway_id");
        String user_id = (String) jsonObject.get("user_id");
        String region_addr = (String) regionDevices.get("region_addr");
        String device_addr = (String) regionDevices.get("device_addr");
        String region_guid = (String) regionDevices.get("region_guid");
        String device_guid=(String) regionDevices.get("table_device_guid");

        if (region_guid==null||region_guid.equals("")){
            throw new ParameterException("-1","region_guid does not exist");
        }

        if (gateway_id==null||gateway_id.equals("")){
            throw new ParameterException("-1","gateway_id does not exist");
        }

        if (user_id==null||user_id.equals("")){
            throw new ParameterException("-1","user_id does not exist");
        }

        if (region_addr==null||region_addr.equals("")){
            throw new ParameterException("-1","region_addr does not exist");
        }

        if (device_addr==null||device_addr.equals("")){
            throw new ParameterException("-1","device_addr does not exist");
        }

        if (device_guid==null||device_guid.equals("")){
            throw new ParameterException("-1","table_device_guid does not exist");
        }

        TableRegionDevice tableRegionDevice=new TableRegionDevice();
        tableRegionDevice.setRegion_addr(region_addr);
        tableRegionDevice.setRegion_guid(region_guid);
        tableRegionDevice.setDevice_addr(device_addr);
        tableRegionDevice.setGateway_id(gateway_id);
        tableRegionDevice.setTable_device_guid(device_guid);

         newRegionDeviceService.deleteByRegion_AddrIdAndDevice_Addr(user_id,tableRegionDevice,jsonObject);
        MessageNoContent message =new MessageNoContent();
        message.setMessage("regionDevice delete success");
        message.setCode("0");
        return message;
//
    }

    //区域设备修改
    @RequestMapping(value = "/new/edition/RegionDevice", method = RequestMethod.PUT)
    @ResponseBody
    public Message Modify(HttpServletRequest httpServletRequest) {
        Message message = new Message();
        try {

            JSONObject jsonObject  = ParamUtils.getJsonObjectFromRequest(httpServletRequest);
            JSONArray j_array = (JSONArray) jsonObject.get("table_region_device");
            String user_id = (String) jsonObject.get("user_id");
            List<UserGateway> list = userService.selectGatewayByUserId(user_id);
            String account_id = list.get(0).getAccount_id();
            List<TableRegionDevice> list1 = new ArrayList<TableRegionDevice>();
            for (int i = 0; i < j_array.size(); i++) {
                JSONObject jsonObject1 = (JSONObject) j_array.get(i);
                String region_guid = jsonObject1.get("region_guid").toString();
                String device_guid = jsonObject1.get("table_device_guid").toString();
                String device_name = jsonObject1.get("device_name").toString();
                String device_addr = jsonObject1.get("device_addr").toString();
                String region_name = jsonObject1.get("region_name").toString();
                String region_addr = jsonObject1.get("region_addr").toString();
                TableRegionDevice tableRegionDevice = new TableRegionDevice();
                tableRegionDevice.setAccount_id(account_id);
                tableRegionDevice.setRegion_guid(region_guid);
                tableRegionDevice.setRegion_name(region_name);
                tableRegionDevice.setDevice_name(device_name);
                tableRegionDevice.setDevice_addr(device_addr);
                tableRegionDevice.setTable_device_guid(device_guid);
                list1.add(tableRegionDevice);
                //设置Device
                TableDevice tableDevice = new TableDevice();
                tableDevice.setDevice_addr(device_addr);
                tableDevice.setDevice_guid(device_guid);
                tableDevice.setAccount_id(account_id);

            }
            message = newRegionDeviceService.ModifyByDrviceOfname(list1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return message;
    }


}
