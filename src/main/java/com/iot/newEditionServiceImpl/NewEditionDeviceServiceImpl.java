package com.iot.newEditionServiceImpl;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.iot.exception.BussinessException;
import com.iot.mapper.*;
import com.iot.pojo.*;
import com.iot.socketUtil.OutPutSocketMessage;
import com.iot.newEditionService.NewEditionDeviceService;
import com.iot.sqlControlUtil.SqlControlUtil;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Created by adminchen on 16/6/21.
 */
@Service
public class NewEditionDeviceServiceImpl implements NewEditionDeviceService {

    private static Logger logger = Logger.getLogger(NewEditionDeviceServiceImpl.class);
    @Resource
    private TableDeviceMapper tableDeviceMapper;
    @Resource
    private UserGatewayMapper userGatewayMapper;

    @Resource
    private TableRegionDeviceMapper tableRegionDeviceMapper;

    @Resource
    private TableGroupMembersMapper tableGroupMembersMapper;

    @Resource
    private TableSceneMembersMapper tableSceneMembersMapper;

    @Resource TableChannelMapper tableChannelMapper;




    //查询用户下所有设备
    public JSONArray findDevice(String user_id,String region_guid,String group_guid,String scene_guid,String gateway_id) {
        List<UserGateway> list=userGatewayMapper.selectByUserId(user_id);
        if (list.size()<1){
            throw new BussinessException("-1","user does not exist");
        }
        String account_id=list.get(0).getAccount_id();
        // 实体类
        TableDevice tableDevice1=new TableDevice();
        tableDevice1.setAccount_id(account_id);
        tableDevice1.setGateway_id(gateway_id);
        List<TableDevice> tableDeviceList=tableDeviceMapper.selectByAccountId(account_id);


        List<TableDevice> tableDeviceList1=new ArrayList<TableDevice>(tableDeviceList);
        for (TableDevice devicesss:tableDeviceList){
            if (devicesss.getDevice_type().equals("gateway")){
                tableDeviceList1.remove(devicesss);
            }
        }

        List<TableDevice> tableDeviceList2s=new ArrayList<TableDevice>();

        if (region_guid!=null||!region_guid.equals("")){

            //实体类
            TableRegionDevice tableRegionDevice=new TableRegionDevice();
            tableRegionDevice.setAccount_id(account_id);
            tableRegionDevice.setGateway_id(gateway_id);
            tableRegionDevice.setRegion_guid(region_guid);
            //查询区域下的所有设备
            List<TableRegionDevice> tableRegionDeviceList=tableRegionDeviceMapper.selectByAccountIdAndRegiongGuidGateway_id(tableRegionDevice);

            //去掉区域设备重复信息,以设备地址过滤
            List<String> regionDevicess=new ArrayList<String>();
            for (TableRegionDevice tableRegionDevice1:tableRegionDeviceList){
                String deviceAddr=tableRegionDevice1.getDevice_addr();
                if (!regionDevicess.contains(deviceAddr)){
                    regionDevicess.add(deviceAddr);
                }
            }

            //获取区域设备,去除已经添加的区域设备
            for (TableDevice tableDevices :tableDeviceList){
                for (int n=0;n<regionDevicess.size();n++){
                 if (tableDevices.getDevice_addr().equals(regionDevicess.get(n).toString())){
                     tableDeviceList1.remove(tableDevices);
                     //已经添加的设备
                     tableDeviceList2s.add(tableDevices);
                 }
                }
            }


            //获取可添加的组设备
            if (group_guid!=null&&!group_guid.equals("")){

                if (tableDeviceList1.size()<1){
                    throw new BussinessException("-1","regionDevice not device");
                }

                //复制区域设备
                List<TableDevice> tableDeviceList2=new ArrayList<TableDevice>(tableDeviceList2s);

                //查询组成员
                TableGroupMembers tableGroupMembers=new TableGroupMembers();
                tableGroupMembers.setGateway_id(gateway_id);
                tableGroupMembers.setAccount_id(account_id);
                tableGroupMembers.setTable_group_guid(group_guid);
                List<TableGroupMembers> tableGroupMembersList=tableGroupMembersMapper.selectByGroupGuidAndAccount_idAndGateway_id(tableGroupMembers);

                //去除已经添加的设备
                for (TableDevice tableDevice11:tableDeviceList2){
                    for (TableGroupMembers tableGroupMembers1:tableGroupMembersList){
                        if (tableDevice11.getDevice_addr().equals(tableGroupMembers1.getDevice_addr())){
                            tableDeviceList2s.remove(tableDevice11);
                        }
                    }
                }
            }

            for (TableDevice tableDevice:tableDeviceList2s){
//                System.out.println("sss:"+tableDevice.getDevice_addr());
//                System.out.println("sss:"+tableDevice.getDevice_type());
            }

            //获取可添加的场景设备
            if (scene_guid!=null&&!scene_guid.equals("")){
                if (tableDeviceList1.size()<1){
                    throw new BussinessException("-1","regionDevice not device");
                }

                //复制区域设备
                List<TableDevice> tableDeviceList2=new ArrayList<TableDevice>(tableDeviceList2s);
//                TableRegionDevice tableRegionDevice1=new TableRegionDevice();
//                tableRegionDevice1.setAccount_id(account_id);
//                tableRegionDevice1.setRegion_guid(region_guid);
//                tableRegionDevice1.setGateway_id(gateway_id);
//                List<TableRegionDevice> tableRegionDeviceList1=tableRegionDeviceMapper.selectByAccountIdAndRegiongGuidGateway_id(tableRegionDevice1);
//                for (TableRegionDevice tableRegionDevice2:tableRegionDeviceList1){
//                    TableDevice tableDevice=new TableDevice();
//                    tableDevice.setAccount_id(account_id);
//                    tableDevice.setDevice_guid(tableRegionDevice2.getTable_device_guid());
//                    tableDevice.setGateway_id(gateway_id);
//                    TableDevice tableDevice2=tableDeviceMapper.selectByDevice_guidAndAccount_idAndGateway_id(tableDevice);
//                    tableDeviceList2.add(tableDevice2);
//
//                }

                //查询场景成员
                TableSceneMembers tableSceneMembers=new TableSceneMembers();
                tableSceneMembers.setGateway_id(gateway_id);
                tableSceneMembers.setAccount_id(account_id);
                tableSceneMembers.setTable_scene_guid(scene_guid);
                List<TableSceneMembers> tableSceneMembersList=tableSceneMembersMapper.selectSceneMemberByScene_guidAndGatewayIdAndAccountId(tableSceneMembers);

                //去除已经添加的设备
                for (TableDevice tableDevice11:tableDeviceList2){
//                    System.out.println("bb:"+tableDevice11.getDevice_type());
//                    System.out.println("bb:"+tableDevice11.getDevice_addr());
                    if (tableDevice11.getDevice_type().equals("sensor")){
                        tableDeviceList2s.remove(tableDevice11);
                        continue;
                    }
                    for (TableSceneMembers tableSceneMembers1:tableSceneMembersList){

                        if (tableDevice11.getDevice_addr().equals(tableSceneMembers1.getDevice_addr())){
                            tableDeviceList2s.remove(tableDevice11);

                        }
                    }
                }
                for (TableDevice aa:tableDeviceList2s){
                    System.out.println("aa:"+aa.getDevice_type());
                }

//
            }

        }

        //查询出获取的所有设备及设备的通道
        JSONArray jsonArray=null;
        List<TableDevice> tableDeviceList2=null;
        if ((group_guid!=null&&!group_guid.equals(""))||(scene_guid!=null&&!scene_guid.equals(""))){
            if (tableDeviceList2s.size()<1){
                throw new BussinessException("-1"," not device");
            }


            jsonArray=(JSONArray) JSONArray.toJSON(tableDeviceList2s);
            tableDeviceList2=new ArrayList<TableDevice>(tableDeviceList2s);

            System.out.println(jsonArray.toString());
        }else {
            jsonArray=(JSONArray) JSONArray.toJSON(tableDeviceList1);
            tableDeviceList2=new ArrayList<TableDevice>(tableDeviceList1);
        }

        for (int i=0;i<tableDeviceList2.size();i++){

            System.out.println("tableDeviceList2:"+tableDeviceList2.toString());
            String  device_values=tableDeviceList2.get(i).getDevice_value();
            int device_values_int=device_values.length()/2;
            String[] channelss=new String[device_values_int];

            for (int j=0;j<channelss.length;j++){
                StringBuffer sb=new StringBuffer();
                sb.append(device_values);
                channelss[j]=sb.substring(j*2,j*2+2);
            }


            TableChannel tableChannel=new TableChannel();
            tableChannel.setGateway_id(tableDeviceList2.get(i).getGateway_id());
            tableChannel.setAccount_id(account_id);
            tableChannel.setTable_device_guid(tableDeviceList2.get(i).getDevice_guid());
            //查询设备通道
            List<TableChannel> tableChannelList=tableChannelMapper.selectByDevice_guidAndAccout_idAndGateway_id(tableChannel);
            if (scene_guid!=null&&!scene_guid.equals("")){
                for (int k=0;k<tableChannelList.size();k++){

                    String channelsValue=String.valueOf(Integer.parseInt(channelss[k],16));
                    tableChannelList.get(k).setChannel_value(channelsValue);
                }
            }

            JSONArray channel=(JSONArray)JSONArray.toJSON(tableChannelList);
            JSONObject devices=(JSONObject) jsonArray.get(i);
            devices.remove("id");
            devices.remove("account_id");
            devices.put("channel",channel);
        }

        return jsonArray;





//
//
// List<TableDevice> list=tableDeviceMapper.selectByAccountId(tableDevice.getAccount_id());
//
//        List<TableDevice> list1 = new LinkedList<TableDevice>();
//
//        for (int i = 0; i < list.size(); i++) {
//
//            if ("gateway".equals(list.get(i).getDevice_type())){
//                continue;
//            }
//
//            list1.add(list.get(i));
//        }
//
//        return list1;
    }


    //查找传感器
    public List<TableDevice> findInfoOfTheDevice(TableDevice tableDevice) {

        return tableDeviceMapper.findTheSensor(tableDevice);

    }
    //查找设备通道
    public List<TableChannel> findChannelInfo(TableChannel tableChannel) {

        return tableChannelMapper.selectChannelInfo(tableChannel);
    }

    //查询设备通道值
    public List<TableChannel> findDeviceChannelValue(TableChannel tableChannel){

        List<TableChannel> list=tableChannelMapper.selectByDevice_guidAndAccout_idAndGateway_id(tableChannel);

        for (TableChannel tableChannel1:list){
            String device_value=(String) tableChannel1.getDevice_value();
            String channnel_number=(String)tableChannel1.getChannel_number();
            int channel_number_int=Integer.parseInt(channnel_number);
            int len = device_value.length()/2;

            String[]  devices=null;
            if (len==0){
                devices = new String[1];
            }else {
                devices = new String[len];
            }

            for (int j = 0; j <devices.length ; j++) {
                StringBuilder SB = new StringBuilder();
                SB.append(device_value);
                if (j==(devices.length-1)){
                    SB.delete(0,j*2);
                    devices[j]=SB.toString();
                }else {
                    devices[j]=SB.substring(j*2,j*2+2).toString();
                }
            }
            String channel_val=devices[channel_number_int-1];
            int channel_valu=Integer.parseInt(channel_val,16);
            String channel_value=String.valueOf(channel_valu);
            tableChannel1.setChannel_value(channel_value);

        }
        return list;
    }

    //设备控制
    public int deviceController(TableDevice tableDevice){
//
//        String [] values = new String[device_value.size()];
//        for (int j = 0; j <device_value.size() ; j++) {
//            JSONObject temp = (JSONObject) device_value.get(j);
//
//            int aa =Integer.parseInt((String) temp.get("channel_number"))-1;
//
//            int a =  Integer.parseInt((String) temp.get("channel_value"));
//            String bb ="";
//
//            if (a<16){
//                bb="0"+Integer.toHexString(a);
//            }else {
//                bb=Integer.toHexString(a);
//            }
//            values[aa]=bb;
//        }
//        String R="";
//        for (int j = 0; j <values.length ; j++) {
//            R+=values[j];
//        }
//        if (device_value.size()>0){
//            tableDevice.setDevice_value(R);
//        }

        int n=tableDeviceMapper.updateGateway_idAndAccount_idAndDevice_addr(tableDevice);
        return n;

    }


    //修改设备名称
    public  int modifyDeviceName(TableDevice tableDevice) {
        int n = tableDeviceMapper.updateGateway_idAndAccount_idAndDevice_guid(tableDevice);
        return n;
    }

    public TableDevice findDeviceValue(TableDevice tableDevice) {

        return tableDeviceMapper.selectByDevice_guidAndAccount_idAndGateway_id(tableDevice);

    }

    public JSONObject socketControllDevice(JSONObject jsonObject, String DestinationID, String SourceID, int packegType) {
        JSONObject result = new JSONObject();

        Set<String> keyset = jsonObject.keySet();
        for (String key: keyset) {
            String[] sqls = SqlControlUtil.controlObjects(key, (JSONArray) jsonObject.get(key));
            for (int i = 0; i < sqls.length; i++) {

                OutPutSocketMessage outPutSocketMessage = new OutPutSocketMessage();
                outPutSocketMessage.setPackegType(packegType);//包类型,app写2,web写-1
                outPutSocketMessage.setDestinationID(DestinationID);//app客户端可以写为任意的16个字符串,web填写目标网关地址
                outPutSocketMessage.setType("NULL");//查询就填表名,非查询填写NULL
                outPutSocketMessage.setMessage(key);//消息用于网关返回消息,下发命令的时候,可以写表名
                outPutSocketMessage.setSourceID(SourceID);//源ID
                outPutSocketMessage.setSql(sqls[i]);//下发的指令(sql语句)

//                System.out.println(sqls[i]);
//                System.out.println(sqls[i]);
//                System.out.println("源id:"+SourceID);
                //发送数据包
                result = outPutSocketMessage.sendMessag(SourceID);
                String status = String.valueOf(result.get("Status"));
                logger.debug("Sql Command : " +sqls[i] + " Gateway_Statue : "+status);
                if (!status.equals("0")){
                    return result;
                }

            }
        }
        return result;
    }

    //修改设备名称
    public void modifyDeviceOfName(TableDevice tableDevice, String userId) {
        List<UserGateway> list=userGatewayMapper.selectByUserId(userId);
        if (list.size()<1){
            throw new BussinessException("-1","user does not exist");
        }
        String accountId=list.get(0).getAccount_id();
        tableDevice.setAccount_id(accountId);

        //查询区域设备
        TableRegionDevice tableRegionDevice=new TableRegionDevice();
        tableRegionDevice.setAccount_id(accountId);
        tableRegionDevice.setTable_device_guid(tableDevice.getDevice_guid());
        tableRegionDevice.setGateway_id(tableDevice.getGateway_id());
        tableRegionDevice.setDevice_name(tableDevice.getDevice_name());
        List<TableRegionDevice> tableRegionDeviceList=tableRegionDeviceMapper.selectByAccountIdAndDrvice_guidAndGatewayId(tableRegionDevice);
        if (tableRegionDeviceList.size()>0){
            int m=tableRegionDeviceMapper.updateByDrvice_guidAccount_idAndGatewayId(tableRegionDevice);
            if (m<1){
                throw new BussinessException("-1","Update region device name failed");
            }
        }

        //查询组成员
        TableGroupMembers tableGroupMembers=new TableGroupMembers();
        tableGroupMembers.setAccount_id(accountId);
        tableGroupMembers.setDevice_name(tableDevice.getDevice_name());
        tableGroupMembers.setGateway_id(tableDevice.getGateway_id());
        tableGroupMembers.setDevice_guid(tableDevice.getDevice_guid());
        List<TableGroupMembers> tableGroupMembersList=tableGroupMembersMapper.selectDeviceGuidAndGatewayIdAndAccountId(tableGroupMembers);
        if (tableGroupMembersList.size()>0){
            int i=tableGroupMembersMapper.updateByDeviceGuidAndGatewayIdAndAccountId(tableGroupMembers);
            if (i<1){
                throw new BussinessException("-1","Update group members name failed");
            }
        }

        //查询场景成员
        TableSceneMembers tableSceneMembers=new TableSceneMembers();
        tableSceneMembers.setAccount_id(accountId);
        tableSceneMembers.setDevice_guid(tableDevice.getDevice_guid());
        tableSceneMembers.setGateway_id(tableDevice.getGateway_id());
        tableSceneMembers.setDevice_name(tableDevice.getDevice_name());
        List<TableSceneMembers> tableSceneMembersList=tableSceneMembersMapper.selectSceneMemberByDeviceguidAndGatewayIdAndAccountIdList(tableSceneMembers);
        if (tableSceneMembersList.size()>0){
            int k=tableSceneMembersMapper.updateByAccountIdAndDevice_guidAndGatewayId(tableSceneMembers);
            if (k<1){
                throw new BussinessException("-1","Update scene members name failed");
            }
        }
        int n=tableDeviceMapper.updateByDeviceName(tableDevice);
        if (n<1){
            throw new BussinessException("-1","update device name faild");
        }
    }
}
