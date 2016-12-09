package com.iot.newController;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.iot.exception.BussinessException;
import com.iot.exception.ParameterException;
import com.iot.message.Message;
import com.iot.message.MessageNoContent;
import com.iot.newService.*;
import com.iot.pojo.*;
import com.iot.service.AccountInfoService;
import com.iot.service.SensorDataService;
import com.iot.service.UserService;
import com.iot.spitUtil.Param;
import com.iot.utils.ParamUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Created by adminchen on 16/6/1.
 */


@Controller
@RequestMapping()
public class NewRegionController {
    private static Logger logger = Logger.getLogger(NewRegionController.class);

    @Resource
    private UserService userService;

    @Resource
    private AccountInfoService accountInfoService;
    @Resource
    private NewRegionDeviceService newRegionDeviceService;
    @Resource
    private NewDeviceService newDeviceService;
    @Resource
    private SensorDataService sensorDataService;
    @Resource
    private NewGroupService newGroupService;
    @Resource
    private NewSceneService newSceneService;
    @Resource
    private NewRegionService newRegionService;

    private JSONObject jsonObject;

    @ModelAttribute  //@ModelAttribute 绑定请求参数到命令对象
    public synchronized void paramInit(HttpServletRequest request) {
        String method = request.getMethod();
        if ("GET".equals(method)) {
            String Query = request.getQueryString();//获取查询字符串
            //System.out.println("query"+Query);
            //String url=request.getRequestURL().toString();
            //System.out.println(url);
            String uri = request.getRequestURI();
            String[] tablenames = uri.split("/");

            String tablename = tablenames[tablenames.length - 1];

            String[] queres = Query.split("&");
            JSONObject jsonObject = new JSONObject();
            //System.out.println("queres"+queres.length);
            for (int i = 0; i < queres.length; i++) {
                System.out.println(queres[i]);
                String[] kv = queres[i].split("=");


                //System.out.println(kv[1]);
                if (!kv[0].equals("appid")) {
                    if (kv.length==1){
                        String[] kv1={kv[0],""};
                        jsonObject.put(kv1[0], kv1[1]);
                    }else {
                        jsonObject.put(kv[0], kv[1]);
                    }
                }
            }
            JSONObject result = new JSONObject();
            result.put(tablename, jsonObject);
            this.jsonObject = result;
        } else {

            BufferedReader in = null;
            try {
                in = new BufferedReader(new InputStreamReader(request.getInputStream(), "utf-8"));
                String line = null;
                String empJson = "";
                while ((line = in.readLine()) != null) {
                    empJson += line;
                }
                JSONObject result = (JSONObject) JSONObject.parse(empJson);//转换
                this.jsonObject = result;//获取值
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public synchronized JSONObject deeppCopy() {
        JSONObject result;
        result = (JSONObject) jsonObject.clone();
        return result;
    }

    //区域添加//去掉双向写入
    @RequestMapping(value = "/region/old/old/old", method = RequestMethod.POST)
    @ResponseBody
    public Object AddRegion() {
        Message message = new Message();
        try {

            JSONObject jsonObject;
            jsonObject = deeppCopy();
            String user_id = (String) jsonObject.get("user_id");
            JSONArray jsonRegions = (JSONArray) jsonObject.get("table_region");
            JSONObject jsonRegion = (JSONObject) jsonRegions.get(0);
            //获取account_id
            List<UserGateway> tableDeviceList = userService.selectGatewayByUserId(user_id);

            if (tableDeviceList.size()<1){
                message.setCode("-1");
                message.setMessage("The user does not exist");
                message.setContent("[]");
                return message;
            }
            String account_id = tableDeviceList.get(0).getAccount_id();
            String SourceId = account_id + Param.WEBAPPID + String.valueOf(System.currentTimeMillis() / 1000000000);
            String gateway_Id = (String) jsonRegion.get("gateway_id");
            String region_name = (String) jsonRegion.get("region_name");
            String region_Id =  UUID.randomUUID().toString();
            String region_Guid =  UUID.randomUUID().toString();
            jsonRegion.put("region_guid",region_Guid);

            //获取最后一次跟新的区域地址,自增
            AccountDataInfo accountDataInfo = new AccountDataInfo();
            accountDataInfo.setAccount_id(account_id);
            accountDataInfo.setGateway_id(gateway_Id);
            AccountDataInfo accountDataInfo1 = accountInfoService.selectLastAddr(accountDataInfo);
            if (accountDataInfo1==null){
                message.setCode("-1");
                message.setMessage("Failed to obtain user information");
                message.setContent(new JSONArray());
                return message;
            }
            int regionaddr = Integer.parseInt(accountDataInfo1.getRegion_addr(), 16) + 1;
            String region_addr = Integer.toHexString(regionaddr);
            jsonRegion.put("region_addr","ff15::" + region_addr);
            jsonRegion.put("region_value","null");
            jsonRegion.put("region_switch","00");
            jsonRegion.put("region_delay","1");
            jsonObject.remove("user_id");

            JSONObject  socketResult = newRegionService.socketAddRegion(jsonObject,gateway_Id,SourceId,2);

            String socketResultStatus = String.valueOf(socketResult.get("Status"));

            if (socketResultStatus.equals("1")){
                message.setCode("-1");
                message.setMessage("Failed to  write data into the sub-gateway");
                message.setContent(new JSONArray());
                logger.error("Failed to  write data into the sub-gateway"+socketResult.toString());
                return message;
            }

            //int跟新accoutDateInfo表

            accountDataInfo.setRegion_addr(region_addr);
            //实体类
            TableRegion tableRegion = new TableRegion();
            tableRegion.setId(region_Id);
            tableRegion.setRegion_guid(region_Guid);
            tableRegion.setAccount_id(account_id);
            tableRegion.setGateway_id(gateway_Id);
            tableRegion.setRegion_name(region_name);
            tableRegion.setRegion_addr("ff15::" + region_addr);
            tableRegion.setRegion_value("null");
            tableRegion.setRegion_switch("00");
            tableRegion.setRegion_delay("1");
            //执行添加次数
            int n = newRegionService.addRegion(tableRegion);
            if (n<1){
                message.setCode("-1");
                message.setMessage("can't write data into the server database");
                message.setContent(new JSONArray());
                return message;
            }
            int a=accountInfoService.updataAddrInfo(accountDataInfo);
            if (a<1){
                message.setCode("-1");
                message.setMessage("Update user area address failed");
                message.setContent(new JSONArray());
            }
            if (n >= 1) {
                JSONObject regionAdd=(JSONObject)JSONObject.toJSON(tableRegion);
                message.setCode("0");
                message.setMessage("add Region successfully!");
                message.setContent(regionAdd);
            } else {
                message.setCode("-1");
                message.setMessage("add Region failed!");
                message.setContent(new JSONArray());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return message;
    }

    //区域删除
    @RequestMapping(value = "/region/old/old/old", method = RequestMethod.DELETE)
    @ResponseBody
    public Message DeleteRegion() {
        Message message = new Message();
        JSONObject jsonObject;
        jsonObject = deeppCopy();
        String user_id = (String) jsonObject.get("user_id");
        jsonObject.remove("user_id");


        JSONArray jsonArray = (JSONArray) jsonObject.get("table_region");
        JSONObject j_Region = (JSONObject) jsonArray.get(0);
        //获取account_id
        List<UserGateway> gatewayList = userService.selectGatewayByUserId(user_id);
        if (gatewayList.isEmpty()) {
            message.setCode("-1");
            message.setMessage("user does not exist");
            message.setContent(new JSONArray());
            return message;
        }
        String account_id =  gatewayList.get(0).getAccount_id();

        String SourceId = account_id + Param.WEBAPPID + String.valueOf(System.currentTimeMillis() / 1000000000);
        String gateway_id = (String) j_Region.get("gateway_id");
        String region_addr = (String) j_Region.get("region_addr");
        String region_guid = (String) j_Region.get("region_guid");

        j_Region.remove("gateway_id");
        TableRegion tableRegion = new TableRegion();
        tableRegion.setAccount_id(account_id);
        tableRegion.setRegion_addr(region_addr);
        tableRegion.setGateway_id(gateway_id);
        tableRegion.setRegion_guid(region_guid);
//
//        //查找出区域下的设备列表
//        List<TableRegionDevice> distRegionDevices = newRegionService.findDistRegionDevice(tableRegion);
//        //查找出区域下的组列表
//        TableRegionGroup tableRegionGroup = new TableRegionGroup();
//        tableRegionGroup.setAccount_id(account_id);
//        tableRegionGroup.setRegion_guid(region_guid);
//        tableRegionGroup.setGateway_id(gateway_id);
//        List<TableRegionGroup> regionGroups = newRegionService.findRegionGroup(tableRegionGroup);
//
//        //查找出组下的所有成员
//        List<TableGroupMembers> groupMembersList = new ArrayList<TableGroupMembers>();
//
//        for (TableRegionGroup regionGroup: regionGroups) {
//
//            TableGroupMembers groupMember = new TableGroupMembers();
//            groupMember.setTable_group_guid(regionGroup.getTable_group_guid());
//            groupMember.setAccount_id(account_id);
//            groupMember.setGateway_id(gateway_id);
//            List<TableGroupMembers> tempList = newGroupService.findGroupMember(groupMember);
//
//            for (TableGroupMembers tempGroupMember: tempList) {
//                groupMembersList.add(tempGroupMember);
//            }
//        }
//
//
//        //查找出区域下的场景列表
//        TableRegionScene tableRegionScene  = new TableRegionScene();
//        tableRegionScene.setGateway_id(gateway_id);
//        tableRegionScene.setAccount_id(account_id);
//        tableRegionScene.setRegion_guid(region_guid);
//        List<TableRegionScene> regionScenes = newRegionService.findRegionScene(tableRegionScene);
//
//
//        //查找出区域下的场景成员
//        List<TableSceneMembers> sceneMembersList = new ArrayList<TableSceneMembers>();
//
//        for (TableRegionScene regionScene: regionScenes) {
//
//            TableSceneMembers tableSceneMembers = new TableSceneMembers();
//            tableSceneMembers.setGateway_id(gateway_id);
//            tableSceneMembers.setAccount_id(account_id);
//            tableSceneMembers.setTable_scene_guid(regionScene.getTable_scene_guid());
//            List<TableSceneMembers> tempList =newSceneService.selectSceneMembers(tableSceneMembers);
//
//            for (TableSceneMembers tempSceneMember: tempList) {
//                sceneMembersList.add(tempSceneMember);
//            }
//
//        }

        JSONObject socketRegiondelete = newRegionService.socketDeleteRegion(jsonObject,gateway_id,SourceId,2);

        String status = String.valueOf(socketRegiondelete.get("Status"));

        if ("1".equals(status)){
            message.setCode("-1");
            message.setMessage("Sub gateway failed to delete the region!");
            message.setContent(new JSONArray());
            return message;
        }
        if ("2".equals(status)){
            message.setCode("-1");
            message.setMessage("Sub gateway failed to delete the region because of the region if off-line");
            message.setContent(new JSONArray());
            return message;
        }

        //后续使用
        //List list = newRegionService.findTheDeleteDevice(tableRegion);
        //i=-3删除区域设备失败;-4删除区域组失败;-5删除区域场景失败;;-6删除组成员;-7删除场景成员
        int i = newRegionService.deleteRegion(tableRegion);
        if (i==-3){
            message.setCode("-1");
            message.setMessage("RegionDevice delete failed");
            message.setContent(new JSONArray());
            return message;
        }
        if (i==-4){
            message.setCode("-1");
            message.setMessage("RegionGroup delete failed");
            message.setContent(new JSONArray());
            return message;
        }
        if (i==-5){
            message.setCode("-1");
            message.setMessage("RegionScene delete failed");
            message.setContent(new JSONArray());
            return message;
        }
        if (i==-6){
            message.setCode("-1");
            message.setMessage("GroupMembers delete failed");
            message.setContent(new JSONArray());
            return message;
        }
        if (i==-7){
            message.setCode("-1");
            message.setMessage("SceneMembers delete failed");
            message.setContent(new JSONArray());
            return message;
        }
        if (i > 0) {
            message.setCode("0");
            message.setMessage("Region delete success");
            message.setContent(new JSONArray());

        } else {
            message.setCode("-1");
            message.setMessage("Region delete failed");
            message.setContent(new JSONArray());
        }
        return message;
    }

    //区域修改
    @RequestMapping(value = "/new/region", method = RequestMethod.PUT)
    @ResponseBody
    public Object ModifyRegion() {
        Message message = new Message();
        try {
            JSONObject jsonObject;
            jsonObject = deeppCopy();
            JSONArray jsonArray = (JSONArray) jsonObject.get("table_region");
            JSONObject j_ragion = (JSONObject) jsonArray.get(0);
            String user_id = (String) jsonObject.get("user_id");
            //String id = (String) j_ragion.get("id");
            List<UserGateway> tableDeviceList = userService.selectGatewayByUserId(user_id);
            if (tableDeviceList.size()<1){
                message.setCode("-1");
                message.setMessage("user does not exist");
                message.setContent(new JSONArray());
                return message;
            }
            String account_id = tableDeviceList.get(0).getAccount_id();
            //System.out.println(user_id);
            String region_guid = j_ragion.get("region_guid").toString();
            String region_name = j_ragion.get("region_name").toString();

            String gateway_id = j_ragion.get("gateway_id").toString();

            TableRegion tableRegion = new TableRegion();
            tableRegion.setGateway_id(gateway_id);
            tableRegion.setRegion_guid(region_guid);
            tableRegion.setAccount_id(account_id);
            tableRegion.setRegion_name(region_name);


            int n = newRegionService.modify(tableRegion);
            if (n >= 1) {
                message.setCode("0");
                message.setContent("");
                message.setMessage("Modify Region success");
            } else {
                message.setCode("-1");
                message.setMessage("Failed to modify Region");
            }

            return message;
        } catch (Exception e) {
            e.printStackTrace();
            return "NullPointerException";
        }

    }

    //查找区域
    @RequestMapping(value = "/region", method = RequestMethod.GET)
    @ResponseBody
    public Object FindRegion(HttpServletRequest httpServletRequest) throws IOException {

        Message message = new Message();

        JSONObject jsonObject = ParamUtils.getJsonObjectFromRequest(httpServletRequest);

        JSONObject entity = (JSONObject) jsonObject.get("region");

        String user_id = (String) entity.get("user_id");
        //String acount_id=(String)  entity.get("account_id");
        List<UserGateway> getewayList = userService.selectGatewayByUserId(user_id);
        if (getewayList.isEmpty()) {

            throw new BussinessException("-1","user does not exist");
        }
        String acount_id = getewayList.get(0).getAccount_id();
        String SourceId = acount_id + Param.WEBAPPID + String.valueOf(System.currentTimeMillis() / 1000000000);

        TableRegion tableRegion = new TableRegion();
        tableRegion.setAccount_id(acount_id);

        List<TableRegion> list = newRegionService.findAllRegion(tableRegion);

        if (list.size() < 1) {

            throw new BussinessException("0","Search the region empty!");
        }

        //查询该区域是否异常


        JSONArray result = new JSONArray();

        for (int i = 0; i < list.size(); i++) {
            result.add(JSONObject.toJSON(list.get(i)));
        }

        if ("00000000".equals(acount_id)){

            throw new BussinessException("0","Search the region successfully!");

        }

        message.setCode("0");
        message.setMessage("Search the region successfully!");
        message.setContent(result);

        return message;
    }


    //添加区域场景
    @RequestMapping(value = "/region/scene/old/old/old", method = RequestMethod.POST)
    @ResponseBody
    public Message addRegionScene() {
        Message message = new Message();
        // 参数实例化
        JSONObject jsonObject;
        jsonObject = deeppCopy();
        JSONArray regionScenes = (JSONArray) jsonObject.get("table_region_scene");
        JSONObject regionScene = (JSONObject) regionScenes.get(0);
        String user_id = (String) jsonObject.get("user_id");
        String gateway_id = (String) regionScene.get("gateway_id");
        if (gateway_id.equals(null)||gateway_id.length()<=0){
            message.setCode("-1");
            message.setMessage("gateway_id cannot be empty");
            message.setContent(new JSONArray());
            return message;
        }
        String table_scene_guid = (String) regionScene.get("table_scene_guid");
        if (table_scene_guid.equals(null)||table_scene_guid.length()<=0){
            message.setCode("-1");
            message.setMessage("table_scene_guid cannot be empty");
            message.setContent(new JSONArray());
            return message;
        }
        String scene_addr = (String) regionScene.get("scene_addr");
        if (scene_addr.equals(null)||scene_addr.length()<=0){
            message.setCode("-1");
            message.setMessage("scene_addr cannot be empty");
            message.setContent(new JSONArray());
            return message;
        }
        String scene_name = (String) regionScene.get("scene_name");
        if (scene_name.equals(null)||scene_name.length()<=0){
            message.setCode("-1");
            message.setMessage("scene_name cannot be empty");
            message.setContent(new JSONArray());
            return message;
        }
        String region_guid = (String) regionScene.get("region_guid");
        if (region_guid.equals(null)||region_guid.length()<=0){
            message.setCode("-1");
            message.setMessage("region_guid cannot be empty");
            message.setContent(new JSONArray());
            return message;
        }
        List<UserGateway> list = userService.selectGatewayByUserId(user_id);
        if (list.size()<1){
            message.setCode("-1");
            message.setMessage("user does not exist");
            message.setContent(new JSONArray());
            return message;
        }
        String account_id = list.get(0).getAccount_id();
        String region_scene_guide=UUID.randomUUID().toString();
        jsonObject.remove("user_id");
        String SourceId=account_id+Param.WEBAPPID+String.valueOf((System.currentTimeMillis()/1000000000));
        for (int i=0;i<regionScenes.size();i++){
            JSONObject temRegionScene=(JSONObject) regionScenes.get(i);
            temRegionScene.put("region_scene_guid",region_scene_guide);
        }

        //实体类
        TableRegionScene tableRegionScene = new TableRegionScene();
        tableRegionScene.setAccount_id(account_id);
        tableRegionScene.setGateway_id(gateway_id);
        tableRegionScene.setTable_scene_guid(table_scene_guid);
        tableRegionScene.setScene_addr(scene_addr);
        tableRegionScene.setScene_name(scene_name);
        tableRegionScene.setRegion_guid(region_guid);
        tableRegionScene.setRegion_scene_guid(region_scene_guide);
        tableRegionScene.setId(UUID.randomUUID().toString());
        TableRegionScene tableRegionScene1=newRegionService.findRegionSceneOne(tableRegionScene);
        if (tableRegionScene1!=null){
            message.setCode("-1");
            message.setMessage("The scene already exists");
            message.setContent(new JSONArray());
            return message;
        }
        JSONObject socketResult=null;
        try{
            socketResult=newRegionService.socketAddRegionScene(jsonObject,gateway_id,SourceId,2);
        }catch (Exception e){
            logger.error(e);
        }
        if (socketResult==null){
            message.setCode("-1");
            message.setMessage("Gteway socket read time out!");
            message.setContent(new JSONArray());
            return message;
        }
        String status=String.valueOf(socketResult.get("Status"));
        if (status.equals("1")){
            message.setCode("-1");
            message.setMessage("Sub-gateway retrun status '1' means command can't be executed");
            message.setContent(new JSONArray());
            return message;
        }
        if (status.equals("2")){
            message.setCode("-1");
            message.setMessage("Sub-gateway return status '2' means deivce is offline");
            message.setContent(new JSONArray());
            return message;
        }
        int n = newRegionService.addRegionScene(tableRegionScene);
        if (n > 0) {
            message.setCode("0");
            message.setMessage("RegionScene add success");
            message.setContent("");
        } else {
            message.setCode("-1");
            message.setMessage("RegionScene add failed");
            message.setContent(new JSONArray());
        }
        return message;
    }

    //删除区域场景
    @RequestMapping(value = "/new/region/scene/old/old/old", method = RequestMethod.DELETE)
    @ResponseBody
    public Message deleteRegionScene() {
        Message message = new Message();
        try {
            //参数初始化
            JSONObject jsonObject;
            jsonObject = deeppCopy();
            JSONArray jsonArray = (JSONArray) jsonObject.get("table_region_scene");
            JSONObject jsonObject1 = (JSONObject) jsonArray.get(0);
            String user_id = (String) jsonObject.get("user_id");
            String gateway_id = (String) jsonObject1.get("gateway_id");
            String scene_addr = (String) jsonObject1.get("scene_addr");
            String table_scene_guid= (String) jsonObject1.get("table_scene_guid");
            //System.out.println(table_scene_guid);
            List<UserGateway> list = userService.selectGatewayByUserId(user_id);
            if (list.size()<1){
                message.setCode("-1");
                message.setMessage("user does not exist");
                message.setContent(new JSONArray());
                return message;
            }
            String account_id = list.get(0).getAccount_id();
            //实体类
            TableRegionScene tableRegionScene = new TableRegionScene();
            tableRegionScene.setTable_scene_guid(table_scene_guid);
            tableRegionScene.setScene_addr(scene_addr);
            tableRegionScene.setGateway_id(gateway_id);
            tableRegionScene.setAccount_id(account_id);
            int n = newRegionService.deleteRegionScene(tableRegionScene);
            if (n==-1){
                message.setCode("-1");
                message.setMessage("SceneMembers delete failed");
                message.setContent(new JSONArray());
            }
            if (n > 0) {
                message.setCode("0");
                message.setMessage("RegionScene delete success");
                message.setContent("");
            } else {
                message.setCode("-1");
                message.setMessage("RegionScene delete failed");
                message.setContent(new JSONArray());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return message;
    }

    //查询区域场景//// TODO: 16/9/2  
    @RequestMapping(value = "/region/scene", method = RequestMethod.GET)
    @ResponseBody
    public Message findRegionScene() {
        Message message = new Message();
        try {
            //参数初始化
            JSONObject jsonObject;
            jsonObject = deeppCopy();
            JSONObject jsonObject1 = (JSONObject) jsonObject.get("scene");
            String user_id = (String) jsonObject1.get("user_id");
            String gateway_id = (String) jsonObject1.get("gateway_id");
            String region_guid = (String) jsonObject1.get("region_guid");
            //String region_addr=(String)jsonObject1.get("region_addr");
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

    //添加区域组
    @RequestMapping(value = "/region/group/old/old/old", method = RequestMethod.POST)
    @ResponseBody
    public Message addRegionGroup() {
        Message message = new Message();
        try {
            JSONObject jsonObject;
            jsonObject = deeppCopy();
            JSONArray regionGroups = (JSONArray) jsonObject.get("table_region_group");
            JSONObject regionGroup = (JSONObject) regionGroups.get(0);
            String user_id = (String) jsonObject.get("user_id");
            jsonObject.remove("user_id");

            String region_group_guid = UUID.randomUUID().toString();
            String regionGuid = (String) regionGroup.get("region_guid");
            String groupGuid = (String) regionGroup.get("table_group_guid");
            String groupAddr = (String) regionGroup.get("group_addr");
            String groupName = (String) regionGroup.get("group_name");
            String gatewayId = (String) regionGroup.get("gateway_id");
            regionGroup.put("region_group_guid",region_group_guid);


            List<UserGateway> list = userService.selectGatewayByUserId(user_id);

            String account_id = list.get(0).getAccount_id();

            String SourceId = account_id + Param.WEBAPPID + String.valueOf(System.currentTimeMillis() / 1000000000);

            JSONObject socketResult = newRegionService.socketAddRegionGroup(jsonObject,gatewayId,SourceId,2);

            String status = String.valueOf(socketResult.get("Status"));

            if (status.equals("1")) {

                message.setCode("-1");
                message.setMessage("the sub-gateway return the status '1' means failed to add the group into the region");
                message.setContent(new JSONArray());
                return message;

            }
            if (status.equals("2")) {
                message.setCode("-1");
                message.setMessage("the sub-gateway return the status '2' mesns the group is off line");
                message.setContent(new JSONArray());
                return message;
            }


            //实体类
            TableRegionGroup tableRegionGroup = new TableRegionGroup();
            tableRegionGroup.setRegion_guid(regionGuid);
            tableRegionGroup.setTable_group_guid(groupGuid);
            tableRegionGroup.setGroup_addr(groupAddr);
            tableRegionGroup.setGroup_name(groupName);
            tableRegionGroup.setAccount_id(account_id);
            tableRegionGroup.setId(UUID.randomUUID().toString());
            tableRegionGroup.setRegion_group_guid(region_group_guid);
            tableRegionGroup.setGateway_id(gatewayId);
            System.out.println(tableRegionGroup.toString());
            int n = newRegionService.addRegionGroup(tableRegionGroup);

            if (n > 0) {
                message.setCode("0");
                message.setMessage("The group has been added into the region successfully!");
                message.setContent(tableRegionGroup);
            } else {
                message.setCode("-1");
                message.setMessage("Failed to add the group into the region because of system failed to insert data into the cloud database ");
                message.setContent(new JSONArray());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return message;
    }

    //删除区域组
    @RequestMapping(value = "/region/group/old/old/old", method = RequestMethod.DELETE)
    @ResponseBody
    public Message deleteRegionGroup() {
        Message message = new Message();
        try {
            //获取参数
            JSONObject jsonObject;
            jsonObject = deeppCopy();
            String user_id = (String) jsonObject.get("user_id");
            jsonObject.remove("user_id");

            JSONArray jsonRegionGroupsParam = (JSONArray) jsonObject.get("table_region_group");
            JSONObject jsonRegionGroupParam = (JSONObject) jsonRegionGroupsParam.get(0);
            String region_guid = (String) jsonRegionGroupParam.get("region_guid");
            String tableGroupGuid = (String) jsonRegionGroupParam.get("table_group_guid");
            String gateway_id=(String)jsonRegionGroupParam.get("gateway_id");

            List<UserGateway> list = userService.selectGatewayByUserId(user_id);
            String account_id = list.get(0).getAccount_id();
            String SourceId = account_id + Param.WEBAPPID + String.valueOf(System.currentTimeMillis() / 1000000000);


            TableRegionGroup tempData = new TableRegionGroup();
            tempData.setAccount_id(account_id);
            tempData.setTable_group_guid(tableGroupGuid);
            tempData.setGateway_id(gateway_id);
            tempData.setRegion_guid(region_guid);
            TableRegionGroup paramInfo = newRegionService.selectRegionGroupByGroupGuid(tempData);
            String group_addr=paramInfo.getGroup_addr();





            TableGroupMembers tableGroupMembers = new TableGroupMembers();
            tableGroupMembers.setGateway_id(gateway_id);
            tableGroupMembers.setAccount_id(account_id);
            tableGroupMembers.setGroup_addr(group_addr);

            List<TableGroupMembers> groupMembersList = newGroupService.findGroupMemberByGroupAddr(tableGroupMembers);



            for (TableGroupMembers groupMember :groupMembersList) {

                JSONArray jsonGroupMembers = new JSONArray();

                JSONObject jsonGroupMember = new JSONObject();

//                jsonGroupMember.put("gateway_id",groupMember.getGateway_id());
                jsonGroupMember.put("device_addr",groupMember.getDevice_addr());
                jsonGroupMember.put("group_addr",groupMember.getGroup_addr());

                jsonGroupMembers.add(jsonGroupMember);

                JSONObject socketParam = new JSONObject();

                socketParam.put("table_group_members",jsonGroupMembers);

                JSONObject socketResult = newGroupService.socketDeleteGroupMembers(socketParam,groupMember.getGateway_id(),SourceId,2);

                String status = String.valueOf(socketResult.get("Status"));

                if ("2".equals(status)){
                    message.setCode("-1");
                    message.setContent(new JSONArray());
                    message.setMessage("Gateway Error : Device is offline,please try later!");
                    return message;
                }

                if (status.equals("1")){
                    message.setCode("-1");
                    message.setContent(new JSONArray());
                    message.setMessage("Failed to add the member into the group, please try later!");
                    return message;
                }

            }




                JSONObject tempSocketParam = new JSONObject();
                tempSocketParam.put("group_guid",tableGroupGuid);

                JSONArray temListParam = new JSONArray();
                temListParam.add(tempSocketParam);

                JSONObject socketParam  = new JSONObject();
                socketParam.put("table_group",temListParam);



            JSONObject socketGroupDeleteResult = newGroupService.socketDeleteGroup(socketParam,gateway_id,SourceId,2);
            String statusGroupDeleteResult = String.valueOf(socketGroupDeleteResult.get("Status"));
            if ("2".equals(statusGroupDeleteResult)){
                message.setCode("-1");
                message.setContent(new JSONArray());
                message.setMessage("Gateway Error : Device is offline,please try later!");
                return message;
            }

            if (statusGroupDeleteResult.equals("1")){
                message.setCode("-1");
                message.setContent(new JSONArray());
                message.setMessage("Failed to remove the group, please try later!");
                return message;
            }


//            TableRegionGroup regionGroup = new TableRegionGroup();
//            regionGroup.setAccount_id(account_id);
//            regionGroup.setGateway_id(gateway_id);
//            regionGroup.setGroup_addr(group_addr);
//            TableRegionGroup resultRegionGroup=newGroupService.findRegionGroupByGroupAddr(regionGroup);


            JSONObject socketRegionGroupParam = new JSONObject();
            JSONArray jsonRegionGrouos =new JSONArray();
            JSONObject jsonRegionGroup = new JSONObject();

//            jsonRegionGroup.put("gateway_id",resultRegionGroup.getGateway_id());
            jsonRegionGroup.put("table_group_guid",tableGroupGuid);
            jsonRegionGroup.put("region_guid",region_guid);

            jsonRegionGrouos.add(jsonRegionGroup);

            socketRegionGroupParam.put("table_region_group",jsonRegionGrouos);

            JSONObject socketRegionGroupDeleteResult = newGroupService.socketDeleteRegionGroup(socketRegionGroupParam,gateway_id,SourceId,2);

            String statusRegionDeleteResult = String.valueOf(socketRegionGroupDeleteResult.get("Status"));

            if ("2".equals(statusRegionDeleteResult)){
                message.setCode("-1");
                message.setContent(new JSONArray());
                message.setMessage("Gateway Error : Device is offline,please try later!");
                return message;
            }

            if (statusRegionDeleteResult.equals("1")){
                message.setCode("-1");
                message.setContent(new JSONArray());
                message.setMessage("Failed to remove the group from the region, please try later!");
                return message;
            }


            //设置
            TableGroup tableGroup = new TableGroup();
            tableGroup.setAccount_id(account_id);
            //tableGroup.setGroup_guid(group_guid);
            tableGroup.setGroup_addr(group_addr);
            tableGroup.setGateway_id(gateway_id);
            //删除
            int n = newGroupService.deleteGroup(tableGroup);
            if (n>0){
                message.setCode("0");
                message.setMessage("Group delete success");
                message.setContent("");
            }else {
                message.setCode("-1");
                message.setMessage("Group delete failed");
                message.setContent(new JSONArray());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return message;
    }

    //查找区域组//// TODO: 16/9/2  
    @RequestMapping(value = "/old/region/group", method = RequestMethod.GET)
    @ResponseBody
    public Message findRegionGroup() {
        Message message = new Message();
        try {
            JSONObject jsonObject;
            jsonObject = deeppCopy();
            JSONObject jsonObject1 = (JSONObject) jsonObject.get("group");
            String user_id = (String) jsonObject1.get("user_id");
            String region_guid = (String) jsonObject1.get("region_guid");
            String gateway_id = (String) jsonObject1.get("gateway_id");
            List<UserGateway> list = userService.selectGatewayByUserId(user_id);
           if (list.size()<1){
               message.setCode("-1");
               message.setMessage("user does not exist");
               message.setContent(new JSONArray());
           }
            String account_id = list.get(0).getAccount_id();
            TableRegionGroup tableRegionGroup = new TableRegionGroup();
            tableRegionGroup.setRegion_guid(region_guid);
            tableRegionGroup.setGateway_id(gateway_id);
            tableRegionGroup.setAccount_id(account_id);
            List<TableRegionGroup> list1 = newRegionService.findRegionGroup(tableRegionGroup);
            if (list1.size() > 0) {
                message.setCode("0");
                message.setMessage("RegionGroup query success");
                JSONArray jsonArray = (JSONArray) JSONArray.toJSON(list1);
                message.setContent(jsonArray);
            } else {
                message.setCode("0");
                message.setMessage("RegionGroup query is empty");
                message.setContent(new JSONArray());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return message;
    }

    // 查找区域通道
    @RequestMapping(value = "/region/region_channel", method = RequestMethod.GET)
    @ResponseBody
    public Message findchannel() {
        Message message = new Message();
        JSONObject jsonObject;
        jsonObject = deeppCopy();
        JSONObject temp = (JSONObject) jsonObject.get("region_channel");
        String user_id = (String) temp.get("user_id");

        //获取account_id
        List<UserGateway> list = userService.selectGatewayByUserId(user_id);
        if (list.size()<1){
            message.setCode("-1");
            message.setMessage("user does not exist");
            message.setContent(new JSONArray());
            return message;
        }
        String account_id = list.get(0).getAccount_id();
        String region_guid = (String) temp.get("region_guid");
        String gateway_id = (String) temp.get("gateway_id");
        //实体类区域设备
        TableRegionDevice tableRegionDevice = new TableRegionDevice();
        tableRegionDevice.setAccount_id(account_id);
        tableRegionDevice.setRegion_guid(region_guid);
        tableRegionDevice.setGateway_id(gateway_id);
        //获取一个map数据集/获取设备
        List<Map> maps = newRegionDeviceService.fingRegionDevice(tableRegionDevice);
        if (maps.size() < 1) {
            message.setCode("0");
            message.setMessage("Region is empty, there is no channel");
            message.setContent(new JSONArray());
            return message;
        }
        //设备device主键
        String device_guid_bulb = null;
        for (Map map : maps) {
            String device_guid = (String) map.get("table_device_guid");
            String account_id1 = (String) map.get("account_id");

            TableDevice record = new TableDevice();
            record.setAccount_id(account_id1);
            record.setDevice_guid(device_guid);
            record.setGateway_id(gateway_id);
            //查找设备类表
            List<TableDevice> templist = newDeviceService.findInfoOfTheDevice(record);
            //
            if (templist.size() < 1) {
                device_guid_bulb = device_guid;
                break;
            }

        }
        TableChannel record = new TableChannel();
        record.setAccount_id(account_id);
        record.setGateway_id(gateway_id);
        record.setTable_device_guid(device_guid_bulb);

        List<TableChannel> tableChannel = newDeviceService.findChannelInfo(record);

        if (tableChannel.size() < 1) {
            message.setCode("-1");
            message.setMessage("Error: the device in the region can't find it's channel info!");
            message.setContent(new JSONArray());
            return message;
        }

        TableRegion tableRegionTemp = new TableRegion();
        tableRegionTemp.setGateway_id(gateway_id);
        tableRegionTemp.setAccount_id(account_id);
        tableRegionTemp.setRegion_guid(region_guid);
        //获取区域的值,
        TableRegion tableRegion = newRegionService.findTheValueOftheRegion(tableRegionTemp);

        String regionValue = tableRegion.getRegion_value();

        if (regionValue.toUpperCase().equals("NULL")||regionValue==null){
            message.setCode("0");
            message.setMessage("Region no channel");
            message.setContent(new JSONArray());
            return message;
        }
        int len = regionValue.length() / 2;

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
        for (int i = 0; i < tableChannel.size(); i++) {

            JSONObject channel = new JSONObject();
            channel.put("channel_number", tableChannel.get(i).getChannel_number());
            String strChannelNum = tableChannel.get(i).getChannel_number();
            int intChannelNum = Integer.parseInt(strChannelNum);

            String hexChannelValue = devices[intChannelNum - 1];
            //System.out.println("值"+hexChannelValue);
            int channelValue = Integer.parseInt(hexChannelValue, 16);
            String strChannelValue = String.valueOf(channelValue);

            channel.put("channel_value", strChannelValue);
            channel.put("channel_name", tableChannel.get(i).getChannel_name());

            jsonArray.add(channel);
        }
        message.setCode("0");
        message.setMessage("Region query channel success");
        message.setContent(jsonArray);
        return message;
    }

    // 控制区域
    @RequestMapping(value = "/region/controll", method = RequestMethod.PUT)
    @ResponseBody
    public Object controllRegion() {
        MessageNoContent message = new MessageNoContent();

        JSONObject jj;
        jj = deeppCopy();

        //System.out.println(jj.toString());
//
        String userId = (String) jj.get("user_id");

        List<UserGateway> tableDeviceList = userService.selectGatewayByUserId(userId);
        if (tableDeviceList.size()<1){
//            message.setCode("-1");
//            message.setMessage("user does not exist");
//            message.setContent(new JSONArray());
//            return message;
            throw new BussinessException("-1","user does not exist");
        }
        String account_id = tableDeviceList.get(0).getAccount_id();
        String SourceId = account_id + Param.WEBAPPID + String.valueOf(System.currentTimeMillis() / 1000000000);

        jj.remove("user_id");


        JSONArray jsonTableRegion = (JSONArray) jj.get("table_region");
        //System.out.println("array:"+jsonTableRegion.size());

        if (jsonTableRegion.size() < 1) {

//            message.setCode("-1");
//            message.setMessage("Param for Controller is missing");
//            message.setContent(new JSONArray());
//            return message;
            throw new BussinessException("-1","Param for Controller is missing");
        }

        JSONObject jsonRegion = (JSONObject) jsonTableRegion.get(0);
        System.out.println("json"+jsonRegion.size());
        TableRegion region = JSONObject.parseObject(jsonRegion.toString(), TableRegion.class);
        //JSONObject region_value=new JSONObject();
        //String region_value_str=(String)jsonRegion.get("region_value");
        JSONObject region_value = (JSONObject) jsonRegion.get("region_value");
        //region_value.put("region_value",region_value_str);
        String region_guid = (String) jsonRegion.get("region_guid");
        String gateway_id = (String) jsonRegion.get("gateway_id");
        String region_switch = (String) jsonRegion.get("region_switch");

        if (region_guid == null || gateway_id == null) {
//            message.setCode("-1");
//            message.setContent(new JSONArray());
//            message.setMessage("Missing the region_guid or gateway_id");
//            return message;
            throw new ParameterException("-1","Missing the region_guid or gateway_id");
        }
        if (region_value != null & region_switch != null) {
//            message.setCode("-1");
//            message.setMessage("the operation should only be turn on/off or adjust the region value");
//            message.setContent(new JSONArray());
//            return message;
            throw new ParameterException("-1","the operation should only be turn on/off or adjust the region value");
        }
        if (region_value != null && region_switch == null) {

            TableRegionDevice tableRegionDevice = new TableRegionDevice();
            tableRegionDevice.setAccount_id(account_id);
            tableRegionDevice.setRegion_guid(region_guid);
            tableRegionDevice.setGateway_id(gateway_id);

            List<Map> maps = newRegionDeviceService.fingRegionDevice(tableRegionDevice);
            if (maps.size() < 1) {
//                message.setCode("0");
//                message.setMessage("Region is empty, there is no channel");
//                message.setContent(new JSONArray());
//                return message;
                 throw new BussinessException("0","Region is empty, there is no channel");
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
//                message.setCode("-1");
//                message.setMessage("Error: the device in the region can't find it's channel info!");
//                message.setContent(new JSONArray());
//                return message;
                throw new BussinessException("-1","Error: the device in the region can't find it's channel info!");
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
            System.out.println("jsonArray:"+jsonArray);
            String[] value_arr_str = new String[jsonArray.size()];

            for (int j = 0; j < jsonArray.size(); j++) {
                JSONObject channel_value = (JSONObject) jsonArray.get(j);
                String channel_number1 = (String) channel_value.get("channel_number");

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
                //System.out.println("通道值"+value_arr_str[index]);
            }
            String final_value = "";
            for (int j = 0; j < value_arr_str.length; j++) {
                final_value += value_arr_str[j];
                System.out.println("final_value:"+final_value);
            }
            jsonRegion.remove("region_value");
            jsonRegion.put("region_value", final_value);
            jsonRegion.remove("gateway_id");

            region.setRegion_value(final_value);
            region.setAccount_id(account_id);
            JSONObject jsonResult=null;

            try {
                jsonResult = newRegionService.socketControlReion(jj, gateway_id, SourceId, 2);
            }catch (Exception e){
                logger.error(e);
            }

            if (jsonResult==null){
//                message.setCode("-1");
//                message.setMessage("Time Out for read message from gateway");
//                message.setContent(new JSONArray());
//                return message;
                throw new BussinessException("-1","Time Out for read message from gateway");
            }

            String status = String.valueOf(jsonResult.get("Status"));

            if (status.equals("1")) {

//                message.setCode("-1");
//                message.setMessage("Failed to control the region");
//                message.setContent(new JSONArray());
//                return message;
                throw new BussinessException("-1","Failed to control the region");

            }
            if (status.equals("2")) {
//                message.setCode("-1");
//                message.setMessage("The region is out of control because it's off line");
//                message.setContent(new JSONArray());
//                return message;
                throw new BussinessException("-1","The region is out of control because it's off line");
            }


            int num = newRegionService.mysqlControlRegion(region);

            if (num < 1) {

                throw new BussinessException("-1","Failed to update the cloud DataBase");
            }

        }

        if (region_value == null && region_switch != null) {

            jsonRegion.remove("gateway_id");

            JSONObject jsonResult=null;

            try {
                jsonResult = newRegionService.socketControlReion(jj, gateway_id, SourceId, 2);
            }catch (Exception e){
                logger.error(e);
            }

            if (jsonResult==null){
//                message.setCode("-1");
//                message.setMessage("Time Out for read message from gateway");
//                message.setContent(new JSONArray());
//                return message;
                throw new BussinessException("-1","Time Out for read message from gateway");
            }

            String status = String.valueOf(jsonResult.get("Status"));

            if (status.equals("1")) {

//                message.setCode("-1");
//                message.setMessage("Failed to control the region");
//                message.setContent(new JSONArray());
//                return message;
                throw new BussinessException("-1","Failed to control the region");

            }
            if (status.equals("2")) {
//                message.setCode("-1");
//                message.setMessage("The region is out of control because it's off line");
//                message.setContent(new JSONArray());
//                return message;
                throw new BussinessException("-1","The region is out of control because it's off line");
            }
            region.setAccount_id(account_id);


            int num = newRegionService.mysqlControlRegion(region);

            if (num < 1) {
//                message.setCode("-1");
//                message.setMessage("Failed to update the cloud DataBase");
//                message.setContent(new JSONArray());
//                return message;
                throw new BussinessException("-1","Failed to update the cloud DataBase");
            }

        }


        message.setCode("0");
        message.setMessage("Control the region successfully!");
        return message;
    }
}
