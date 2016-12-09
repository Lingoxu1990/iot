package com.iot.newController;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.iot.exception.BussinessException;


import com.iot.exception.ParameterException;

import com.iot.message.Message;
import com.iot.message.MessageNoContent;
import com.iot.newService.NewDeviceService;
import com.iot.newService.NewGroupService;
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
 * Created by adminchen on 16/6/13.
 */


@Controller
@RequestMapping()
public class NewGroupController {

    private static Logger logger = Logger.getLogger(NewGroupController.class);

    @Resource
    private UserService userService;

    @Resource
    private AccountInfoService accountInfoService;

    @Resource
    private NewGroupService newGroupService;
    @Resource
    private NewDeviceService newDeviceService;



    //删除组
    @RequestMapping(value = "/device/group/old/old/old",method = RequestMethod.DELETE)
    @ResponseBody
    public Message deleteGroup(HttpServletRequest httpServletRequest) {
        Message message = new Message();
        try {
            //获取参数
            JSONObject jsonObject = ParamUtils.getJsonObjectFromRequest(httpServletRequest);
            String user_id = (String) jsonObject.get("user_id");
            jsonObject.remove("user_id");

            JSONArray jsonArray = (JSONArray) jsonObject.get("table_group");
            JSONObject jsonObject1 = (JSONObject) jsonArray.get(0);
            //String group_guid = (String) jsonObject1.get("group_guid");
            String group_addr = (String) jsonObject1.get("group_addr");
            String gateway_id=(String)jsonObject1.get("gateway_id");

            List<UserGateway> list = userService.selectGatewayByUserId(user_id);
            String account_id = list.get(0).getAccount_id();
            String SourceId = account_id + Param.WEBAPPID + String.valueOf(System.currentTimeMillis() / 1000000000);

            //设置
            TableGroup tableGroup = new TableGroup();
            tableGroup.setAccount_id(account_id);
            //tableGroup.setGroup_guid(group_guid);
            tableGroup.setGroup_addr(group_addr);
            tableGroup.setGateway_id(gateway_id);


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

            jsonObject.remove("gateway_id");
            JSONObject socketGroupDeleteResult = newGroupService.socketDeleteGroup(jsonObject,gateway_id,SourceId,2);
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


            TableRegionGroup regionGroup = new TableRegionGroup();
            regionGroup.setAccount_id(account_id);
            regionGroup.setGateway_id(gateway_id);
            regionGroup.setGroup_addr(group_addr);
            TableRegionGroup resultRegionGroup=newGroupService.findRegionGroupByGroupAddr(regionGroup);


            JSONObject socketRegionGroupParam = new JSONObject();
            JSONArray jsonRegionGrouos =new JSONArray();
            JSONObject jsonRegionGroup = new JSONObject();

//            jsonRegionGroup.put("gateway_id",resultRegionGroup.getGateway_id());
            jsonRegionGroup.put("table_group_guid",resultRegionGroup.getTable_group_guid());
            jsonRegionGroup.put("region_guid",resultRegionGroup.getRegion_guid());

            jsonRegionGrouos.add(jsonRegionGroup);

            socketRegionGroupParam.put("table_region_group",jsonRegionGrouos);

            JSONObject socketRegionGroupDeleteResult = newGroupService.socketDeleteRegionGroup(socketRegionGroupParam,resultRegionGroup.getGateway_id(),SourceId,2);

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

    //组名修改
    @RequestMapping(value = "/new/group",method = RequestMethod.PUT)
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
    @RequestMapping(value = "/new/group",method = RequestMethod.GET)
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
                message.setContent(new JSONArray());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return message;
    }



    //查询组成员
    @RequestMapping( value = "/device/group/group_member", method = RequestMethod.GET)
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
//                message.setCode("-1");
//                message.setMessage("user does not exist");
//                message.setContent(new JSONArray());
//                return message;
                throw new BussinessException("-1","user does not exist");
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
//                message.setCode("0");
//                message.setMessage("there is no group members in the group");
//                message.setContent(new JSONArray());
                throw new BussinessException("0","there is no group members in the group");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return message;
    }

    //删除组成员讯息
    @RequestMapping(value = "/device/group/group_member/old/old/old", method = RequestMethod.DELETE)
    @ResponseBody
    public Message deleteGroupMembers(HttpServletRequest httpServletRequest) {
        Message message = new Message();
        try {
            //获取初始化参数
            JSONObject jsonObject = ParamUtils.getJsonObjectFromRequest(httpServletRequest);
            JSONArray jsonGroupMembers = (JSONArray) jsonObject.get("table_group_members");

            JSONObject jsonGroupMember = (JSONObject) jsonGroupMembers.get(0);


            String gateway_id = (String) jsonGroupMember.get("gateway_id");
            String group_addr = (String) jsonGroupMember.get("group_addr");
            String device_addr=(String)jsonGroupMember.get("device_addr");

            jsonGroupMember.remove("gateway_id");

            String user_id = (String) jsonObject.get("user_id");

            jsonObject.remove("user_id");

            List<UserGateway> list = userService.selectGatewayByUserId(user_id);
            if (list.size()<1){
                message.setCode("-1");
                message.setMessage("user does not exist");
                message.setContent("[]");
                return message;
            }

            String account_id = list.get(0).getAccount_id();
            String SourceId = account_id + Param.WEBAPPID + String.valueOf(System.currentTimeMillis() / 1000000000);

            JSONObject socketResult = newGroupService.socketDeleteGroupMembers(jsonObject,gateway_id,SourceId,2);

            String status = String.valueOf(socketResult.get("Status"));
            if ("2".equals(status)){
                message.setCode("-1");
                message.setContent(new JSONArray());
                message.setMessage("Sub gateway retrun status '2' means the device is offline!");
                return message;
            }

            if ("1".equals(status)){
                message.setCode("-1");
                message.setContent(new JSONArray());
                message.setMessage("Sub gateway return status '1' means that: failed to add the member into the group!");
                return message;
            }


            //设置TableMembers实体
            TableGroupMembers tableGroupMembers = new TableGroupMembers();
            tableGroupMembers.setGroup_addr(group_addr);
            tableGroupMembers.setGateway_id(gateway_id);
            tableGroupMembers.setAccount_id(account_id);
            tableGroupMembers.setDevice_addr(device_addr);
            int n = newGroupService.deleteGroupMembers(tableGroupMembers);
            if (n > 0) {
                message.setCode("0");
                message.setMessage("GroupMembers delete success");
                message.setContent(new JSONArray());
            } else {
                message.setCode("-1");
                message.setMessage("failed to remove the device from the groups,because the cloud database operation is failed!");
                message.setContent(new JSONArray());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return message;
    }

    //修改组成员
    @RequestMapping(value = "/new/group/members", method = RequestMethod.PUT)
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
    @RequestMapping(value = "/device/group/group_channel" ,method = RequestMethod.GET)
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
//            message.setCode("-1");
//            message.setMessage("user does not exist");
//            message.setContent("[]");
//            return message;
            throw new BussinessException("-1","user does not exist");
        }
        String account_id = list.get(0).getAccount_id();
        System.out.println(account_id);

        TableGroupMembers tableGroupMembers = new TableGroupMembers();
        tableGroupMembers.setAccount_id(account_id);
        tableGroupMembers.setGateway_id(gateway_id);
        tableGroupMembers.setTable_group_guid(table_group_guid);

        List<TableGroupMembers> groupMembers= newGroupService.findGroupMember(tableGroupMembers);

        if (groupMembers.size()<1){

//            message.setCode("0");
//            message.setContent(new JSONArray());
//            message.setMessage("There is no members in the group!");
//            return message;
            throw new BussinessException("0","There is no members in the group!");
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
//            message.setCode("-1");
//            message.setMessage("Group is not exits");
//            message.setContent(new JSONArray());
//            return message;
            throw new BussinessException("-1","Group is not exits");
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
    @RequestMapping(value = "/device/group/group_control",method = RequestMethod.PUT)
    @ResponseBody
    public MessageNoContent GroupController(HttpServletRequest httpServletRequest){
        MessageNoContent message=new MessageNoContent();
        try {
            JSONObject jsonObject = ParamUtils.getAttributess(httpServletRequest);
            String user_id = (String) jsonObject.get("user_id");
            jsonObject.remove("user_id");

            JSONArray table_groups = (JSONArray) jsonObject.get("table_group");

            JSONObject table_group = (JSONObject) table_groups.get(0);

            String gateway_id = (String) table_group.get("gateway_id");
            String group_switch = (String) table_group.get("group_switch");
            JSONArray groupValue = (JSONArray) table_group.get("group_value");
            String group_delay = (String) table_group.get("group_delay");
            String group_guid = (String) table_group.get("group_guid");

            table_group.remove("gateway_id");

            List<UserGateway> list = userService.selectGatewayByUserId(user_id);
            if (list.size() < 1) {
//                message.setCode("-1");
//                message.setMessage("user does not exist");
//                message.setContent("[]");
//                return message;
                throw new BussinessException("-1","user does not exist");
            }
            String account_id = list.get(0).getAccount_id();

            String SourceId = account_id + Param.WEBAPPID + String.valueOf(System.currentTimeMillis() / 1000000000);

            //实体类
            TableGroup tableGroup = new TableGroup();
            tableGroup.setAccount_id(account_id);
            tableGroup.setGateway_id(gateway_id);
            tableGroup.setGroup_guid(group_guid);
            tableGroup.setGroup_delay(group_delay);


            if (groupValue != null && group_switch != null) {
//                message.setCode("-1");
//                message.setContent(new JSONArray());
//                message.setMessage("Can't not control the value and switch at one moment");
//                return message;
                throw new ParameterException("-1","Can't not control the value and switch at one moment");

            }
            if (groupValue == null && group_switch == null) {
//                message.setCode("-1");
//                message.setContent(new JSONArray());
//                message.setMessage("Missing the control content!");
//                return message;
                throw new ParameterException("-1","Missing the control content!");

            }

            if (groupValue != null && group_switch == null) {

                String[] values = new String[groupValue.size()];
                for (int j = 0; j < groupValue.size(); j++) {
                    JSONObject temp = (JSONObject) groupValue.get(j);

                    int aa = Integer.parseInt((String) temp.get("channel_number")) - 1;

                    int a = Integer.parseInt((String) temp.get("channel_value"));
                    String bb = "";

                    if (a < 16) {
                        bb = "0" + Integer.toHexString(a);
                    } else {
                        bb = Integer.toHexString(a);
                    }
                    values[aa] = bb;
                }

                String R = "";
                for (int j = 0; j < values.length; j++) {
                    R += values[j];
                }
                table_group.remove("group_value");
                table_group.remove("gateway_id");
                table_group.put("group_value", R);

                JSONObject jsonControlSocket = newGroupService.socketControlGroup(jsonObject, gateway_id, SourceId, 2);

                String status = String.valueOf(jsonControlSocket.get("Status"));

                if ("1".equals(status)) {
//                    message.setCode("-1");
//                    message.setContent(new JSONArray());
//                    message.setMessage("Sub-gateway return status '1' means failed to control the group");
//                    return message;
                    throw new BussinessException("-1","Sub-gateway return status '1' means failed to control the group");
                }
                if ("2".equals(status)) {
//                    message.setCode("-1");
//                    message.setContent(new JSONArray());
//                    message.setMessage("Sub-gateway return status '2' means the group is offline");
//                    return message;
                    throw new BussinessException("-1","Sub-gateway return status '2' means the group is offline");
                }

            }
            if (groupValue == null && group_switch != null) {


                tableGroup.setGroup_switch(group_switch);
                JSONObject jsonControlSocket = newGroupService.socketControlGroup(jsonObject, gateway_id, SourceId, 2);

                String status = String.valueOf(jsonControlSocket.get("Status"));

                if ("1".equals(status)) {
//                    message.setCode("-1");
//                    message.setContent(new JSONArray());
//                    message.setMessage("Sub-gateway return status '1' means failed to control the group");
//                    return message;
                    throw new BussinessException("-1","Sub-gateway return status '1' means failed to control the group");
                }
                if ("2".equals(status)) {
//                    message.setCode("-1");
//                    message.setContent(new JSONArray());
//                    message.setMessage("Sub-gateway return status '2' means the group is offline");
//                    return message;
                    throw new BussinessException("-1","Sub-gateway return status '2' means the group is offline");
                }

            }
            //jsonArray1组值---通道值跟通道的排序
            int n = newGroupService.groupController(tableGroup, groupValue);
            if (n > 0) {
                message.setCode("0");
                message.setMessage("Group control success");

            } else {
//                message.setMessage("Group control failed");
//                message.setCode("-1");
//                message.setContent(new JSONArray());
                throw new BussinessException("-1","Group control failed");
            }
        }catch (Exception e){
            e.printStackTrace();

        }

        return message;
    }
    

}
