package com.iot.newEditionController;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.iot.exception.ParameterException;
import com.iot.message.Message;
import com.iot.message.MessageNoContent;
import com.iot.newEditionService.NewEditionSceneService;
import com.iot.pojo.*;
import com.iot.service.AccountInfoService;
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
import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Created by adminchen on 16/6/16.
 */

@Controller
@RequestMapping()
public class NewEditionSceneController {

    private static Logger logger = Logger.getLogger(NewEditionSceneController.class);

    @Resource
    private UserService userService;

    @Resource
    private AccountInfoService accountInfoService;

    @Resource
    private NewEditionSceneService sceneService;

    //添加场景
    @RequestMapping(value = "/device/scene", method = RequestMethod.POST)
    @ResponseBody
    public Message addScene(HttpServletRequest httpServletRequest) throws IOException {
        Message message = new Message();
        JSONObject jsonObject = ParamUtils.getAttributess(httpServletRequest);
        JSONArray scenes = (JSONArray) jsonObject.get("table_scene");
        JSONObject scene = (JSONObject) scenes.get(0);
        String user_id = (String) jsonObject.get("user_id");
        String gateway_id = (String) scene.get("gateway_id");
        String scene_name = (String) scene.get("scene_name");

        if (user_id==null||user_id.equals("")){
            throw new ParameterException("-1","user_id does not exist");
        }

        if (gateway_id==null||gateway_id.equals("")){
            throw new ParameterException("-1","gateway_id does not exist");
        }

        if (scene_name==null||scene_name.equals("")){
            throw new ParameterException("-1","scene_name does not exist");
        }

        //实体类
        TableScene tableScene=new TableScene();
        tableScene.setGateway_id(gateway_id);
        tableScene.setScene_name(scene_name);
        tableScene.setScene_guid(UUID.randomUUID().toString());
        tableScene.setScene_switch("01");
        tableScene.setId(UUID.randomUUID().toString());

        TableScene addScene = sceneService.insertScene(user_id,tableScene);

        //返回值
        JSONObject object=(JSONObject) JSONObject.toJSON(addScene) ;
        JSONArray jsonArray=new JSONArray();
        jsonArray.add(object);

        message.setCode("0");
        message.setMessage("scene add success");
        message.setContent(jsonArray);
        return message;
//
    }

    //修改场景
    @RequestMapping(value = "/edition/device/scene", method = RequestMethod.PUT)
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
                message.setContent("[]");
                return message;
            }
            String account_id = list.get(0).getAccount_id();
            //String SourceId = account_id + Param.WEBAPPID + String.valueOf(System.currentTimeMillis() / 1000000000);

            //设置实体类TableScene
            TableScene tableScene = new TableScene();
            tableScene.setScene_guid(scene_guid);
            //tableScene.setScene_addr(scene_addr);
            tableScene.setAccount_id(account_id);
            tableScene.setScene_name(scene_name);
            tableScene.setGateway_id(gateway_id);
            jsonObject.remove("user_id");
//            for (int i = 0; i < scenes.size(); i++) {
//                JSONObject temscenes = (JSONObject) scenes.get(i);
//                temscenes.remove("gateway_id");
//                temscenes.remove("scene_addr");
//            }
//            JSONObject socketResult = null;
//            try {
//                socketResult = sceneService.socketmodifyScene(jsonObject, gateway_id, SourceId, 2);
//            } catch (Exception e) {
//                logger.error(e);
//            }
//            if (socketResult == null) {
//                message.setCode("-1");
//                message.setMessage("Gateway socket read time out!");
//                message.setContent(new JSONArray());
//                return message;
//            }
//            String status = String.valueOf(socketResult.get("Status"));
//
//            if (status.equals("1")) {
//                message.setCode("-1");
//                message.setMessage("Sub-gateway retrun status '1' means command can't be executed");
//                message.setContent(new JSONArray());
//                return message;
//            }
//            if (status.equals("2")) {
//                message.setCode("-1");
//                message.setContent(new JSONArray());
//                message.setMessage("Sub-gateway return status '2' means deivce is offline");
//                return message;
//            }
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
//                JSONObject jsonObjectRegion = new JSONObject();
//                jsonObjectRegion = (JSONObject) JSONObject.toJSON(tableRegionScenes);
//                jsonObjectRegion.remove("id");
//                jsonObjectRegion.remove("scene_name");
//                jsonObjectRegion.put("scene_name", scene_name);
//                JSONObject socketResult1 = null;
//                try {
//                    socketResult1 = sceneService.socketmodifyRegionScene(jsonObjectRegion, gateway_id, SourceId, 2);
//                } catch (Exception e) {
//                    logger.error(e);
//                }
//                if (socketResult1 == null) {
//                    message.setCode("-1");
//                    message.setMessage("Gateway socket read time out!");
//                    message.setContent(new JSONArray());
//                    return message;
//                }
//                String status1 = String.valueOf(socketResult.get("Status"));
//
//                if (status1.equals("1")) {
//                    message.setCode("-1");
//                    message.setMessage("Sub-gateway retrun status1 '1' means command can't be executed");
//                    message.setContent(new JSONArray());
//                    return message;
//                }
//                if (status1.equals("2")) {
//                    message.setCode("-1");
//                    message.setContent(new JSONArray());
//                    message.setMessage("Sub-gateway return status1 '2' means deivce is offline");
//                    return message;
//                }
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

    //场景查询(用户下)
    @RequestMapping(value = "/edition/scene", method = RequestMethod.GET)
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
                message.setCode("-1");
                message.setMessage("user does not exist");
                message.setContent("[]");
                return message;
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
                message.setCode("-1");
                message.setMessage("Scene no data");
                message.setContent("[]");
            } else {
                message.setCode("-1");
                message.setMessage("Scene query failed");
                message.setContent("[]");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return message;
    }

    //场景删除/----删除区域场景
    @RequestMapping(value = "/device/scene", method = RequestMethod.DELETE)
    @ResponseBody
    public MessageNoContent deleteScene(HttpServletRequest httpServletRequest) throws IOException {
        JSONObject jsonObject = ParamUtils.getAttributess(httpServletRequest);
        JSONArray sceness=(JSONArray) jsonObject.get("table_scene");
        JSONObject scenes=(JSONObject) sceness.get(0);
        String user_id=(String) jsonObject.get("user_id");
        String gateway_id=(String) scenes.get("gateway_id");
        String scene_addr=(String) scenes.get("scene_addr");
        String scene_guid=(String) scenes.get("scene_guid");

        if (user_id==null||user_id.equals("")){
            throw new ParameterException("-1","user_id does not exist");
        }

        if (gateway_id==null||gateway_id.equals("")){
            throw new ParameterException("-1","gateway_id does not exist");
        }

        if (scene_addr==null||scene_addr.equals("")){
            throw new ParameterException("-1","scene_addr does not exist");
        }

        if (scene_guid==null||scene_guid.equals("")){
            throw new ParameterException("-1","scene_guid does not exist");
        }

        //实体类
        TableScene tableScene=new TableScene();
        tableScene.setScene_addr(scene_addr);
        tableScene.setScene_guid(scene_guid);
        tableScene.setGateway_id(gateway_id);

        sceneService.deleteScene(user_id,tableScene,jsonObject);

        MessageNoContent message=new MessageNoContent();
        message.setCode("0");
        message.setMessage("scene delete successs");
        return message;
    }

    //添加场景成员
    @RequestMapping(value = "/device/scene/scene_members", method = RequestMethod.POST)
    @ResponseBody
    public MessageNoContent addSceneMembers(HttpServletRequest httpServletRequest) throws IOException {

        JSONObject jsonObject = ParamUtils.getAttributess(httpServletRequest);
        JSONArray scene_memberss = (JSONArray) jsonObject.get("table_scene_members");
        JSONObject scene_member = (JSONObject) scene_memberss.get(0);
        String user_id = (String) jsonObject.get("user_id");
        String scene_guid = (String) scene_member.get("table_scene_guid");
        String scene_addr = (String) scene_member.get("scene_addr");
        String device_addr = (String) scene_member.get("device_addr");
        String device_delay = (String) scene_member.get("device_delay");
        String device_guid = (String) scene_member.get("device_guid");
        String gateway_id = (String) scene_member.get("gateway_id");
        String device_name = (String) scene_member.get("device_name");
        JSONArray device_value = (JSONArray) scene_member.get("device_value");

        if (user_id==null||user_id.equals("")){
            throw new ParameterException("-1","user_id does not exist");
        }

        if (scene_guid==null||scene_guid.equals("")){
            throw new ParameterException("-1","scene_guid does not exist");
        }

        if (scene_addr==null||scene_addr.equals("")){
            throw new ParameterException("-1","scene_addr does not exist");
        }

        if (device_addr==null||device_addr.equals("")){
            throw new ParameterException("-1","device_addr does not exist");
        }

        if (device_delay==null||device_delay.equals("")){
            throw new ParameterException("-1","device_delay does not exist");
        }

        if (device_guid==null||device_guid.equals("")){
            throw new ParameterException("-1","device_guid does not exist");
        }

        if (gateway_id==null||gateway_id.equals("")){
            throw new ParameterException("-1","gateway_id does not exist");
        }

        if (device_name==null||device_name.equals("")){
            throw new ParameterException("-1","device_name does not exist");
        }

        if (device_value==null||device_value.equals("")||device_value.size()==0){
            throw new ParameterException("-1","device_value does not exist");
        }

        //实体类
        TableSceneMembers tableSceneMembers=new TableSceneMembers();
        tableSceneMembers.setDevice_guid(device_guid);
        tableSceneMembers.setDevice_name(device_name);
        tableSceneMembers.setDevice_addr(device_addr);
        tableSceneMembers.setDevice_delay(device_delay);
        tableSceneMembers.setscene_addr(scene_addr);
        tableSceneMembers.setTable_scene_guid(scene_guid);
        tableSceneMembers.setGateway_id(gateway_id);
        tableSceneMembers.setId(UUID.randomUUID().toString());
        tableSceneMembers.setScene_members_guid(UUID.randomUUID().toString());

        //添加场景成员
         sceneService.insertSceneMembers(user_id,tableSceneMembers,jsonObject,device_value);

        //返回值
        //JSONObject object=(JSONObject) JSONObject.toJSON(insertSceneMembers);
        MessageNoContent message=new MessageNoContent();
        message.setCode("0");
        message.setMessage("sceneMembers add success");
        return message;

    }

    //删除场景成员
    @RequestMapping(value = "/device/scene/scene_members", method = RequestMethod.DELETE)
    @ResponseBody
    public MessageNoContent deleteSceneMembers(HttpServletRequest httpServletRequest) throws IOException {
        //初始化参数
        JSONObject jsonObject = ParamUtils.getAttributess(httpServletRequest);
        JSONArray jsonArray = (JSONArray) jsonObject.get("table_scene_members");
        JSONObject jsonObject1 = (JSONObject) jsonArray.get(0);
        String user_id = (String) jsonObject.get("user_id");
        String gateway_id = (String) jsonObject1.get("gateway_id");
        String scene_addr = (String) jsonObject1.get("scene_addr");
        String device_addr = (String) jsonObject1.get("device_addr");
        if( user_id == null || user_id.equals("")){
            throw new ParameterException("-1","user_id can not be null");
        }

        if( gateway_id == null || gateway_id.equals("")){
            throw new ParameterException("-1","scene_members_guid can not be null");
        }

        if( scene_addr == null || scene_addr.equals("")){
            throw new ParameterException("-1","scene_addr can not be null");
        }

        if( device_addr == null || device_addr.equals("")){
            throw new ParameterException("-1","device_addr can not be null");
        }

        //实体类
        TableSceneMembers tableSceneMembers = new TableSceneMembers();
        //tableSceneMembers.setScene_members_guid(scene_members_guid);
        tableSceneMembers.setGateway_id(gateway_id);
        //tableSceneMembers.setAccount_id(account_id);
        tableSceneMembers.setscene_addr(scene_addr);
        tableSceneMembers.setDevice_addr(device_addr);

        sceneService.deleteSceneMembers(user_id,tableSceneMembers,jsonObject);

        MessageNoContent message=new MessageNoContent();
        message.setCode("0");
        message.setMessage("SceneMembers delete success");
        return message;


//      try {
//            //初始化参数
//            JSONObject jsonObject;
//            jsonObject = getJsonObjectFromRequest();
//            JSONArray jsonArray = (JSONArray) jsonObject.get("table_scene_members");
//            JSONObject jsonObject1 = (JSONObject) jsonArray.get(0);
//            String user_id = (String) jsonObject.get("user_id");
//            String scene_members_guid = (String) jsonObject1.get("scene_members_guid");
//            String gateway_id = (String) jsonObject1.get("gateway_id");
//            String scene_addr = (String) jsonObject1.get("scene_addr");
//            String device_addr = (String) jsonObject1.get("device_addr");
//            List<UserGateway> list = userService.selectGatewayByUserId(user_id);
//            if (list.size() < 1) {
//                message.setCode("-1");
//                message.setMessage("user does not exist");
//                message.setContent("[]");
//                return message;
//            }
//            String account_id = list.get(0).getAccount_id();
//            String SourceId = account_id + Param.WEBAPPID + String.valueOf(System.currentTimeMillis() / 1000000000);
//            //实体类
//            TableSceneMembers tableSceneMembers = new TableSceneMembers();
//            tableSceneMembers.setScene_members_guid(scene_members_guid);
//            tableSceneMembers.setGateway_id(gateway_id);
//            tableSceneMembers.setAccount_id(account_id);
//            tableSceneMembers.setscene_addr(scene_addr);
//            tableSceneMembers.setDevice_addr(device_addr);
//            jsonObject.remove("user_id");
//            JSONObject socketResult = null;
//            try {
//                socketResult = sceneService.socketDeleteSceneMembers(jsonObject, gateway_id, SourceId, 2);
//            } catch (Exception e) {
//                logger.error(e);
//            }
//            if (socketResult == null) {
//                message.setCode("-1");
//                message.setMessage("");
//                message.setContent(new JSONArray());
//                return message;
//            }
//            String status = String.valueOf(socketResult.get("Status"));
//            if (status.equals("1")) {
//                message.setCode("-1");
//                message.setMessage("Sub-gateway retrun status '1' means command can't be executed");
//                message.setContent(new JSONArray());
//                return message;
//            }
//            if (status.equals("2")) {
//                message.setCode("-1");
//                message.setMessage("Sub-gateway return status '2' means deivce is offline");
//                message.setContent(new JSONArray());
//                return message;
//            }
//            int n = sceneService.deleteSceneMembers(tableSceneMembers);
//            if (n > 0) {
//                message.setCode("0");
//                message.setMessage("SceneMembers delete success ");
//                message.setContent("");
//            } else {
//                message.setCode("-1");
//                message.setMessage("SceneMembers delete failed");
//                message.setContent("[]");
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        //return message;
    }

    //查询场景下的成员
    @RequestMapping(value = "/device/scene/scene_members", method = RequestMethod.GET)
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
                    System.out.println("num:"+num);
                    String[] devicess = null;
                    if (num == 0) {
                        devicess = new String[1];
                    } else {
                        devicess = new String[num];
                    }


                    for (int i = 0; i < devicess.length; i++) {
                        StringBuilder sb = new StringBuilder();

                        sb.append(device_value);
                        if (i==devicess.length - 1) {
                            devicess[i] = sb.delete(0,i*2).toString();

                        } else {
                            devicess[i] = sb.substring(i * 2, i * 2 + 2).toString();
                            System.out.println(devicess[i].toString());
                        }
                    }
                    //查询场景成员通道
                    List<TableChannel> channelList = sceneService.selectSceneMembersByChannel(tableChannel);
                    System.out.println("channelList:"+channelList.size());
                    System.out.println("devicess:"+devicess.length);
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
    @RequestMapping(value = "/edition/device/scene/scene_members", method = RequestMethod.PUT)
    @ResponseBody
    public Message updateSceneMembers(HttpServletRequest httpServletRequest) {
        Message message = new Message();
        try {
            //初始化参数
            JSONObject jsonObject = ParamUtils.getJsonObjectFromRequest(httpServletRequest);
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
                message.setCode("-1");
                message.setMessage("user does not exist");
                message.setContent("[]");
                return message;
            }

            String account_id = list.get(0).getAccount_id();
            //String SourceId = account_id + Param.WEBAPPID + String.valueOf(System.currentTimeMillis() / 1000000000);
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
                message.setCode("-1");
                message.setMessage(" scene member does not exist");
                message.setContent(new JSONArray());
                return message;
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
//            jsonObject.remove("user_id");
//            for (int i = 0; i < sceneMemberss.size(); i++) {
//                JSONObject temSceneMembers = (JSONObject) sceneMemberss.get(i);
//                temSceneMembers.remove("device_value");
//                temSceneMembers.put("device_value", device_values);
//                temSceneMembers.remove("device_addr");
//                temSceneMembers.remove("table_scene_guid");
//                temSceneMembers.remove("device_guid");
//                temSceneMembers.remove("scene_addr");
//                temSceneMembers.remove("gateway_id");
//            }
//            JSONObject socketResult = null;
//            try {
//                socketResult = sceneService.socketModifySceneMembers(jsonObject, gateway_id, SourceId, 2);
//            } catch (Exception e) {
//                logger.error(e);
//            }
//            if (socketResult == null) {
//                message.setCode("-1");
//                message.setMessage("Gateway socket read time out!");
//                message.setContent(new JSONArray());
//                return message;
//            }
//            String status = String.valueOf(socketResult.get("Status"));
//
//            if (status.equals("1")) {
//                message.setCode("-1");
//                message.setMessage("Sub-gateway retrun status '1' means command can't be executed");
//                message.setContent(new JSONArray());
//                return message;
//            }
//            if (status.equals("2")) {
//                message.setCode("-1");
//                message.setContent(new JSONArray());
//                message.setMessage("Sub-gateway return status '2' means deivce is offline");
//                return message;
//            }
            int n = sceneService.updateSceneMembers(tableSceneMembers);
            if (n > 0) {
                message.setCode("0");
                message.setMessage("SceneMembers successful modification");
                message.setContent(tableSceneMembers);
            } else {
                message.setCode("-1");
                message.setMessage("SceneMembers modification failed");
                message.setContent("[]");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return message;
    }

    //场景应用
    @RequestMapping(value = "/edition/sence/application", method = RequestMethod.PUT)
    @ResponseBody
    public Message sceneApplication(HttpServletRequest httpServletRequest) throws IOException {
        Message message = new Message();
        JSONObject jsonObject = ParamUtils.getAttributess(httpServletRequest);
        JSONArray scenes = (JSONArray) jsonObject.get("table_scene");
        JSONObject scene = (JSONObject) scenes.get(0);
        String user_id = (String) jsonObject.get("user_id");
        String gateway_id = (String) scene.get("gateway_id");
        String sence_addr = (String) scene.get("scene_addr");
        String sence_switch = (String) scene.get("scene_switch");
        //String scene_guid=(String) scene.get("sence_guid");
        List<UserGateway> list = userService.selectGatewayByUserId(user_id);
        if (list.size() < 1) {
            message.setCode("-1");
            message.setMessage("user does not exist");
            message.setContent("[]");
            return message;
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
            message.setCode("-1");
            message.setMessage("Gateway socket read time out!");
            message.setContent(new JSONArray());
            return message;
        }
        String status = String.valueOf(socketResult.get("Status"));
        if (status.equals("1")) {
            message.setCode("-1");
            message.setMessage("Sub-gateway retrun status '1' means command can't be executed ");
            message.setContent(new JSONArray());
            return message;
        }
        if (status.equals("2")) {
            message.setCode("2");
            message.setMessage("Sub-gateway return status '2' means deivce is offline");
            message.setContent(new JSONArray());
            return message;
        }
        int n = sceneService.sceneApplication(tableScene);
        if (n < 1) {
            message.setCode("-1");
            message.setMessage("Scene Application failed");
            message.setContent(new JSONArray());
            return message;
        }
        message.setCode("0");
        message.setMessage("Scene Application success");
        message.setContent(tableScene);
        return message;
    }

}
