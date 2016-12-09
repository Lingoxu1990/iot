package com.iot.newServiceImpl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.iot.socketUtil.OutPutSocketMessage;
import com.iot.mapper.*;
import com.iot.message.Message;
import com.iot.newService.NewGroupService;
import com.iot.pojo.*;
import com.iot.sqlControlUtil.SqlControlUtil;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;

/**
 * Created by adminchen on 16/6/13.
 */
@Service
public class NewGroupServiceImpl implements NewGroupService{

    private static Logger logger = Logger.getLogger(NewGroupServiceImpl.class);
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

    public int  insertGroup(TableGroup tableGroup){

      int n=  tableGroupMapper.insert(tableGroup);

        return n;
    }

    //删除
    public int deleteGroup(TableGroup tableGroup) {

        //设置TableGroupMembers
        TableGroupMembers tableGroupMembers=new TableGroupMembers();
        tableGroupMembers.setGroup_addr(tableGroup.getGroup_addr());
        tableGroupMembers.setAccount_id(tableGroup.getAccount_id());
        tableGroupMembers.setGateway_id(tableGroup.getGateway_id());
        //tableGroupMembers.setTable_group_guid(tableGroup.getGroup_guid());
        List<TableGroupMembers> list=tableGroupMembersMapper.selectByGroupAddrAndAccount_idAndGateway_id(tableGroupMembers);
        //设置TableRegionGroup
        TableRegionGroup tableRegionGroup=new TableRegionGroup();
        tableRegionGroup.setAccount_id(tableGroup.getAccount_id());
        tableRegionGroup.setGroup_addr(tableGroup.getGroup_addr());
        tableRegionGroup.setGateway_id(tableGroup.getGateway_id());
        //tableRegionGroup.setTable_group_guid(tableGroup.getGroup_guid());


        TableRegionGroup RegionGroup=tableRegionGroupMapper.findRegionGroupByAccountIdAndGroupaddrAndGatewayid(tableRegionGroup);

        int n=0;

        //先删除组成员
        if (list.size()!=0){
             n=n+tableGroupMembersMapper.deleteByAccountIdAndGatewayAndGroup_addr(tableGroupMembers);

        }
        //再删除组
        n=n+tableGroupMapper.deleteByAccountIdAndGroupaddrAndGateway_id(tableGroup);

        //最后删除区域下的组
        if (RegionGroup!=null){
            n=n+tableRegionGroupMapper.deleteGroupOfAccount_idAndgateway_idAndAddr(tableRegionGroup);
        }

        return n;
    }
    //修改
    public  Message updateGoup(TableGroup tableGroup){
        Message message =new Message();
        TableRegionGroup tableRegionGroup=new TableRegionGroup();
        tableRegionGroup.setTable_group_guid(tableGroup.getGroup_guid());
        tableRegionGroup.setGroup_addr(tableGroup.getGroup_addr());
        tableRegionGroup.setAccount_id(tableGroup.getAccount_id());
        tableRegionGroup.setGroup_name(tableGroup.getGroup_name());
        tableRegionGroup.setGateway_id(tableGroup.getGateway_id());
        TableRegionGroup RegionGroup=tableRegionGroupMapper.findRegionGroupByAccountIdAndGroupId(tableRegionGroup);
        if (RegionGroup!=null){
            int a=tableRegionGroupMapper.updateByAccountIdAndGroupGuid(tableRegionGroup);
            if (a==0){
                message.setCode("-1");
                message.setMessage("RegionGroup modification failed");
                message.setContent("[]");
                return message;
            }
        }

        int n=tableGroupMapper.updateByAccountId(tableGroup);
        if (n>0){
            message.setCode("0");
            message.setMessage("Group successful modification");
            message.setContent("");
        }else {
            message.setCode("-1");
            message.setMessage("Group modification failed");
            message.setContent("[]");
        }
        return message;
    }

    //查找组用户下所有的组
    public List<TableGroup> findGroup(TableGroup tableGroup){
        List<TableGroup> list=tableGroupMapper.selectGroup(tableGroup);
        return list;
    }

    //查询组成员
    public List<TableGroupMembers> findGroupMembers(TableGroupMembers tableGroupMembers){
        List<TableGroupMembers> list=tableGroupMembersMapper.selectByGroupGuidAndAccount_idAndGateway_id(tableGroupMembers);
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
    public Message insertGroupMembers(TableGroupMembers tableGroupMembers){
        Message message=new Message();
        //组实体类
        TableGroup tableGroup = new TableGroup();
        tableGroup.setGroup_guid(tableGroupMembers.getTable_group_guid());
        tableGroup.setGateway_id(tableGroupMembers.getGateway_id());
        tableGroup.setAccount_id(tableGroupMembers.getAccount_id());
        //获取TableGroup 判断是否为空值,初始化
        List<TableGroup> groupList=tableGroupMapper.selectGroup(tableGroup);
        if (groupList.size()<1){
            message.setCode("-1");
            message.setMessage("Group is not exist!");
            message.setContent(new JSONArray());
            return message;
        }
        TableGroup tableGroup1 =  groupList.get(0);
        if (tableGroup1!=null){
            String group_value = tableGroup1.getGroup_value();
                //值是否为空
            if (group_value.toUpperCase().equals("NULL")){
                String guid=tableGroupMembers.getDevice_guid();
                TableDevice tableDevice = new TableDevice();
                tableDevice.setDevice_guid(tableGroupMembers.getDevice_guid());
                tableDevice.setGateway_id(tableGroupMembers.getGateway_id());
                tableDevice.setAccount_id(tableGroupMembers.getAccount_id());
                //查询设备
                TableDevice tableDevice1 = tableDeviceMapper.selectByDevice_guidAndAccount_idAndGateway_id(tableDevice);
                tableGroup1.setGroup_value(tableDevice1.getDevice_value());

                //插入组成员
                int p=tableGroupMembersMapper.insertSelective(tableGroupMembers);
                //更新组表组值
                int a=tableGroupMapper.updateByGroup_guidAndAccount_id(tableGroup1);

                if(a>0&&p>0){
                    message.setCode("0");
                    message.setMessage("Group members add success");
                    JSONObject jsonGroupMember = (JSONObject) JSONObject.toJSON(tableGroupMembers);
                    JSONArray list  = new JSONArray();
                    list.add(jsonGroupMember);
                    message.setContent(list);
                    return message;
                }else{
                    message.setCode("-1");
                    message.setMessage("members add failed,");
                    message.setContent(new JSONArray());
                    return message;
                }

            }else {
                //通道实体类
                TableChannel tableChannel=new TableChannel();
                tableChannel.setAccount_id(tableGroupMembers.getAccount_id());
                tableChannel.setTable_device_guid(tableGroupMembers.getDevice_guid());
                tableChannel.setGateway_id(tableGroupMembers.getGateway_id());
                //查询设备现要添加设备的通道
                List<TableChannel> channelList = tableChannelMapper.selectByDevice_idAndAccout_idAndGateway_id(tableChannel);
                String addingDeviceClass  = channelList.get(0).getChannel_class();
                //获取现有组成员表里任意一个成员
                List<TableGroupMembers> groupMemberses =tableGroupMembersMapper.selectByGroupGuidAndAccount_idAndGateway_id(tableGroupMembers);
                TableChannel tableChannel1=new TableChannel();
                tableChannel1.setAccount_id(tableGroupMembers.getAccount_id());
                tableChannel1.setTable_device_guid(groupMemberses.get(0).getDevice_guid());
                tableChannel1.setGateway_id(tableGroupMembers.getGateway_id());
                //获取组成员设备的通道
                List<TableChannel> channelList1 = tableChannelMapper.selectByDevice_idAndAccout_idAndGateway_id(tableChannel1);
                //List<TableChannel> channelLis1=tableChannelMapper.selectByDeviceGuid(groupMemberses.get(0).getDevice_guid());
                String membersDeviceClass  = channelList1.get(0).getChannel_class();
                //判断两个通道类是否一致
                if (addingDeviceClass.equals(membersDeviceClass)){
                    List<TableGroupMembers> list =tableGroupMembersMapper.selectByDevice_guidAndGroup_guid(tableGroupMembers);
                    if (list.size()>0){
                        message.setCode("-1");
                        message.setMessage("GroupMembers already exists");
                        message.setContent(new JSONArray());
                        return message;
                    }
                    int p=tableGroupMembersMapper.insertSelective(tableGroupMembers);
                    if (p>0){
                        message.setCode("0");
                        message.setMessage("GroupNembers add success");
                        JSONObject jsonGroupMember = (JSONObject) JSONObject.toJSON(tableGroupMembers);
                        JSONArray resultList  = new JSONArray();
                        resultList.add(jsonGroupMember);
                        message.setContent(resultList);

                    }else {
                        message.setCode("-1");
                        message.setMessage("GroupNembers add failed");
                        message.setContent(new JSONArray());
                    }
                }else {
                    message.setCode("-1");
                    message.setMessage("GroupNembers add failed,Type is not consistent");
                    message.setContent(new JSONArray());
                }
            }
        }else {
            message.setCode("-1");
            message.setMessage("Group is not exist!");
            message.setContent(new JSONArray());
        }
        return message;
    }

    //删除组成员
    public int deleteGroupMembers(TableGroupMembers tableGroupMembers ){
        int n=tableGroupMembersMapper.deleteByAccountIdAndDeviceaddrAndGroup_addrAndGatewayId(tableGroupMembers);
        return  n;
    }
    //修改组成员
    public int modifyGroupMembers(TableGroupMembers tableGroupMembers) {
        int n=tableGroupMembersMapper.updateByAccountIdAndGroupMemberGuid(tableGroupMembers);
        return n;
    }

    public List<TableGroupMembers> findGroupMember(TableGroupMembers tableGroupMembers) {

        return tableGroupMembersMapper.selectByGroupGuidAndAccount_idAndGateway_id(tableGroupMembers);

    }

    //组控制,控制一组通道(单个通道)
    public  int groupController(TableGroup tableGroup, JSONArray channel_value_arr){

        if (channel_value_arr!=null){
            String [] values = new String[channel_value_arr.size()];
            for (int j = 0; j <channel_value_arr.size() ; j++) {
                JSONObject temp = (JSONObject) channel_value_arr.get(j);

                int aa =Integer.parseInt((String) temp.get("channel_number"))-1;

                int a =  Integer.parseInt((String) temp.get("channel_value"));
                String bb ="";

                if (a<16){
                    bb="0"+Integer.toHexString(a);
                }else {
                    bb=Integer.toHexString(a);
                }
                values[aa]=bb;
            }
            String R="";
            for (int j = 0; j <values.length ; j++) {
                R+=values[j];
            }
            if (channel_value_arr.size()>0){
                tableGroup.setGroup_value(R);
            }
        }

        int n=tableGroupMapper.updateByGroup_guidAndAccount_idAndGateway_id(tableGroup);
        return n;
    }

    public JSONObject socketAddGroup(JSONObject jsonObject, String DestinationID, String SourceID, int packegType) {

        Set<String> keyset = jsonObject.keySet();

        JSONObject result =new JSONObject();

        for (String key : keyset) {
            String [] sqls = SqlControlUtil.addObjects(key, (JSONArray) jsonObject.get(key));
            for (int i = 0 ; i < sqls.length ; i ++){
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
                if (!status.equals("0")){
                    return result;
                }
                logger.error("Sql Command : " +sqls[i] + " Gateway_Statue : "+status);

            }
        }

        return result;
    }

    public JSONObject socketAddGroupMembers(JSONObject jsonObject, String DestinationID, String SourceID, int packegType) {
        JSONObject result=new JSONObject();
        Set<String> keyset=jsonObject.keySet();
        //JSONArray list=new JSONArray();
        for (String key:keyset){
            String[] sqls= SqlControlUtil.addObjects(key, (JSONArray) jsonObject.get(key));
            for (int i=0;i<sqls.length;i++){
                OutPutSocketMessage outPutSocketMessage=new OutPutSocketMessage();
                outPutSocketMessage.setMessage(key);
                outPutSocketMessage.setDestinationID(DestinationID);
                outPutSocketMessage.setType("NULL");
                outPutSocketMessage.setSourceID(SourceID);
                outPutSocketMessage.setPackegType(packegType);
                outPutSocketMessage.setSql(sqls[i]);
                System.out.println(sqls[i]);

                result=outPutSocketMessage.sendMessag(SourceID);
                String status=String.valueOf(result.get("Status"));
                logger.error("Sql Command:"+sqls[i]+"Gateway_Status:"+status);
                if (!"0".equals(status)){
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
        for (String key : keyset){
            String[] sqls = SqlControlUtil.controlObjects(key, (JSONArray) jsonObject.get(key));
            for (int i = 0 ; i<sqls.length;i++){
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
                String status = String.valueOf(result.get("Status")) ;
                if (!"0".equals(status)) {
                    return result;
                }
            }
        }

        return result;
    }
}
