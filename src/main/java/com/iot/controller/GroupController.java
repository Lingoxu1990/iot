package com.iot.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.iot.requestUtil.RequestCaseUtil;
import com.iot.socketUtil.OutPutSocketMessage;
import com.iot.dbUtil.PropsUtil;
import com.iot.message.Message;
import com.iot.pojo.AccountDataInfo;
import com.iot.pojo.TableRegionGroup;
import com.iot.pojo.UserGateway;
import com.iot.service.AccountInfoService;
import com.iot.service.GroupService;
import com.iot.service.RegionService;
import com.iot.service.UserService;
import com.iot.spitUtil.Param;
import com.iot.sqlControlUtil.SqlControlUtil;
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
 * 改控制器用于设备组的操作
 * <p>
 * 1 组的增删改查
 * 2 组成员的正删改查
 * 3 组的控制
 * Created by Jacob on 16/4/7.
 */

/*
* 组设备控制
* */
@Controller
@RequestMapping()
public class GroupController {
    private static Logger logger = Logger.getLogger(GroupController.class);

    @Resource
    private GroupService groupService;
    @Resource
    private UserService userService;
    @Resource
    private AccountInfoService accountInfoService;
    @Resource
    private RegionService regionService;

    /**
     * 用于添加设组
     * <p>
     * 按照正常的业务逻辑添加设备组应该传入gateway_id,目前本接口采用默认的网关id;
     * <p>
     * 主要逻辑:
     * <p>
     * 1 获取根据用户id,获取用户的账户id(用于生成源ID)
     * <p>
     * 2 根据网关id,获取DestinationId;
     * <p>
     * 3 获取保存在配置中的 组地址,自增后 更新配置,做为需要创建的组的地址
     * <p>
     * 4 调用socket工具,发送指令至网关
     *
     * @return
     */


    /**
     * 用于修改组的名称
     * <p>
     * 按照正常的业务逻辑添加设备组应该传入gateway_id,目前本接口采用默认的网关id;
     * <p>
     * 主要逻辑:
     * <p>
     * 1 获取根据用户id,获取用户的账户id(用于生成源ID)
     * <p>
     * 2 根据网关id,获取DestinationId;
     * <p>
     * 3 调用socket工具,发送指令至网关
     *
     * @return
     */
    @RequestMapping(value = "/device/group",method = RequestMethod.PUT)
    @ResponseBody
    public Object modifyGroup(HttpServletRequest httpServletRequest) throws IOException {

        JSONObject jj = ParamUtils.getAttributess(httpServletRequest);

        String user_id = (String) jj.get("user_id");
        jj.remove("user_id");

        List<UserGateway> tableDeviceList = userService.selectGatewayByUserId(user_id);

        String account_id = tableDeviceList.get(0).getAccount_id();

        String SourceId = account_id + Param.WEBAPPID + String.valueOf(System.currentTimeMillis() / 1000000000);
        String DestinationId = tableDeviceList.get(0).getGateway_id();

        logger.error("service : device/group"  +" action : add group" +" \nparam : "+jj.toString()+
                "\nSourceId :"+SourceId+ " DestinationId : "+DestinationId);

        Message message = groupService.modifyGroup(jj, DestinationId, SourceId, 2);
        if (message.getCode().equals("-1")) {
            logger.error("failed to update the group_info!");
            return message;
        }


        JSONObject region_group_act = new JSONObject();
        JSONArray region_group_arry = new JSONArray();



        JSONArray groups = (JSONArray) jj.get("table_group");

        for (int i = 0; i < groups.size(); i++) {
            JSONObject group = (JSONObject) groups.get(i);

            Set<String> keys = group.keySet();
            JSONObject j = new JSONObject();

            for (String key : keys) {
                if (key.equals("group_guid")) {
                    j.put("table_group_guid", group.get(key));
                }
            }

            TableRegionGroup tableRegionGroup = JSONObject.parseObject(j.toString(), TableRegionGroup.class);

            JSONObject tableRegiongroup = regionService.findRegionGroupByAccountIdAndGroupId(tableRegionGroup);
            tableRegiongroup.remove("id");
            tableRegiongroup.remove("account_id");
            tableRegiongroup.remove("group_name");
            tableRegiongroup.put("group_name", group.get("group_name"));

            region_group_arry.add(tableRegiongroup);
        }

        region_group_act.put("table_region_group", region_group_arry);


        Message message1 = regionService.modifyRegionGroup(region_group_act, DestinationId, SourceId, 2);

        if (message1.getCode().equals("-1")) {
            return message1;
        }

        Message message2 = new Message();
        message2.setCode("0");
        message2.setContent("[]");
        message2.setMessage("update the group successfully!");

        return message2;
    }

    /**
     * 用于查找用户的所有组
     * <p>
     * 按照正常的业务逻辑添加设备组应该传入gateway_id,目前本接口采用默认的网关id;
     * <p>
     * 主要逻辑:
     * <p>
     * 1 获取根据用户id,获取用户的账户id(用于生成源ID)
     * <p>
     * 2 根据网关id,获取DestinationId;
     * <p>
     * 3 调用socket工具,发送指令至网关
     *
     * @return
     */
    @RequestMapping(value = "/device/group",method = RequestMethod.GET)
    @ResponseBody
    public Object findGroup(HttpServletRequest httpServletRequest) throws IOException {
        JSONObject jj = ParamUtils.getJsonObjectFromRequest(httpServletRequest);
        JSONObject jsonObject = (JSONObject) jj.get("group");

        String user_id = (String) jsonObject.get("user_id");
        jsonObject.remove("user_id");

        List<UserGateway> tableDeviceList = userService.selectGatewayByUserId(user_id);

        String account_id = tableDeviceList.get(0).getAccount_id();

        String SourceId = account_id + Param.WEBAPPID + String.valueOf(System.currentTimeMillis() / 1000000000);
        String DestinationId = tableDeviceList.get(0).getGateway_id();

        List<AccountDataInfo> tableDeviceList1 = userService.getALLagateway(account_id);

        logger.error("service : device/group"  +" action : find all group" +" \nparam : "+jj.toString()+
                "\nSourceId :"+SourceId+ " Gateway Size : "+ tableDeviceList1.size());

        JSONArray RE = new JSONArray();

        for (AccountDataInfo acccountDataInfo:tableDeviceList1) {

            String gateway_id = acccountDataInfo.getGateway_id();

            JSONObject toFind = new JSONObject();
            JSONObject group = new JSONObject();
            group.put("gateway_id",gateway_id);

            Set<String> keys = jsonObject.keySet();
            for (String key : keys) {
                group.put(key, jsonObject.get(key));
            }
            toFind.put("table_group", group);


            Message message = groupService.findGroup(toFind, gateway_id, SourceId, 2);
            if (message.getCode().equals("-1")) {
                return message;
            }
            JSONArray jsonArray = (JSONArray) message.getContent();
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject JJ = (JSONObject) jsonArray.get(i);
                RE.add(JJ);
//                for (int j = 0; j < JJ.size(); j++) {
//                    RE.add(JJ.get(j));
//                }
            }

        }


        for (int i = 0; i <RE.size() ; i++) {

            JSONObject group_info  = (JSONObject) RE.get(i);
            String group_guid=(String) group_info.get("group_guid");
            String gateway_id=(String) group_info.get("gateway_id");

            String host = "";

            try {
                PropsUtil configProps = new PropsUtil("config.properties");
                host = configProps.get("host");
            } catch (IOException e) {
                e.printStackTrace();
            }

            String uri = "device/group/group_channel?user_id="+user_id+"&table_group_guid="+group_guid+"&gateway_id="+gateway_id;
            String url = host+uri;

            String str_response = RequestCaseUtil.requestGetCase(url);
            if (str_response==null){
                group_info.put("channel",new JSONArray());
                continue;
            }
            JSONObject jsonResponse = JSONObject.parseObject(str_response);

            JSONArray content = (JSONArray) jsonResponse.get("content");

            group_info.put("channel",content);

        }


        Message message1 = new Message();
        message1.setCode("0");
        message1.setContent(RE);
        message1.setMessage("Search Group Successfully!");

        return message1;
    }

    /**
     * 用于删除用户的组(该接口未完成socket测试)
     *
     * 按照正常的业务逻辑添加设备组应该传入gateway_id,目前本接口采用默认的网关id;
     *
     * 主要逻辑:
     *
     * 1 获取根据用户id,获取用户的账户id(用于生成源ID)
     *
     * 2 根据网关id,获取DestinationId;
     *
     * 3 发送socket指令,删除用户组
     *
     * 4 在该组所在网关中,查找区域组表是否存在符合条件的记录,有就继续发送socket指令,删除区域下的组
     *
     * 5 删除组下成员;
     *
     * @return
     */
    @RequestMapping(value = "/device/group/old",method = RequestMethod.DELETE)
    @ResponseBody
    public Object deleteGroup(HttpServletRequest httpServletRequest) throws IOException {

        JSONObject jj = ParamUtils.getJsonObjectFromRequest(httpServletRequest);

        String user_id = (String) jj.get("user_id");

        List<UserGateway> tableDeviceList = userService.selectGatewayByUserId(user_id);

        String account_id = tableDeviceList.get(0).getAccount_id();

        String SourceId = account_id + Param.WEBAPPID + String.valueOf(System.currentTimeMillis() / 1000000000);


        JSONArray groups = (JSONArray) jj.get("table_group");
        JSONObject ttt =(JSONObject) groups.get(0);
        String gateway_id = (String) ttt.get("gateway_id");
        String DestinationId = gateway_id;

        jj.remove("user_id");

        logger.error("service : device/group"  +" action : find all group" +" \nparam : "+jj.toString()+
                "\nSourceId :"+SourceId+ " DestinationId : "+ DestinationId);

        Message message = groupService.deleteGroup(jj, DestinationId, SourceId, 2);

        if (message.getCode().equals("-1")) {
            logger.error(message.getMessage());
            return message;
        }

        JSONObject region_group_act = new JSONObject();
        JSONArray region_group_arry = new JSONArray();

        for (int i = 0; i < groups.size(); i++) {
            JSONObject group = (JSONObject) groups.get(i);

            Set<String> keys = group.keySet();
            JSONObject j = new JSONObject();
            for (String key : keys) {
                if (key.equals("group_guid")) {
                    j.put("table_group_guid", group.get(key));
                }
            }
            j.put("account_id", account_id);

            TableRegionGroup tableRegionGroup = JSONObject.parseObject(j.toString(), TableRegionGroup.class);

            JSONObject tableRegiongroup = regionService.findRegionGroupByAccountIdAndGroupId(tableRegionGroup);

            if (tableRegiongroup == null) {
                return message;
            }
            tableRegiongroup.remove("id");
            tableRegiongroup.remove("account_id");
            tableRegiongroup.remove("group_name");
            tableRegiongroup.remove("gateway_id");
            tableRegiongroup.remove("group_addr");

            region_group_arry.add(tableRegiongroup);
        }

        region_group_act.put("table_region_group", region_group_arry);
        System.out.println(region_group_act.toString());


        Message message1 = regionService.deleteRegionGroup(region_group_act, DestinationId, SourceId, 2);


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
     * 用于为组添加成员设备
     * <p>
     * 按照正常的业务逻辑添加设备组应该传入gateway_id,目前本接口采用默认的网关id;
     * <p>
     * 主要逻辑:
     * <p>
     * 1 获取根据用户id,获取用户的账户id(用于生成源ID)
     * <p>
     * 2 根据网关id,获取DestinationId;
     * <p>
     * 3 发送socket指令,添加组成员;
     *
     * @return
     */

    /**
     * 用于删除组下成员
     * <p>
     * 按照正常的业务逻辑添加设备组应该传入gateway_id,目前本接口采用默认的网关id;
     * <p>
     * 主要逻辑:
     * <p>
     * 1 获取根据用户id,获取用户的账户id(用于生成源ID)
     * <p>
     * 2 根据网关id,获取DestinationId;
     * <p>
     * 3 发送socket指令,添加组成员;
     *
     * @return
     */
    @RequestMapping(value = "/device/group/group_member/old", method = RequestMethod.DELETE)
    @ResponseBody
    public Object deleteGroupMember(HttpServletRequest httpServletRequest) throws IOException {
        JSONObject jj = ParamUtils.getAttributess(httpServletRequest);
        Message message = new Message();
        String user_id = (String) jj.get("user_id");
        jj.remove("user_id");


        List<UserGateway> tableDeviceList = userService.selectGatewayByUserId(user_id);

        String account_id = tableDeviceList.get(0).getAccount_id();

        String SourceId = account_id + Param.WEBAPPID + String.valueOf(System.currentTimeMillis() / 1000000000);

        JSONArray group_members = (JSONArray) jj.get("table_group_members");
        JSONObject deivce = (JSONObject) group_members.get(0);
        String gateway_id = (String) deivce.get("gateway_id");
//        String group_members_guid = (String) deivce.get("group_members_guid");
        String group_members_guid="";


        logger.error("service : device/group/group_members"  + " action : delete group_members" +" param : "+jj.toString());

        if (gateway_id==null || group_members_guid==null){
            logger.error("service : device/group/group_members"  + " action : delete group_members" +" Error : missing param");
            message.setCode("-1");
            message.setMessage("Missing gateway_id or group_members_guid");
            message.setContent("[]");
            return message;

        }

        String DestinationId = gateway_id;
        deivce.remove("gateway_id");

        message = groupService.deleteGroupMember(jj, DestinationId, SourceId, 2);
        return message;
    }

    /**
     * 用于修改组成员(与控制业务逻辑重复,方法不使用)
     * <p>
     * 按照正常的业务逻辑添加设备组应该传入gateway_id,目前本接口采用默认的网关id;
     * <p>
     * 主要逻辑:
     * <p>
     * 1 获取根据用户id,获取用户的账户id(用于生成源ID)
     * <p>
     * 2 根据网关id,获取DestinationId;
     * <p>
     * 3 发送socket指令,添加组成员;
     *
     * @return
     */
    @RequestMapping(value = "/device/group/group_member", method = RequestMethod.PUT)
    @ResponseBody
    public Object modifyGroupMember(HttpServletRequest httpServletRequest) throws IOException {
        JSONObject jj = ParamUtils.getAttributess(httpServletRequest);

        String user_id = (String) jj.get("user_id");

        List<UserGateway> tableDeviceList = userService.selectGatewayByUserId(user_id);

        String account_id = tableDeviceList.get(0).getAccount_id();

        String SourceId = account_id + Param.WEBAPPID + String.valueOf(System.currentTimeMillis() / 1000000000);
        JSONArray group_members = (JSONArray) jj.get("table_group_members");
        JSONObject device = (JSONObject) group_members.get(0);
        
        String DestinationId =(String) device.get("gateway_id");
        jj.remove("user_id");
        logger.error("service : device/group/group_members"  + " action : modify the group_members" +"\n"+jj.toString()+
                "\n SourceId : "+SourceId+" DestinationId : "+DestinationId);

        Message message = groupService.modifyGroupMembers(jj, DestinationId, SourceId, 2);
        
        return message;
    }

    /**
     * 用于获取组下成员
     * <p>
     * 按照正常的业务逻辑添加设备组应该传入gateway_id,目前本接口采用默认的网关id;
     * <p>
     * 主要逻辑:
     * <p>
     * 1 获取根据用户id,获取用户的账户id(用于生成源ID)
     * <p>
     * 2 根据网关id,获取DestinationId;
     * <p>
     * 3 发送socket指令,获取组下成员;
     *
     * @return
     */
    @RequestMapping(value = "/device/group/group_member/old", method = RequestMethod.GET)
    @ResponseBody
    public Object findGroupMember(HttpServletRequest httpServletRequest) throws IOException {
        JSONObject jj = ParamUtils.getJsonObjectFromRequest(httpServletRequest);

        JSONObject jsonObject = (JSONObject) jj.get("group_member");

        String user_id = (String) jsonObject.get("user_id");
        String gateway_id = (String)jsonObject.get("gateway_id");
        jsonObject.remove("gateway_id");
        jsonObject.remove("user_id");

        List<UserGateway> tableDeviceList = userService.selectGatewayByUserId(user_id);

        String account_id = tableDeviceList.get(0).getAccount_id();

        String SourceId = account_id + Param.WEBAPPID + String.valueOf(System.currentTimeMillis() / 1000000000);
        String DestinationId = gateway_id;

        List<AccountDataInfo> tableDeviceList1 = userService.getALLagateway(account_id);

        logger.error("service : device/group/group_members"  + " action : modify the group_members" +"\n"+jj.toString());

//        JSONObject toFind = new JSONObject();
//        JSONObject group = new JSONObject();
//
//        Set<String> keys = jsonObject.keySet();
//        for (String key : keys) {
//            group.put(key, jsonObject.get(key));
//        }
//        toFind.put("table_group_members", group);
//        JSONArray RE = new JSONArray();
//
//        for (AccountDataInfo accountDataInfo: tableDeviceList1) {
//            String gateway_id = accountDataInfo.getGateway_id();
//            logger.error("SourceId : "+SourceId+" DestinationId : "+gateway_id);
//            Message message = groupService.findGroupMembers(toFind, gateway_id, SourceId, 2);
//            JSONArray jsonArray = (JSONArray) message.getContent();
//            for (int i = 0; i < jsonArray.size(); i++) {
//                JSONArray JJ = (JSONArray) jsonArray.get(i);
//                for (int j = 0; j < JJ.size(); j++) {
//                    RE.add(JJ.get(j));
//                }
//            }
//        }
        


        JSONObject toFind = new JSONObject();
        JSONObject group = new JSONObject();

        Set<String> keys = jsonObject.keySet();
        for (String key : keys) {
            group.put(key, jsonObject.get(key));
        }
        toFind.put("table_group_members", group);


        Message message = groupService.findGroupMembers(toFind, DestinationId, SourceId, 2);

        JSONArray RE = new JSONArray();
        JSONArray jsonArray = (JSONArray) message.getContent();
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONArray JJ = (JSONArray) jsonArray.get(i);
            for (int j = 0; j < JJ.size(); j++) {
                RE.add(JJ.get(j));
            }
        }


        Message message1 = new Message();
        message1.setCode("0");
        message1.setContent(RE);
        message1.setMessage("Search Group members Successfully!");


        return message1;
    }

    /**
     * 用于控制设备组
     * <p>
     * 按照正常的业务逻辑添加设备组应该传入gateway_id,目前本接口采用默认的网关id;
     * <p>
     * 主要逻辑:
     * <p>
     * 1 获取根据用户id,获取用户的账户id(用于生成源ID)
     * <p>
     * 2 根据网关id,获取DestinationId;
     * <p>
     * 3 获取设备组的值,根据传入通道与数值生成设备组值;
     * <p>
     * 3 发送socket指令,获取组下成员;
     *
     * @return
     */
    @RequestMapping(value = "/device/group/group_control/old", method = RequestMethod.PUT)
    @ResponseBody
    public Object controlGroup(HttpServletRequest httpServletRequest) throws IOException {

        JSONObject jj = ParamUtils.getJsonObjectFromRequest(httpServletRequest);

        String user_id = (String) jj.get("user_id");
        jj.remove("user_id");

        List<UserGateway> tableDeviceList = userService.selectGatewayByUserId(user_id);

        String account_id = tableDeviceList.get(0).getAccount_id();

        String SourceId = account_id + Param.WEBAPPID + String.valueOf(System.currentTimeMillis() / 1000000000);
        JSONArray jsonArray = (JSONArray) jj.get("table_group");
        JSONObject temp_group = (JSONObject) jsonArray.get(0);
        String gateway_id = (String) temp_group.get("gateway_id");
        String DestinationId = gateway_id;

        logger.error("service : device/group/group_control"  + " action : control the group" +"\n"+jj.toString());

        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject json = (JSONObject) jsonArray.get(i);
            JSONObject device_value = (JSONObject) json.get("group_value");
            String group_guid = (String) json.get("group_guid");
            String group_switch = (String)json.get("group_switch") ;

            if (group_switch!=null){
                json.remove("gateway_id");
                continue;
            }
            String host = "";

            try {
                PropsUtil configProps = new PropsUtil("config.properties");
                host = configProps.get("host");
            } catch (IOException e) {
                e.printStackTrace();
            }

            host += "device/group/group_channel?user_id=" + user_id + "&gateway_id=" + DestinationId + "&table_group_guid=" + group_guid;

            String result = RequestCaseUtil.requestGetCase(host);


            JSONObject res = JSONObject.parseObject(result);
            JSONArray content = (JSONArray) res.get("content");

            for (int j = 0; j < content.size(); j++) {

                JSONObject v = (JSONObject) content.get(j);
                String c_number = (String) v.get("channel_number");
                if (c_number.equals(device_value.get("channel_number"))) {
                    v.remove("value");
                    v.put("value", device_value.get("value"));
                }
            }

            String[] values = new String[content.size()];

            for (int j = 0; j < content.size(); j++) {
                JSONObject value = (JSONObject) content.get(j);
                String channel_num = (String) value.get("channel_number");
                String channel_value = (String) value.get("value");

                if (Integer.parseInt(channel_value) < 16) {
                    values[Integer.parseInt(channel_num) - 1] = "0" + Integer.toHexString(Integer.parseInt(channel_value));
                } else {
                    values[Integer.parseInt(channel_num) - 1] = Integer.toHexString(Integer.parseInt(channel_value));
                }

            }
            json.remove("group_value");
            String finalyValue = "";
            for (int j = 0; j < content.size(); j++) {
                finalyValue += values[j];
            }
            json.put("group_value", finalyValue);

            json.remove("gateway_id");
        }

        Message message = groupService.controlGroup(jj, DestinationId, SourceId, 2);

        return message;
    }

    /**
     * 用于获取设备组的值,根据通道拆分返回
     * <p>
     * 按照正常的业务逻辑添加设备组应该传入gateway_id,目前本接口采用默认的网关id;
     * <p>
     * 主要逻辑:
     * <p>
     * 1 获取根据用户id,获取用户的账户id(用于生成源ID)
     * <p>
     * 2 根据网关id,获取DestinationId;
     * <p>
     * 3 获取组下成员,任选一个,根据设备的主键,获取通道信息,拆分通道信息并返回;
     * <p>
     * 3 发送socket指令,获取组下成员;
     *
     * @return
     */
    @RequestMapping(value = "/device/group/group_channel/old", method = RequestMethod.GET)
    @ResponseBody
    public Object getGroupChannel(HttpServletRequest httpServletRequest) throws IOException {
        JSONObject jj = ParamUtils.getJsonObjectFromRequest(httpServletRequest);
        Message finnal_message = new Message();


        JSONObject jsonObject = (JSONObject) jj.get("group_channel");

        String user_id = (String) jsonObject.get("user_id");
        jsonObject.remove("user_id");

        String gateway_id = (String) jsonObject.get("gateway_id");

        List<UserGateway> tableDeviceList = userService.selectGatewayByUserId(user_id);

        String account_id = tableDeviceList.get(0).getAccount_id();

        String SourceId = account_id + Param.WEBAPPID + String.valueOf(System.currentTimeMillis() / 1000000000);
        String DestinationId = gateway_id;

        logger.error("service : device/group/group_control"  + " action : get the group_channel" +"\n"+jj.toString());
        JSONObject toFind = new JSONObject();
        JSONObject group = new JSONObject();

        Set<String> keys = jsonObject.keySet();

        for (String key : keys) {

            if (!key.equals("gateway_id")) {
                group.put(key, jsonObject.get(key));
            }
        }

        toFind.put("table_group_members", group);


        //获取组成员的设备主键
        Message message = groupService.findGroupMembers(toFind, DestinationId, SourceId, 2);
        JSONArray RE = new JSONArray();
        JSONArray jsonArray = (JSONArray) message.getContent();
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONArray JJ = (JSONArray) jsonArray.get(i);
            for (int j = 0; j < JJ.size(); j++) {
                RE.add(JJ.get(j));
            }
        }
        if (RE.size()<1){
            message.setCode("-1");
            message.setMessage("no group members");
            message.setContent(new JSONArray());
            return message;
        }


        JSONObject jjj = (JSONObject) RE.get(0);
        if (jjj==null){
            finnal_message.setCode("-1");
            finnal_message.setMessage("there is no member in the group");
            finnal_message.setContent("[]");
            logger.error("failed to get member in the group");
            return finnal_message;
        }
        JSONObject channel = new JSONObject();

        channel.put("table_device_guid", jjj.get("device_guid"));
        //根据组成员设备主键,获取通道信息

        String result = SqlControlUtil.selectObjects("table_channel", channel);

        OutPutSocketMessage outPutSocketMessage = new OutPutSocketMessage();
        outPutSocketMessage.setPackegType(2);//包类型,app写2,web写-1
        outPutSocketMessage.setDestinationID(DestinationId);//app客户端可以写为任意的16个字符串,web填写目标网关地址
        outPutSocketMessage.setMessage("table_channel");//查询就填表名,非查询填写NULL
        outPutSocketMessage.setType("table_channel");//消息用于网关返回消息,下发命令的时候,可以写表名
        outPutSocketMessage.setSourceID(SourceId);//源ID
        outPutSocketMessage.setSql(result);//下发的指令(sql语句)

        JSONObject jsonResult = outPutSocketMessage.sendMessag(SourceId);
        String status = String.valueOf(jsonResult.get("Status"));
        logger.error("Command : "+result +" Gateway Status : "+status);

        if (!status.equals("0")){
            finnal_message.setCode("-1");
            finnal_message.setMessage("failed to get the channel of the group");
            finnal_message.setContent("[]");
            logger.error("failed to get the group's channel info");
            return finnal_message;
        }


        JSONArray channel_list = (JSONArray) jsonResult.get("List");

        //获取通道信息后,查询组的历史值,如果历史值为"00",则按照通道初始化组的值,如果非00,则按照通道返回各通道的值

        JSONObject group_tmp = new JSONObject();
        group_tmp.put("group_guid", jsonObject.get("table_group_guid"));


        String result1 = SqlControlUtil.selectObjects("table_group", group_tmp);

        OutPutSocketMessage outPutSocketMessage2 = new OutPutSocketMessage();
        outPutSocketMessage2.setPackegType(2);//包类型,app写2,web写-1
        outPutSocketMessage2.setDestinationID(DestinationId);//app客户端可以写为任意的16个字符串,web填写目标网关地址
        outPutSocketMessage2.setMessage("table_channel");//查询就填表名,非查询填写NULL
        outPutSocketMessage2.setType("table_channel");//消息用于网关返回消息,下发命令的时候,可以写表名
        outPutSocketMessage2.setSourceID(SourceId);//源ID
        outPutSocketMessage2.setSql(result1);//下发的指令(sql语句)


        JSONObject jsonResult2 = outPutSocketMessage2.sendMessag(SourceId);
        String statue = String.valueOf(jsonResult2.get("Status"));
        logger.error("Command : "+result1 +" Gateway Status : "+statue);
        if (!statue.equals("0")) {
            finnal_message.setCode("-1");
            finnal_message.setContent("[]");
            finnal_message.setMessage("Failed to get the group value!");
            logger.error("failed to get the group_value");
            return finnal_message;
        }


        JSONArray groups = (JSONArray) jsonResult2.get("List");
        JSONObject group1 = (JSONObject) groups.get(0);
        String group_value = (String) group1.get("group_value");
        if (group_value.equals("00")) {
            //各通道的数值都设置为0
            for (int i = 0; i < channel_list.size(); i++) {
                JSONObject j = (JSONObject) channel_list.get(i);
                j.put("value", "0");
                j.remove("table_device_guid");
                j.remove("channel_guid");
                j.remove("device_name");
            }
        } else {
            //拆分组值,写入各通道

            String[] values = new String[channel_list.size()];
            for (int i = 0; i < channel_list.size(); i++) {
                if (i == channel_list.size() - 1) {
                    values[i] = group_value.substring(2 * i);
                } else {
                    values[i] = group_value.substring(2 * (i + 1) - 2, 2 * (i + 1));
                }

                if (values[i].equals("00") && values.length > 1) {
                    JSONObject j = (JSONObject) channel_list.get(i);
                    j.put("value", values[i]);
                    j.remove("table_device_guid");
                    j.remove("channel_guid");
                    j.remove("device_name");
                } else {
                    int var = Integer.parseInt(values[i], 16);
                    String var_str = String.valueOf(var);
                    JSONObject j = (JSONObject) channel_list.get(i);
                    j.put("value", var_str);
                    j.remove("table_device_guid");
                    j.remove("channel_guid");
                    j.remove("device_name");
                }
            }
        }

        finnal_message.setCode("0");
        finnal_message.setContent(channel_list);
        finnal_message.setMessage("Group Channel search Successfully!");

        return finnal_message;
    }


}
