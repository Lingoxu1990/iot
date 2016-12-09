package com.iot.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.iot.requestUtil.RequestCaseUtil;
import com.iot.socketUtil.OutPutSocketMessage;
import com.iot.dbUtil.PropsUtil;
import com.iot.message.Message;
import com.iot.pojo.AccountDataInfo;
import com.iot.pojo.UserGateway;
import com.iot.service.*;
import com.iot.spitUtil.Param;
import com.iot.utils.ParamUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;

/**
 * Created by xulingo on 16/4/5.
 */

@Controller
@RequestMapping
public class RegionController {

    @Resource
    private RegionService regionService;
    @Resource
    private UserService userService;
    @Resource
    private AccountInfoService accountInfoService;
    @Resource
    private GroupService groupService;
    @Resource
    private SensorDataService sensorDataService;

    @RequestMapping(value = "/region/old",method = RequestMethod.POST)
    @ResponseBody
    public Object addRegion(HttpServletRequest httpServletRequest) throws IOException {

        //获取用户的account_id
        JSONObject jsonObject = ParamUtils.getJsonObjectFromRequest(httpServletRequest);


        String user_id = (String) jsonObject.get("user_id");

        JSONArray J_regions = (JSONArray) jsonObject.get("table_region");
        JSONObject J_region = (JSONObject) J_regions.get(0);


        List<UserGateway> tableDeviceList = userService.selectGatewayByUserId(user_id);

        String account_id = tableDeviceList.get(0).getAccount_id();

        String SourceId = account_id + Param.WEBAPPID + String.valueOf(System.currentTimeMillis() / 1000000000);


        String DestinationId = (String) J_region.get("gateway_id");
        AccountDataInfo accountDataInfo = new AccountDataInfo();
        accountDataInfo.setAccount_id(account_id);
        accountDataInfo.setGateway_id(DestinationId);

        //获取最后一次更新的区域地址,自增以后写入用户数据表
        AccountDataInfo accountDataInfo1 = accountInfoService.selectLastAddr(accountDataInfo);

        int regiogaddr = Integer.parseInt(accountDataInfo1.getRegion_addr(), 16) + 1;

        String region_addr = Integer.toHexString(regiogaddr);
        accountDataInfo1.setRegion_addr(region_addr);



        accountDataInfo.setRegion_addr(region_addr);

        //将新的区域地址添加进入参中
        JSONArray regions = (JSONArray) jsonObject.get("table_region");
        JSONObject region = (JSONObject) regions.get(0);
        region.put("region_addr", "ff15::" + region_addr);

        //添加网关id至区域 ,网关由前端传入
//        region.put("gateway_id",DestinationId);

        jsonObject.remove("user_id");

        Message message = regionService.addRegion(jsonObject, DestinationId, SourceId, 2);
        int n = accountInfoService.updataAddrInfo(accountDataInfo1);

        return message;
    }

    @RequestMapping(value = "/region",method = RequestMethod.PUT)
    @ResponseBody
    public Object modifyRegion(HttpServletRequest httpServletRequest) throws IOException {

        //// TODO: 16/4/5 获取本次操作对应的子网关 以及用户的sourceId
        JSONObject jsonObject = ParamUtils.getAttributess(httpServletRequest);
        Message message = regionService.modifyRegion(jsonObject, "158d00007e1595", "1234567802345677", -1);
        return message;
    }

    @RequestMapping(value = "/region/old",method = RequestMethod.GET)
    @ResponseBody
    public Object findRegion(HttpServletRequest httpServletRequest) throws IOException {

        Message message = new Message();

        JSONObject jsonObject = ParamUtils.getJsonObjectFromRequest(httpServletRequest);
        System.out.println(jsonObject.toString());

        JSONObject entity = (JSONObject) jsonObject.get("region");
        String user_id = (String) entity.get("user_id");

        List<UserGateway> tableDeviceList1 = userService.selectGatewayByUserId(user_id);

        String account_id = tableDeviceList1.get(0).getAccount_id();

        List<AccountDataInfo> tableDeviceList = userService.getALLagateway(account_id);

        String SourceId = account_id + Param.WEBAPPID + String.valueOf(System.currentTimeMillis() / 1000000000);

        JSONArray result = new JSONArray();

        for (AccountDataInfo userGateway : tableDeviceList) {
            String DestinationId = userGateway.getGateway_id();
            JSONObject region = new JSONObject();
            region.put("gateway_id", DestinationId);
            JSONObject pack = new JSONObject();
            pack.put("table_region", region);
            JSONArray tempMessage = regionService.findRegion(pack, DestinationId, SourceId, 2);
            for (int i = 0; i < tempMessage.size(); i++) {
                result.add(tempMessage.get(i));
            }
        }


        for (int i = 0; i < result.size(); i++) {

            JSONObject region = (JSONObject) result.get(i);

            String regionGuid = (String) region.get("region_guid");
            String DestinationId = (String) region.get("gateway_id");

            OutPutSocketMessage getRegionDevice = new OutPutSocketMessage();
            String sql0 = "SELECT * FROM table_region_device where region_guid = '" + regionGuid + "'";
            getRegionDevice.setPackegType(2);//包类型,app写2,web写-1
            getRegionDevice.setDestinationID(DestinationId);//app客户端可以写为任意的16个字符串,web填写目标网关地址
            getRegionDevice.setSourceID(SourceId);//源ID
            getRegionDevice.setType("table_region_device");//查询就填表名,非查询填写NULL
            getRegionDevice.setMessage("table_region_device");//消息用于网关返回消息,下发命令的时候,可以写表名
            getRegionDevice.setSql(sql0);//下发的指令(sql语句)

            JSONObject result0 = getRegionDevice.sendMessag(SourceId);

            JSONArray jsonArray = (JSONArray) result0.get("List");
            if (jsonArray.size() < 1) {

                region.put("realTimeData", new JSONObject());
                continue;
            }

            //去重
            HashMap<String, String> tempMap = new HashMap<String, String>();

            for (int j = 0; j < jsonArray.size(); j++) {
                JSONObject temp = (JSONObject) jsonArray.get(j);
                String deviceGuid = (String) temp.get("table_device_guid");
                tempMap.put(deviceGuid, "");
            }

            Queue<String> senorQueue = new LinkedList<String>();

            Set<String> keys = tempMap.keySet();

            for (String key : keys) {

                String sql = "SELECT * FROM table_device WHERE device_guid = '" + key + "'" + " AND device_type='sensor'";

                OutPutSocketMessage outPutSocketMessage = new OutPutSocketMessage();
                outPutSocketMessage.setPackegType(2);//包类型,app写2,web写-1
                outPutSocketMessage.setDestinationID(DestinationId);//app客户端可以写为任意的16个字符串,web填写目标网关地址
                outPutSocketMessage.setSourceID(SourceId);//源ID
                outPutSocketMessage.setType("table_device");//查询就填表名,非查询填写NULL
                outPutSocketMessage.setMessage("table_device");//消息用于网关返回消息,下发命令的时候,可以写表名
                outPutSocketMessage.setSql(sql);//下发的指令(sql语句)

                //发送数据包
                JSONObject jsonResult = outPutSocketMessage.sendMessag(SourceId);

                JSONArray sensors = (JSONArray) jsonResult.get("List");
                if (sensors.size() > 0) {
                    JSONObject sensor = (JSONObject) sensors.get(0);
                    String sensor_guid = (String) sensor.get("device_guid");
                    senorQueue.offer(sensor_guid);
                }

            }


            if (senorQueue.size() < 1) {
                region.put("realTimeData", new JSONObject());
                continue;
            }

            int queueSize = senorQueue.size();


            JSONArray sensorsResult = new JSONArray();
            for (int j = 0; j < queueSize; j++) {

                String sensor_guid = senorQueue.poll();
                JSONArray realTime = sensorDataService.getRealTime(sensor_guid, account_id, DestinationId, SourceId);

                if (realTime.size() < 1) {
                    region.put("realTimeData", new JSONObject());
                }
                for (int k = 0; k < realTime.size(); k++) {
                    sensorsResult.add(realTime.get(k));
                }

            }

            for (int j = 0; j < sensorsResult.size(); j++) {

                float air_temperature = 0;
                float air_humidity = 0;
                float soil_temperature = 0;
                float soil_humidity = 0;
                float soil_PH_value = 0;
                float carbon_dioxide = 0;
                float illuminance = 0;
                float soil_conductivity = 0;
                float photons = 0;
                float liquid_PH_value = 0;
                float lai_value = 0;

                JSONObject tempSensorResult = (JSONObject) sensorsResult.get(j);

                air_temperature += Float.parseFloat((String) tempSensorResult.get("air_temperature"));
                air_humidity += Float.parseFloat((String) tempSensorResult.get("air_humidity"));
                soil_temperature += Float.parseFloat((String) tempSensorResult.get("soil_temperature"));
                soil_humidity += Float.parseFloat((String) tempSensorResult.get("soil_humidity"));
                soil_PH_value += Float.parseFloat((String) tempSensorResult.get("soil_PH_value"));
                carbon_dioxide += Float.parseFloat((String) tempSensorResult.get("carbon_dioxide"));
                illuminance += Float.parseFloat((String) tempSensorResult.get("illuminance"));
                soil_conductivity += Float.parseFloat((String) tempSensorResult.get("soil_conductivity"));
                photons += Float.parseFloat((String) tempSensorResult.get("photons"));
                liquid_PH_value += Float.parseFloat((String) tempSensorResult.get("liquid_PH_value"));

                lai_value += 0;


                if (j == sensorsResult.size() - 1) {

                    air_temperature = air_temperature / sensorsResult.size();
                    air_humidity = air_humidity / sensorsResult.size();
                    soil_temperature = soil_temperature / sensorsResult.size();
                    soil_humidity = soil_humidity / sensorsResult.size();
                    soil_PH_value = soil_PH_value / sensorsResult.size();
                    carbon_dioxide = carbon_dioxide / sensorsResult.size();
                    illuminance = illuminance / sensorsResult.size();
                    soil_conductivity = soil_conductivity / sensorsResult.size();
                    photons = photons / sensorsResult.size();
                    liquid_PH_value = liquid_PH_value / sensorsResult.size();
                    soil_humidity = soil_humidity / sensorsResult.size();
                    lai_value = lai_value / sensorsResult.size();
                    JSONObject JJJ = new JSONObject();
                    JJJ.put("air_temperature", air_temperature);
                    JJJ.put("air_humidity", air_humidity);
                    JJJ.put("soil_temperature", soil_temperature);
                    JJJ.put("soil_humidity", soil_humidity);
                    JJJ.put("soil_PH_value", soil_PH_value);
                    JJJ.put("carbon_dioxide", carbon_dioxide);
                    JJJ.put("illuminance", illuminance);
                    JJJ.put("soil_conductivity", soil_conductivity);
                    JJJ.put("photons", photons);
                    JJJ.put("liquid_PH_value", liquid_PH_value);
                    JJJ.put("soil_humidity", soil_humidity);
                    JJJ.put("lai_value", lai_value);
                    result.add(JJJ);
                    region.put("realTimeData", JJJ);
                }

            }

        }


        message.setCode("0");
        message.setContent(result);
        message.setMessage("success!");

        return message;
    }

    @RequestMapping(value = "/region/old",method = RequestMethod.DELETE)
    @ResponseBody
    private Object deleteRegion(HttpServletRequest httpServletRequest) throws IOException {
        Message message = new Message();

        JSONObject jsonObject = ParamUtils.getJsonObjectFromRequest(httpServletRequest);

        String user_id = (String) jsonObject.get("user_id");

        JSONArray J_regions = (JSONArray) jsonObject.get("table_region");
        JSONObject J_region = (JSONObject) J_regions.get(0);

        List<UserGateway> tableDeviceList = userService.selectGatewayByUserId(user_id);


        String account_id = tableDeviceList.get(0).getAccount_id();

        String SourceId = account_id + Param.WEBAPPID + String.valueOf(System.currentTimeMillis() / 1000000000);

        String gateway_id = (String) J_region.get("gateway_id");

        String region_addr = (String) J_region.get("region_addr");

        String region_guid = (String) J_region.get("region_guid");


        OutPutSocketMessage delete_region_scene_message = new OutPutSocketMessage();

        String sql_delte_the_region_scene = "delete from table_region_scene where region_guid ='" + region_guid + "'";
        delete_region_scene_message.setPackegType(2);//包类型，app为2，web端为-1
        delete_region_scene_message.setDestinationID(gateway_id);//app客户可以为任意的16个字符串，web填写目标网关地址
        delete_region_scene_message.setType("NULL");//查询就填写表明，非查询就填写“NULL”
        delete_region_scene_message.setMessage("table_region_scene");//消息用于网关返回消息，下发命令时可以填写表明
        delete_region_scene_message.setSourceID(SourceId);//源ID
        delete_region_scene_message.setSql(sql_delte_the_region_scene);//下发sql指令

        JSONObject sql_delte_the_region_scene_result = delete_region_scene_message.sendMessag(SourceId);
        System.out.println(sql_delte_the_region_scene_result.toString());

        String scene_status = String.valueOf(sql_delte_the_region_scene_result.get("Status"));

        if (!scene_status.equals("0")) {
            message.setCode("-1");
            message.setMessage("Failed to remove the region scene!");
            message.setContent("[]");
            return message;
        }

        OutPutSocketMessage delete_region_group_message = new OutPutSocketMessage();

        String sql_delte_the_region_group = "delete from table_region_group where region_guid='" + region_guid + "'";
        delete_region_group_message.setPackegType(2);//包类型，app为2，web端为-1
        delete_region_group_message.setDestinationID(gateway_id);//app客户可以为任意的16个字符串，web填写目标网关地址
        delete_region_group_message.setType("NULL");//查询就填写表明，非查询就填写“NULL”
        delete_region_group_message.setMessage("table_region_group");//消息用于网关返回消息，下发命令时可以填写表明
        delete_region_group_message.setSourceID(SourceId);//源ID
        delete_region_group_message.setSql(sql_delte_the_region_group);//下发sql指令

        JSONObject sql_delte_the_region_group_result = delete_region_group_message.sendMessag(SourceId);
        String group_status = String.valueOf(sql_delte_the_region_group_result.get("Status"));

        if (!group_status.equals("0")) {
            message.setCode("-1");
            message.setMessage("Failed to remove the region group!");
            message.setContent("[]");
            return message;
        }

        // TODO: 16/5/27  删除区域下的设备暂时不做,需要对区域下的设备做一个查询操作

        OutPutSocketMessage get_region_device_message = new OutPutSocketMessage();

        String sql_get_the_region_degice_message = "select * from table_region_device where region_guid='" + region_guid + "'";
        get_region_device_message.setPackegType(2);
        get_region_device_message.setDestinationID(gateway_id);
        get_region_device_message.setType("table_region_device");
        get_region_device_message.setMessage("table_region_device");
        get_region_device_message.setSourceID(SourceId);
        get_region_device_message.setSql(sql_get_the_region_degice_message);

        JSONObject sql_get_the_region_device_result = get_region_device_message.sendMessag(SourceId);
        String get_device_status = String.valueOf(sql_get_the_region_device_result.get("Status"));
        if (get_device_status.equals("1")) {
            message.setCode("-1");
            message.setMessage("Failed to get the region device");
            message.setContent("[]");
            return message;
        }

        JSONArray device_list = (JSONArray) sql_get_the_region_device_result.get("List");

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < device_list.size(); i++) {
            String param = "(";
            JSONObject device = (JSONObject) device_list.get(i);
            Set<String> ketset = device.keySet();

            sb.append(param);
        }

//        OutPutSocketMessage delete_region_device_message=new OutPutSocketMessage();
//
//        String sql_delete_the_region_degice_message="delete from table_region_device where region_addr ='"+region_addr+"'";
//        delete_region_device_message.setPackegType(2);//包类型，app为2，web端为-1
//        delete_region_device_message.setDestinationID(gateway_id);//app客户可以为任意的16个字符串，web填写目标网关地址
//        delete_region_device_message.setType("NULL");//查询就填写表明，非查询就填写“NULL”
//        delete_region_device_message.setMessage("table_region_device");//消息用于网关返回消息，下发命令时可以填写表明
//        delete_region_device_message.setSourceID(SourceId);//源ID
//        delete_region_device_message.setSql(sql_delete_the_region_degice_message);//下发sql指令
//
//        JSONObject sql_delte_the_region_device_result=delete_region_device_message.sendMessag();
//        String device_status = String.valueOf(sql_delte_the_region_device_result.get("Status"));
//
//        if (!device_status.equals("0")){
//            message.setCode("-1");
//            message.setMessage("Failed to remove the region device!");
//            message.setContent("[]");
//            return message;
//        }


        OutPutSocketMessage delete_region_message = new OutPutSocketMessage();
        String sql_delete_the_region_message = "delete from table_region where region_addr ='" + region_addr + "'";

        delete_region_message.setPackegType(2);//包类型，app为2，web端为-1
        delete_region_message.setDestinationID(gateway_id);//app客户可以为任意的16个字符串，web填写目标网关地址
        delete_region_message.setType("NULL");//查询就填写表明，非查询就填写“NULL”
        delete_region_message.setMessage("table_region_device");//消息用于网关返回消息，下发命令时可以填写表明
        delete_region_message.setSourceID(SourceId);//源ID
        delete_region_message.setSql(sql_delete_the_region_message);//下发sql指令

        JSONObject sql_delte_the_region_result = delete_region_message.sendMessag(SourceId);
        String region_status = String.valueOf(sql_delte_the_region_result.get("Status"));
        if (!region_status.equals("0")) {
            message.setCode("-1");
            message.setMessage("Failed to remove the region!");
            message.setContent("[]");
            return message;
        }

        message.setCode("0");
        message.setMessage("delete the region successfully!");
        message.setContent("[]");
        return message;
    }



    @RequestMapping(value = "/region/device/old", method = RequestMethod.DELETE)
    @ResponseBody
    public Object deleteRegionDevice(HttpServletRequest httpServletRequest) throws IOException {

        JSONObject jsonObject  = ParamUtils.getJsonObjectFromRequest(httpServletRequest);
        System.out.println(jsonObject.toString());

        JSONObject temp = new JSONObject();

        String user_id = (String) jsonObject.get("user_id");

        List<UserGateway> tableDeviceList = userService.selectGatewayByUserId(user_id);

        String account_id = tableDeviceList.get(0).getAccount_id();

        String SourceId = account_id + Param.WEBAPPID + String.valueOf(System.currentTimeMillis() / 1000000000);
        String DestinationId = tableDeviceList.get(0).getGateway_id();

        jsonObject.remove("user_id");


        JSONArray region_devices = (JSONArray) jsonObject.get("table_region_device");


        JSONObject region_device = (JSONObject) region_devices.get(0);

        String gateway_id = (String) region_device.get("gateway_id");
        region_device.remove("gateway_id");

        System.out.println(jsonObject.toString());

        Message message = regionService.deleteRegionDevice(jsonObject, gateway_id, SourceId, 2);


        return message;

    }



    /**
     * {
     * "user_id":"",
     * "table_region_group":[｛
     * "region_guid":"区域主键",
     * "table_group_guid":"",
     * "group_addr":"",
     * "group_name:"",
     * "gateway_id":""
     * ｝
     * ]
     * }
     *
     * @return
     */
    @RequestMapping(value = "/region/group/old", method = RequestMethod.POST)
    @ResponseBody
    public Object addRegionGroup(HttpServletRequest httpServletRequest) throws IOException {

        JSONObject jj = ParamUtils.getJsonObjectFromRequest(httpServletRequest);
        System.out.println(jj.toString());

        String user_id = (String) jj.get("user_id");
        String region_gateway_id = (String) jj.get("gateway_id");
        List<UserGateway> tableDeviceList = userService.selectGatewayByUserId(user_id);

        String account_id = tableDeviceList.get(0).getAccount_id();

        String SourceId = account_id + Param.WEBAPPID + String.valueOf(System.currentTimeMillis() / 1000000000);
        String DestinationId = tableDeviceList.get(0).getGateway_id();

        JSONArray groups = (JSONArray) jj.get("table_region_group");


        Map<String, JSONObject> map = regionService.specialAction(groups, SourceId, "table_region_group", region_gateway_id);

        Set<Map.Entry<String, JSONObject>> entries = map.entrySet();

        Message message = new Message();
        for (Map.Entry<String, JSONObject> entry : entries) {
            entry.getKey();

            message = regionService.addRegionGroup(entry.getValue(), entry.getKey(), SourceId, 2);

            if (!message.getCode().equals("0")) {
                return message;
            }
        }

        message.setCode("0");
        message.setMessage("success!");
        message.setContent("[]");

        return message;
    }

    @RequestMapping(value = "/region/group/old", method = RequestMethod.GET)
    @ResponseBody
    public Object findRegionGroup(HttpServletRequest httpServletRequest) throws IOException {

        JSONObject jj  = ParamUtils.getJsonObjectFromRequest(httpServletRequest);
        System.out.println(jj.toString());

        JSONObject body = (JSONObject) jj.get("group");

        String user_id = (String) body.get("user_id");

        String region_guid = (String) body.get("region_guid");
        String region_gateway_id = (String) body.get("gateway_id");

        List<UserGateway> tableDeviceList = userService.selectGatewayByUserId(user_id);

        String account_id = tableDeviceList.get(0).getAccount_id();

        String SourceId = account_id + Param.WEBAPPID + String.valueOf(System.currentTimeMillis() / 1000000000);

        String DestinationId = region_gateway_id;

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("region_guid", region_guid);
        JSONObject J = new JSONObject();
        J.put("table_region_group", jsonObject);

        Message message = regionService.findRegionGroupByAccountIdAndRegionId(J, DestinationId, SourceId, 2);

        JSONArray content = (JSONArray) message.getContent();

        for (int i = 0; i < content.size(); i++) {

            JSONObject region_group = (JSONObject) content.get(i);
            String region_group_guid = (String) region_group.get("table_group_guid");
            String gateway_id = (String) region_group.get("gateway_id");


            JSONObject toFind = new JSONObject();
            JSONObject group = new JSONObject();
            group.put("group_guid", region_group_guid);


            toFind.put("table_group", group);

            Message message1 = groupService.findGroup(toFind, gateway_id, SourceId, 2);

            JSONArray contents = (JSONArray) message1.getContent();

            if (contents.size() > 0) {

                JSONObject group_temp = (JSONObject) contents.get(0);
                region_group.put("group_switch", group_temp.get("group_switch"));
            }
        }

        return message;
    }

    @RequestMapping(value = "/region/group/old", method = RequestMethod.DELETE)
    @ResponseBody
    public Object deleteRegionGroup(HttpServletRequest httpServletRequest) throws IOException {

        JSONObject jj = ParamUtils.getJsonObjectFromRequest(httpServletRequest);

        System.out.println(jj);
        String user_id = (String) jj.get("user_id");


        List<UserGateway> tableDeviceList = userService.selectGatewayByUserId(user_id);

        String account_id = tableDeviceList.get(0).getAccount_id();

        String SourceId = account_id + Param.WEBAPPID + String.valueOf(System.currentTimeMillis() / 1000000000);
        String DestinationId = tableDeviceList.get(0).getGateway_id();

        jj.remove("user_id");
        JSONArray region_groups = (JSONArray) jj.get("table_region_group");
        JSONObject region_group = (JSONObject) region_groups.get(0);
        String gateway_id = (String) region_group.get("gateway_id");
        region_group.remove("gateway_id");


        Message message = regionService.deleteRegionGroup(jj, gateway_id, SourceId, 2);


        return message;
    }


    @RequestMapping(value = "/region/scene/old", method = RequestMethod.POST)
    @ResponseBody
    public Object addRegionScene(HttpServletRequest httpServletRequest) throws IOException {

        JSONObject jj = ParamUtils.getJsonObjectFromRequest(httpServletRequest);
        System.out.println(jj.toString());


        String user_id = (String) jj.get("user_id");
        String region_gateway_id = (String) jj.get("gateway_id");
        List<UserGateway> tableDeviceList = userService.selectGatewayByUserId(user_id);

        String account_id = tableDeviceList.get(0).getAccount_id();

        String SourceId = account_id + Param.WEBAPPID + String.valueOf(System.currentTimeMillis() / 1000000000);
        String DestinationId = tableDeviceList.get(0).getGateway_id();

        JSONArray scenes = (JSONArray) jj.get("table_region_scene");


        Map<String, JSONObject> map = regionService.specialAction(scenes, SourceId, "table_region_scene", region_gateway_id);

        Set<Map.Entry<String, JSONObject>> entries = map.entrySet();

        Message message = new Message();

        JSONObject result = new JSONObject();


        for (Map.Entry<String, JSONObject> entry : entries) {
            entry.getKey();

            message = regionService.addRegionScene(entry.getValue(), entry.getKey(), SourceId, 2);

            if (!message.getCode().equals("0")) {
                return message;
            }
            result.put(entry.getKey(), entry.getValue());

        }

        message.setCode("0");
        message.setMessage("success!");
        message.setContent(result);

        return message;
    }

    @RequestMapping(value = "/region/scene/old", method = RequestMethod.GET)
    @ResponseBody
    public Object findRegionScene(HttpServletRequest httpServletRequest) throws IOException {

        JSONObject jj = ParamUtils.getJsonObjectFromRequest(httpServletRequest);
        System.out.println(jj.toString());

        JSONObject body = (JSONObject) jj.get("scene");

        String user_id = (String) body.get("user_id");

        String region_guid = (String) body.get("region_guid");

        String region_gatway_id = (String) body.get("gateway_id");

        List<UserGateway> tableDeviceList = userService.selectGatewayByUserId(user_id);

        String account_id = tableDeviceList.get(0).getAccount_id();

        String SourceId = account_id + Param.WEBAPPID + String.valueOf(System.currentTimeMillis() / 1000000000);


        JSONObject jsonObject = new JSONObject();
        jsonObject.put("region_guid", region_guid);
        jsonObject.put("gateway_id", region_gatway_id);
        JSONObject J = new JSONObject();
        J.put("table_region_scene", jsonObject);

        Message searchResult = regionService.findRegionSceneByAccountIdAndRegionId(J, region_gatway_id, SourceId, 2);


        return searchResult;
    }

    @RequestMapping(value = "/region/scene/old", method = RequestMethod.DELETE)
    @ResponseBody
    public Object deleteRegionScene(HttpServletRequest httpServletRequest) throws IOException {

        JSONObject jj = ParamUtils.getJsonObjectFromRequest(httpServletRequest);

        System.out.println(jj);
        String user_id = (String) jj.get("user_id");

        List<UserGateway> tableDeviceList = userService.selectGatewayByUserId(user_id);

        String account_id = tableDeviceList.get(0).getAccount_id();

        String SourceId = account_id + Param.WEBAPPID + String.valueOf(System.currentTimeMillis() / 1000000000);
        String DestinationId = tableDeviceList.get(0).getGateway_id();

        jj.remove("user_id");
        System.out.println(jj.toString());


        JSONArray region_groups = (JSONArray) jj.get("table_region_scene");
        JSONObject region_group = (JSONObject) region_groups.get(0);
        String gateway_id = (String) region_group.get("gateway_id");
        region_group.remove("gateway_id");

        Message message = regionService.deleteRegionScene(jj, gateway_id, SourceId, 2);


        return message;

    }

    @RequestMapping(value = "/region/cdts", method = RequestMethod.POST)
    @ResponseBody
    public Object addRegionCdts(HttpServletRequest httpServletRequest) throws IOException {

        JSONObject jj = ParamUtils.getJsonObjectFromRequest(httpServletRequest);
        System.out.println(jj.toString());


        String user_id = (String) jj.get("user_id");
        String region_gateway_id = (String) jj.get("gateway_id");

        List<UserGateway> tableDeviceList = userService.selectGatewayByUserId(user_id);

        String account_id = tableDeviceList.get(0).getAccount_id();

        String SourceId = account_id + Param.WEBAPPID + String.valueOf(System.currentTimeMillis() / 1000000000);
//        String DestinationId = tableDeviceList.get(0).getGateway_id();

        JSONArray scenes = (JSONArray) jj.get("table_region_cdts_ctrl");


        Map<String, JSONObject> map = regionService.specialAction(scenes, SourceId, "table_region_cdts_ctrl", region_gateway_id);

        Set<Map.Entry<String, JSONObject>> entries = map.entrySet();

        Message message = new Message();

        JSONArray result = new JSONArray();

        for (Map.Entry<String, JSONObject> entry : entries) {
            entry.getKey();
            message = regionService.addRegionCdts(entry.getValue(), entry.getKey(), SourceId, 2);

            if (!message.getCode().equals("0")) {
                return message;
            }
            JSONArray listTemp = (JSONArray) message.getContent();

            for (int i = 0; i < listTemp.size(); i++) {

                result.add(listTemp.get(i));

            }
        }

        message.setCode("0");
        message.setMessage("success!");
        message.setContent(result);

        return message;
    }


    @RequestMapping(value = "/region/cdts", method = RequestMethod.GET)
    @ResponseBody
    public Object findAllCdts(HttpServletRequest httpServletRequest) throws IOException {

        Message message = new Message();

        JSONObject jj = ParamUtils.getJsonObjectFromRequest(httpServletRequest);
        System.out.println(jj.toString());

        String userId = (String) jj.get("user_id");
        jj.remove("user_id");

        List<UserGateway> tableDeviceList = userService.selectGatewayByUserId(userId);

        String account_id = tableDeviceList.get(0).getAccount_id();
        String SourceId = account_id + Param.WEBAPPID + String.valueOf(System.currentTimeMillis() / 1000000000);

        String gatewayId = (String) jj.get("gateway_id");
        String regionName = (String) jj.get("region_name");


        String getRegionCdtsList = "select * from table_region_cdts_ctrl where region_name='" + regionName + "'";
        OutPutSocketMessage outPutSocketMessage = new OutPutSocketMessage();
        outPutSocketMessage.setPackegType(2);//包类型,app写2,web写-1
        outPutSocketMessage.setDestinationID(gatewayId);//app客户端可以写为任意的16个字符串,web填写目标网关地址
        outPutSocketMessage.setType("NULL");//查询就填表名,非查询填写NULL
        outPutSocketMessage.setMessage("table_region_cdts_ctrl");//消息用于网关返回消息,下发命令的时候,可以写表名
        outPutSocketMessage.setSourceID(SourceId);//源ID
        outPutSocketMessage.setSql(getRegionCdtsList);//下发的指令(sql语句)

        JSONObject jsonResult = outPutSocketMessage.sendMessag(SourceId);

        String status = String.valueOf(jsonResult.get("Status"));

        JSONArray list = (JSONArray) jsonResult.get("List");

        if (list.size() < 1) {
            message.setCode("0");
            message.setMessage("there is no data!");
            message.setContent(new JSONArray());
            return message;
        }

        String result = "";
        for (int i = 0; i < list.size(); i++) {


            JSONObject json = (JSONObject) list.get(i);

            String cdts_list_guid = (String) json.get("cdts_list_guid");

            String host = "";

            try {

                PropsUtil configProps = new PropsUtil("config.properties");

                host = configProps.get("host");

            } catch (IOException e) {

                e.printStackTrace();
            }

            host += "device/term?user_id=" + userId + "&gateway_id=" + gatewayId + "&cdts_list_guid=" + cdts_list_guid;

            result = RequestCaseUtil.requestGetCase(host);


        }
        return JSONObject.parseObject(result);
    }



    @RequestMapping(value = "/region/controll/old", method = RequestMethod.PUT)
    @ResponseBody
    public Object controllRegion(HttpServletRequest httpServletRequest) throws IOException {
        JSONObject jj = ParamUtils.getJsonObjectFromRequest(httpServletRequest);
        System.out.println(jj.toString());

        String user_id = (String) jj.get("user_id");

        jj.remove("user_id");

        List<UserGateway> tableDeviceList = userService.selectGatewayByUserId(user_id);

        String account_id = tableDeviceList.get(0).getAccount_id();

        String SourceId = account_id + Param.WEBAPPID + String.valueOf(System.currentTimeMillis() / 1000000000);

        String gateway_id = "";

        String region_switch = "";

        JSONArray jsonArray = (JSONArray) jj.get("table_region");

        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject json = (JSONObject) jsonArray.get(i);
            JSONObject region_value = (JSONObject) json.get("region_value");
            String region_guid = (String) json.get("region_guid");
            gateway_id = (String) json.get("gateway_id");
            region_switch = (String) json.get("region_switch");

            if (region_switch != null) {
                continue;
            }

            String host = "";

            try {
                PropsUtil configProps = new PropsUtil("config.properties");
                host = configProps.get("host");
            } catch (IOException e) {
                e.printStackTrace();
            }
            String url = "";
            url = host + "region?user_id=" + user_id;

            String result = RequestCaseUtil.requestGetCase(url);

            JSONObject res = JSONObject.parseObject(result);
            JSONArray content = (JSONArray) res.get("content");
            String str_region_value = "";

            for (int j = 0; j < content.size(); j++) {
                JSONObject JJ = (JSONObject) content.get(j);
                String tt = (String) JJ.get("region_guid");
                if (tt.equals(region_guid)) {
                    str_region_value = (String) JJ.get("region_value");
                }
            }

            String url2 = "";

            url2 = host + "region/region_channel?user_id=" + user_id + "&region_guid=" + region_guid + "&gateway_id=" + gateway_id +
                    "&region_value=" + str_region_value;

            result = RequestCaseUtil.requestGetCase(url2);

            res = JSONObject.parseObject(result);
            JSONArray channel_content = (JSONArray) res.get("content");
            String[] value_arr_str = new String[channel_content.size()];

            for (int j = 0; j < channel_content.size(); j++) {
                JSONObject channel_value = (JSONObject) channel_content.get(j);
                String channel_number1 = (String) channel_value.get("channel_number");
                System.out.println("channel_number : " + channel_number1);
                System.out.println("channel_number : " + region_value.get("channel_number"));
                if (channel_number1.equals(region_value.get("channel_number"))) {
                    channel_value.remove("channel_value");
                    String str_value = (String) region_value.get("channel_value");
                    int int_str = Integer.parseInt(str_value);
                    String str_hex_value = "";
                    if (int_str < 16) {
                        str_hex_value = "0" + Integer.toHexString(int_str);
                    } else {
                        str_hex_value = Integer.toHexString(int_str);
                    }
                    channel_value.put("channel_value", str_hex_value);
                } else {

                    String str_channel_value = (String) channel_value.get("channel_value");
                    int int_channel_value = Integer.parseInt(str_channel_value);
                    channel_value.remove("channel_value");
                    String str_hex_value = "";
                    if (int_channel_value < 16) {
                        str_hex_value = "0" + Integer.toHexString(int_channel_value);
                    } else {
                        str_hex_value = Integer.toHexString(int_channel_value);

                    }
                    channel_value.put("channel_value", str_hex_value);
                }
                int index = Integer.parseInt(channel_number1) - 1;
                value_arr_str[index] = (String) channel_value.get("channel_value");
            }
            String final_value = "";
            for (int j = 0; j < value_arr_str.length; j++) {
                final_value += value_arr_str[j];
            }
            json.remove("region_value");
            json.put("region_value", final_value);

        }

        JSONArray jsonObject = (JSONArray) jj.get("table_region");


        for (int i = 0; i < jsonObject.size(); i++) {
            JSONObject temp = (JSONObject) jsonObject.get(i);
            temp.remove("gateway_id");
        }

        Message message = regionService.controlReion(jj, gateway_id, SourceId, 2);

        return message;
    }

}
