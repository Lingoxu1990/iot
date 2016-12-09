package com.iot.newEditionController;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.iot.exception.BussinessException;
import com.iot.exception.ParameterException;
import com.iot.message.Message;
import com.iot.message.MessageNoContent;
import com.iot.newEditionService.*;
import com.iot.pojo.*;
import com.iot.service.AccountInfoService;
import com.iot.service.SensorDataService;
import com.iot.service.UserService;
import com.iot.spitUtil.Param;
import com.iot.utils.ParamUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Created by adminchen on 16/7/8.
 */
@Controller
@RequestMapping()
public class NewEditionRegionController {
    private static Logger logger = Logger.getLogger(com.iot.newController.NewRegionController.class);

    @Resource
    private UserService userService;
    @Resource
    private AccountInfoService accountInfoService;
    @Resource
    private NewEditionRegionDeviceService newRegionDeviceService;
    @Resource
    private NewEditionDeviceService newDeviceService;
    @Resource
    private SensorDataService sensorDataService;
    @Resource
    private NewEditionGroupService newGroupService;
    @Resource
    private NewEditionSceneService newSceneService;
    @Resource
    private NewEditionRegionService newRegionService;

    //区域添加//去掉双向写入
    @RequestMapping(value = "/region", method = RequestMethod.POST)
    @ResponseBody
    public MessageNoContent addRegion(HttpServletRequest request) throws IOException {
        MessageNoContent message = new MessageNoContent();
        JSONObject jsonObject = ParamUtils.getAttributess(request);
        String user_id = (String) jsonObject.get("user_id");
        System.out.println("user_id:" + user_id);
        JSONArray jsonRegions = (JSONArray) jsonObject.get("table_region");
        JSONObject jsonRegion = (JSONObject) jsonRegions.get(0);
        String gateway_Id = (String) jsonRegion.get("gateway_id");
        String region_name = (String) jsonRegion.get("region_name");


        if (user_id == null || user_id.equals("")) {
            throw new ParameterException("-1", "user_id does not exist");
        }

        if (gateway_Id == null || gateway_Id.equals("")) {
            throw new ParameterException("-1", "gateway_Id does not exist");
        }

        if (region_name == null || region_name.equals("")) {
            throw new ParameterException("-1", "region_name does not exist");
        }

        //实体类
        TableRegion tableRegion = new TableRegion();
        tableRegion.setId(UUID.randomUUID().toString());
        tableRegion.setRegion_guid(UUID.randomUUID().toString());
        tableRegion.setGateway_id(gateway_Id);
        tableRegion.setRegion_name(region_name);
        tableRegion.setRegion_value("null");
        tableRegion.setRegion_switch("00");
        tableRegion.setRegion_delay("1");

        //添加区域
         newRegionService.addRegion(user_id, tableRegion);
        message.setCode("0");
        message.setMessage("Region add Success");

        return message;

    }

    //区域删除
    @RequestMapping(value = "/region", method = RequestMethod.DELETE)
    @ResponseBody
    public MessageNoContent DeleteRegion(HttpServletRequest httpServletRequest) throws IOException {
        JSONObject jsonObject = ParamUtils.getAttributess(httpServletRequest);
        JSONArray table_region = (JSONArray) jsonObject.get("table_region");
        JSONObject regiones = (JSONObject) table_region.get(0);
        String region_guid = (String) regiones.get("region_guid");
        String region_addr = (String) regiones.get("region_addr");
        String gateway_id = (String) regiones.get("gateway_id");
        String user_id = (String) jsonObject.get("user_id");
        if (user_id == null || user_id.equals("")) {
            throw new ParameterException("-1", "user_id does not exist");
        }

        if (region_guid == null || region_guid.equals("")) {
            throw new ParameterException("-1", "region_guid does not exist");
        }

        if (region_addr == null || region_addr.equals("")) {
            throw new ParameterException("-1", "region_addr does not exist");
        }

        if (gateway_id == null || gateway_id.equals("")) {
            throw new ParameterException("-1", "gateway_id does not exist");
        }

        TableRegion tableRegion = new TableRegion();
        tableRegion.setRegion_guid(region_guid);
        tableRegion.setGateway_id(gateway_id);
        tableRegion.setRegion_addr(region_addr);

        newRegionService.deleteRegion(user_id, tableRegion, jsonObject);

        MessageNoContent message = new MessageNoContent();
        message.setCode("0");
        message.setMessage("region delete success");
        return message;
//
    }

    //区域名称修改
    @RequestMapping(value = "/regionName", method = RequestMethod.PUT)
    @ResponseBody
    public MessageNoContent ModifyRegion(HttpServletRequest httpServletRequest) throws IOException {

        JSONObject jsonObject = ParamUtils.getAttributess(httpServletRequest);
        JSONArray jsonArray = (JSONArray) jsonObject.get("table_region");
        JSONObject regions = (JSONObject) jsonArray.get(0);
        String userId = (String) jsonObject.get("user_id");
        String regionGuid = regions.get("region_guid").toString();
        String regionName = regions.get("region_name").toString();
        String gatewayId = regions.get("gateway_id").toString();

        if (userId == null || userId.equals("")) {
            throw new ParameterException("-1", "user_id does not exist");
        }
        if (regionGuid == null || regionGuid.equals("")) {
            throw new ParameterException("-1", "region_guid does not exist");
        }
        if (regionName == null || regionName.equals("")) {
            throw new ParameterException("-1", "region_name does not exist");
        }
        if (gatewayId == null || gatewayId.equals("")) {
            throw new ParameterException("-1", "gateway_id does not exist");
        }

        TableRegion tableRegion = new TableRegion();
        tableRegion.setRegion_guid(regionGuid);
        tableRegion.setRegion_name(regionName);
        tableRegion.setGateway_id(gatewayId);
        newRegionService.modifyRegionName(tableRegion, userId);
        MessageNoContent message = new MessageNoContent();
        message.setCode("0");
        message.setMessage("update region name success");
        return message;
    }

    //查找区域
    @RequestMapping(value = "/edition/region", method = RequestMethod.GET)
    @ResponseBody
    public Object FindRegion(HttpServletRequest httpServletRequest) throws IOException {
        Message message = new Message();


        JSONObject jsonObject = ParamUtils.getJsonObjectFromRequest(httpServletRequest);

        JSONObject entity = (JSONObject) jsonObject.get("region");


        String user_id = (String) entity.get("user_id");
        List<UserGateway> getewayList = userService.selectGatewayByUserId(user_id);
        if (getewayList.isEmpty()) {
            message.setCode("-1");
            message.setMessage("user does not exist ");
            message.setContent(user_id);
            return message;
        }
        String acount_id = getewayList.get(0).getAccount_id();
        String SourceId = acount_id + Param.WEBAPPID + String.valueOf(System.currentTimeMillis() / 1000000000);

        TableRegion tableRegion = new TableRegion();
        tableRegion.setAccount_id(acount_id);

        List<TableRegion> list = newRegionService.findAllRegion(tableRegion);

        if (list.size() < 1) {
            message.setCode("0");
            message.setContent(new JSONArray());
            message.setMessage("Search the region successfully!");
            return message;
        }


        JSONArray result = new JSONArray();
        for (int i = 0; i < list.size(); i++) {
            result.add(JSONObject.toJSON(list.get(i)));
        }
        if ("00000000".equals(acount_id)) {
            message.setCode("0");
            message.setMessage("Search the region successfully!");
            message.setContent(result);
            return message;

        }


        for (int i = 0; i < result.size(); i++) {

            JSONObject region = (JSONObject) result.get(i);

            String regionGuid = (String) region.get("region_guid");
            String gatewayId = (String) region.get("gateway_id");

            TableRegionDevice tableRegionDevice = new TableRegionDevice();
            tableRegionDevice.setAccount_id(acount_id);
            tableRegionDevice.setRegion_guid(regionGuid);
            tableRegionDevice.setGateway_id(gatewayId);

            List<TableRegionDevice> sensors = sensorDataService.getRegionSensor(tableRegionDevice);
            Queue<String> senorQueue = new LinkedList<String>();

            for (TableRegionDevice sensor : sensors) {
                senorQueue.offer(sensor.getTable_device_guid());
            }

            if (senorQueue.size() < 1) {
                region.put("realTimeData", new JSONObject());
                continue;
            }

            int queueSize = senorQueue.size();


            JSONArray sensorsResult = new JSONArray();
            for (int j = 0; j < queueSize; j++) {

                String sensor_guid = senorQueue.poll();
                JSONArray realTime = sensorDataService.getRealTime(sensor_guid, acount_id, gatewayId, SourceId);

                if (realTime.size() < 1) {
                    continue;
                }
                for (int k = 0; k < realTime.size(); k++) {
                    sensorsResult.add(realTime.get(k));
                }

            }
            if (sensorsResult.size() < 1) {
                region.put("realTimeData", new JSONObject());
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
                    region.put("realTimeData", JJJ);
                }

            }

        }

        message.setCode("0");
        message.setMessage("Search the region successfully!");
        message.setContent(result);

        return message;
    }


    //添加区域场景
    @RequestMapping(value = "/region/scene", method = RequestMethod.POST)
    @ResponseBody
    public MessageNoContent addRegionScene(HttpServletRequest httpServletRequest) throws IOException {
        MessageNoContent message = new MessageNoContent();
        JSONObject jsonObject = ParamUtils.getAttributess(httpServletRequest);
        JSONArray regionScenes = (JSONArray) jsonObject.get("table_region_scene");
        JSONObject regionScene = (JSONObject) regionScenes.get(0);
        String user_id = (String) jsonObject.get("user_id");
        String gateway_id = (String) regionScene.get("gateway_id");
        String table_scene_guid = (String) regionScene.get("table_scene_guid");
        String scene_addr = (String) regionScene.get("scene_addr");
        String scene_name = (String) regionScene.get("scene_name");
        String region_guid = (String) regionScene.get("region_guid");

        if (user_id == null || user_id.equals("")) {
            throw new ParameterException("-1", "user_id does not exist");
        }

        if (gateway_id == null || gateway_id.equals("")) {
            throw new ParameterException("-1", "gateway_id does not exist");
        }

        if (table_scene_guid == null || table_scene_guid.equals("")) {
            throw new ParameterException("-1", "table_scene_guid does not exist");
        }

        if (scene_addr == null || scene_addr.equals("")) {
            throw new ParameterException("-1", "scene_addr does not exist");
        }

        if (scene_name == null || scene_name.equals("")) {
            throw new ParameterException("-1", "scene_name does not exist");
        }

        if (region_guid == null || region_guid.equals("")) {
            throw new ParameterException("-1", "region_guid does not exist");
        }

        //实体类
        TableRegionScene tableRegionScene = new TableRegionScene();
        tableRegionScene.setGateway_id(gateway_id);
        tableRegionScene.setTable_scene_guid(table_scene_guid);
        tableRegionScene.setScene_addr(scene_addr);
        tableRegionScene.setScene_name(scene_name);
        tableRegionScene.setRegion_guid(region_guid);
        tableRegionScene.setRegion_scene_guid(UUID.randomUUID().toString());
        tableRegionScene.setId(UUID.randomUUID().toString());

        //添加区域场景
          newRegionService.addRegionScene(user_id, tableRegionScene);

        message.setCode("0");
        message.setMessage("regionScene add success");

        return message;
    }

    //删除区域场景
    @RequestMapping(value = "/region/scene", method = RequestMethod.DELETE)
    @ResponseBody
    public MessageNoContent deleteRegionScene(HttpServletRequest httpServletRequest) throws IOException {
        JSONObject jsonObject = ParamUtils.getAttributess(httpServletRequest);
        JSONArray regionScenees = (JSONArray) jsonObject.get("table_region_scene");
        JSONObject regionScenes = regionScenees.getJSONObject(0);
        String scene_guid = (String) regionScenes.get("table_scene_guid");
        String scene_addr = (String) regionScenes.get("scene_addr");
        String gateway_id = (String) regionScenes.get("gateway_id");
        String user_id = (String) jsonObject.get("user_id");

        if (user_id == null || user_id.equals("")) {
            throw new ParameterException("-1", "user_id does not exist");
        }
        if (gateway_id == null || gateway_id.equals("")) {
            throw new ParameterException("-1", "gateway_id does not exist");
        }

        if (scene_addr == null || scene_addr.equals("")) {
            throw new ParameterException("-1", "scene_addr does not exist");
        }

        if (scene_guid == null || scene_guid.equals("")) {
            throw new ParameterException("-1", "scene_guid does not exist");
        }

        //实体类
        TableRegionScene tableRegionScene = new TableRegionScene();
        tableRegionScene.setScene_addr(scene_addr);
        tableRegionScene.setGateway_id(gateway_id);
        tableRegionScene.setTable_scene_guid(scene_guid);

        newRegionService.deleteRegionScene(user_id, tableRegionScene, jsonObject);
        MessageNoContent message = new MessageNoContent();
        message.setCode("0");
        message.setMessage("regionScene delete success");
        return message;
//
    }

    //查询区域场景
    @RequestMapping(value = "/edition/region/scene", method = RequestMethod.GET)
    @ResponseBody
    public Message findRegionScene(HttpServletRequest httpServletRequest) {
        Message message = new Message();
        try {
            //参数初始化
            JSONObject jsonObject = ParamUtils.getJsonObjectFromRequest(httpServletRequest);
            JSONObject jsonObject1 = (JSONObject) jsonObject.get("scene");
            String user_id = (String) jsonObject1.get("user_id");
            String gateway_id = (String) jsonObject1.get("gateway_id");
            String region_guid = (String) jsonObject1.get("region_guid");
            //String region_addr=(String)jsonObject1.get("region_addr");
            List<UserGateway> list = userService.selectGatewayByUserId(user_id);
            if (list.size() < 1) {
                message.setCode("-1");
                message.setMessage("user does not exist");
                message.setContent(new JSONArray());
                return message;
            }
            String account_id = list.get(0).getAccount_id();
            //实体类
            TableRegionScene tableRegionScene = new TableRegionScene();
            tableRegionScene.setAccount_id(account_id);
            tableRegionScene.setGateway_id(gateway_id);
            tableRegionScene.setRegion_guid(region_guid);
            List<TableRegionScene> list1 = newRegionService.findRegionScene(tableRegionScene);
            if (list1.size() > 0) {
                message.setCode("0");
                message.setMessage("RegionScene query success");
                JSONArray jsonArray = new JSONArray();
                jsonArray = (JSONArray) JSONArray.toJSON(list1);
                message.setContent(jsonArray);
            } else {
                message.setCode("0");
                message.setMessage("RegionScene query is empty");
                message.setContent(new JSONArray());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return message;
    }

    //修改区域场景名称
    @RequestMapping(value = "/region/scene", method = RequestMethod.PUT)
    @ResponseBody
    public MessageNoContent modifyRegionSceneName(HttpServletRequest request) throws IOException {
        JSONObject param = ParamUtils.getAttributess(request);
        String userId = (String) param.get("user_id");
        JSONArray regionScenes = (JSONArray) param.get("table_region_scene");
        JSONObject regionScene = (JSONObject) regionScenes.get(0);
        String gatewaydId = (String) regionScene.get("gateway_id");
        String tableSceneGuid=(String) regionScene.get("table_scene_guid");
        String sceneName=(String) regionScene.get("scene_name");

        if (userId==null||userId.equals("")){
            throw new ParameterException("-1","user_id does not exist");
        }
        if (gatewaydId==null||gatewaydId.equals("")){
            throw new ParameterException("-1","gateway_id does not exist");
        }
        if (tableSceneGuid==null||tableSceneGuid.equals("")){
            throw new ParameterException("-1","table_scene_guid does not exist");
        }
        if (sceneName==null||sceneName.equals("")){
            throw new ParameterException("-1","scene_name does not exist");
        }

        TableRegionScene tableRegionScene=new TableRegionScene();
        tableRegionScene.setGateway_id(gatewaydId);
        tableRegionScene.setTable_scene_guid(tableSceneGuid);
        tableRegionScene.setScene_name(sceneName);

        newRegionService.modifyRegionSceneName(tableRegionScene,userId);
        MessageNoContent message=new MessageNoContent();
        message.setCode("0");
        message.setMessage("update regionScene scene name success");
        return message;
    }

    //添加区域组
    @RequestMapping(value = "/region/group", method = RequestMethod.POST)
    @ResponseBody
    public MessageNoContent addRegionGroup(HttpServletRequest httpServletRequest) throws IOException {
        MessageNoContent message = new MessageNoContent();
        JSONObject jsonObject = ParamUtils.getAttributess(httpServletRequest);
        JSONArray regionGroups = (JSONArray) jsonObject.get("table_region_group");
        JSONObject regionGroup = (JSONObject) regionGroups.get(0);
        String regionGuid = (String) regionGroup.get("region_guid");
        String groupGuid = (String) regionGroup.get("table_group_guid");
        String groupAddr = (String) regionGroup.get("group_addr");
        String groupName = (String) regionGroup.get("group_name");
        String gatewayId = (String) regionGroup.get("gateway_id");
        String user_id = (String) jsonObject.get("user_id");

        if (user_id == null || user_id.equals("")) {
            throw new ParameterException("-1", "user_id does not exist");
        }

        if (gatewayId == null || gatewayId.equals("")) {
            throw new ParameterException("-1", "gatewayId does not exist");
        }

        if (groupName == null || groupName.equals("")) {
            throw new ParameterException("-1", "groupName does not exist");
        }

        if (groupAddr == null || groupAddr.equals("")) {
            throw new ParameterException("-1", "groupAddr does not exist");
        }

        if (groupGuid == null || groupGuid.equals("")) {
            throw new ParameterException("-1", "groupGuid does not exist");
        }

        if (regionGuid == null || regionGuid.equals("")) {
            throw new ParameterException("-1", "regionGuid does not exist");
        }

        //实体类
        TableRegionGroup tableRegionGroup = new TableRegionGroup();
        tableRegionGroup.setGroup_addr(groupAddr);
        tableRegionGroup.setGateway_id(gatewayId);
        tableRegionGroup.setRegion_guid(regionGuid);
        tableRegionGroup.setGroup_name(groupName);
        tableRegionGroup.setTable_group_guid(groupGuid);
        tableRegionGroup.setId(UUID.randomUUID().toString());
        tableRegionGroup.setRegion_group_guid(UUID.randomUUID().toString());

        //添加区域组
         newRegionService.addRegionGroup(user_id, tableRegionGroup);

        message.setCode("0");
        message.setMessage("regionGroup add success");
        return message;

    }

    //删除区域组
    @RequestMapping(value = "/region/group", method = RequestMethod.DELETE)
    @ResponseBody
    public MessageNoContent deleteRegionGroup(HttpServletRequest httpServletRequest) throws IOException {
        JSONObject jsonObject = ParamUtils.getAttributess(httpServletRequest);
        JSONArray table_region_group = (JSONArray) jsonObject.get("table_region_group");
        JSONObject region_groups = (JSONObject) table_region_group.get(0);
        String group_guid = (String) region_groups.get("table_group_guid");
        String group_addr = (String) region_groups.get("group_addr");
        String gateway_id = (String) region_groups.get("gateway_id");
        String user_id = (String) jsonObject.get("user_id");

        if (user_id == null || user_id.equals("")) {
            throw new ParameterException("-1", "user_id does not exist");
        }

        if (group_guid == null || group_guid.equals("")) {
            throw new ParameterException("-1", "group_guid does not exist");
        }

        if (group_addr == null || group_addr.equals("")) {
            throw new ParameterException("-1", "group_addr does not exist");
        }

        if (gateway_id == null || gateway_id.equals("")) {
            throw new ParameterException("-1", "gateway_id does not exist");
        }

        //实体类
        TableRegionGroup tableRegionGroup = new TableRegionGroup();
        tableRegionGroup.setGateway_id(gateway_id);
        tableRegionGroup.setGroup_addr(group_addr);
        tableRegionGroup.setTable_group_guid(group_guid);
        newRegionService.deleteRegionGroup(user_id, tableRegionGroup, jsonObject);

        MessageNoContent message = new MessageNoContent();
        message.setMessage("regionGroup delete success");
        message.setCode("0");
        return message;
    }

    //查找区域组
    @RequestMapping(value = "/region/group", method = RequestMethod.GET)
    @ResponseBody
    public Message findRegionGroup(HttpServletRequest httpServletRequest) throws IOException {

        JSONObject jsonObject;
        jsonObject = ParamUtils.getJsonObjectFromRequest(httpServletRequest);
        JSONObject jsonObject1 = (JSONObject) jsonObject.get("group");
        String userId = (String) jsonObject1.get("user_id");
        String regionGuid = (String) jsonObject1.get("region_guid");
        String gatewayId = (String) jsonObject1.get("gateway_id");

        if (userId==null||userId.equals("")){
            throw new BussinessException("-1","user_id does not exist");
        }
        if (regionGuid==null||regionGuid.equals("")){
            throw new BussinessException("-1","region_guid does not exist");
        }
        if (gatewayId==null||gatewayId.equals("")){
            throw new BussinessException("-1","gateway_id does not exist");
        }
        TableRegionGroup tableRegionGroup = new TableRegionGroup();
        tableRegionGroup.setRegion_guid(regionGuid);
        tableRegionGroup.setGateway_id(gatewayId);
        List<TableRegionGroup> list1 = newRegionService.findRegionGroup(tableRegionGroup,userId);
        Message message = new Message();
        message.setCode("0");
        message.setMessage("RegionGroup query success");
        JSONArray jsonArray = (JSONArray) JSONArray.toJSON(list1);
        message.setContent(jsonArray);
        return message;
    }

    //修改区域组名称
    @RequestMapping(value = "/region/group" ,method =RequestMethod.PUT)
    @ResponseBody
    public MessageNoContent modifyRegionGroupName(HttpServletRequest request) throws IOException {
        JSONObject param=ParamUtils.getAttributess(request);
        String userId=(String) param.get("user_id");
        JSONArray regionGroups=(JSONArray) param.get("table_region_group");
        JSONObject regionGroup=(JSONObject) regionGroups.get(0);
        String gatewayId=(String) regionGroup.get("gateway_id");
        String groupGuid=(String) regionGroup.get("table_group_guid");
        String groupName=(String) regionGroup.get("group_name");

        if (userId==null||userId.equals("")){
            throw new BussinessException("-1","user_id does not exist");
        }
        if (gatewayId==null||gatewayId.equals("")){
            throw new BussinessException("-1","gateway_id does not exist");
        }
        if (groupGuid==null||groupGuid.equals("")){
            throw new BussinessException("-1","table_group_guid does not exist");
        }
        if (groupName==null||groupName.equals("")){
            throw new BussinessException("-1","group_name does not exist");
        }
        TableRegionGroup tableRegionGroup=new TableRegionGroup();
        tableRegionGroup.setTable_group_guid(groupGuid);
        tableRegionGroup.setGateway_id(gatewayId);
        tableRegionGroup.setGroup_name(groupName);
        newRegionService.modifyRegionGroupName(tableRegionGroup,userId);
        MessageNoContent message=new MessageNoContent();
        message.setCode("0");
        message.setMessage("update regionGroup name success");
        return message;
    }

    // 控制区域
    @RequestMapping(value = "/edition/region/controll", method = RequestMethod.PUT)
    @ResponseBody
    public Object controllRegion(HttpServletRequest httpServletRequest) throws IOException {
        Message message = new Message();

        JSONObject jsonObject = ParamUtils.getJsonObjectFromRequest(httpServletRequest);

        //System.out.println(jj.toString());

        String userId = (String) jsonObject.get("user_id");

        List<UserGateway> tableDeviceList = userService.selectGatewayByUserId(userId);
        if (tableDeviceList.size() < 1) {
            message.setCode("-1");
            message.setMessage("user does not exist");
            message.setContent("[]");
            return message;
        }
        String account_id = tableDeviceList.get(0).getAccount_id();
        String SourceId = account_id + Param.WEBAPPID + String.valueOf(System.currentTimeMillis() / 1000000000);

        jsonObject.remove("user_id");


        JSONArray jsonTableRegion = (JSONArray) jsonObject.get("table_region");
        //System.out.println("array:"+jsonTableRegion.size());

        if (jsonTableRegion.size() < 1) {

            message.setCode("-1");
            message.setMessage("Param for Controller is missing");
            message.setContent(new JSONArray());
            return message;
        }

        JSONObject jsonRegion = (JSONObject) jsonTableRegion.get(0);
        System.out.println("json" + jsonRegion.size());
        TableRegion region = JSONObject.parseObject(jsonRegion.toString(), TableRegion.class);
        JSONArray region_value = (JSONArray) jsonRegion.get("region_value");
        String region_guid = (String) jsonRegion.get("region_guid");
        String gateway_id = (String) jsonRegion.get("gateway_id");
        String region_switch = (String) jsonRegion.get("region_switch");

        if (region_guid == null || gateway_id == null) {
            message.setCode("-1");
            message.setContent(new JSONArray());
            message.setMessage("Missing the region_guid or gateway_id");
            return message;
        }
        if (region_value != null & region_switch != null) {
            message.setCode("-1");
            message.setMessage("the operation should only be turn on/off or adjust the region value");
            message.setContent(new JSONArray());
            return message;
        }
        if (region_value != null && region_switch == null) {

            TableRegionDevice tableRegionDevice = new TableRegionDevice();
            tableRegionDevice.setAccount_id(account_id);
            tableRegionDevice.setRegion_guid(region_guid);
            tableRegionDevice.setGateway_id(gateway_id);

            List<Map> maps = newRegionDeviceService.fingRegionDevice(tableRegionDevice);
            if (maps.size() < 1) {
                message.setCode("-1");
                message.setMessage("Region is empty, there is no channel");
                message.setContent("[]");
                return message;
            }
            String device_guid_bulb = null;

            for (Map map : maps) {
                String device_guid = (String) map.get("table_device_guid");
                String account_id1 = (String) map.get("account_id");

                TableDevice record = new TableDevice();
                record.setAccount_id(account_id1);
                record.setDevice_guid(device_guid);
                record.setGateway_id(gateway_id);

                List<TableDevice> templist = newDeviceService.findInfoOfTheDevice(record);

                if (templist.size() < 1) {
                    device_guid_bulb = device_guid;
                    break;
                }

            }
            TableChannel record = new TableChannel();
            record.setAccount_id(account_id);
            record.setGateway_id(gateway_id);
            record.setTable_device_guid(device_guid_bulb);
            //获取通道数
            List<TableChannel> tableChannel = newDeviceService.findChannelInfo(record);

            if (tableChannel.size() < 1) {
                message.setCode("0");
                message.setMessage("Error: the device in the region can't find it's channel info!");
                message.setContent("[]");
                return message;
            }

            TableRegion tableRegionTemp = new TableRegion();
            tableRegionTemp.setGateway_id(gateway_id);
            tableRegionTemp.setAccount_id(account_id);
            tableRegionTemp.setRegion_guid(region_guid);

            TableRegion tableRegion = newRegionService.findTheValueOftheRegion(tableRegionTemp);
            //获取区域值
            String regionValue = tableRegion.getRegion_value();
            int len = regionValue.length() / 2;
            //创建一个数组,获取通道数
            String[] devices = null;
            if (len == 0) {
                devices = new String[1];
            } else {
                devices = new String[len];
            }

            for (int j = 0; j < devices.length; j++) {
                StringBuilder SB = new StringBuilder();
                SB.append(regionValue);
                if (j == (devices.length - 1)) {
                    SB.delete(0, j * 2);
                    devices[j] = SB.toString();
                } else {
                    devices[j] = SB.substring(j * 2, j * 2 + 2).toString();
                }
            }

            JSONArray jsonArray = new JSONArray();
            //通道数
            //System.out.println("通道数:"+tableChannel.size());
            for (int i = 0; i < tableChannel.size(); i++) {

                JSONObject channel = new JSONObject();
                channel.put("channel_number", tableChannel.get(i).getChannel_number());
                String strChannelNum = tableChannel.get(i).getChannel_number();
                int intChannelNum = Integer.parseInt(strChannelNum);

                String hexChannelValue = devices[intChannelNum - 1];
                int channelValue = Integer.valueOf(hexChannelValue, 16);
                String strChannelValue = String.valueOf(channelValue);

                channel.put("channel_value", strChannelValue);
                channel.put("channel_name", tableChannel.get(i).getChannel_name());

                jsonArray.add(channel);
            }
            //System.out.println("jsonArray:"+jsonArray);
            String[] value_arr_str = new String[jsonArray.size()];

            for (int j = 0; j < jsonArray.size(); j++) {
                JSONObject channel_value = (JSONObject) jsonArray.get(j);
                String channel_number1 = (String) channel_value.get("channel_number");
                for (int p = 0; p < region_value.size(); p++) {
                    JSONObject region_value_object = (JSONObject) region_value.get(p);
                    if (channel_number1.equals(region_value_object.get("channel_number"))) {
                        channel_value.remove("channel_value");
                        String str_value = (String) region_value_object.get("channel_value");
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
                }

                int index = Integer.parseInt(channel_number1) - 1;
                value_arr_str[index] = (String) channel_value.get("channel_value");
                //System.out.println("通道值"+value_arr_str[index]);
            }
            String final_value = "";
            for (int j = 0; j < value_arr_str.length; j++) {
                final_value += value_arr_str[j];
                System.out.println("final_value:" + final_value);
            }
            jsonRegion.remove("region_value");
            jsonRegion.put("region_value", final_value);
            jsonRegion.remove("gateway_id");

            region.setRegion_value(final_value);
            region.setAccount_id(account_id);
            JSONObject jsonResult = null;

            try {
                jsonResult = newRegionService.socketControlReion(jsonObject, gateway_id, SourceId, 2);
            } catch (Exception e) {
                logger.error(e);
            }

            if (jsonResult == null) {
                message.setCode("-1");
                message.setMessage("Time Out for read message from gateway");
                message.setContent(new JSONArray());
                return message;
            }

            String status = String.valueOf(jsonResult.get("Status"));

            if (status.equals("1")) {

                message.setCode("-1");
                message.setMessage("Failed to control the region");
                message.setContent(new JSONArray());
                return message;

            }
            if (status.equals("2")) {
                message.setCode("-1");
                message.setMessage("The region is out of control because it's off line");
                message.setContent(new JSONArray());
                return message;
            }


            int num = newRegionService.mysqlControlRegion(region);

            if (num < 1) {
                message.setCode("-1");
                message.setMessage("Failed to update the cloud DataBase");
                message.setContent(new JSONArray());
                return message;
            }

        }

        if (region_value == null && region_switch != null) {

            jsonRegion.remove("gateway_id");

            JSONObject jsonResult = null;

            try {
                jsonResult = newRegionService.socketControlReion(jsonObject, gateway_id, SourceId, 2);
            } catch (Exception e) {
                logger.error(e);
            }

            if (jsonResult == null) {
                message.setCode("-1");
                message.setMessage("Time Out for read message from gateway");
                message.setContent(new JSONArray());
                return message;
            }

            String status = String.valueOf(jsonResult.get("Status"));

            if (status.equals("1")) {

                message.setCode("-1");
                message.setMessage("Failed to control the region");
                message.setContent(new JSONArray());
                return message;

            }
            if (status.equals("2")) {
                message.setCode("-1");
                message.setMessage("The region is out of control because it's off line");
                message.setContent(new JSONArray());
                return message;
            }
            region.setAccount_id(account_id);


            int num = newRegionService.mysqlControlRegion(region);

            if (num < 1) {
                message.setCode("-1");
                message.setMessage("Failed to update the cloud DataBase");
                message.setContent(new JSONArray());
                return message;
            }

        }


        message.setCode("0");
        message.setMessage("Control the region successfully!");
        JSONObject object = (JSONObject) JSONObject.toJSON(region);
        object.remove("id");
        message.setContent(object);

        return message;
    }


    /**
     * 该接口用于下发初始化命令
     *
     * @return
     */
    //区域硬件能力初始化 命令下发接口
    @RequestMapping(value = "/region/init", method = RequestMethod.POST)
    @ResponseBody
    public Object init(@RequestBody @JsonFormat TableRegion tableRegion) {

        MessageNoContent messageNoContent = new MessageNoContent();

        String gatewayId = tableRegion.getGateway_id();

        if (gatewayId == null || "".equals(gatewayId)) {

            messageNoContent.setCode("-1");
            messageNoContent.setMessage("Missing the param gateway_id");
            return messageNoContent;
        }

        String regionGuid = tableRegion.getRegion_guid();

        if (regionGuid == null || "".equals(regionGuid)) {

            messageNoContent.setCode("-1");
            messageNoContent.setMessage("Missing the param region_guid");
            return messageNoContent;

        }

        String accountId = tableRegion.getAccount_id();


        if (accountId == null || "".equals(accountId)) {

            messageNoContent.setCode("-1");
            messageNoContent.setMessage("Missing the param account_id");
            return messageNoContent;

        }

        newRegionService.initRegion(tableRegion, accountId);

        messageNoContent.setCode("0");
        messageNoContent.setMessage("region init success");

        return messageNoContent;
    }


    /**
     *
     * 该接口用于接收子网关上报的区域控制状态
     * @param regionStatus
     * @return
     */
    @RequestMapping(value = "/region/status", method = RequestMethod.POST)
    @ResponseBody
    public Object status(@RequestBody @JsonFormat RegionStatus regionStatus) {

        MessageNoContent messageNoContent = new MessageNoContent();

        String regionGuid = regionStatus.getRegion_guid();
        String status  = regionStatus.getRegion_status();

        if (regionGuid==null || "".equals(regionGuid)){
            throw new ParameterException("-1", "region_guid does not exist");
        }
        if (status==null || "".equals(status)){
            throw new ParameterException("-1", "status does not exist");
        }

        newRegionService.removeRegionStatus(regionStatus);

        newRegionService.insertRegionStatus(regionStatus);

        messageNoContent.setCode("0");
        messageNoContent.setMessage("stauts update successfully");

        return messageNoContent;
    }

}