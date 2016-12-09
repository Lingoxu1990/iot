package com.iot.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.iot.message.Message;
import com.iot.pojo.AccountDataInfo;
import com.iot.pojo.TableRegionScene;
import com.iot.pojo.UserGateway;
import com.iot.service.AccountInfoService;
import com.iot.service.RegionService;
import com.iot.service.SceneService;
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
import java.util.Set;

/**
 * Created by chenweixiang on 16/3/23.
 */
@Controller
@RequestMapping()
public class SceneController {

    private static Logger logger = Logger.getLogger(SceneController.class);

    @Resource
    private SceneService senceService;
    @Resource
    private UserService userService;
    @Resource
    private AccountInfoService accountInfoService;
    @Resource
    private RegionService regionService;

    /**
     * 添加场景
     * <p>
     * 主要逻辑与组的添加相同
     *
     * @return
     */
    @RequestMapping(value = "/device/scene/old",method = RequestMethod.POST)
    @ResponseBody
    public Object addScene(HttpServletRequest httpServletRequest) throws IOException {

        JSONObject jj = ParamUtils.getJsonObjectFromRequest(httpServletRequest);
        System.out.println(jj.toString());
        Message message = new Message();

        String user_id = (String) jj.get("user_id");
        jj.remove("user_id");


        JSONArray sences = (JSONArray) jj.get("table_scene");
        JSONObject sence = (JSONObject) sences.get(0);

        List<UserGateway> tableDeviceList = userService.selectGatewayByUserId(user_id);

        String account_id = tableDeviceList.get(0).getAccount_id();

        String SourceId = account_id + Param.WEBAPPID + String.valueOf(System.currentTimeMillis() / 1000000000);
        String DestinationId = (String) sence.get("gateway_id");


        AccountDataInfo accountDataInfo = new AccountDataInfo();
        accountDataInfo.setAccount_id(account_id);
        accountDataInfo.setGateway_id(DestinationId);

        //获取最后一次更新的区域地址,自增以后写入用户数据表
        AccountDataInfo accountDataInfo1 = accountInfoService.selectLastAddr(accountDataInfo);

        int int_scene_addr = Integer.parseInt(accountDataInfo1.getSence_adde(), 16);


        logger.error("service : device/group"  +" action : add scene" +" \nparam : "+jj.toString()+
                "\nSourceId :"+SourceId+ " DestinationId : "+DestinationId + "scene_addr : ff15::" +(int_scene_addr+1));
        JSONArray groups = (JSONArray) jj.get("table_scene");

        for (int i = 0; i < groups.size(); i++) {

            int_scene_addr += 1;
            String scene_addr = Integer.toHexString(int_scene_addr);
            JSONObject group = (JSONObject) groups.get(i);
            group.put("scene_addr", "ff15::" + scene_addr);
            group.put("scene_switch", "01");
        }

        String scene_addr = Integer.toHexString(int_scene_addr);
        accountDataInfo1.setSence_adde(scene_addr);
        accountDataInfo1.setAccount_id(account_id);
        accountDataInfo1.setGateway_id(DestinationId);

        int n = accountInfoService.updataAddrInfo(accountDataInfo1);

        if (n!=1){
            logger.error("failed to update the accountDataInfo");
            message.setCode("-1");
            message.setMessage("failed to update the sence addr");
            message.setContent(new JSONArray());
            return message;
        }
        message = senceService.addScene(jj, DestinationId, SourceId, 2);

        return message;
    }

    /**
     * 修改场景名称(未开放)
     * <p>
     * 逻辑与组信息修改接口相同;
     *
     * @return
     */
    @RequestMapping(value = "/device/scene/old",method = RequestMethod.PUT)
    @ResponseBody
    public Object modifyScene(HttpServletRequest httpServletRequest) throws IOException {

        JSONObject jj = ParamUtils.getJsonObjectFromRequest(httpServletRequest);

        String user_id = (String) jj.get("user_id");

        List<UserGateway> tableDeviceList = userService.selectGatewayByUserId(user_id);

        String account_id = tableDeviceList.get(0).getAccount_id();

        String SourceId = account_id + Param.WEBAPPID + String.valueOf(System.currentTimeMillis() / 1000000000);
        String DestinationId = tableDeviceList.get(0).getGateway_id();
        jj.remove("user_id");

        Message message = senceService.modifyScene(jj, DestinationId, SourceId, 2);
        if (message.getCode().equals("-1")) {
            return message;
        }
        JSONObject region_scene_act = new JSONObject();
        JSONArray region_group_arry = new JSONArray();

        JSONArray scenes = (JSONArray) jj.get("table_scene");

        for (int i = 0; i < scenes.size(); i++) {
            JSONObject scene = (JSONObject) scenes.get(i);

            Set<String> keys = scene.keySet();
            JSONObject j = new JSONObject();

            for (String key : keys) {
                if (key.equals("scene_guid")) {
                    j.put("table_scene_guid", scene.get(key));
                }
            }
            j.put("account_id", account_id);

            TableRegionScene tableRegionScene = JSONObject.parseObject(j.toString(), TableRegionScene.class);

            JSONObject tableRegionscene = regionService.findRegionSceneByAccountIdAndSceneId(tableRegionScene);

            tableRegionscene.remove("id");
            tableRegionscene.remove("account_id");

            if (scene.containsKey("scene_name")) {
                tableRegionscene.remove("scene_name");
                tableRegionscene.put("scene_name", scene.get("scene_name"));
            }
            if (scene.containsKey("scene_switch")) {
                tableRegionscene.remove("scene_switch");
                tableRegionscene.put("scene_switch", scene.get("scene_switch"));
            }

            region_group_arry.add(tableRegionscene);
        }

        region_scene_act.put("table_region_scene", region_group_arry);

        Message message1 = regionService.modifyRegionScene(region_scene_act, DestinationId, SourceId, 2);

        if (message1.getCode().equals("-1")) {
            return message1;
        }

        Message message2 = new Message();
        message2.setCode("0");
        message2.setContent("");
        message2.setMessage("update the scene successfully!");

        return message2;

    }

    /**
     * 获取用户的所有场景
     * <p>
     * 接口逻辑与获取所有组相同
     *
     * @return
     */
    @RequestMapping(value = "/device/scene",method = RequestMethod.GET)
    @ResponseBody
    public Object findScene(HttpServletRequest httpServletRequest) throws IOException {
        JSONObject jj  = ParamUtils.getJsonObjectFromRequest(httpServletRequest);
        Message message = new Message();

        JSONObject jsonObject = (JSONObject) jj.get("scene");

        String user_id = (String) jsonObject.get("user_id");
        jsonObject.remove("user_id");

        List<UserGateway> tableDeviceList = userService.selectGatewayByUserId(user_id);

        String account_id = tableDeviceList.get(0).getAccount_id();

        String SourceId = account_id + Param.WEBAPPID + String.valueOf(System.currentTimeMillis() / 1000000000);
        String DestinationId = tableDeviceList.get(0).getGateway_id();

        List<AccountDataInfo> tableDeviceList1 = userService.getALLagateway(account_id);

        logger.error("service : device/group"  +" action : add scene" +" \nparam : "+jj.toString()+
                "\nSourceId :"+SourceId+ " Gateway Size : "+tableDeviceList1.size());



        JSONArray RE = new JSONArray();
        JSONObject toFind = new JSONObject();
        JSONObject group = new JSONObject();

        Set<String> keys = jsonObject.keySet();
        for (String key : keys) {
            if (!key.equals("account_id")) {
                group.put(key, jsonObject.get(key));
            }
        }
        toFind.put("table_scene", group);

        for (AccountDataInfo accountDataInfo:tableDeviceList1) {

            String gateway_id = accountDataInfo.getGateway_id();
            logger.error(" DestinationId : "+gateway_id);
            message = senceService.findScene(toFind, gateway_id, SourceId, 2);

            JSONArray jsonArray = (JSONArray) message.getContent();
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONArray JJ = (JSONArray) jsonArray.get(i);
                for (int j = 0; j < JJ.size(); j++) {
                    RE.add(JJ.get(j));
                }
            }
        }
        message.setCode("0");
        message.setMessage("search the scene successfully!");
        message.setContent(RE);


//        JSONObject toFind = new JSONObject();
//        JSONObject group = new JSONObject();
//
//        Set<String> keys = jsonObject.keySet();
//        for (String key : keys) {
//            if (!key.equals("account_id")) {
//                group.put(key, jsonObject.get(key));
//            }
//        }
//
//        toFind.put("table_scene", group);
//        Message message = senceService.findScene(toFind, DestinationId, SourceId, 2);
//        if (message.getCode().equals("-1")) {
//            return message;
//        }
//        JSONArray RE = new JSONArray();
//        JSONArray jsonArray = (JSONArray) message.getContent();
//        for (int i = 0; i < jsonArray.size(); i++) {
//            JSONArray JJ = (JSONArray) jsonArray.get(i);
//            for (int j = 0; j < JJ.size(); j++) {
//                RE.add(JJ.get(j));
//            }
//        }
//        message.setContent(RE);


        return message;
    }

    /**
     * 删除用户的所有场景
     * <p>
     * 接口逻辑与删除用户所有组相同
     *
     * @return
     */
    @RequestMapping(value = "/device/scene/old",method = RequestMethod.DELETE)
    @ResponseBody
    public Object deleteScene(HttpServletRequest httpServletRequest) throws IOException {

        JSONObject jj  = ParamUtils.getJsonObjectFromRequest(httpServletRequest);

        String user_id = (String) jj.get("user_id");

        List<UserGateway> tableDeviceList = userService.selectGatewayByUserId(user_id);

        String account_id = tableDeviceList.get(0).getAccount_id();

        String SourceId = account_id + Param.WEBAPPID + String.valueOf(System.currentTimeMillis() / 1000000000);
        String DestinationId = tableDeviceList.get(0).getGateway_id();

        jj.remove("user_id");

        Message message = senceService.deleteScene(jj, DestinationId, SourceId, 2);

        if (message.getCode().equals("-1")) {
            return message;
        }


        JSONObject region_scene_act = new JSONObject();
        JSONArray region_scene_arry = new JSONArray();

        JSONArray groups = (JSONArray) jj.get("table_scene");

        for (int i = 0; i < groups.size(); i++) {
            JSONObject scene = (JSONObject) groups.get(i);

            Set<String> keys = scene.keySet();
            JSONObject j = new JSONObject();
            for (String key : keys) {
                if (key.equals("scene_guid")) {
                    j.put("table_scene_guid", scene.get(key));
                }
            }
            j.put("account_id", account_id);

            TableRegionScene tableRegionScene = JSONObject.parseObject(j.toString(), TableRegionScene.class);

            JSONObject tableRegionscene = regionService.findRegionSceneByAccountIdAndSceneId(tableRegionScene);

            if (tableRegionscene == null) {
                Message n = new Message();
                n.setCode("0");
                n.setContent("");
                n.setMessage("scene delete successfully! without relation!");
            }

            tableRegionscene.remove("id");
            tableRegionscene.remove("region_guid");
            tableRegionscene.remove("table_scene_guid");
            tableRegionscene.remove("gateway_id");
            tableRegionscene.remove("scene_addr");
            tableRegionscene.remove("scene_name");


            region_scene_arry.add(tableRegionscene);
        }

        region_scene_act.put("table_region_scene", region_scene_arry);
        System.out.println(region_scene_act.toString());


        Message message1 = regionService.deleteRegionScene(region_scene_act, DestinationId, SourceId, 2);


        if (message1.getCode().equals("-1")) {
            return message1;
        }

        Message message2 = new Message();
        message2.setCode("0");
        message2.setContent("");
        message2.setMessage("delete successfully!");
        return message2;
    }

    /**
     * 为场景添加成员
     * <p>
     * 接口逻辑与添加组成员相同,但是入参需要增加一个场景成员的预设值
     *
     * @return
     */
    @RequestMapping(value = "/device/scene/scene_members/old", method = RequestMethod.POST)
    @ResponseBody
    public Object addSceneMember(HttpServletRequest httpServletRequest) throws IOException {
        Message message= new Message();

        JSONObject jj = ParamUtils.getJsonObjectFromRequest(httpServletRequest);

        String user_id = (String) jj.get("user_id");

        List<UserGateway> tableDeviceList = userService.selectGatewayByUserId(user_id);

        String account_id = tableDeviceList.get(0).getAccount_id();

        String SourceId = account_id + Param.WEBAPPID + String.valueOf(System.currentTimeMillis() / 1000000000);

        String DestinationId ="";

        jj.remove("user_id");


        JSONArray jsonArray = (JSONArray) jj.get("table_scene_members");


        for (int i = 0; i <jsonArray.size() ; i++) {
            JSONObject scene_temp = (JSONObject) jsonArray.get(i);
            JSONArray scene_value = (JSONArray) scene_temp.get("device_value");
            DestinationId = (String)scene_temp.get("gateway_id");

            String [] str_scene_value_arr = new String[scene_value.size()];

            if (scene_value.size()>0){

                for (int j = 0; j <scene_value.size() ; j++) {

                    JSONObject scene_channel_values = (JSONObject) scene_value.get(i);

                    String str_channle_num = (String) scene_channel_values.get("channel_number");
                    int index = Integer.parseInt(str_channle_num) - 1;

                    String str_channel_value = (String) scene_channel_values.get("channel_value");
                    int int_channel_value = Integer.parseInt(str_channel_value);

                    String str_hex_channel_value = Integer.toHexString(int_channel_value);

                    str_scene_value_arr[index] = str_hex_channel_value;
                }

            }
            String finnal_value = "";
                for (int j = 0; j <str_scene_value_arr.length ; j++) {
                    System.out.println(str_scene_value_arr[j]);
                    finnal_value+=str_scene_value_arr[j];
                }
            scene_temp.remove("device_value");
            scene_temp.put("device_value",finnal_value);




//            if (scene_value!=null){
//                String device_guid = (String) scene_temp.get("device_guid");
//                String sql = "SELECT * FROM table_device where device_guid = '"+device_guid+"'";
//                String  DestinationID= (String) scene_temp.get("gateway_id");
//                OutPutSocketMessage outPutSocketMessage = new OutPutSocketMessage();
//                outPutSocketMessage.setPackegType(2);
//                outPutSocketMessage.setSourceID(SourceId);
//                outPutSocketMessage.setDestinationID(DestinationID);
//                outPutSocketMessage.setType("NULL");
//                outPutSocketMessage.setMessage("table_scene");
//                outPutSocketMessage.setSql(sql);
//
//                JSONObject result = outPutSocketMessage.sendMessag();
//
//                String status = String.valueOf(result.get("Status")) ;
//
//                if (!"0".equals(status)){
//                    message.setCode("-1");
//                    message.setMessage("Failed to get the device_value!");
//                    message.setContent("[]");
//                    return message;
//                }
//
//                JSONArray devices = (JSONArray) result.get("List");
//                JSONObject device = (JSONObject) devices.get(0);
//                String device_value = (String) device.get("device_value");
//
//
//                int len = device_value.length()/2;
//
//                String[]  device_values=null;
//                if (len==0){
//                    device_values = new String[1];
//                }else {
//                    device_values = new String[len];
//                }
//
//                for (int j = 0; j <device_values.length ; j++) {
//                    StringBuilder sb = new StringBuilder();
//                    sb.append(device_value);
//                    if (j==(device_values.length-1)){
//                        device_values[j]=sb.toString();
//                    }else {
//                        device_values[j]=sb.substring(j*2,j*2+1).toString();
//                    }
//                }
//                String channel_number= (String) scene_value.get("channel_number");
//                int int_channel_number = Integer.parseInt(channel_number);
//                String channel_value= (String) scene_value.get("channel_value");
//                int int_channel_value = Integer.parseInt(channel_value);
//                String str_hex_channel_value  =Integer.toHexString(int_channel_value);
//                device_values[int_channel_number-1]=str_hex_channel_value;
//
//                String finnal_value = "";
//                for (int j = 0; j <device_values.length ; j++) {
//                    finnal_value+=device_values[j];
//                }
//
//                scene_temp.remove("device_value");
//                scene_temp.put("device_value",finnal_value);
//            }

        }
        System.out.println(jj);

        message = senceService.addSceneMember(jj, DestinationId, SourceId, 2);

        return message;
    }

    /**
     * 修改场景添加成员 未开放
     * <p>
     * 接口逻辑与修改用户组逻辑相同
     *
     * @return
     */
    @RequestMapping(value = "/device/scene/scene_members/old", method = RequestMethod.PUT)
    @ResponseBody
    public Object modifySceneMember(HttpServletRequest httpServletRequest) throws IOException {

        JSONObject jj  = ParamUtils.getJsonObjectFromRequest(httpServletRequest);

        String user_id = (String) jj.get("user_id");

        List<UserGateway> tableDeviceList = userService.selectGatewayByUserId(user_id);

        String account_id = tableDeviceList.get(0).getAccount_id();

        String SourceId = account_id + Param.WEBAPPID + String.valueOf(System.currentTimeMillis() / 1000000000);

        String DestinationId = tableDeviceList.get(0).getGateway_id();

        jj.remove("user_id");

        Message message = senceService.modifySceneMembers(jj, DestinationId, SourceId, 2);

        return message;
    }

    /**
     * 查找场景成员
     * <p>
     * 接口逻辑与查找组成员逻辑相同
     *
     * @return
     */
    @RequestMapping(value = "/device/scene/scene_members/old", method = RequestMethod.GET)
    @ResponseBody
    public Object findsceneMember(HttpServletRequest httpServletRequest) throws IOException {
        JSONObject jj = ParamUtils.getJsonObjectFromRequest(httpServletRequest);
        System.out.println(jj.toString());

        JSONObject jsonObject = (JSONObject) jj.get("scene_members");

        String user_id = (String) jsonObject.get("user_id");
        jsonObject.remove("user_id");

        String gateway_id = (String) jsonObject.get("gateway_id");

        List<UserGateway> tableDeviceList = userService.selectGatewayByUserId(user_id);

        String account_id = tableDeviceList.get(0).getAccount_id();

        String SourceId = account_id + Param.WEBAPPID + String.valueOf(System.currentTimeMillis() / 1000000000);
        String DestinationId = gateway_id;

        jj.remove("user_id");

        JSONObject j = new JSONObject();
        j.put("table_scene_members", jsonObject);
        Message message = senceService.findSceneMembers(j, DestinationId, SourceId, 2);


        JSONArray jsonArray = (JSONArray) message.getContent();

        JSONArray reslut = new JSONArray();
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONArray temp = (JSONArray) jsonArray.get(i);
            for (int k = 0; k < temp.size(); k++) {
                reslut.add(temp.get(k));
            }
        }


        message.setContent(reslut);

        return message;
    }

    /**
     * 删除场景成员
     * <p>
     * 接口逻辑删除组成员一致
     *
     * @return
     */
    @RequestMapping(value = "/device/scene/scene_members/old", method = RequestMethod.DELETE)
    @ResponseBody
    public Object deleteSceneMember(HttpServletRequest httpServletRequest) throws IOException {
        JSONObject jj = ParamUtils.getJsonObjectFromRequest(httpServletRequest);
        System.out.println(jj.toString());

        String user_id = (String) jj.get("user_id");

        List<UserGateway> tableDeviceList = userService.selectGatewayByUserId(user_id);
        System.out.println(tableDeviceList.size());

        String account_id = tableDeviceList.get(0).getAccount_id();

        String SourceId = account_id + Param.WEBAPPID + String.valueOf(System.currentTimeMillis() / 1000000000);

        jj.remove("user_id");

        JSONArray scene_membsers = (JSONArray) jj.get("table_scene_members");
        JSONObject scene_member = (JSONObject) scene_membsers.get(0);
        String gateway_id = (String) scene_member.get("gateway_id");
        scene_member.remove("gateway_id");

        Message message = senceService.deleteSceneMember(jj, gateway_id, SourceId, 2);
        return message;
    }

    /**
     * 场景控制接口
     * <p>
     * 接口主要逻辑
     * <p>
     * 1 根据user_id获取account_id,生成目标id
     * <p>
     * 2 用过socket发送控制语句
     *
     * @return
     */
    @RequestMapping(value = "/device/scene/control", method = RequestMethod.PUT)
    @ResponseBody
    public Object controlScene(HttpServletRequest httpServletRequest) throws IOException {
        JSONObject jj = ParamUtils.getAttributess(httpServletRequest);

        String user_id = (String) jj.get("user_id");
        jj.remove("user_id");

        List<UserGateway> tableDeviceList = userService.selectGatewayByUserId(user_id);

        String account_id = tableDeviceList.get(0).getAccount_id();

        String SourceId = account_id + Param.WEBAPPID + String.valueOf(System.currentTimeMillis() / 1000000000);
        String DestinationId = tableDeviceList.get(0).getGateway_id();

        JSONArray jsonArray = (JSONArray) jj.get("table_scene");



        JSONObject json = (JSONObject) jsonArray.get(0);

        String gateway_id = (String) json.get("gateway_id");
        json.remove("gateway_id");

        Message message = senceService.controlScene(jj, gateway_id, SourceId, 2);


        return message;

    }


}
