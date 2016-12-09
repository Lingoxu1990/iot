package com.iot.newEditionServiceImpl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.iot.socketUtil.OutPutSocketMessage;
import com.iot.exception.BussinessException;
import com.iot.mapper.*;
import com.iot.message.Message;
import com.iot.newEditionService.NewEditionGroupService;
import com.iot.pojo.*;
import com.iot.spitUtil.Param;
import com.iot.sqlControlUtil.SqlControlUtil;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Created by adminchen on 16/6/13.
 */
@Service
public class NewEditionGroupServiceImpl implements NewEditionGroupService {

    private static Logger logger = Logger.getLogger(NewEditionGroupServiceImpl.class);
    @Resource
    private TableGroupMapper tableGroupMapper;

    @Resource
    private TableGroupMembersMapper tableGroupMembersMapper;

    @Resource
    private TableRegionGroupMapper tableRegionGroupMapper;

    @Resource
    private TableDeviceMapper tableDeviceMapper;

    @Resource
    private TableChannelMapper tableChannelMapper;

    @Resource
    private UserGatewayMapper userGatewayMapper;

    @Resource
    private AccountDataInfoMapper accountDataInfoMapper;

    public TableGroup insertGroup(String user_id, TableGroup tableGroup) {
        List<UserGateway> list = userGatewayMapper.selectByUserId(user_id);
        if (list.size() < 1) {
            throw new BussinessException("-1", "user does not exist");
        }

        String account_id = list.get(0).getAccount_id();

        //实体类
        AccountDataInfo accountDataInfo = new AccountDataInfo();
        accountDataInfo.setAccount_id(account_id);
        accountDataInfo.setGateway_id(tableGroup.getGateway_id());

        //获取组地址
        AccountDataInfo accountDataInfo1 = accountDataInfoMapper.selectByAccountIdAndGatewayId(accountDataInfo);
        String group_addr = accountDataInfo1.getGroup_addr();
        String groupAddr = Integer.toHexString(Integer.parseInt(group_addr, 16) + 1);

        //实体类
        tableGroup.setAccount_id(account_id);
        tableGroup.setGroup_addr("ff15::" + groupAddr);
        tableGroup.setGroup_switch("01");
        tableGroup.setGroup_value("null");
        tableGroup.setGroup_delay("0");
        tableGroup.setId(UUID.randomUUID().toString());
        tableGroup.setGroup_guid(UUID.randomUUID().toString());

        int n = tableGroupMapper.insert(tableGroup);
        if (n < 1) {
            throw new BussinessException("-1", "group add failed");
        }
        //跟新用户信息表
        accountDataInfo.setGroup_addr(groupAddr);
        int a = accountDataInfoMapper.updateByAccountIdAndGatewayId(accountDataInfo);
        if (a < 1) {
            throw new BussinessException("-1", " update accountDataInfo failed");
        }
        return tableGroup;
    }

    //删除
    public void deleteGroup(String user_id, TableGroup tableGroup, JSONObject jsonObject) {
        List<UserGateway> list = userGatewayMapper.selectByUserId(user_id);
        if (list.size() < 1) {
            throw new BussinessException("-1", "user does not exist");
        }

        String account_id = list.get(0).getAccount_id();
        String SourceId = account_id + Param.WEBAPPID + String.valueOf(System.currentTimeMillis() / 1000000000);

        //　实体类
        TableGroupMembers tableGroupMembers = new TableGroupMembers();
        tableGroupMembers.setGateway_id(tableGroup.getGateway_id());
        tableGroupMembers.setTable_group_guid(tableGroup.getGroup_guid());
        tableGroupMembers.setAccount_id(account_id);
        tableGroupMembers.setGroup_addr(tableGroup.getGroup_addr());

        //Json
        JSONObject jsonGroupMembers = new JSONObject();
        JSONObject jsonMembers = new JSONObject();
        jsonMembers.put("group_addr",tableGroupMembers.getGroup_addr());
        jsonMembers.put("gateway_id",tableGroupMembers.getGateway_id());

        JSONArray table_group_members = new JSONArray();
        table_group_members.add(jsonMembers);
        jsonGroupMembers.put("table_group_members", table_group_members);

        //删除组成员
        List<TableGroupMembers> groupMembersList = tableGroupMembersMapper.selectByGroupGuidAndAccount_idAndGateway_id(tableGroupMembers);
        if (groupMembersList.size() > 0) {
            JSONObject socketResult = socketDeleteGroup(jsonGroupMembers, tableGroup.getGateway_id(), SourceId, 2);
            if (socketResult == null) {
                throw new BussinessException("-1", "Geteway socket read time out!");
            }

            String status = String.valueOf(socketResult.get("Status"));
            if ("1".equals(status)) {
                throw new BussinessException("-1", "Sub-gateway retrun status '1' means command can't be executed");
            }
            if ("2".equals(status)) {
                throw new BussinessException("-1", "Sub-gateway return status '2' means deivce is offline");
            }
            int n = tableGroupMembersMapper.deleteByAccountIdAndGatewayAndGroup_addr(tableGroupMembers);
            if (n < 1) {
                throw new BussinessException("-1", "groupMembers delete failed");
            }
        }

        tableGroup.setAccount_id(account_id);
        List<TableGroup> groupList = tableGroupMapper.selectGroup(tableGroup);
        if (groupList.size() < 1) {
            throw new BussinessException("-1", "group does not exist");
        }

        jsonObject.remove("user_id");
        //删除组
        JSONObject socketResult1 = socketDeleteGroup(jsonObject, tableGroup.getGateway_id(), SourceId, 2);
        if (socketResult1 == null) {
            throw new BussinessException("-1", "Geteway socket read time out!");
        }

        String status1 = String.valueOf(socketResult1.get("Status"));
        if ("1".equals(status1)) {
            throw new BussinessException("-1", "Sub-gateway retrun status '1' means command can't be executed");
        }
        if ("2".equals(status1)) {
            throw new BussinessException("-1", "Sub-gateway return status '2' means deivce is offline");
        }

        int a = tableGroupMapper.deleteByAccountIdAndGroupaddrAndGateway_id(tableGroup);
        if (a < 1) {
            throw new BussinessException("-1", "group delete failed");
        }

        //实体类
        TableRegionGroup tableRegionGroup = new TableRegionGroup();
        tableRegionGroup.setAccount_id(account_id);
        tableRegionGroup.setTable_group_guid(tableGroup.getGroup_guid());
        tableRegionGroup.setGateway_id(tableGroup.getGateway_id());
        tableRegionGroup.setGroup_addr(tableGroup.getGroup_addr());
        //json
        JSONObject regionGroupjson = new JSONObject();
        JSONArray table_region_group = new JSONArray();
        JSONObject region_groupesjson = new JSONObject();
        region_groupesjson.put("group_addr",tableRegionGroup.getGroup_addr());
        region_groupesjson.put("gateway_id",tableRegionGroup.getGateway_id());
        table_region_group.add(region_groupesjson);
        regionGroupjson.put("table_region_group",table_region_group);

        List<TableRegionGroup> regionGroupList = tableRegionGroupMapper.findRegionGroupByAccountIdAndGroupGuidAndGatewayid(tableRegionGroup);
        if (regionGroupList.size() > 0) {

            JSONObject socketResult2 = socketDeleteGroup(regionGroupjson, tableGroup.getGateway_id(), SourceId, 2);
            if (socketResult2 == null) {
                throw new BussinessException("-1", "Geteway socket read time out!");
            }

            String status2 = String.valueOf(socketResult2.get("Status"));
            if ("1".equals(status2)) {
                throw new BussinessException("-1", "Sub-gateway retrun status '1' means command can't be executed");
            }
            if ("2".equals(status2)) {
                throw new BussinessException("-1", "Sub-gateway return status '2' means deivce is offline");
            }
            int p = tableRegionGroupMapper.deleteGroupOfAccount_idAndgateway_idAndAddr(tableRegionGroup);
            if (p < 1) {
                throw new BussinessException("-1", "regionGroup delete failed");
            }
        }

//
    }

    public int deleteRegionGroupes(TableGroup tableGroup) {
        return 0;
    }

    //修改
    public Message updateGoup(TableGroup tableGroup) {
        Message message = new Message();
        TableRegionGroup tableRegionGroup = new TableRegionGroup();
        tableRegionGroup.setTable_group_guid(tableGroup.getGroup_guid());
        tableRegionGroup.setGroup_addr(tableGroup.getGroup_addr());
        tableRegionGroup.setAccount_id(tableGroup.getAccount_id());
        tableRegionGroup.setGroup_name(tableGroup.getGroup_name());
        tableRegionGroup.setGateway_id(tableGroup.getGateway_id());
        TableRegionGroup RegionGroup = tableRegionGroupMapper.findRegionGroupByAccountIdAndGroupId(tableRegionGroup);
        if (RegionGroup != null) {
            int a = tableRegionGroupMapper.updateByAccountIdAndGroupGuid(tableRegionGroup);
            if (a == 0) {
                message.setCode("-1");
                message.setMessage("RegionGroup modification failed");
                message.setContent("[]");
                return message;
            }
        }

        int n = tableGroupMapper.updateByAccountId(tableGroup);
        if (n > 0) {
            message.setCode("0");
            message.setMessage("Group successful modification");
            message.setContent("");
        } else {
            message.setCode("-1");
            message.setMessage("Group modification failed");
            message.setContent("[]");
        }
        return message;
    }

    //查找组用户下所有的组
    public List<TableGroup> findGroup(TableGroup tableGroup) {
        List<TableGroup> list = tableGroupMapper.selectGroup(tableGroup);
        return list;
    }

    //查询组成员
    public List<TableGroupMembers> findGroupMembers(TableGroupMembers tableGroupMembers) {
        List<TableGroupMembers> list = tableGroupMembersMapper.selectByGroupGuidAndAccount_idAndGateway_id(tableGroupMembers);
        return list;
    }

    //查询区域下的组
//    public Message findRegionGroup(TableRegionGroup tableRegionGroup) {
//        Message message=new Message();
//        List<TableRegionGroup> list=tableRegionGroupMapper.findRegionGroupByAccountIdAndRegionId(tableRegionGroup);
//        if (list.size()>0){
//            message.setCode("0");
//            message.setMessage("RegionGroup query success");
//            JSONArray array=new JSONArray();
//            JSONObject jsonObject=new JSONObject();
//            for (int i=1;i<list.size();i++){
//                jsonObject=(JSONObject)JSONObject.toJSON(list.get(i));
//                array.add(jsonObject);
//            }
//            message.setContent(array);
//        }else if(list.size()==0){
//            message.setCode("-1");
//            message.setMessage("RegionGroup query does not have Group");
//            message.setContent("[]");
//        }else {
//            message.setCode("-1");
//            message.setMessage("RegionGroup query failed");
//            message.setContent("[]");
//        }
//        return message;
//    }

    //添加设备成员/添加组成员
    public TableGroupMembers insertGroupMembers(String user_id, TableGroupMembers tableGroupMembers, JSONObject jsonObject) {
        List<UserGateway> list = userGatewayMapper.selectByUserId(user_id);
        if (list.size() < 1) {
            throw new BussinessException("-1", "user does not exist");
        }

        String account_id = list.get(0).getAccount_id();
        String SourceId = account_id + Param.WEBAPPID + String.valueOf(System.currentTimeMillis() / 1000000000);
        tableGroupMembers.setAccount_id(account_id);

        //判断该成员是否存在
        List<TableGroupMembers>  groupMembersList = tableGroupMembersMapper.selectByGroupGuidAndAccount_idAndGateway_idAndDeviceGuid(tableGroupMembers);
        if (groupMembersList.size()>0) {
            throw new BussinessException("-1", "The member already exists");
        }

        //实体类
        TableGroup tableGroup = new TableGroup();
        tableGroup.setAccount_id(account_id);
        tableGroup.setGateway_id(tableGroupMembers.getGateway_id());
        tableGroup.setGroup_guid(tableGroupMembers.getTable_group_guid());

        // 初始化,,查询组是否有值
        List<TableGroup> tableGroup1 = tableGroupMapper.selectGroup(tableGroup);
        if (tableGroup1.size() < 1) {
            throw new BussinessException("-1", "The group does not exist");
        }
        String group_value = tableGroup1.get(0).getGroup_value();

        TableGroupMembers tableGroupMembers1 = new TableGroupMembers();
        tableGroupMembers1.setDevice_addr(tableGroupMembers.getDevice_addr());
        tableGroupMembers1.setDevice_guid(tableGroupMembers.getDevice_guid());
        tableGroupMembers1.setDevice_name(tableGroupMembers.getDevice_name());
        tableGroupMembers1.setGroup_members_guid(tableGroupMembers.getGroup_members_guid());
        tableGroupMembers1.setGroup_addr(tableGroupMembers.getGroup_addr());
        tableGroupMembers1.setTable_group_guid(tableGroupMembers.getTable_group_guid());
        tableGroupMembers1.setGateway_id(tableGroupMembers.getGateway_id());


        jsonObject.remove("user_id");
        JSONArray table_group_members = new JSONArray();
        JSONObject jsonObject1 = (JSONObject) JSONObject.toJSON(tableGroupMembers1);
        table_group_members.add(jsonObject1);
        jsonObject.remove("table_group_members");
        jsonObject.put("table_group_members", table_group_members);
        System.out.println(jsonObject.toString());

        if (group_value.toUpperCase().equals("NULL") || group_value == null) {

            TableDevice tableDevice = new TableDevice();
            tableDevice.setDevice_guid(tableGroupMembers.getDevice_guid());
            tableDevice.setGateway_id(tableGroupMembers.getGateway_id());
            tableDevice.setAccount_id(tableGroupMembers.getAccount_id());
            //获取设备值
            TableDevice tableDevice1 = tableDeviceMapper.selectByDevice_guidAndAccount_idAndGateway_id(tableDevice);

            tableGroup.setGroup_value(tableDevice1.getDevice_value());
//
            JSONObject socketResult = socketAddGroupMembers(jsonObject, tableGroupMembers.getGateway_id(), SourceId, 2);
            if (socketResult == null) {
                throw new BussinessException("-1", "Geteway socket read time out!");
            }
            String status = String.valueOf(socketResult.get("Status"));
            if (status.equals("1")) {
                throw new BussinessException("-1", "Sub-gateway retrun status '1' means command can't be executed");
            }
            if (status.equals("2")) {
                throw new BussinessException("-1", "Sub-gateway return status '2' means deivce is offline");
            }

//                //插入组成员
            int p = tableGroupMembersMapper.insertSelective(tableGroupMembers);
            if (p < 1) {
                throw new BussinessException("-1", "GroupMembers add failed");
            }
//                //更新组表组值
//
            int a = tableGroupMapper.updateByGroup_guidAndAccount_id(tableGroup);
            if (a < 1) {
                throw new BussinessException("-1", "group update failed");
            }

        } else {
            //通道实体类
            TableChannel tableChannel = new TableChannel();
            tableChannel.setAccount_id(tableGroupMembers.getAccount_id());
            tableChannel.setTable_device_guid(tableGroupMembers.getDevice_guid());
            tableChannel.setGateway_id(tableGroupMembers.getGateway_id());

            //查询设备现要添加设备的通道
            List<TableChannel> channelList = tableChannelMapper.selectByDevice_idAndAccout_idAndGateway_id(tableChannel);
            if (channelList.size() < 1) {
                throw new BussinessException("-1", "The device has no channel!");
            }
            String addingDeviceClass = channelList.get(0).getChannel_class();

            //获取现有组成员表里任意一个成员
            List<TableGroupMembers> groupMemberses = tableGroupMembersMapper.selectByGroupGuidAndAccount_idAndGateway_id(tableGroupMembers);
            TableChannel tableChannel1 = new TableChannel();
            tableChannel1.setAccount_id(tableGroupMembers.getAccount_id());
            tableChannel1.setTable_device_guid(groupMemberses.get(0).getDevice_guid());
            tableChannel1.setGateway_id(tableGroupMembers.getGateway_id());
            //获取组成员设备的通道
            List<TableChannel> channelList1 = tableChannelMapper.selectByDevice_idAndAccout_idAndGateway_id(tableChannel1);
            //List<TableChannel> channelLis1=tableChannelMapper.selectByDeviceGuid(groupMemberses.get(0).getDevice_guid());
            String membersDeviceClass = channelList1.get(0).getChannel_class();
            if (addingDeviceClass.equals(membersDeviceClass)) {

                JSONObject socketResult = socketAddGroupMembers(jsonObject, tableGroupMembers.getGateway_id(), SourceId, 2);
                if (socketResult == null) {
                    throw new BussinessException("-1", "Geteway socket read time out!");
                }
                String status = String.valueOf(socketResult.get("Status"));
                if (status.equals("1")) {
                    throw new BussinessException("-1", "Sub-gateway retrun status '1' means command can't be executed");
                }
                if (status.equals("2")) {
                    throw new BussinessException("-1", "Sub-gateway return status '2' means deivce is offline");
                }

                int p = tableGroupMembersMapper.insertSelective(tableGroupMembers);
                if (p < 1) {
                    throw new BussinessException("-1", "groupMembers add failed");
                }
            } else {
                throw new BussinessException("-1", "Member type is not consistent");
            }

        }
        return tableGroupMembers;

    }

    //删除组成员
    public void deleteGroupMembers(String user_id,TableGroupMembers tableGroupMembers,JSONObject jsonObject) {
        List<UserGateway> list=userGatewayMapper.selectByUserId(user_id);
        if (list.size()<1){
            throw new BussinessException("-1","user does not exist");
        }

        String account_id=list.get(0).getAccount_id();
        String SourceId = account_id + Param.WEBAPPID + String.valueOf(System.currentTimeMillis() / 1000000000);
        //实体类
        tableGroupMembers.setAccount_id(account_id);


        List<TableGroupMembers> list1=tableGroupMembersMapper.selectByGroupGuidAndAccount_idAndGateway_id(tableGroupMembers);

        //json
        jsonObject.remove("user_id");
        JSONArray table_group_members=(JSONArray) jsonObject.get("table_group_members");
        JSONObject jsonObject1=(JSONObject) table_group_members.get(0);
        jsonObject1.remove("table_group_guid");
        jsonObject1.remove("gateway_id");
        JSONObject socketResult = socketDeleteGroupMembers(jsonObject, tableGroupMembers.getGateway_id(), SourceId, 2);
        if (socketResult == null) {
            throw new BussinessException("-1", "Geteway socket read time out!");
        }

        String status = String.valueOf(socketResult.get("Status"));
        if ("1".equals(status)) {
            throw new BussinessException("-1", "Sub-gateway retrun status '1' means command can't be executed");
        }
        if ("2".equals(status)) {
            throw new BussinessException("-1", "Sub-gateway return status '2' means deivce is offline");
        }

        int n = tableGroupMembersMapper.deleteByAccountIdAndDeviceaddrAndGroup_addrAndGatewayId(tableGroupMembers);
        if (n<1){
            throw new BussinessException("-1","groupMembers delete failed");
        }

        if (list1.size()==1){
            TableGroup tableGroup=new TableGroup();
            tableGroup.setAccount_id(account_id);
            tableGroup.setGateway_id(tableGroupMembers.getGateway_id());
            tableGroup.setGroup_guid(tableGroupMembers.getTable_group_guid());
            tableGroup.setGroup_value("null");
            int p=tableGroupMapper.updateByGroup_guidAndAccount_idAndGateway_id(tableGroup);
            if (p<1){
                throw new BussinessException("-1","update group failed");
            }
        }


    }

    //删除组下的所有成员
    public int deleteGroupOfMembers(TableGroupMembers tableGroupMembers) {
        int n = tableGroupMembersMapper.deleteByAccountIdAndGatewayAndGroup_addr(tableGroupMembers);
        return n;
    }

    //修改组成员
    public int modifyGroupMembers(TableGroupMembers tableGroupMembers) {
        int n = tableGroupMembersMapper.updateByAccountIdAndGroupMemberGuid(tableGroupMembers);
        return n;
    }

    public List<TableGroupMembers> findGroupMember(TableGroupMembers tableGroupMembers) {

        return tableGroupMembersMapper.selectByGroupGuidAndAccount_idAndGateway_id(tableGroupMembers);

    }

    //组控制,控制一组通道(多个通道)
    public TableGroup groupController(String user_id, TableGroup tableGroup, JSONArray channel_value_arr, JSONObject jsonObject) {
        List<UserGateway> list = userGatewayMapper.selectByUserId(user_id);
        if (list.size() < 1) {
            throw new BussinessException("-1", "user_id does not exist");
        }

        String account_id = list.get(0).getAccount_id();
        String SourceId = account_id + Param.WEBAPPID + String.valueOf(System.currentTimeMillis() / 1000000000);

        //当控制通道值有时
        if (channel_value_arr != null) {
            String[] values = new String[channel_value_arr.size()];

            for (int j = 0; j < channel_value_arr.size(); j++) {
                JSONObject temp = (JSONObject) channel_value_arr.get(j);
                int channel_number = Integer.parseInt((String) temp.get("channel_number")) - 1;
                int channel_value = Integer.parseInt((String) temp.get("channel_value"));
                String channelValue = "";

                if (channel_value < 16) {
                    channelValue = "0" + Integer.toHexString(channel_value);
                } else {
                    channelValue = Integer.toHexString(channel_value);
                }
                values[channel_number] = channelValue;
            }

            String group_value = "";
            for (int j = 0; j < values.length; j++) {
                group_value += values[j];
            }
            if (channel_value_arr.size() > 0) {
                tableGroup.setGroup_value(group_value);
                jsonObject.remove("group_value");
                jsonObject.remove("group_switch");
            }

        } else if (channel_value_arr == null && tableGroup.getGroup_switch() == null) {
            throw new BussinessException("-1", "Missing the control content");
        } else if (tableGroup.getGroup_switch() != null) {
            jsonObject.remove("group_value");
        }

        jsonObject.remove("user_id");
        JSONObject jsonControlSocket = socketControlGroup(jsonObject, tableGroup.getGateway_id(), SourceId, 2);

        if (jsonControlSocket == null) {
            throw new BussinessException("-1", "Geteway socket read time out!");
        }

        String status = String.valueOf(jsonControlSocket.get("Status"));
        if (status.equals("1")) {
            throw new BussinessException("-1", "Sub-gateway retrun status '1' means command can't be executed");
        }

        if (status.equals("2")) {
            throw new BussinessException("-1", "Sub-gateway return status '2' means deivce is offline");
        }

        // 修改控制
        int n = tableGroupMapper.updateByGroup_addrAndAccount_idAndGateway_id(tableGroup);
        if (n < 1) {
            throw new BussinessException("-1", "group Group control failed");
        }
        return tableGroup;
    }

    public JSONObject socketAddGroup(JSONObject jsonObject, String DestinationID, String SourceID, int packegType) {

        Set<String> keyset = jsonObject.keySet();

        JSONObject result = new JSONObject();

        for (String key : keyset) {
            String[] sqls = SqlControlUtil.addObjects(key, (JSONArray) jsonObject.get(key));
            for (int i = 0; i < sqls.length; i++) {
                OutPutSocketMessage outPutSocketMessage = new OutPutSocketMessage();
                outPutSocketMessage.setPackegType(packegType);//包类型，app为2,web为-1
                outPutSocketMessage.setDestinationID(DestinationID);//app客户端可以写任意的16个字符串，web填写相关网关地址
                outPutSocketMessage.setType("NULL");//查询填写表明，非查询就填写“NULL”
                outPutSocketMessage.setMessage(key);//消息同于网关返回消息，下发命令的时候可以填写表明
                outPutSocketMessage.setSourceID(SourceID);//源ID
                outPutSocketMessage.setSql(sqls[i]);//下发sql语句指令

                //发送数据包
                result = outPutSocketMessage.sendMessag(SourceID);

                String status = String.valueOf(result.get("Status"));
                if (!status.equals("0")) {
                    return result;
                }
                logger.error("Sql Command : " + sqls[i] + " Gateway_Statue : " + status);

            }
        }

        return result;
    }

    public JSONObject socketAddGroupMembers(JSONObject jsonObject, String DestinationID, String SourceID, int packegType) {

        Set<String> keyset = jsonObject.keySet();

        JSONObject result = new JSONObject();
        for (String key : keyset) {
            String[] sqls = SqlControlUtil.addObjects(key, (JSONArray) jsonObject.get(key));
            for (int i = 0; i < sqls.length; i++) {
                OutPutSocketMessage outPutSocketMessage = new OutPutSocketMessage();
                outPutSocketMessage.setPackegType(packegType);//包类型，app为2,web为-1
                outPutSocketMessage.setDestinationID(DestinationID);//app客户端可以写任意的16个字符串，web填写相关网关地址
                outPutSocketMessage.setType("NULL");//查询填写表明，非查询就填写“NULL”
                outPutSocketMessage.setMessage(key);//消息同于网关返回消息，下发命令的时候可以填写表明
                outPutSocketMessage.setSourceID(SourceID);//源ID
                outPutSocketMessage.setSql(sqls[i]);//下发sql语句指令

                //发送数据包
                result = outPutSocketMessage.sendMessag(SourceID);
                String status = String.valueOf(result.get("Status"));
                logger.error("Sql Command : " + sqls[i] + " Gateway_Statue : " + status);

                if (!"0".equals(status)) {
                    return result;
                }
            }
        }

        return result;
    }

    public JSONObject socketDeleteGroupMembers(JSONObject jsonObject, String DestinationID, String SourceID, int packegType) {
        JSONObject result = new JSONObject();
        Set<String> keyset = jsonObject.keySet();

        for (String key : keyset) {
            String[] sqls = SqlControlUtil.deleteObjects(key, (JSONArray) jsonObject.get(key));
            for (int i = 0; i < sqls.length; i++) {
                OutPutSocketMessage outPutSocketMessage = new OutPutSocketMessage();
                outPutSocketMessage.setPackegType(packegType);//包类型，app为2,web为-1
                outPutSocketMessage.setDestinationID(DestinationID);//app客户端可以写任意的16个字符串，web填写相关网关地址
                outPutSocketMessage.setType("NULL");//查询填写表明，非查询就填写“NULL”
                outPutSocketMessage.setMessage(key);//消息同于网关返回消息，下发命令的时候可以填写表明
                outPutSocketMessage.setSourceID(SourceID);//源ID
                outPutSocketMessage.setSql(sqls[i]);//下发sql语句指令


                //发送数据包
                result = outPutSocketMessage.sendMessag(SourceID);
                String status = String.valueOf(result.get("Status"));
                logger.error("Sql Command : " + sqls[i] + " Gateway_Statue : " + status);
                if (!"0".equals(status)) {
                    return result;
                }
            }
        }
        return result;
    }

    public List<TableGroupMembers> findGroupMemberByGroupAddr(TableGroupMembers tableGroupMembers) {

        return tableGroupMembersMapper.findGroupMemberByGroupAddr(tableGroupMembers);
    }

    public JSONObject socketDeleteGroup(JSONObject jsonObject, String DestinationID, String SourceID, int packegType) {
        JSONObject result = new JSONObject();
        Set<String> keyset = jsonObject.keySet();

        for (String key : keyset) {
            String[] sqls = SqlControlUtil.deleteObjects(key, (JSONArray) jsonObject.get(key));
            for (int i = 0; i < sqls.length; i++) {
                OutPutSocketMessage outPutSocketMessage = new OutPutSocketMessage();
                outPutSocketMessage.setPackegType(packegType);//包类型，app为2,web为-1
                outPutSocketMessage.setDestinationID(DestinationID);//app客户端可以写任意的16个字符串，web填写相关网关地址
                outPutSocketMessage.setType("NULL");//查询填写表明，非查询就填写“NULL”
                outPutSocketMessage.setMessage(key);//消息同于网关返回消息，下发命令的时候可以填写表明
                outPutSocketMessage.setSourceID(SourceID);//源ID
                outPutSocketMessage.setSql(sqls[i]);//下发sql语句指令


                //发送数据包
                result = outPutSocketMessage.sendMessag(SourceID);
                String status = String.valueOf(result.get("Status"));
                logger.error("Sql Command : " + sqls[i] + " Gateway_Statue : " + status);
                if (!"0".equals(status)) {
                    return result;
                }
            }
        }
        return result;
    }

    public TableRegionGroup findRegionGroupByGroupAddr(TableRegionGroup tableRegionGroup) {

        return tableRegionGroupMapper.findRegionGroupByAccountIdAndGroupaddrAndGatewayid(tableRegionGroup);

    }

    public JSONObject socketDeleteRegionGroup(JSONObject jsonObject, String DestinationID, String SourceID, int packegType) {
        JSONObject result = new JSONObject();
        Set<String> keyset = jsonObject.keySet();

        for (String key : keyset) {
            String[] sqls = SqlControlUtil.deleteObjects(key, (JSONArray) jsonObject.get(key));
            for (int i = 0; i < sqls.length; i++) {
                OutPutSocketMessage outPutSocketMessage = new OutPutSocketMessage();
                outPutSocketMessage.setPackegType(packegType);//包类型，app为2,web为-1
                outPutSocketMessage.setDestinationID(DestinationID);//app客户端可以写任意的16个字符串，web填写相关网关地址
                outPutSocketMessage.setType("NULL");//查询填写表明，非查询就填写“NULL”
                outPutSocketMessage.setMessage(key);//消息同于网关返回消息，下发命令的时候可以填写表明
                outPutSocketMessage.setSourceID(SourceID);//源ID
                outPutSocketMessage.setSql(sqls[i]);//下发sql语句指令


                //发送数据包
                result = outPutSocketMessage.sendMessag(SourceID);
                String status = String.valueOf(result.get("Status"));
                logger.error("Sql Command : " + sqls[i] + " Gateway_Statue : " + status);
                if (!"0".equals(status)) {
                    return result;
                }
            }
        }
        return result;
    }

    public JSONObject socketControlGroup(JSONObject jsonObject, String DestinationID, String SourceID, int packegType) {
        Set<String> keyset = jsonObject.keySet();
        JSONObject result = new JSONObject();
        for (String key : keyset) {
            String[] sqls = SqlControlUtil.controlObjects(key, (JSONArray) jsonObject.get(key));
            for (int i = 0; i < sqls.length; i++) {
                OutPutSocketMessage outPutSocketMessage = new OutPutSocketMessage();
                outPutSocketMessage.setPackegType(packegType);//包类型,app写2,web写-1
                outPutSocketMessage.setDestinationID(DestinationID);//app客户端可以写为任意的16个字符串,web填写目标网关地址
                outPutSocketMessage.setType("NULL");//查询就填表名,非查询填写NULL
                outPutSocketMessage.setMessage(key);//消息用于网关返回消息,下发命令的时候,可以写表名
                outPutSocketMessage.setSourceID(SourceID);//源ID
                outPutSocketMessage.setSql(sqls[i]);//下发的指令(sql语句)
                System.out.println(sqls[i]);
                //发送数据包
                result = outPutSocketMessage.sendMessag(SourceID);
                String status = String.valueOf(result.get("Status"));
                if (!"0".equals(status)) {
                    return result;
                }
            }
        }
        return result;
    }

    public JSONObject socketUpdate(JSONObject jsonObject, String DestinationID, String SourceID, int packegType) {
        Set<String> keyset = jsonObject.keySet();
        JSONObject result = new JSONObject();
        for (String key : keyset) {
            String[] sqls = SqlControlUtil.putObjects(key, (JSONArray) jsonObject.get(key));
            for (int i = 0; i < sqls.length; i++) {
                OutPutSocketMessage outPutSocketMessage = new OutPutSocketMessage();
                outPutSocketMessage.setPackegType(packegType);//包类型,app写2,web写-1
                outPutSocketMessage.setDestinationID(DestinationID);//app客户端可以写为任意的16个字符串,web填写目标网关地址
                outPutSocketMessage.setType("NULL");//查询就填表名,非查询填写NULL
                outPutSocketMessage.setMessage(key);//消息用于网关返回消息,下发命令的时候,可以写表名
                outPutSocketMessage.setSourceID(SourceID);//源ID
                outPutSocketMessage.setSql(sqls[i]);//下发的指令(sql语句)
                System.out.println(sqls[i]);
                //发送数据包
                result = outPutSocketMessage.sendMessag(SourceID);
                String status = String.valueOf(result.get("Status"));
                if (!"0".equals(status)) {
                    return result;
                }
            }
        }
        return result;
    }
}
