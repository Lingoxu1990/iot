package com.iot.newEditionController;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.iot.exception.BussinessException;
import com.iot.exception.ParameterException;
import com.iot.message.Message;
import com.iot.message.MessageNoContent;
import com.iot.newEditionService.NewEditionDeviceService;
import com.iot.newEditionService.NewEditionGroupService;
import com.iot.pojo.*;
import com.iot.service.AccountInfoService;
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
import java.util.List;
import java.util.UUID;

/**
 * Created by adminchen on 16/6/13.
 */


@Controller
@RequestMapping()
public class NewEditionGroupController {

    private static Logger logger = Logger.getLogger(NewEditionGroupController.class);

    @Resource
    private UserService userService;

    @Resource
    private AccountInfoService accountInfoService;

    @Resource
    private NewEditionGroupService newGroupService;
    @Resource
    private NewEditionDeviceService newDeviceService;

    //添加组
    @RequestMapping(value = "/device/group",method = RequestMethod.POST)
    @ResponseBody
    public Message addGroup(HttpServletRequest httpServletRequest) throws IOException {
        Message message = new Message();
        JSONObject jsonObject = ParamUtils.getAttributess(httpServletRequest);
        JSONArray groups = (JSONArray) jsonObject.get("table_group");
        JSONObject group = (JSONObject) groups.get(0);
        String groupName = (String) group.get("group_name");
        System.out.println("groupName:"+groupName);
        groupName=new String(groupName.getBytes("UTF-8"));
        //System.out.println("groupName:"+groupName);
        String gatewayId = (String) group.get("gateway_id");
        String user_id = (String) jsonObject.get("user_id");

        if (user_id==null||user_id.equals("")){
            throw new ParameterException("-1","user_id does not exist");
        }

        if (gatewayId==null||gatewayId.equals("")){
            throw new ParameterException("-1","gatewayId does not exist");
        }

        if (groupName==null||groupName.equals("")){
            throw new ParameterException("-1","groupName does not exist");
        }

        //实体类
        TableGroup tableGroup=new TableGroup();
        tableGroup.setGateway_id(gatewayId);
        tableGroup.setGroup_name(groupName);

        TableGroup insertGroup= newGroupService.insertGroup(user_id,tableGroup);
        //返回值
        JSONObject object=(JSONObject)JSONObject.toJSON(insertGroup);
        JSONArray jsonArray=new JSONArray();
        jsonArray.add(object);

        message.setCode("0");
        message.setMessage("group add success");
        message.setContent(jsonArray);
        return message;

    }

    //删除组
    @RequestMapping(value = "/device/group",method = RequestMethod.DELETE)
    @ResponseBody
    public MessageNoContent deleteGroup(HttpServletRequest httpServletRequest) throws IOException {
        MessageNoContent message = new MessageNoContent();
        //获取参数
        JSONObject jsonObject = ParamUtils.getAttributess(httpServletRequest);
        JSONArray jsonArray = (JSONArray) jsonObject.get("table_group");
        JSONObject jsonObject1 = (JSONObject) jsonArray.get(0);
        String group_guid = (String) jsonObject1.get("group_guid");
        String group_addr = (String) jsonObject1.get("group_addr");
        String gateway_id=(String)jsonObject1.get("gateway_id");
        String user_id = (String) jsonObject.get("user_id");

        if(user_id==null||user_id.equals("")){
            throw new ParameterException("-1","user_id does not exist");
        }

        if(group_guid==null||group_guid.equals("")){
            throw new ParameterException("-1","group_guid does not exist");
        }

        if(group_addr==null||group_addr.equals("")){
            throw new ParameterException("-1","group_addr does not exist");
        }

        if(gateway_id==null||gateway_id.equals("")){
            throw new ParameterException("-1","gateway_id does not exist");
        }

        TableGroup tableGroup=new TableGroup();
        tableGroup.setGateway_id(gateway_id);
        tableGroup.setGroup_addr(group_addr);
        tableGroup.setGroup_guid(group_guid);

        // 删除组
         newGroupService.deleteGroup(user_id,tableGroup,jsonObject);

        message.setCode("0");
        message.setMessage("group delete success!");
        return message;
//
    }

    //组名修改
    @RequestMapping(value = "/edition/new/group",method = RequestMethod.PUT)
    @ResponseBody
    public Message updateGroup(HttpServletRequest httpServletRequest) {
        Message message = new Message();
        try {
            //获取参数
            JSONObject jsonObject = ParamUtils.getJsonObjectFromRequest(httpServletRequest);
            JSONArray jsonArray = (JSONArray) jsonObject.get("table_group");
            JSONObject jsonObject1 = (JSONObject) jsonArray.get(0);
            String user_id = (String) jsonObject.get("user_id");
            String group_guid = (String) jsonObject1.get("group_guid");
            String group_addr = (String) jsonObject1.get("group_addr");
            String group_name = (String) jsonObject1.get("group_name");
            String gateway_id = (String) jsonObject1.get("gateway_id");
            List<UserGateway> list = userService.selectGatewayByUserId(user_id);
            String account_id = list.get(0).getAccount_id();
            TableGroup tableGroup = new TableGroup();
            tableGroup.setGroup_addr(group_addr);
            tableGroup.setGroup_guid(group_guid);
            tableGroup.setAccount_id(account_id);
            tableGroup.setGroup_name(group_name);
            tableGroup.setGateway_id(gateway_id);
            message = newGroupService.updateGoup(tableGroup);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return message;
    }

    //查找用户下的组
    @RequestMapping(value = "/edition/new/group",method = RequestMethod.GET)
    @ResponseBody
    public Message findGroup(HttpServletRequest httpServletRequest){
        Message message=new Message();
        try{
            //参数初始化
            JSONObject jsonObject = ParamUtils.getJsonObjectFromRequest(httpServletRequest);
            JSONObject jsonObject1=(JSONObject)jsonObject.get("group");
            String user_id=(String)jsonObject1.get("user_id");
            String gateway_id=(String)jsonObject1.get("gateway_id");
            List<UserGateway> list=userService.selectGatewayByUserId(user_id);
            String account_id=list.get(0).getAccount_id();
            TableGroup tableGroup=new TableGroup();
            tableGroup.setGateway_id(gateway_id);
            tableGroup.setAccount_id(account_id);
            List<TableGroup> list1=newGroupService.findGroup(tableGroup);
            if (list1.size()>0){
                message.setCode("0");
                message.setMessage("Group query success");
                JSONArray jsonArray=new JSONArray();
                jsonArray=(JSONArray)JSONArray.toJSON(list1);
                message.setContent(jsonArray);
            }else {
                message.setCode("-1");
                message.setMessage("Group query is empty");
                message.setContent("[]");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return message;
    }

    //添加组成员
    @RequestMapping(value = "/device/group/group_member", method = RequestMethod.POST)
    @ResponseBody
    public MessageNoContent addGroupMembers(HttpServletRequest httpServletRequest) throws IOException {

        JSONObject jsonObject = ParamUtils.getAttributess(httpServletRequest);
        JSONArray jsonArray = (JSONArray) jsonObject.get("table_group_members");
        JSONObject jsonObject1 = (JSONObject) jsonArray.get(0);
        String user_id = (String) jsonObject.get("user_id");
        String gateway_id = (String) jsonObject1.get("gateway_id");
        String group_guid = (String) jsonObject1.get("table_group_guid");
        String group_addr = (String) jsonObject1.get("group_addr");
        String device_guid = (String) jsonObject1.get("device_guid");
        String device_addr = (String) jsonObject1.get("device_addr");
        String device_name=(String)jsonObject1.get("device_name");

        if (user_id==null||user_id.equals("")){
            throw new ParameterException("-1","user_id does not exist");
        }

        if (gateway_id==null||gateway_id.equals("")){
            throw new ParameterException("-1","gateway_id does not exist");
        }

        if (group_guid==null||group_guid.equals("")){
            throw new ParameterException("-1","group_guid does not exist");
        }

        if (group_addr==null||group_addr.equals("")){
            throw new ParameterException("-1","group_addr does not exist");
        }

        if (device_guid==null||device_guid.equals("")){
            throw new ParameterException("-1","device_guid does not exist");
        }

        if (device_addr==null||device_addr.equals("")){
            throw new ParameterException("-1","device_addr does not exist");
        }

        if (device_name==null||device_name.equals("")){
            throw new ParameterException("-1","device_name does not exist");
        }

        //实体类
        TableGroupMembers tableGroupMembers=new TableGroupMembers();
        tableGroupMembers.setGateway_id(gateway_id);
        tableGroupMembers.setTable_group_guid(group_guid);
        tableGroupMembers.setGroup_addr(group_addr);
        tableGroupMembers.setDevice_guid(device_guid);
        tableGroupMembers.setDevice_addr(device_addr);
        tableGroupMembers.setDevice_name(device_name);
        tableGroupMembers.setId(UUID.randomUUID().toString());
        tableGroupMembers.setGroup_members_guid(UUID.randomUUID().toString());

        //添加成员
        TableGroupMembers tableGroupMembers1 = newGroupService.insertGroupMembers(user_id,tableGroupMembers,jsonObject);
        //返回值
        JSONObject object=(JSONObject)JSONObject.toJSON(tableGroupMembers1);
        MessageNoContent message=new MessageNoContent();
        message.setCode("0");
        message.setMessage("groupMembers add success");

        return message;
//
    }

    //查询组成员
    @RequestMapping( value = "/edition/device/group/group_member", method = RequestMethod.GET)
    @ResponseBody
    public Message findGroupMembers(HttpServletRequest httpServletRequest) {
        Message message = new Message();
        try {
            JSONObject jsonObject = ParamUtils.getJsonObjectFromRequest(httpServletRequest);
            JSONObject jsonObject1 = (JSONObject) jsonObject.get("group_member");
            String gateway_id = (String) jsonObject1.get("gateway_id");
            String user_id = (String) jsonObject1.get("user_id");
            String table_region_guid = (String) jsonObject1.get("table_group_guid");
            List<UserGateway> list = userService.selectGatewayByUserId(user_id);
            if (list.size()<1){
                message.setCode("-1");
                message.setMessage("user does not exist");
                message.setContent(new JSONArray());
                return message;
            }
            String account_id = list.get(0).getAccount_id();
            //设置实体类TableRegionGroup
            TableGroupMembers tableGroupMembers=new TableGroupMembers();
            tableGroupMembers.setTable_group_guid(table_region_guid);
            tableGroupMembers.setGateway_id(gateway_id);
            tableGroupMembers.setAccount_id(account_id);
            List<TableGroupMembers> list1=newGroupService.findGroupMember(tableGroupMembers);
            if (list1.size()>0){
                message.setCode("0");
                message.setMessage("GroupMembers querys success");
                JSONArray jsonArray=new JSONArray();
                jsonArray=(JSONArray)JSONArray.toJSON(list1);
                message.setContent(jsonArray);
            }else {
                message.setCode("0");
                message.setMessage("there is no group members in the group");
                message.setContent(new JSONArray());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return message;
    }

    //删除组成员讯息
    @RequestMapping(value = "/device/group/group_member", method = RequestMethod.DELETE)
    @ResponseBody
    public MessageNoContent deleteGroupMembers(HttpServletRequest httpServletRequest) throws IOException {
        //获取初始化参数
        JSONObject jsonObject = ParamUtils.getAttributess(httpServletRequest);
        JSONArray jsonGroupMembers = (JSONArray) jsonObject.get("table_group_members");
        JSONObject jsonGroupMember = (JSONObject) jsonGroupMembers.get(0);
        String gateway_id = (String) jsonGroupMember.get("gateway_id");
        String group_addr = (String) jsonGroupMember.get("group_addr");
        String device_addr=(String)jsonGroupMember.get("device_addr");
        String group_guid = (String) jsonGroupMember.get("table_group_guid");
        String user_id = (String) jsonObject.get("user_id");
        if (user_id==null||user_id.equals("")){
            throw new ParameterException("-1","user_id does not exist");
        }

        if (gateway_id==null||gateway_id.equals("")){
            throw new ParameterException("-1","gateway_id does not exist");
        }

        if (group_addr==null||group_addr.equals("")){
            throw new ParameterException("-1","group_addr does not exist");
        }

        if (device_addr==null||device_addr.equals("")){
            throw new ParameterException("-1","device_addr does not exist");
        }

        if (group_guid==null||group_guid.equals("")){
            throw new ParameterException("-1","group_guid does not exist");
        }

        TableGroupMembers tableGroupMembers=new TableGroupMembers();
        tableGroupMembers.setDevice_addr(device_addr);
        tableGroupMembers.setGateway_id(gateway_id);
        tableGroupMembers.setGroup_addr(group_addr);
        tableGroupMembers.setTable_group_guid(group_guid);

        //删除组成员
        newGroupService.deleteGroupMembers(user_id,tableGroupMembers,jsonObject);
        MessageNoContent message=new MessageNoContent();
        message.setCode("0");
        message.setMessage("groupMembers delete success");
        return message;

    }

    //修改组成员
    @RequestMapping(value = "/edition/new/group/members", method = RequestMethod.PUT)
    @ResponseBody
    public Message modifyGroupMembers(HttpServletRequest httpServletRequest) {
        Message message = new Message();
        try {
            JSONObject jsonObject = ParamUtils.getJsonObjectFromRequest(httpServletRequest);
            JSONArray jsonArray = (JSONArray) jsonObject.get("table_group_members");
            JSONObject jsonObject1 = (JSONObject) jsonArray.get(0);
            String user_id = (String) jsonObject.get("user_id");
            String gateway_id = (String) jsonObject1.get("gateway_id");
            String group_members_guid = (String) jsonObject1.get("group_members_guid");
            String group_guid = (String) jsonObject1.get("table_group_guid");
            String group_addr = (String) jsonObject1.get("group_addr");
            String device_addr = (String) jsonObject1.get("device_addr");
            String device_guid = (String) jsonObject1.get("device_guid");
            String device_name = (String) jsonObject1.get("device_name");
            List<UserGateway> list = userService.selectGatewayByUserId(user_id);
            String account_id = list.get(0).getAccount_id();
            TableGroupMembers tableGroupMembers = new TableGroupMembers();
            tableGroupMembers.setDevice_addr(device_addr);
            tableGroupMembers.setDevice_guid(device_guid);
            tableGroupMembers.setDevice_name(device_name);
            tableGroupMembers.setGroup_members_guid(group_members_guid);
            tableGroupMembers.setTable_group_guid(group_guid);
            tableGroupMembers.setGroup_addr(group_addr);
            tableGroupMembers.setGateway_id(gateway_id);
            tableGroupMembers.setAccount_id(account_id);
            int n = newGroupService.modifyGroupMembers(tableGroupMembers);
            if (n > 0) {
                message.setCode("0");
                message.setMessage("GroupMembers successful modification");
                message.setContent("");
            } else {
                message.setCode("-1");
                message.setMessage("GroupMembers modification failed");
                message.setContent("[]");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return message;
    }

    //查询组通道
    @RequestMapping(value = "/edition/device/group/group_channel" ,method = RequestMethod.GET)
    @ResponseBody
    public Object findGroupChannel(HttpServletRequest httpServletRequest) throws IOException {

        JSONObject jj = ParamUtils.getJsonObjectFromRequest(httpServletRequest);
        Message message = new Message();


        JSONObject jsonObject = (JSONObject) jj.get("group_channel");

        String user_id = (String) jsonObject.get("user_id");
        jsonObject.remove("user_id");

        String gateway_id = (String) jsonObject.get("gateway_id");
        String table_group_guid = (String) jsonObject.get("table_group_guid");

        List<UserGateway> list = userService.selectGatewayByUserId(user_id);
        if (list.size()<1){
            message.setCode("-1");
            message.setMessage("user does not exist");
            message.setContent("[]");
            return message;
        }
        String account_id = list.get(0).getAccount_id();

        TableGroupMembers tableGroupMembers = new TableGroupMembers();
        tableGroupMembers.setAccount_id(account_id);
        tableGroupMembers.setGateway_id(gateway_id);
        tableGroupMembers.setTable_group_guid(table_group_guid);

        List<TableGroupMembers> groupMembers= newGroupService.findGroupMember(tableGroupMembers);

        if (groupMembers.size()<1){

            message.setCode("0");
            message.setContent(new JSONArray());
            message.setMessage("There is no members in the group!");
            return message;
        }
        TableGroupMembers tableGroupMember = groupMembers.get(0);
        String deviceGuid = tableGroupMember.getDevice_guid();
        TableChannel tableChannel = new TableChannel();
        tableChannel.setGateway_id(gateway_id);
        tableChannel.setTable_device_guid(deviceGuid);
        tableChannel.setAccount_id(account_id);

        List<TableChannel>  channelList  = newDeviceService.findChannelInfo(tableChannel);

        TableGroup groupTemp = new TableGroup();
        groupTemp.setAccount_id(account_id);
        groupTemp.setGateway_id(gateway_id);
        groupTemp.setGroup_guid(table_group_guid);

        List<TableGroup> groupList = newGroupService.findGroup(groupTemp);

        if (groupList.size()<1){
            message.setCode("-1");
            message.setMessage("Group is not exits");
            message.setContent(new JSONArray());
            return message;
        }

        TableGroup group = groupList.get(0);
        String groupValue =  group.getGroup_value();

        int len = groupValue.length()/2;

        String[]  devices=null;
        if (len==0){
            devices = new String[1];
        }else {
            devices = new String[len];
        }
        //判断通道数据跟组值拆分数据数量是否一致
        if (devices.length!=channelList.size()){
            throw new BussinessException("-1","Channel number is not consistent");
        }
        for (int j = 0; j <devices.length ; j++) {
            StringBuilder SB = new StringBuilder();
            SB.append(groupValue);
            if (j==(devices.length-1)){
                SB.delete(0,j*2);
                devices[j]=SB.toString();
            }else {
                devices[j]=SB.substring(j*2,j*2+2).toString();
            }
        }

        JSONArray result = new JSONArray();

        for (TableChannel channel:channelList) {

            JSONObject channelTemp = new JSONObject();
            channelTemp.put("channel_number",channel.getChannel_number());

            String strChannelNum = channel.getChannel_number();
            int intChannelNum = Integer.parseInt(strChannelNum);

            String hexChannelValue = devices[intChannelNum-1];
            int channelValue = Integer.valueOf(hexChannelValue,16);
            String strChannelValue = String.valueOf(channelValue);
            channelTemp.put("channel_value",strChannelValue);
            channelTemp.put("channel_name",channel.getChannel_name());

            result.add(channelTemp);
        }

        message.setCode("0");
        message.setMessage("Group channel query successfully!");
        message.setContent(result);
        return message;
    }

    //组控制
    @RequestMapping(value = "/edition/device/group/group_control",method = RequestMethod.PUT)
    @ResponseBody
    public Message GroupController(HttpServletRequest httpServletRequest) throws IOException {
        Message message=new Message();

        JSONObject jsonObject = ParamUtils.getJsonObjectFromRequest(httpServletRequest);
        jsonObject.remove("user_id");
        JSONArray table_groups = (JSONArray) jsonObject.get("table_group");
        JSONObject table_group = (JSONObject) table_groups.get(0);
        String gateway_id = (String) table_group.get("gateway_id");
        String group_switch = (String) table_group.get("group_switch");
        JSONArray groupValue = (JSONArray) table_group.get("group_value");
        String group_delay = (String) table_group.get("group_delay");
        String group_guid = (String) table_group.get("group_guid");
        String group_addr = (String) table_group.get("group_addr");


        String user_id = (String) jsonObject.get("user_id");

        if (user_id==null||user_id.equals("")){
            throw new ParameterException("-1","user_id does not exist");
        }

        if (group_addr==null||group_addr.equals("")){
            throw new ParameterException("-1","group_addr does not exist");
        }

        if (group_guid==null||group_guid.equals("")){
            throw new ParameterException("-1","group_guid does not exist");
        }

        if (group_delay==null||group_delay.equals("")){
            throw new ParameterException("-1","group_delay does not exist");
        }

        if (gateway_id==null||gateway_id.equals("")){
            throw new ParameterException("-1","gateway_id does not exist");
        }

        if ((groupValue != null||!groupValue.equals("")) && (group_switch != null||!group_switch.equals(""))) {
            throw new ParameterException("-1","Can't not control the value and switch at one moment");
        }

        if ((groupValue == null||groupValue.equals("")) && (group_switch == null||group_switch.equals(""))) {
            throw new ParameterException("-1","Missing the control content");
        }


        //实体类
        TableGroup tableGroup = new TableGroup();
        if ((groupValue != null||!groupValue.equals("")) && (group_switch == null||group_switch.equals(""))) {

            //实体类
            tableGroup.setGateway_id(gateway_id);
            tableGroup.setGroup_guid(group_guid);
            tableGroup.setGroup_delay(group_delay);
            tableGroup.setGroup_addr(group_addr);
            //修改组控制


        }

        if ((groupValue == null||groupValue.equals("")) && (group_switch != null||!group_switch.equals(""))) {
            //实体类
            tableGroup.setGateway_id(gateway_id);
            tableGroup.setGroup_guid(group_guid);
            tableGroup.setGroup_delay(group_delay);
            tableGroup.setGroup_switch(group_switch);
            tableGroup.setGroup_addr(group_addr);
        }
        TableGroup tableGroup1=newGroupService.groupController(user_id,tableGroup, groupValue,jsonObject);
        //返回值
        JSONObject object=(JSONObject)JSONObject.toJSON(tableGroup1);
        message.setCode("0");
        message.setMessage("Group control success");
        message.setContent(object);
        return message;
// try {
//
//
//            JSONObject jsonObject;
//            jsonObject = getJsonObjectFromRequest();
//            String user_id = (String) jsonObject.get("user_id");
//            jsonObject.remove("user_id");
//
//            JSONArray table_groups = (JSONArray) jsonObject.get("table_group");
//
//            JSONObject table_group = (JSONObject) table_groups.get(0);
//
//            String gateway_id = (String) table_group.get("gateway_id");
//            String group_switch = (String) table_group.get("group_switch");
//            JSONArray groupValue = (JSONArray) table_group.get("group_value");
//            String group_delay = (String) table_group.get("group_delay");
//            String group_guid = (String) table_group.get("group_guid");
//
//            table_group.remove("gateway_id");
//
//            List<UserGateway> list = userService.selectGatewayByUserId(user_id);
//            if (list.size() < 1) {
//                message.setCode("-1");
//                message.setMessage("user does not exist");
//                message.setContent("[]");
//                return message;
//            }
//            String account_id = list.get(0).getAccount_id();
//
//            String SourceId = account_id + Param.WEBAPPID + String.valueOf(System.currentTimeMillis() / 1000000000);
//
//            //实体类
//            TableGroup tableGroup = new TableGroup();
//            tableGroup.setAccount_id(account_id);
//            tableGroup.setGateway_id(gateway_id);
//            tableGroup.setGroup_guid(group_guid);
//            tableGroup.setGroup_delay(group_delay);
//
//
//            if (groupValue != null && group_switch != null) {
//                message.setCode("-1");
//                message.setContent(new JSONArray());
//                message.setMessage("Can't not control the value and switch at one moment");
//                return message;
//
//            }
//            if (groupValue == null && group_switch == null) {
//                message.setCode("-1");
//                message.setContent(new JSONArray());
//                message.setMessage("Missing the control content!");
//                return message;
//
//            }
//
//
//            if (groupValue != null && group_switch == null) {
//
//                String[] values = new String[groupValue.size()];
//                System.out.println(values.length);
//
//
//                for (int j = 0; j < groupValue.size(); j++) {
//                    System.out.println("j"+j);
//                    JSONObject temp = (JSONObject) groupValue.get(j);
//
//                    int channelNumber = Integer.parseInt((String) temp.get("channel_number")) - 1;
//
//                    int channelValue = Integer.parseInt((String) temp.get("channel_value"));
//                    String channelValues = "";
//
//                    if (channelValue < 16) {
//                        channelValues = "0" + Integer.toHexString(channelValue);
//                    } else {
//                        channelValues = Integer.toHexString(channelValue);
//                    }
//                    values[channelNumber] = channelValues;
//                }
//
//                String R = "";
//                for (int j = 0; j < values.length; j++) {
//                    R += values[j];
//                }
//                table_group.remove("group_value");
//                table_group.remove("gateway_id");
//                table_group.put("group_value", R);
//
//                JSONObject jsonControlSocket = newGroupService.socketControlGroup(jsonObject, gateway_id, SourceId, 2);
//
//                String status = String.valueOf(jsonControlSocket.get("Status"));
//
//                if ("1".equals(status)) {
//                    message.setCode("-1");
//                    message.setContent(new JSONArray());
//                    message.setMessage("Sub-gateway return status '1' means failed to control the group");
//                    return message;
//                }
//                if ("2".equals(status)) {
//                    message.setCode("-1");
//                    message.setContent(new JSONArray());
//                    message.setMessage("Sub-gateway return status '2' means the group is offline");
//                    return message;
//                }
//
//            }
//            if (groupValue == null && group_switch != null) {
//
//
//                tableGroup.setGroup_switch(group_switch);
//                JSONObject jsonControlSocket = newGroupService.socketControlGroup(jsonObject, gateway_id, SourceId, 2);
//
//                String status = String.valueOf(jsonControlSocket.get("Status"));
//
//                if ("1".equals(status)) {
//                    message.setCode("-1");
//                    message.setContent(new JSONArray());
//                    message.setMessage("Sub-gateway return status '1' means failed to control the group");
//                    return message;
//                }
//                if ("2".equals(status)) {
//                    message.setCode("-1");
//                    message.setContent(new JSONArray());
//                    message.setMessage("Sub-gateway return status '2' means the group is offline");
//                    return message;
//                }
//
//            }
//            //jsonArray1组值---通道值跟通道的排序
//            int n = newGroupService.groupController(tableGroup, groupValue);
//            if (n > 0) {
//                message.setCode("0");
//                message.setMessage("Group control success");
//                jsonObject.remove("user_id");
//                message.setContent(jsonObject);
//            } else {
//                message.setMessage("Group control failed");
//                message.setCode("-1");
//                message.setContent(new JSONArray());
//            }
//        }catch (Exception e){
//            e.printStackTrace();
//
//        }

        //return message;
    }
    

}
