package com.iot.newController;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.iot.exception.BussinessException;
import com.iot.message.Message;
import com.iot.message.MessageNoContent;
import com.iot.newService.NewSceneService;
import com.iot.pojo.*;
import com.iot.service.AccountInfoService;
import com.iot.service.UserService;
import com.iot.spitUtil.Param;
import com.iot.utils.ParamUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Created by adminchen on 16/6/16.
 */

@Controller
@RequestMapping("")
public class NewSceneController {

    private static Logger logger = Logger.getLogger(NewSceneController.class);

    @Resource
    private UserService userService;

    @Resource
    private AccountInfoService accountInfoService;

    @Resource
    private NewSceneService sceneService;

    //添加场景
    @RequestMapping(value = "/device/scene/old/old/old", method = RequestMethod.POST)
    @ResponseBody
    public Message addScene(HttpServletRequest httpServletRequest) {
        Message message = new Message();
        try {
            JSONObject jsonObject;
            jsonObject = ParamUtils.getJsonObjectFromRequest(httpServletRequest);
            JSONArray scenes = (JSONArray) jsonObject.get("table_scene");
            JSONObject scene = (JSONObject) scenes.get(0);
            String user_id = (String) jsonObject.get("user_id");
            String gateway_id = (String) scene.get("gateway_id");
            String scene_name = (String) scene.get("scene_name");
            List<UserGateway> list = userService.selectGatewayByUserId(user_id);
            if (list.size() < 1) {
                message.setCode("-1");
                message.setMessage("user does not exist");
                message.setContent("[]");
                return message;
            }

            jsonObject.remove("user_id");
            String account_id = list.get(0).getAccount_id();
            String SourceId = account_id + Param.WEBAPPID + String.valueOf(System.currentTimeMillis() / 1000000000);
            //获取地址
            AccountDataInfo accountDataInfo = new AccountDataInfo();
            accountDataInfo.setAccount_id(account_id);
            accountDataInfo.setGateway_id(gateway_id);
            AccountDataInfo accountDataInfo1 = accountInfoService.selectLastAddr(accountDataInfo);
            if (accountDataInfo1 == null) {
                message.setCode("-1");
                message.setMessage("No gateway");
                message.setContent("[]");
                return message;
            }
            String addr = accountDataInfo1.getSence_adde();
            int int_addr = Integer.parseInt(addr, 16) + 1;
            String account_addr = Integer.toHexString(int_addr);
            AccountDataInfo accountDataInfo2 = new AccountDataInfo();
            accountDataInfo2.setAccount_id(account_id);
            accountDataInfo2.setGateway_id(gateway_id);
            accountDataInfo2.setSence_adde(account_addr);

            String scene_addr = "ff15::" + account_addr;
            String scene_guid = UUID.randomUUID().toString();
            for (int i = 0; i < scenes.size(); i++) {
                JSONObject temScene = (JSONObject) scenes.get(i);
                temScene.put("scene_addr", scene_addr);
                temScene.put("scene_guid", scene_guid);
                temScene.put("scene_switch", "01");
            }
            JSONObject socketResult = null;
            try {
                socketResult = sceneService.socketAddScene(jsonObject, gateway_id, SourceId, 2);
            } catch (Exception e) {
                logger.error(e);
            }
            if (socketResult == null) {
                message.setCode("-1");
                message.setMessage("Gateway socket read time out!");
                message.setContent(new JSONArray());
                return message;
            }
            String status = String.valueOf(socketResult.get("Status"));

            if (status.equals("1")) {
                message.setCode("-1");
                message.setMessage("Sub-gateway retrun status '1' means command can't be executed");
                message.setContent(new JSONArray());
                return message;
            }
            if (status.equals("2")) {
                message.setCode("-1");
                message.setContent(new JSONArray());
                message.setMessage("Sub-gateway return status '2' means deivce is offline");
                return message;
            }
            //设置实体类TableScene
            TableScene tableScene = new TableScene();
            tableScene.setAccount_id(account_id);
            tableScene.setGateway_id(gateway_id);
            tableScene.setScene_name(scene_name);
            tableScene.setId(UUID.randomUUID().toString());
            tableScene.setScene_guid(scene_guid);
            tableScene.setScene_addr(scene_addr);
            tableScene.setScene_switch("01");
            int n = sceneService.insertScene(tableScene);
            int a = accountInfoService.updataAddrInfo(accountDataInfo2);
            JSONObject returnScene = (JSONObject) JSONObject.toJSON(tableScene);
            JSONArray result = new JSONArray();
            result.add(returnScene);
            if (a < 1) {
                message.setCode("-1");
                message.setMessage("AccountDateInfo update failed");
                message.setContent(new JSONArray());
                return message;
            }
            if (n > 0) {
                message.setCode("0");
                message.setMessage("Scene add success");
                message.setContent(result);
            } else {
                message.setCode("-1");
                message.setMessage("Scene add failed");
                message.setContent(new JSONArray());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return message;
    }

    //修改场景
    @RequestMapping(value = "/device/scene/old/old/old", method = RequestMethod.PUT)
    @ResponseBody
    public Message modifySence(HttpServletRequest httpServletRequest) {
        Message message = new Message();
        try {
            JSONObject jsonObject = ParamUtils.getJsonObjectFromRequest(httpServletRequest);
            JSONArray scenes = (JSONArray) jsonObject.get("table_scene");
            JSONObject scene = (JSONObject) scenes.get(0);
            String user_id = (String) jsonObject.get("user_id");
            String scene_guid = (String) scene.get("scene_guid");
            //String scene_addr = (String) jsonObject1.get("scene_addr");
            String scene_name = (String) scene.get("scene_name");
            String gateway_id = (String) scene.get("gateway_id");
            List<UserGateway> list = userService.selectGatewayByUserId(user_id);
            if (list.size() < 1) {
                message.setCode("-1");
                message.setMessage("user does not exist");
                message.setContent(new JSONArray());
                return message;
            }
            String account_id = list.get(0).getAccount_id();
            String SourceId = account_id + Param.WEBAPPID + String.valueOf(System.currentTimeMillis() / 1000000000);

            //设置实体类TableScene
            TableScene tableScene = new TableScene();
            tableScene.setScene_guid(scene_guid);
            //tableScene.setScene_addr(scene_addr);
            tableScene.setAccount_id(account_id);
            tableScene.setScene_name(scene_name);
            tableScene.setGateway_id(gateway_id);
            jsonObject.remove("user_id");
            for (int i = 0; i < scenes.size(); i++) {
                JSONObject temscenes = (JSONObject) scenes.get(i);
                temscenes.remove("gateway_id");
                temscenes.remove("scene_addr");
            }
            JSONObject socketResult = null;
            try {
                socketResult = sceneService.socketmodifyScene(jsonObject, gateway_id, SourceId, 2);
            } catch (Exception e) {
                logger.error(e);
            }
            if (socketResult == null) {
                message.setCode("-1");
                message.setMessage("Gateway socket read time out!");
                message.setContent(new JSONArray());
                return message;
            }
            String status = String.valueOf(socketResult.get("Status"));

            if (status.equals("1")) {
                message.setCode("-1");
                message.setMessage("Sub-gateway retrun status '1' means command can't be executed");
                message.setContent(new JSONArray());
                return message;
            }
            if (status.equals("2")) {
                message.setCode("-1");
                message.setContent(new JSONArray());
                message.setMessage("Sub-gateway return status '2' means deivce is offline");
                return message;
            }
            int n = sceneService.updateScene(tableScene);
            if (n < 1) {
                message.setCode("-1");
                message.setMessage("Scene failed modification");
                message.setContent(new JSONArray());
                return message;
            }


            //实体类TableRegionScene
            TableRegionScene tableRegionScene = new TableRegionScene();
            tableRegionScene.setTable_scene_guid(scene_guid);
            tableRegionScene.setGateway_id(gateway_id);
            tableRegionScene.setAccount_id(account_id);
            tableRegionScene.setScene_name(scene_name);
            TableRegionScene tableRegionScenes = sceneService.findRegionSceneone(tableRegionScene);
            if (tableRegionScenes != null) {
                JSONObject jsonObjectRegion = new JSONObject();
                jsonObjectRegion = (JSONObject) JSONObject.toJSON(tableRegionScenes);
                jsonObjectRegion.remove("id");
                jsonObjectRegion.remove("scene_name");
                jsonObjectRegion.put("scene_name", scene_name);
                JSONObject socketResult1 = null;
                try {
                    socketResult1 = sceneService.socketmodifyRegionScene(jsonObjectRegion, gateway_id, SourceId, 2);
                } catch (Exception e) {
                    logger.error(e);
                }
                if (socketResult1 == null) {
                    message.setCode("-1");
                    message.setMessage("Gateway socket read time out!");
                    message.setContent(new JSONArray());
                    return message;
                }
                String status1 = String.valueOf(socketResult.get("Status"));

                if (status1.equals("1")) {
                    message.setCode("-1");
                    message.setMessage("Sub-gateway retrun status1 '1' means command can't be executed");
                    message.setContent(new JSONArray());
                    return message;
                }
                if (status1.equals("2")) {
                    message.setCode("-1");
                    message.setContent(new JSONArray());
                    message.setMessage("Sub-gateway return status1 '2' means deivce is offline");
                    return message;
                }
                int a = sceneService.updateRegionScene(tableRegionScene);
                if (a < 1) {
                    message.setCode("-1");
                    message.setMessage("RegionScene modification failed");
                    message.setContent(new JSONArray());
                    return message;
                }
            }
            message.setCode("0");
            message.setMessage("Scene successful modification");
            message.setContent(tableScene);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return message;
    }

    //场景查询
    @RequestMapping(value = "/user/scene", method = RequestMethod.GET)
    @ResponseBody
    public Message findOfScene(HttpServletRequest httpServletRequest) {
        Message message = new Message();
        try {
            //获取参数
            JSONObject jsonObject = ParamUtils.getJsonObjectFromRequest(httpServletRequest);
            JSONObject jsonObject1 = (JSONObject) jsonObject.get("scene");
            String user_id = (String) jsonObject1.get("user_id");
            String gateway_id = (String) jsonObject1.get("gateway_id");
            List<UserGateway> list = userService.selectGatewayByUserId(user_id);
            if (list.size() < 1) {
//                message.setCode("-1");
//                message.setMessage("user does not exist");
//                message.setContent(new JSONArray());
//                return message;
                throw new BussinessException("-1","user does not exist");
            }
            String account_id = list.get(0).getAccount_id();
            String SourceId = account_id + Param.WEBAPPID + String.valueOf(System.currentTimeMillis() / 1000000000);
            //实体类
            TableScene tableScene = new TableScene();
            tableScene.setGateway_id(gateway_id);
            tableScene.setAccount_id(account_id);
            List<TableScene> list1 = sceneService.findScene(tableScene);
            if (list1.size() > 0) {
                message.setCode("0");
                message.setMessage("Scene query success");
                JSONArray array = (JSONArray) JSONArray.toJSON(list1);
                message.setContent(array);
            } else if (list1.size() == 0) {
//                message.setCode("-1");
//                message.setMessage("Scene no data");
//                message.setContent(new JSONArray());
                throw new BussinessException("0","Scene no data");
            } else {
//                message.setCode("-1");
//                message.setMessage("Scene query failed");
//                message.setContent(new JSONArray());
                throw new BussinessException("-1","Scene query failed");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return message;
    }

    //场景删除/----删除区域场景
    @RequestMapping(value = "/region/scene/old/old/old", method = RequestMethod.DELETE)
    @ResponseBody
    public Message deleteScene(HttpServletRequest httpServletRequest) {
        Message message = new Message();
        try {
            //参数初始化
            JSONObject jsonObject = ParamUtils.getJsonObjectFromRequest(httpServletRequest);
            JSONArray scenes = (JSONArray) jsonObject.get("table_region_scene");
            JSONObject scene = (JSONObject) scenes.get(0);
            String user_id = (String) jsonObject.get("user_id");

            jsonObject.remove("user_id");
            String scene_guid = (String) scene.get("table_scene_guid");
            String scene_addr = (String) scene.get("scene_addr");
            String gateway_id = (String) scene.get("gateway_id");
            List<UserGateway> list = userService.selectGatewayByUserId(user_id);
            if (list.size() < 1) {
                message.setCode("-1");
                message.setMessage("user does not exist");
                message.setContent(new JSONArray());
                return message;
            }
            String account_id = (String) list.get(0).getAccount_id();
            String SourceId = account_id + Param.WEBAPPID + String.valueOf(System.currentTimeMillis() / 1000000000);
            //实体类TableScene
            TableScene tableScene = new TableScene();
            tableScene.setAccount_id(account_id);
            tableScene.setScene_guid(scene_guid);
            tableScene.setScene_addr(scene_addr);
            tableScene.setGateway_id(gateway_id);
            //

            //查询场景成员
            TableSceneMembers tableSceneMembers = new TableSceneMembers();
            tableSceneMembers.setAccount_id(tableScene.getAccount_id());
            tableSceneMembers.setscene_addr(tableScene.getScene_addr());
            tableSceneMembers.setGateway_id(tableScene.getGateway_id());
            tableSceneMembers.setTable_scene_guid(scene_guid);
            List<TableSceneMembers> membersList = sceneService.selectSceneMembers(tableSceneMembers);
            //System.out.println("场景成员:"+membersList.size());
            if (membersList.size() > 0) {
                for (TableSceneMembers tableSceneMembers01 : membersList) {
                    JSONObject sceneMembers = new JSONObject();
                    JSONArray table_scene_members = new JSONArray();
                    JSONObject tem_scene_members = new JSONObject();
                    tem_scene_members.put("scene_addr", tableScene.getScene_addr());
                    tem_scene_members.put("device_addr", tableSceneMembers01.getDevice_addr());
//                    tem_scene_members.put("gateway_id",tableScene.getGateway_id());
                    table_scene_members.add(tem_scene_members);
                    sceneMembers.put("table_scene_members", table_scene_members);
                    JSONObject resultsceneMembersSocket = null;
                    try {
                        resultsceneMembersSocket = sceneService.socketDeleteScene(sceneMembers, gateway_id, SourceId, 2);
                    } catch (Exception e) {
                        logger.error(e);
                    }
                    if (resultsceneMembersSocket == null) {
                        message.setCode("-1");
                        message.setMessage("Gateway socket read time out!");
                        message.setContent(new JSONArray());
                        return message;
                    }
                    String status = String.valueOf(resultsceneMembersSocket.get("Status"));
                    if (status.equals("1")) {
                        message.setMessage("Sub-gateway retrun status '1' means command can't be executed");
                        message.setCode("-1");
                        message.setContent(new JSONArray());
                        return message;
                    }
                    if (status.equals("2")) {
                        message.setCode("-1");
                        message.setMessage("Sub-gateway return status '2' means deivce is offline");
                        message.setContent(new JSONArray());
                        return message;
                    }
                }

                int b = sceneService.deleteSceneByMembers(tableSceneMembers);
                if (b < 1) {
                    message.setCode("-1");
                    message.setMessage("SceneMembers delete failed");
                    message.setContent(new JSONArray());
                    return message;
                }
            }
            JSONObject socketResult = null;
            try {
                socketResult = sceneService.socketDeleteScene(jsonObject, gateway_id, SourceId, 2);
            } catch (Exception e) {
                e.printStackTrace();
                logger.error(e);
            }
            if (socketResult == null) {
                message.setCode("-1");
                message.setMessage("Gateway socket read time out!");
                message.setContent(new JSONArray());
                return message;
            }
            String status = String.valueOf(socketResult.get("Status"));
            if (status.equals("1")) {
                message.setMessage("Sub-gateway retrun status '1' means command can't be executed");
                message.setCode("-1");
                message.setContent(new JSONArray());
                return message;
            }
            if (status.equals("2")) {
                message.setCode("-1");
                message.setMessage("Sub-gateway return status '2' means deivce is offline");
                message.setContent(new JSONArray());
                return message;
            }
            int n = sceneService.deleteScene(tableScene);
            if (n < 1) {
                message.setCode("-1");
                message.setMessage("Scene delete failed");
                message.setContent(new JSONArray());
                return message;
            }
            //查询区域场景
            TableRegionScene tableRegionScene = new TableRegionScene();
            tableRegionScene.setGateway_id(gateway_id);
            tableRegionScene.setScene_addr(scene_addr);
            tableRegionScene.setAccount_id(account_id);
            tableRegionScene.setTable_scene_guid(scene_guid);
            TableRegionScene tableRegionScene1 = sceneService.findRegionScene(tableRegionScene);
            //删除区域场景
            if (tableRegionScene1 != null) {
                JSONObject regionScenes = new JSONObject();
                JSONArray table_region_scene = new JSONArray();
                JSONObject tem_region_scene = new JSONObject();
                tem_region_scene.put("scene_addr", tableScene.getScene_addr());
                //tem_region_scene.put("account_id", tableScene.getAccount_id());

                table_region_scene.add(tem_region_scene);
                regionScenes.put("table_region_scene", table_region_scene);

                JSONObject resultRegionSceneSocket = null;
                try {
                    resultRegionSceneSocket = sceneService.socketDeleteRegionScene(regionScenes, gateway_id, SourceId, 2);
                } catch (Exception e) {
                    logger.error(e);
                }
                if (resultRegionSceneSocket == null) {
                    message.setCode("-1");
                    message.setMessage("Gateway socket read time out!");
                    message.setContent(new JSONArray());
                    return message;
                }
                String status1 = String.valueOf(socketResult.get("Status"));
                if (status1.equals("1")) {
                    message.setMessage("Sub-gateway retrun status '1' means command can't be executed");
                    message.setCode("-1");
                    message.setContent(new JSONArray());
                    return message;
                }
                if (status1.equals("2")) {
                    message.setCode("-1");
                    message.setMessage("Sub-gateway return status '2' means deivce is offline");
                    message.setContent(new JSONArray());
                    return message;
                }
                int a = sceneService.deleteRegionScene(tableRegionScene);
                if (a < 1) {
                    message.setCode("-1");
                    message.setMessage("RegionScene delete failed");
                    message.setContent(new JSONArray());
                    return message;
                }
            }

            message.setCode("0");
            message.setMessage("Scene delete success");
            message.setContent(new JSONArray());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return message;
    }

    //添加场景成员
    @RequestMapping(value = "/device/scene/scene_members/old/old/old", method = RequestMethod.POST)
    @ResponseBody
    public Message addSceneMembers(HttpServletRequest httpServletRequest) {
        Message message = new Message();
        try {
            //初始化参数
            JSONObject jsonObject = ParamUtils.getJsonObjectFromRequest(httpServletRequest);
            JSONArray scene_memberss = (JSONArray) jsonObject.get("table_scene_members");
            JSONObject scene_member = (JSONObject) scene_memberss.get(0);
            String user_id = (String) jsonObject.get("user_id");
            String scene_guid = (String) scene_member.get("table_scene_guid");
            if (scene_guid.equals(null) || scene_guid.length() <= 0) {
                message.setCode("-1");
                message.setMessage("table_scene_guid cannot be empty");
                message.setContent(new JSONArray());
                return message;
            }
            String scene_addr = (String) scene_member.get("scene_addr");
            if (scene_addr.equals(null) || scene_addr.length() <= 0) {
                message.setCode("-1");
                message.setMessage("scene_addr cannot be empty");
                message.setContent(new JSONArray());
                return message;
            }
            String device_addr = (String) scene_member.get("device_addr");
            if (device_addr.equals(null) || device_addr.length() <= 0) {
                message.setCode("-1");
                message.setMessage("device_addr cannot be empty");
                message.setContent(new JSONArray());
                return message;
            }
            JSONArray device_value = (JSONArray) scene_member.get("device_value");
            if (device_value.equals(null) || device_value.size() <= 0) {
                message.setCode("-1");
                message.setMessage("device_value cannot be empty");
                message.setContent(new JSONArray());
                return message;
            }
            String device_delay = (String) scene_member.get("device_delay");
            if (device_delay.equals(null) || device_delay.length() <= 0) {
                message.setCode("-1");
                message.setMessage("device_delay cannot be empty");
                message.setContent(new JSONArray());
                return message;
            }
            String device_guid = (String) scene_member.get("device_guid");
            if (device_guid.equals(null) || device_guid.length() <= 0) {
                message.setCode("-1");
                message.setMessage("device_guid cannot be empty");
                message.setContent(new JSONArray());
                return message;
            }
            String gateway_id = (String) scene_member.get("gateway_id");
            if (gateway_id.equals(null) || gateway_id.length() <= 0) {
                message.setCode("-1");
                message.setMessage("gateway_id cannot be empty");
                message.setContent(new JSONArray());
                return message;
            }
            String device_name = (String) scene_member.get("device_name");
            if (device_name.equals(null) || device_name.length() <= 0) {
                message.setCode("-1");
                message.setMessage("device_name cannot be empty");
                message.setContent(new JSONArray());
                return message;
            }
            List<UserGateway> list = userService.selectGatewayByUserId(user_id);
            if (list.size() < 1) {
                message.setCode("-1");
                message.setMessage("user does not exist");
                message.setContent(new JSONArray());
                return message;
            }

            String account_id = list.get(0).getAccount_id();
            String SourceId = account_id + Param.WEBAPPID + String.valueOf(System.currentTimeMillis() / 1000000000);
            String id = UUID.randomUUID().toString();
            String scene_members_guid = UUID.randomUUID().toString();
            //
            TableChannel tableChannel=new TableChannel();
            tableChannel.setGateway_id(gateway_id);
            tableChannel.setAccount_id(account_id);
            tableChannel.setTable_device_guid(device_guid);
            //
            TableDevice tableDevice=new TableDevice();
            tableDevice.setDevice_guid(device_guid);
            tableDevice.setGateway_id(gateway_id);
            tableDevice.setAccount_id(account_id);

            List<TableChannel> channelList=sceneService.selectSceneMembersByChannel(tableChannel);
            TableDevice tableDevice1 =sceneService.selectDevice(tableDevice);
            String device_vs=tableDevice1.getDevice_value();

            //System.out.println("channel_number:"+channelList.size());
            if (device_vs.length()/2!=channelList.size()){
                message.setCode("-1");
                message.setMessage("Device value and number of channels are not consistent ");
                message.setContent(new JSONArray());
                return message;
            }
            String[] devicess=new String[channelList.size()];
            //System.out.println("shuzu:"+devicess.length);
            for (int i = 0; i < devicess.length; i++) {
                StringBuilder sb = new StringBuilder();
                sb.append(device_vs);
                System.out.println(sb);
                if (i == (devicess.length - 1)) {
                    //sb.delete(0, i * 2);
                    devicess[i] = sb.toString();
                } else {
                    devicess[i] = sb.substring(i * 2, i * 2 + 2).toString();
                }
            }
            //System.out.println("shuzu11:"+devicess.toString());
            for (int j = 0; j < device_value.size(); j++) {
                JSONObject tem_channel = (JSONObject) device_value.get(j);
                String channel_number = (String) tem_channel.get("channel_number");
                String value = (String)tem_channel.get("value");
                int int_value=Integer.parseInt(value);
                System.out.println(int_value);
                String deviceValues=Integer.toHexString(int_value);
                System.out.println("deviceValues:"+deviceValues);
                int int_channel_number=Integer.parseInt(channel_number);
                devicess[int_channel_number-1]=deviceValues;
            }
            String device_valuess="";
            for (int p=0;p<devicess.length;p++){
                device_valuess+=devicess[p];
            }
            //System.out.println("device_value:"+device_valuess);
            //实体类TableSceneMembers
            TableSceneMembers tableSceneMembers = new TableSceneMembers();
            tableSceneMembers.setGateway_id(gateway_id);
            tableSceneMembers.setscene_addr(scene_addr);
            tableSceneMembers.setAccount_id(account_id);
            tableSceneMembers.setDevice_addr(device_addr);
            tableSceneMembers.setDevice_delay(device_delay);
            tableSceneMembers.setDevice_guid(device_guid);
            tableSceneMembers.setTable_scene_guid(scene_guid);
            tableSceneMembers.setDevice_value(device_valuess);
            tableSceneMembers.setDevice_name(device_name);
            tableSceneMembers.setId(id);
            tableSceneMembers.setScene_members_guid(scene_members_guid);
            TableSceneMembers tableSceneMembers1 = sceneService.selectSceneMemberByDevice(tableSceneMembers);
            if (tableSceneMembers1 != null) {
                message.setMessage("The Device already exists");
                message.setCode("-1");
                message.setContent(new JSONArray());
                return message;
            }
            jsonObject.remove("user_id");
            scene_member.remove("device_value");
            scene_member.put("device_value",device_valuess);
            //System.out.println("jsonObject+:"+jsonObject.toString());
            for (int i = 0; i < scene_memberss.size(); i++) {
                JSONObject temSceneMembers = (JSONObject) scene_memberss.get(i);
                temSceneMembers.put("scene_members_guid", scene_members_guid);
            }
            JSONObject socketResult = null;
            try {
                socketResult = sceneService.socketAddSceneMembers(jsonObject, gateway_id, SourceId, 2);
            } catch (Exception e) {
                logger.error(e);
            }
            if (socketResult == null) {
                message.setCode("-1");
                message.setMessage("Geteway socket read time out!");
                message.setContent(new JSONArray());
                return message;
            }
            String status = String.valueOf(socketResult.get("Status"));
            if (status.equals("1")) {
                message.setCode("-1");
                message.setMessage("Sub-gateway retrun status '1' means command can't be executed");
                message.setContent(new JSONArray());
                return message;
            }
            if (status.equals("2")) {
                message.setCode("-1");
                message.setMessage("Sub-gateway return status '2' means deivce is offline");
                message.setContent(new JSONArray());
                return message;
            }
            int n = sceneService.insertSceneMembers(tableSceneMembers);
            if (n > 0) {
                message.setCode("0");
                message.setMessage("SceneMembers add success");
                message.setContent(jsonObject);
            } else {
                message.setCode("-1");
                message.setMessage("SceneMembers add failed");
                message.setContent(new JSONArray());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return message;
    }

    //删除场景成员
    @RequestMapping(value = "//device/scene/scene_members/old/old/old", method = RequestMethod.DELETE)
    @ResponseBody
    public Message deleteSceneMembers(HttpServletRequest httpServletRequest) {
        Message message = new Message();
        try {
            //初始化参数
            JSONObject jsonObject = ParamUtils.getJsonObjectFromRequest(httpServletRequest);
            JSONArray jsonArray = (JSONArray) jsonObject.get("table_scene_members");
            JSONObject jsonObject1 = (JSONObject) jsonArray.get(0);
            String user_id = (String) jsonObject.get("user_id");
            String scene_members_guid = (String) jsonObject1.get("scene_members_guid");
            String gateway_id = (String) jsonObject1.get("gateway_id");
            String scene_addr = (String) jsonObject1.get("scene_addr");
            String device_addr = (String) jsonObject1.get("device_addr");
            List<UserGateway> list = userService.selectGatewayByUserId(user_id);
            if (list.size() < 1) {
                message.setCode("-1");
                message.setMessage("user does not exist");
                message.setContent(new JSONArray());
                return message;
            }
            String account_id = list.get(0).getAccount_id();
            String SourceId = account_id + Param.WEBAPPID + String.valueOf(System.currentTimeMillis() / 1000000000);
            //实体类
            TableSceneMembers tableSceneMembers = new TableSceneMembers();
            tableSceneMembers.setScene_members_guid(scene_members_guid);
            tableSceneMembers.setGateway_id(gateway_id);
            tableSceneMembers.setAccount_id(account_id);
            tableSceneMembers.setscene_addr(scene_addr);
            tableSceneMembers.setDevice_addr(device_addr);
            jsonObject.remove("user_id");
            JSONObject socketResult = null;
            try {
                socketResult = sceneService.socketDeleteSceneMembers(jsonObject, gateway_id, SourceId, 2);
            } catch (Exception e) {
                logger.error(e);
            }
            if (socketResult == null) {
                message.setCode("-1");
                message.setMessage("");
                message.setContent(new JSONArray());
                return message;
            }
            String status = String.valueOf(socketResult.get("Status"));
            if (status.equals("1")) {
                message.setCode("-1");
                message.setMessage("Sub-gateway retrun status '1' means command can't be executed");
                message.setContent(new JSONArray());
                return message;
            }
            if (status.equals("2")) {
                message.setCode("-1");
                message.setMessage("Sub-gateway return status '2' means deivce is offline");
                message.setContent(new JSONArray());
                return message;
            }
            int n = sceneService.deleteSceneMembers(tableSceneMembers);
            if (n > 0) {
                message.setCode("0");
                message.setMessage("SceneMembers delete success ");
                message.setContent("");
            } else {
                message.setCode("-1");
                message.setMessage("SceneMembers delete failed");
                message.setContent(new JSONArray());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return message;
    }

    //查询场景下的成员
    @RequestMapping(value = "/device/scene/scene_members/old/old/old", method = RequestMethod.GET)
    @ResponseBody
    public Message findSceneMembers(HttpServletRequest httpServletRequest) {
        Message message = new Message();
        try {
            //初始化参数
            JSONObject jsonObject = ParamUtils.getJsonObjectFromRequest(httpServletRequest);
            JSONObject jsonObject1 = (JSONObject) jsonObject.get("scene_members");
            String user_id = (String) jsonObject1.get("user_id");
            String sence_guid = (String) jsonObject1.get("table_scene_guid");
            String gateway_id = (String) jsonObject1.get("gateway_id");
            //String scene_addr = (String) jsonObject1.get("scene_addr");
            List<UserGateway> list = userService.selectGatewayByUserId(user_id);
            if (list.size() < 1) {
                message.setCode("-1");
                message.setMessage("user does not exist");
                message.setContent(new JSONArray());
                return message;
            }
            String account_id = list.get(0).getAccount_id();
            //实体类
            TableSceneMembers tableSceneMembers = new TableSceneMembers();
            tableSceneMembers.setAccount_id(account_id);
            tableSceneMembers.setTable_scene_guid(sence_guid);
            //tableSceneMembers.setscene_addr(scene_addr);
            tableSceneMembers.setGateway_id(gateway_id);
            List<TableSceneMembers> list1 = sceneService.selectSceneMembers(tableSceneMembers);
            JSONArray sceneMembers = (JSONArray) JSONArray.toJSON(list1);
            if (list1.size() > 0) {
                int j = 0;//循环变量
                for (TableSceneMembers tableSceneMembers1 : list1) {
                    TableChannel tableChannel = new TableChannel();
                    tableChannel.setAccount_id(account_id);
                    tableChannel.setGateway_id(gateway_id);
                    tableChannel.setTable_device_guid(tableSceneMembers1.getDevice_guid());
                    String device_value = tableSceneMembers1.getDevice_value();
                    int num = device_value.length() / 2;
                    String[] devicess = null;
                    if (num == 0) {
                        devicess = new String[1];
                    } else {
                        devicess = new String[num];
                    }
                    for (int i = 0; i < devicess.length; i++) {
                        StringBuilder sb = new StringBuilder();
                        sb.append(device_value);
                        if (i == devicess.length - 1) {
                            devicess[i] = sb.toString();
                        } else {
                            devicess[i] = sb.delete(i * 2, i * 2 + 2).toString();
                        }
                    }
                    //查询场景成员通道
                    List<TableChannel> channelList = sceneService.selectSceneMembersByChannel(tableChannel);
//                    System.out.println("channelList:"+channelList.size());
//                    System.out.println("devicess:"+devicess.length);
//                    String pp="";
//                    for (int a=0;a<devicess.length;a++){
//                        pp+=devicess[a];
//                        System.out.println(pp.toString());
//                    }
//                    System.out.println("devicess:"+pp.toString());
                    if (channelList.size() > 0) {
                        //int p=0;//循环变量
                        for (TableChannel tableChannel1 : channelList) {
                            int channel_number = Integer.parseInt(tableChannel1.getChannel_number());
                            String channel_values = devicess[channel_number - 1];
                            int channel_value = Integer.parseInt(channel_values, 16);
                            String channelValue = String.valueOf(channel_value);
                            tableChannel1.setChannel_value(channelValue);
                        }
                    }
                    JSONObject sceneMember = (JSONObject) sceneMembers.get(j);
                    JSONArray sceneMembersChannels = (JSONArray) JSONArray.toJSON(channelList);
                    sceneMember.put("channel", sceneMembersChannels);
                    j++;
                }
            }
            if (list1.size() > 0) {
                message.setCode("0");
                message.setMessage("SceneMembers query success ");
                message.setContent(sceneMembers);
            } else {
                message.setCode("0");
                message.setMessage("SceneMembers query is empty");
                message.setContent(new JSONArray());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return message;
    }

    //修改场景成员
    @RequestMapping(value = "/device/scene/scene_members", method = RequestMethod.PUT)
    @ResponseBody
    public Message updateSceneMembers(HttpServletRequest httpServletRequest) {
        Message message = new Message();
        try {
            //初始化参数
            JSONObject jsonObject = ParamUtils.getAttributess(httpServletRequest);
            JSONArray sceneMemberss = (JSONArray) jsonObject.get("table_scene_members");
            JSONObject sceneMembers = (JSONObject) sceneMemberss.get(0);
            String user_id = (String) jsonObject.get("user_id");
            String gateway_id = (String) sceneMembers.get("gateway_id");
            String scene_members_guid = (String) sceneMembers.get("scene_members_guid");
            String table_scene_guid = (String) sceneMembers.get("table_scene_guid");
            String scene_addr = (String) sceneMembers.get("scene_addr");
            String device_addr = (String) sceneMembers.get("device_addr");
            String device_delay = (String) sceneMembers.get("device_delay");
            String device_guid = (String) sceneMembers.get("device_guid");
            System.out.println(device_guid);
            //设备值,
            JSONArray device_value = (JSONArray) sceneMembers.get("device_value");

            List<UserGateway> list = userService.selectGatewayByUserId(user_id);
            if (list.size() < 1) {
//                message.setCode("-1");
//                message.setMessage("user does not exist");
//                message.setContent(new JSONArray());
//                return message;
                throw new BussinessException("-1","user does not exist");
            }

            String account_id = list.get(0).getAccount_id();
            String SourceId = account_id + Param.WEBAPPID + String.valueOf(System.currentTimeMillis() / 1000000000);
            //设置实体类
            TableSceneMembers tableSceneMembers = new TableSceneMembers();
            tableSceneMembers.setGateway_id(gateway_id);
            tableSceneMembers.setScene_members_guid(scene_members_guid);
            tableSceneMembers.setAccount_id(account_id);
            tableSceneMembers.setscene_addr(scene_addr);
            tableSceneMembers.setTable_scene_guid(table_scene_guid);
            tableSceneMembers.setDevice_addr(device_addr);
            //tableSceneMembers.setDevice_value(device_value);
            tableSceneMembers.setDevice_delay(device_delay);
            tableSceneMembers.setDevice_guid(device_guid);
            //查找成员的值
            TableSceneMembers tableSceneMembers1 = sceneService.selectSceneMemberByDevice(tableSceneMembers);
            if (tableSceneMembers1 == null) {
//                message.setCode("-1");
//                message.setMessage(" scene member does not exist");
//                message.setContent(new JSONArray());
//                return message;
                throw new BussinessException("-1","scene member does not exist");
            }
            String device = tableSceneMembers1.getDevice_value();
            System.out.println("device长度:" + device.length());
            int num = device.length() / 2;
            System.out.println("num:" + num);
            String[] deviceValue = null;
            if (num == 0) {
                deviceValue = new String[1];
            } else {
                deviceValue = new String[num];
            }
            System.out.println("shuzu:" + deviceValue.length);
            for (int i = 0; i < deviceValue.length; i++) {
                StringBuilder sb = new StringBuilder();
                sb.append(device);
                System.out.println(sb);
                if (i == (deviceValue.length - 1)) {
                    sb.delete(0, i * 2);
                    deviceValue[i] = sb.toString();
                } else {
                    deviceValue[i] = sb.substring(i * 2, i * 2 + 2).toString();
                }
            }
//            System.out.println("deviceValue:"+deviceValue[0]);
//            System.out.println("deviceValue长度:"+deviceValue.length);
            //取设备通道跟设备值
            for (int j = 0; j < device_value.size(); j++) {
                JSONObject tempDevice = (JSONObject) device_value.get(j);
                String channelNumber = (String) tempDevice.get("channel_number");
                int channelNumber_int = Integer.parseInt(channelNumber);
                System.out.println(channelNumber_int);
                String value = (String) tempDevice.get("value");
                int value_int = Integer.parseInt(value);
                String value_str = "";
                if (value_int < 16) {
                    value_str = "0" + Integer.toHexString(value_int);
                } else {
                    value_str = Integer.toHexString(value_int);
                }

                deviceValue[channelNumber_int - 1] = value_str;
            }
            String device_values = "";
            for (int k = 0; k < deviceValue.length; k++) {
                device_values += deviceValue[k].toString();
            }
            tableSceneMembers.setDevice_value(device_values);
            jsonObject.remove("user_id");
            for (int i = 0; i < sceneMemberss.size(); i++) {
                JSONObject temSceneMembers = (JSONObject) sceneMemberss.get(i);
                temSceneMembers.remove("device_value");
                temSceneMembers.put("device_value", device_values);
                temSceneMembers.remove("device_addr");
                temSceneMembers.remove("table_scene_guid");
                temSceneMembers.remove("device_guid");
                temSceneMembers.remove("scene_addr");
                temSceneMembers.remove("gateway_id");
            }
            JSONObject socketResult = null;
            try {
                socketResult = sceneService.socketModifySceneMembers(jsonObject, gateway_id, SourceId, 2);
            } catch (Exception e) {
                logger.error(e);
            }
            if (socketResult == null) {
//                message.setCode("-1");
//                message.setMessage("Gateway socket read time out!");
//                message.setContent(new JSONArray());
//                return message;
                throw new BussinessException("-1","Gateway socket read time out!");
            }
            String status = String.valueOf(socketResult.get("Status"));

            if (status.equals("1")) {
//                message.setCode("-1");
//                message.setMessage("Sub-gateway retrun status '1' means command can't be executed");
//                message.setContent(new JSONArray());
//                return message;
                throw new BussinessException("-1","Sub-gateway retrun status '1' means command can't be executed");
            }
            if (status.equals("2")) {
//                message.setCode("-1");
//                message.setContent(new JSONArray());
//                message.setMessage("Sub-gateway return status '2' means deivce is offline");
//                return message;
                throw new BussinessException("-1","Sub-gateway return status '2' means deivce is offline");
            }
            int n = sceneService.updateSceneMembers(tableSceneMembers);
            if (n > 0) {
                message.setCode("0");
                message.setMessage("SceneMembers successful modification");
                message.setContent(tableSceneMembers);
            } else {
//                message.setCode("-1");
//                message.setMessage("SceneMembers modification failed");
//                message.setContent(new JSONArray());
                throw new BussinessException("-1","SceneMembers modification failed");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return message;
    }

    //场景应用
    @RequestMapping(value = "/sence/application", method = RequestMethod.PUT)
    @ResponseBody
    public MessageNoContent sceneApplication(HttpServletRequest httpServletRequest) throws IOException {
        MessageNoContent message = new MessageNoContent();
        JSONObject jsonObject = ParamUtils.getJsonObjectFromRequest(httpServletRequest);
        JSONArray scenes = (JSONArray) jsonObject.get("table_scene");
        JSONObject scene = (JSONObject) scenes.get(0);
        String user_id = (String) jsonObject.get("user_id");
        String gateway_id = (String) scene.get("gateway_id");
        String sence_addr = (String) scene.get("scene_addr");
        String sence_switch = (String) scene.get("scene_switch");
        //String scene_guid=(String) scene.get("sence_guid");
        List<UserGateway> list = userService.selectGatewayByUserId(user_id);
        if (list.size() < 1) {
//            message.setCode("-1");
//            message.setMessage("user does not exist");
//            message.setContent(new JSONArray());
//            return message;
            throw new BussinessException("-1","user does not exist");
        }
        String account_id = list.get(0).getAccount_id();
        String SourceId = account_id + Param.WEBAPPID + String.valueOf(System.currentTimeMillis() / 1000000000);
        TableScene tableScene = new TableScene();
        tableScene.setScene_addr(sence_addr);
        tableScene.setGateway_id(gateway_id);
        tableScene.setAccount_id(account_id);
        tableScene.setScene_switch(sence_switch);
        //tableScene.setScene_guid(scene_guid);
        jsonObject.remove("user_id");
        for (int j = 0; j < scenes.size(); j++) {
            JSONObject temscenes = (JSONObject) scenes.get(j);
            temscenes.remove("gateway_id");
        }
        System.out.println(jsonObject.toString());
        JSONObject socketResult = null;
        try {
            socketResult = sceneService.socketApplication(jsonObject, gateway_id, SourceId, 2);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e);
        }
        if (socketResult == null) {
//            message.setCode("-1");
//            message.setMessage("Gateway socket read time out!");
//            message.setContent(new JSONArray());
//            return message;
            throw new BussinessException("-1","Gateway socket read time out!");
        }
        String status = String.valueOf(socketResult.get("Status"));
        if (status.equals("1")) {
//            message.setCode("-1");
//            message.setMessage("Sub-gateway retrun status '1' means command can't be executed ");
//            message.setContent(new JSONArray());
//            return message;
            throw new BussinessException("-1","Sub-gateway retrun status '1' means command can't be executed");
        }
        if (status.equals("2")) {
//            message.setCode("2");
//            message.setMessage("Sub-gateway return status '2' means deivce is offline");
//            message.setContent(new JSONArray());
//            return message;
            throw new BussinessException("-1","Sub-gateway return status '2' means deivce is offline");
        }
        int n = sceneService.sceneApplication(tableScene);
        if (n < 1) {
//            message.setCode("-1");
//            message.setMessage("Scene Application failed");
//            message.setContent(new JSONArray());
//            return message;
            throw new BussinessException("-1","Scene Application failed");
        }
        message.setCode("0");
        message.setMessage("Scene Application success");

        return message;
    }

}
