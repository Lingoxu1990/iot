package com.iot.newEditionServiceImpl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.iot.socketUtil.OutPutSocketMessage;
import com.iot.exception.BussinessException;
import com.iot.mapper.*;
import com.iot.message.Message;
import com.iot.newEditionService.NewEditionRegionDeviceService;
import com.iot.pojo.*;
import com.iot.spitUtil.Param;
import com.iot.sqlControlUtil.SqlControlUtil;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;


/**
 * Created by adminchen on 16/6/3.
 */
@Service
public class NewEditionRegionDeviceServiceImpl implements NewEditionRegionDeviceService {


    private static Logger logger= Logger.getLogger(NewEditionRegionDeviceService.class);
    @Resource
    private TableChannelMapper tableChannelMapper;

    @Resource
    private TableRegionDeviceMapper tableRegionDeviceMapper;

    @Resource
    private TableRegionMapper tableRegionMapper;

    @Resource
    private TableDeviceMapper tableDeviceMapper;

    @Resource
    private UserGatewayMapper userGatewayMapper;

    @Resource
    private TableRegionGroupMapper  tableRegionGroupMapper;

    @Resource
    private TableGroupMembersMapper tableGroupMembersMapper;

    @Resource
    private TableRegionSceneMapper tableRegionSceneMapper;

    @Resource
    private TableSceneMembersMapper tableSceneMembersMapper;

    //查找通道
    public List<TableChannel> findChannel(TableChannel tableChannel) {

        List<TableChannel> list = tableChannelMapper.selectByDeviceGuid(tableChannel);
        return list;
    }

    //查找该设备设备重复
    public List<TableRegionDevice> FindDeviceRepeat(TableRegionDevice tableRegionDevice) {
        //boolean flag= false;
        List<TableRegionDevice> list = tableRegionDeviceMapper.selectByAccountIdAndDrvice_guidRegion_guid(tableRegionDevice);

//        if (list.size()<1){
//            flag=false;
//        }else {
//            flag=true;
//        }

        return list;
    }

    //添加区域设备
    public JSONObject insertRegionDevice(String user_id, TableRegionDevice tableRegionDevice,JSONObject jsonObject){
        List<UserGateway> list=userGatewayMapper.selectByUserId(user_id);
        if (list.size()<1){
            throw new BussinessException("-1","user does not exist");
        }
        String account_id=list.get(0).getAccount_id();
        String SourceId = account_id + Param.WEBAPPID + String.valueOf(System.currentTimeMillis() / 1000000000);

        //实体类
        tableRegionDevice.setAccount_id(account_id);

        //判断是否有该设备
        List<TableRegionDevice> regionDeviceList=tableRegionDeviceMapper.selectByAccountIdAndRegiongGuidGateway_idAndDeviceGuid(tableRegionDevice);
        if (regionDeviceList.size()>0){
            throw new BussinessException("-1","The device already exist");
        }

        //实体类
        TableRegion tableRegion=new TableRegion();
        tableRegion.setAccount_id(tableRegionDevice.getAccount_id());
        tableRegion.setGateway_id(tableRegionDevice.getGateway_id());
        tableRegion.setRegion_guid(tableRegionDevice.getRegion_guid());

        //实体类
        TableChannel tableChannel=new TableChannel();
        tableChannel.setGateway_id(tableRegionDevice.getGateway_id());
        tableChannel.setTable_device_guid(tableRegionDevice.getTable_device_guid());
        tableChannel.setAccount_id(tableRegionDevice.getAccount_id());

        //实体类
        TableDevice tableDevice=new TableDevice();
        tableDevice.setDevice_guid(tableRegionDevice.getTable_device_guid());
        tableDevice.setGateway_id(tableRegionDevice.getGateway_id());
        tableDevice.setAccount_id(account_id);

        //获取通道
        List<TableChannel> channelList=tableChannelMapper.selectByDevice_guidAndAccout_idAndGateway_id(tableChannel);
        if (channelList.size()<1){
            throw new BussinessException("-1","The device has no channel");
        }

        TableChannel tableChannels=channelList.get(0);
        //设置TableRegionDevice集合
        List<TableRegionDevice> regionDeviceArrayList=new ArrayList<TableRegionDevice>();
//
        for (TableChannel channels:channelList){
            TableRegionDevice tableRegionDevices=new TableRegionDevice();
            tableRegionDevices.setChannel_bit_num(channels.getChannel_bit_num());
            tableRegionDevices.setChannel_class(channels.getChannel_class());
            tableRegionDevices.setChannel_guid(channels.getChannel_guid());

            tableRegionDevices.setChannel_name(channels.getChannel_name());
            tableRegionDevices.setChannel_type(channels.getChannel_type());
            tableRegionDevices.setRegion_device_guid(UUID.randomUUID().toString());
            tableRegionDevices.setId(UUID.randomUUID().toString());
            tableRegionDevices.setAccount_id(account_id);
            tableRegionDevices.setGateway_id(tableRegionDevice.getGateway_id());
            tableRegionDevices.setRegion_guid(tableRegionDevice.getRegion_guid());
            tableRegionDevices.setRegion_name(tableRegionDevice.getRegion_name());
            tableRegionDevices.setRegion_addr(tableRegionDevice.getRegion_addr());
            tableRegionDevices.setDevice_addr(tableRegionDevice.getDevice_addr());
            tableRegionDevices.setDevice_name(tableRegionDevice.getDevice_name());
            tableRegionDevices.setTable_device_guid(tableRegionDevice.getTable_device_guid());
            regionDeviceArrayList.add(tableRegionDevices);
        }

        //区域初始化,判断区域值是否为空
        TableRegion tableRegion1=tableRegionMapper.selectByRegionGuidAndGateway_idAndAccount_id(tableRegion);
        String region_value=tableRegion1.getRegion_value();

        //设置json
        jsonObject.remove("user_id");
        JSONArray table_region_device=(JSONArray) jsonObject.get("table_region_device");
        table_region_device.remove(0);
        //jsonObject.put("table_region_device",table_region_device);
        //for (int i=0;i<table_region_device.size();i++){
        JSONObject jsonObject1=(JSONObject) JSONObject.toJSON(regionDeviceArrayList.get(0));
        jsonObject1.remove("id");
        jsonObject1.remove("account_id");
        table_region_device.add(jsonObject1);
        System.out.println("jsonObject:"+jsonObject.toString());

        //}
        TableDevice tableDevicees=tableDeviceMapper.selectByDevice_guidAndAccount_idAndGateway_id(tableDevice);
        String device_typees=tableDevicees.getDevice_type();
        if (region_value.equals("NULL")||region_value.toUpperCase().equals("NULL")||device_typees.equals("sensor")){

            System.out.println("jsonObject:"+jsonObject.toString());
            //网关
            JSONObject socketResult = socketAddRegionDevice(jsonObject, tableRegionDevice.getGateway_id(), SourceId, 2);
            if (socketResult == null) {
                throw  new BussinessException("-1","Geteway socket read time out!");
            }

            String status = String.valueOf(socketResult.get("Status"));

            if (status.equals("1")) {
                throw new BussinessException("-1","Gateway return the status code '1' means sub-gateway failed to insert the data");
            } else if (status.equals("2")) {
                throw new BussinessException("-1","Gateway return the status code '2' means sub-gateway failed to insert the data because of the device is off line");
            }
            for (int i=0;i<regionDeviceArrayList.size();i++){

                int n = tableRegionDeviceMapper.insertSelective(regionDeviceArrayList.get(i));
                if (n<1){
                    throw new BussinessException("-1","add regionDevice failed");
                }
            }
            //查询设备获取值
            TableDevice tableDevice1=tableDeviceMapper.selectByDevice_guidAndAccount_idAndGateway_id(tableDevice);
            tableRegion.setRegion_value(tableDevice1.getDevice_value());
            if (!device_typees.equals("sensor")){
                int c = tableRegionMapper.updateByUidAndAccountIdAndGatewayID(tableRegion);
                if (c<1){
                    throw new BussinessException("-1","Failed to update the value of region");
                }
            }

        }else {

            //获取待加入区域的设备的 设备类型
            TableDevice tableDevice1=tableDeviceMapper.selectByDevice_guidAndAccount_idAndGateway_id(tableDevice);
            String device_type1=tableDevice1.getDevice_type();
            String device_value1=tableDevice1.getDevice_value();

            //获取当前区域设备中任意一个设备的值
            List<TableRegionDevice> tableRegionDevicess = tableRegionDeviceMapper.selectByAccountIdAndRegiongGuidGateway_id(tableRegionDevice);
            TableRegionDevice regionDevice=tableRegionDevicess.get(0);
            String deviceGuid=regionDevice.getTable_device_guid();
            tableDevice.setDevice_guid(deviceGuid);
            TableDevice tableDevices=tableDeviceMapper.selectByDevice_guidAndAccount_idAndGateway_id(tableDevice);
            String device_type2=tableDevices.getDevice_type();
            String device_value2=tableDevices.getDevice_value();


            //判断要添加设备跟已经添加的设备类型是否一致
            if ((device_type1.equals(device_type2))&&(device_value1.equals(device_value2))){
                //网关
                JSONObject socketResult = socketAddRegionDevice(jsonObject, tableRegionDevice.getGateway_id(), SourceId, 2);
                if (socketResult == null) {

                    throw  new BussinessException("-1","Geteway socket read time out!");
                }

                String status = String.valueOf(socketResult.get("Status"));

                if (status.equals("1")) {
                    throw new BussinessException("-1","Gateway return the status code '1' means sub-gateway failed to insert the data");
                }
                if (status.equals("2")) {
                    throw new BussinessException("-1","Gateway return the status code '2' means sub-gateway failed to insert the data because of the device is off line");
                }
                for (int i=0;i<regionDeviceArrayList.size();i++){

                    int n = tableRegionDeviceMapper.insertSelective(regionDeviceArrayList.get(i));
                    if (n<1){
                        throw new BussinessException("-1","add regionDevice failed");
                    }
                }
            }else {
                throw new BussinessException("-1","Device type is not consistent");
            }
        }
        return jsonObject;

    }

    public boolean insert(List<TableRegionDevice> list, TableRegionDevice tableRegionDevice) {

        boolean flag = false;

        int a = 0;
        for (int i = 0; i < list.size(); i++) {
            a = tableRegionDeviceMapper.insertSelective(list.get(i));
        }


//        TableDevice tableDevice = tableDeviceMapper.selectByDevice_guid(TableRegionDevice.getTable_device_guid());

        TableDevice tableDevice = new TableDevice();
        tableDevice.setAccount_id(tableRegionDevice.getAccount_id());
        tableDevice.setGateway_id(tableRegionDevice.getGateway_id());
        tableDevice.setDevice_guid(tableRegionDevice.getTable_device_guid());

        TableDevice tableDeviceForValue = tableDeviceMapper.selectByDevice_guidAndAccount_idAndGateway_id(tableDevice);

        if (tableDeviceForValue.getDevice_type().toUpperCase().equals("SENSOR")){

            flag = true;
        }else {
            String device_value = tableDeviceForValue.getDevice_value();

            TableRegion tableRegion = new TableRegion();
            tableRegion.setRegion_value(device_value);
            tableRegion.setAccount_id(tableRegionDevice.getAccount_id());
            tableRegion.setRegion_guid(tableRegionDevice.getRegion_guid());
            tableRegion.setGateway_id(tableRegionDevice.getGateway_id());

            int num = tableRegionMapper.updateByUidAndAccountId(tableRegion);

            if (num<1){
                flag = false;
            }else {
                flag = true;
            }
        }

        return flag;
    }

    //查找区域
    public TableRegion findRegion(String region_guid) {
        TableRegion tableRegion = tableRegionMapper.selectByRegionGuid(region_guid);
        return tableRegion;
    }

    //查找设备
    public TableDevice findDevice(String table_device_guid) {
        TableDevice result = tableDeviceMapper.selectByDevice_guid(table_device_guid);
        return result;
    }

    //根据区域地址和设备地址删除区域设备
    public void deleteByRegion_AddrIdAndDevice_Addr(String user_id,TableRegionDevice tableRegionDevice,JSONObject jsonObject) {
        List<UserGateway> list=userGatewayMapper.selectByUserId(user_id);
        if (list.size()<1){
            throw new BussinessException("-1","user does not exist");
        }
        String account_id=list.get(0).getAccount_id();
        String SourceId = account_id + Param.WEBAPPID + String.valueOf(System.currentTimeMillis() / 1000000000);
        tableRegionDevice.setAccount_id(account_id);

        //查询是否有组成员
        TableRegionGroup tableRegionGroup=new TableRegionGroup();
        tableRegionGroup.setAccount_id(account_id);
        tableRegionGroup.setRegion_guid(tableRegionDevice.getRegion_guid());
        tableRegionGroup.setGateway_id(tableRegionDevice.getGateway_id());

        List<TableRegionGroup> tableRegionGroupList=tableRegionGroupMapper.findRegionGroupByAccountIdAndRegionguidAndGatewayid(tableRegionGroup);
        if (tableRegionGroupList.size()>0){
            for (TableRegionGroup tableRegionGroup1:tableRegionGroupList){
                TableGroupMembers tableGroupMembers=new TableGroupMembers();
                tableGroupMembers.setAccount_id(account_id);
                tableGroupMembers.setTable_group_guid(tableRegionGroup1.getTable_group_guid());
                tableGroupMembers.setGateway_id(tableRegionGroup1.getGateway_id());
                tableGroupMembers.setDevice_guid(tableRegionDevice.getTable_device_guid());
                TableGroupMembers tableGroupMembers1=tableGroupMembersMapper.selectByDevice_guidAndGroup_guidAndAccountIdAndGatewayId(tableGroupMembers);
                if (tableGroupMembers1!=null){
                    throw new BussinessException("-1","Please delete group members first");
                }
            }
        }

        //查询是否有场景成员
        TableRegionScene tableRegionScene=new TableRegionScene();
        tableRegionScene.setAccount_id(account_id);
        tableRegionScene.setRegion_guid(tableRegionDevice.getRegion_guid());
        tableRegionScene.setGateway_id(tableRegionDevice.getGateway_id());
        List<TableRegionScene> tableRegionSceneList=tableRegionSceneMapper.findRegionSceneByAccountIdAndRegionguidAndGatewayId(tableRegionScene);
        if (tableRegionSceneList.size()>0){
            for (TableRegionScene tableRegionScene1:tableRegionSceneList){
                TableSceneMembers tableSceneMembers=new TableSceneMembers();
                tableSceneMembers.setAccount_id(account_id);
                tableSceneMembers.setTable_scene_guid(tableRegionScene1.getTable_scene_guid());
                tableSceneMembers.setGateway_id(tableRegionScene1.getGateway_id());
                tableSceneMembers.setDevice_guid(tableRegionDevice.getTable_device_guid());
                TableSceneMembers tableSceneMembers1=tableSceneMembersMapper.selectSceneMemberByDeviceguidAndGatewayIdAndAccountIdAndSceneGuid(tableSceneMembers);
                if (tableSceneMembers1!=null){
                    throw new BussinessException("-1","Please delete scene members first");
                }
            }
        }



        jsonObject.remove("user_id");
        JSONArray temp=(JSONArray) jsonObject.get("table_region_device");
        for (int k=0;k<temp.size();k++){
            JSONObject regionDevices=(JSONObject) temp.get(k);
            regionDevices.remove("region_guid");
            regionDevices.remove("gateway_id");
        }

        List<TableRegionDevice> tableRegionDeviceList=tableRegionDeviceMapper.selectByAccountIdAndRegiongGuidGateway_idgroupByDeviceGuid(tableRegionDevice);

        JSONObject socketResult = socketDeleteRegionDevice(jsonObject, tableRegionDevice.getGateway_id(), SourceId, 2);
        if (socketResult==null){
            throw new BussinessException("-1","Geteway socket read time out!");
        }

        String stauts = String.valueOf(socketResult.get("Status"));
        if (stauts.equals("1")) {
            throw new BussinessException("-1","Gateway return the status code '1' means sub-gateway failed to remove the data!");
        }
        if (stauts.equals("2")) {
            throw new BussinessException("-1","Gateway return the status code '2' means the device is off line,please try later!");
        }
        int n = tableRegionDeviceMapper.deleteByRegion_AddrIdAndDevice_Addr(tableRegionDevice);
        if (n<1){
            throw new BussinessException("-1","regionDevice delete failed");
        }
        List<TableDevice> tableDeviceList=new ArrayList<TableDevice>();
        if (tableRegionDeviceList.size()>0){


            for (TableRegionDevice tableRegionDevice1s:tableRegionDeviceList){
                TableDevice tableDevice=new TableDevice();
                tableDevice.setDevice_guid(tableRegionDevice1s.getTable_device_guid());
                tableDevice.setGateway_id(tableRegionDevice1s.getGateway_id());
                tableDevice.setAccount_id(tableRegionDevice1s.getAccount_id());
                TableDevice tableDevice1=tableDeviceMapper.selectByDevice_guidAndAccount_idAndGateway_id(tableDevice);
                if (!tableDevice1.getDevice_type().equals("sensor")){
                    tableDeviceList.add(tableDevice1);
                }


            }
        }

        if (tableDeviceList.size()<=1){
            TableRegion tableRegion=new TableRegion();
            tableRegion.setAccount_id(account_id);
            tableRegion.setRegion_guid(tableRegionDevice.getRegion_guid());
            tableRegion.setGateway_id(tableRegionDevice.getGateway_id());
            tableRegion.setRegion_value("null");
            int p=tableRegionMapper.updateByUidAndAccountIdAndGatewayID(tableRegion);
            if (p<1){
                throw new BussinessException("-1","region update failed");
            }
        }

    }

    //查找区域设备
    public List<Map> fingRegionDevice(TableRegionDevice tableRegionDevice) {
        List<Map> list = tableRegionDeviceMapper.selectByAccountIdAndRegiongGuidAndgatewayid(tableRegionDevice);
//        List<Map> list1=new ArrayList<Map>();
//       for(Map maps:list){
//        if ("sensor".equals(maps.get("device_type"))){
//
//        }else {
//            list1.add(maps);
//        }
//       }
        return list;
    }

    //判断区域值是否为空
    public boolean IsNullRegionValue(TableRegion tableRegion) {
        TableRegion tableRegions = tableRegionMapper.selectByRegionGuidAndGateway_idAndAccount_id(tableRegion);
        if (tableRegions.getRegion_value().toUpperCase().equals("NULL")) {
            return true;
        }
        return false;
    }

    //查询当前区域
    public  TableRegion findRegionOfNow(TableRegion tableRegion){
        TableRegion tableRegions = tableRegionMapper.selectByRegionGuidAndGateway_idAndAccount_id(tableRegion);
        return tableRegions;
    }

    //判断是否为同一类型设备
    public boolean IsTypeRegionDevices(TableRegionDevice tableRegionDevice) {

        TableDevice tableDevice1 = tableDeviceMapper.selectByDevice_guid(tableRegionDevice.getTable_device_guid());

        if (tableDevice1.getDevice_type().equals("sensor")){
            return true;
        }

        List<TableRegionDevice> list = tableRegionDeviceMapper.selectByAccountIdAndRegiongGuid(tableRegionDevice);
        if (list.isEmpty()) {
            return true;
        }
        String device_type="";

        for (TableRegionDevice RegionDevice :list){

            TableDevice tableDevice = tableDeviceMapper.selectByDevice_guid(RegionDevice.getTable_device_guid());
            if (!tableDevice.getDevice_type().equals("sensor")){
                device_type = tableDevice.getDevice_type();
            }else {
                continue;
            }
        }


        String device_type1 = tableDevice1.getDevice_type();
        if (device_type.equals(device_type1)) {
            return true;
        }
        return false;
    }

    public Message ModifyByDrviceOfname(List<TableRegionDevice> list) {
        Message message = new Message();
        int n = 0;
        int a = 0;
        //List<TableRegionDevice> list1=new ArrayList<TableRegionDevice>();
        for (TableRegionDevice table : list) {
            String region_guid = table.getRegion_guid();
            String device_guid = table.getTable_device_guid();
            String device_name = table.getDevice_name();
            String device_addr = table.getDevice_addr();
            String accout_id = table.getAccount_id();
            TableRegionDevice table1 = new TableRegionDevice();
            table1.setAccount_id(accout_id);
            table1.setRegion_guid(region_guid);
            table1.setDevice_name(device_name);
            table1.setTable_device_guid(device_guid);
            TableDevice tableDevice = new TableDevice();
            tableDevice.setAccount_id(accout_id);
            tableDevice.setDevice_addr(device_addr);
            tableDevice.setDevice_guid(device_guid);
            //list1.add(table1);
            n = tableRegionDeviceMapper.updateByRagion_guidAndDrvice_guidAccount_id(table1);
            a = tableDeviceMapper.updateDevice_guidAndAccount_idAndDevice_addr(tableDevice);
        }
        if (n > 0 && a > 0) {

            message.setCode("0");
            message.setMessage("RegionDevice successful modification");
            message.setContent("");
        } else {
            message.setCode("-1");
            message.setMessage("RegionDevice modification failed");
            message.setContent("[]");
        }

        return message;
    }



    //socket控制方法
    public JSONObject socketAddRegionDevice(JSONObject jsonObject, String DestinationID, String SourceID, int packegType) {

        JSONObject jsonResult=new JSONObject();
        Set<String> keyset = jsonObject.keySet();

        for (String key : keyset) {
            String[] result = SqlControlUtil.addObjects(key, (JSONArray) jsonObject.get(key));

            for (int i = 0; i < result.length; i++) {
                OutPutSocketMessage outPutSocketMessage = new OutPutSocketMessage();
                outPutSocketMessage.setPackegType(packegType);//包类型,app写2,web写-1
                outPutSocketMessage.setDestinationID(DestinationID);//app客户端可以写为任意的16个字符串,web填写目标网关地址
                outPutSocketMessage.setType("NULL");//查询就填表名,非查询填写NULL
                outPutSocketMessage.setMessage(key);//消息用于网关返回消息,下发命令的时候,可以写表名
                outPutSocketMessage.setSourceID(SourceID);//源ID
                outPutSocketMessage.setSql(result[i]);//下发的指令(sql语句)
                JSONObject reslut = outPutSocketMessage.sendMessag(SourceID);

                //jsonResult=reslut;
                String status = String.valueOf(reslut.get("Status"));
                logger.error("Sql Command:"+result[i]+"Gateway_Status:"+status);
                if (!status.equals("0")){
                    return reslut;
                }
            }

        }


//        JSONObject result=new JSONObject();
//        Set<String> keyset=jsonObject.keySet();
//        //JSONArray list=new JSONArray();
//        for (String key:keyset){
//            String[] sqls=sqlControlUtil.addObjects(key,(JSONArray)jsonObject.get(key));
//            for (int i=0;i<sqls.length;i++){
//                OutPutSocketMessage outPutSocketMessage=new OutPutSocketMessage();
//                outPutSocketMessage.setMessage(key);
//                outPutSocketMessage.setDestinationID(DestinationID);
//                outPutSocketMessage.setType("NULL");
//                outPutSocketMessage.setSourceID(SourceID);
//                outPutSocketMessage.setPackegType(packegType);
//                outPutSocketMessage.setSql(sqls[i]);
//                System.out.println(sqls[i]);
//
//                result=outPutSocketMessage.sendMessag(SourceID);
//                String status=String.valueOf(result.get("Status"));
//                logger.error("Sql Command:"+sqls[i]+"Gateway_Status:"+status);
//                if (!"0".equals(status)){
//                    return result;
//                }
//            }
//        }
//        return result;

        return jsonResult;
    }

    public JSONObject socketDeleteRegionDevice(JSONObject jsonObject, String DestinationID, String SourceID, int packegType) {

        JSONObject result =new JSONObject();
        Set<String> keys = jsonObject.keySet();

        for (String key:keys) {

            String [] sqls = SqlControlUtil.deleteObjects("table_region_device", (JSONArray) jsonObject.get(key));

            for (int i = 0; i<sqls.length;i++){
                OutPutSocketMessage outPutSocketMessage = new OutPutSocketMessage();
                outPutSocketMessage.setPackegType(packegType);//包类型,app写2,web写-1
                outPutSocketMessage.setDestinationID(DestinationID);//app客户端可以写为任意的16个字符串,web填写目标网关地址
                outPutSocketMessage.setType("NULL");//查询就填表名,非查询填写NULL
                outPutSocketMessage.setMessage(key);//消息用于网关返回消息,下发命令的时候,可以写表名
                outPutSocketMessage.setSourceID(SourceID);//源ID
                outPutSocketMessage.setSql(sqls[i]);//下发的指令(sql语句)

                System.out.println(sqls[i]);

                JSONObject jsonResult = outPutSocketMessage.sendMessag(SourceID);

                result=jsonResult;

                String status = String.valueOf(result.get("Status"));

                if (!status.equals("0")){
                    return result;
                }
            }

        }
        return result;
    }
}
