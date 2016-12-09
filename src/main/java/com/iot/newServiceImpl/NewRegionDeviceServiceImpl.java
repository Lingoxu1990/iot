package com.iot.newServiceImpl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.iot.socketUtil.OutPutSocketMessage;
import com.iot.mapper.TableChannelMapper;
import com.iot.mapper.TableDeviceMapper;
import com.iot.mapper.TableRegionDeviceMapper;
import com.iot.mapper.TableRegionMapper;
import com.iot.message.Message;

import com.iot.newService.NewRegionDeviceService;
import com.iot.pojo.TableChannel;
import com.iot.pojo.TableDevice;
import com.iot.pojo.TableRegion;
import com.iot.pojo.TableRegionDevice;
import com.iot.sqlControlUtil.SqlControlUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.nio.channels.Channel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import java.util.UUID;

import java.util.Set;


/**
 * Created by adminchen on 16/6/3.
 */
@Service("NewRegionDeviceServiceImpl")
public class NewRegionDeviceServiceImpl implements NewRegionDeviceService {

    @Resource
    private TableChannelMapper tableChannelMapper;

    @Resource
    private TableRegionDeviceMapper tableRegionDeviceMapper;

    @Resource
    private TableRegionMapper tableRegionMapper;

    @Resource
    private TableDeviceMapper tableDeviceMapper;

    //查找通道
    public List<TableChannel> findChannel(TableChannel tableChannel) {
        TableDevice tableDevice=new TableDevice();
        tableDevice.setDevice_guid(tableChannel.getTable_device_guid());
        tableDevice.setAccount_id(tableChannel.getAccount_id());
        tableDevice.setGateway_id(tableChannel.getGateway_id());
        //System.out.println(tableDevice.getAccount_id()+","+tableDevice.getGateway_id()+","+tableDevice.getDevice_guid());
        TableDevice tableDevice1=tableDeviceMapper.selectByDevice_guidAndAccount_idAndGateway_idas(tableDevice);
        List<TableChannel> list=null;
        if (tableDevice1==null){
            List<TableChannel> list1=new ArrayList<TableChannel>();
            return list1;

        } else {
            if (tableDevice1.getDevice_type().equals("sensor")){
                list = tableChannelMapper.selectByDeviceGuidAndAccountId(tableChannel);
            }else {
                System.out.println("456");
                list = tableChannelMapper.selectByDeviceGuid(tableChannel);
            }

        }

        return list;
    }

    //判断设备重复
    public boolean FindDeviceRepeat(TableRegionDevice tableRegionDevice) {
        boolean flag= false;
        List<TableRegionDevice> list = tableRegionDeviceMapper.selectByAccountIdAndDrvice_guidRegion_guid(tableRegionDevice);

        if (list.size()<1){
            flag=false;
        }else {
            flag=true;
        }

        return flag;
    }

    //添加设备
    public int insertRegionDevice(TableRegionDevice tableRegionDevice){
        int falg=0;//0表示空
        int a=0;
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
        //
        TableDevice tableDevice=new TableDevice();
        tableDevice.setDevice_guid(tableRegionDevice.getTable_device_guid());
        tableDevice.setGateway_id(tableRegionDevice.getGateway_id());
        tableDevice.setAccount_id(tableRegionDevice.getAccount_id());
        //List<TableChannel> list = tableChannelMapper.selectByDeviceGuid(table_device_guid);
        //判断是否有该设备了
        List<TableRegionDevice> list=tableRegionDeviceMapper.selectByAccountIdAndDrvice_guidRegion_guid(tableRegionDevice);
        if (list.size()>0){
            return falg;
        }else {
           TableRegion tableRegion1=tableRegionMapper.selectByRegionGuidAndGateway_idAndAccount_id(tableRegion);
            System.out.println("region_value:"+tableRegion1.getRegion_value());
            System.out.println("region_value:"+tableRegion1.getRegion_value().length());
            List<TableChannel> channelList=tableChannelMapper.selectByDevice_guidAndAccout_idAndGateway_id(tableChannel);

            List<TableRegionDevice> regionDeviceList=new ArrayList<TableRegionDevice>();
            for (TableChannel c:channelList){
                tableRegionDevice.setChannel_bit_num(c.getChannel_number());
                tableRegionDevice.setChannel_class(c.getChannel_class());
                tableRegionDevice.setChannel_guid(c.getChannel_guid());
                tableRegionDevice.setChannel_name(c.getChannel_name());
                tableRegionDevice.setChannel_type(c.getChannel_type());
                tableRegionDevice.setId(UUID.randomUUID().toString());
                tableRegionDevice.setRegion_device_guid(UUID.randomUUID().toString());
                regionDeviceList.add(tableRegionDevice);
            }
            if (tableRegion1.getRegion_value() != null ||!tableRegion1.getRegion_value().toUpperCase().equals("NULL")){

                //获取待加入区域的设备的 设备类型
                TableDevice tableDevice1=tableDeviceMapper.selectByDevice_guidAndAccount_idAndGateway_id(tableDevice);
                String device_type1=tableDevice1.getDevice_type();

                //获取当前区域设备中任意一个设备的值
                List<TableRegionDevice> tableRegionDevices = tableRegionDeviceMapper.selectByAccountIdAndRegiongGuidGateway_id(tableRegionDevice);
                if (tableRegionDevices.size()<1){
                    return 0;
                }
                TableRegionDevice tableRegionDevice1=tableRegionDevices.get(0);
                String device_guid=tableRegionDevice1.getTable_device_guid();
                TableDevice tableDevice2=new TableDevice();
                tableDevice2.setAccount_id(tableRegionDevice.getAccount_id());
                tableDevice2.setGateway_id(tableRegionDevice.getGateway_id());
                tableDevice2.setDevice_guid(device_guid);
                TableDevice tableDevice3=tableDeviceMapper.selectByDevice_guidAndAccount_idAndGateway_id(tableDevice);

                String device_type2=tableDevice3.getDevice_type();
                if (device_type1.equals(device_type2)){
                    for (int i=0;i<regionDeviceList.size();i++){
                        a = tableRegionDeviceMapper.insertSelective(regionDeviceList.get(i));
                    }
                    if (a<1){
                        return falg;
                    }else {
                        falg=1;
                    }
                }
            }else {


                for (int i=0;i<regionDeviceList.size();i++){
                    a = tableRegionDeviceMapper.insertSelective(regionDeviceList.get(i));
                }
                TableDevice tableDevice1=tableDeviceMapper.selectByDevice_guidAndAccount_idAndGateway_id(tableDevice);
                tableRegion.setRegion_value(tableDevice1.getDevice_value());
                int n = tableRegionMapper.updateByUidAndAccountIdAndGatewayID(tableRegion);
                if (a>0&&n>0){
                    falg=1;
                }else {
                    return falg;
                }
                //

            }

        }

        return falg;
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
        //System.out.println("a:"+tableDevice.getDevice_guid()+","+tableDevice.getGateway_id()+","+tableDevice.getAccount_id());

        TableDevice tableDeviceForValue = tableDeviceMapper.selectByDevice_guidAndAccount_idAndGateway_idas(tableDevice);

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
    public int deleteByRegion_AddrIdAndDevice_Addr(TableRegionDevice tableRegionDevice) {
        int n = tableRegionDeviceMapper.deleteByRegion_AddrIdAndDevice_Addr(tableRegionDevice);
        return n;
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

    //查询设备通道(该通道)
    public TableChannel selectChannelsOne(TableChannel tableChannel){
        TableChannel tableChannel1=tableChannelMapper.selectByDeselectByChannelGuidAndAccount_idAndGateway_id(tableChannel);
        return tableChannel1;
    }

    //判断区域值是否为空
    public boolean IsNullRegionValue(String region_guid) {
        TableRegion tableRegion = tableRegionMapper.selectByRegionGuid(region_guid);
        if (tableRegion.getRegion_value().toUpperCase().equals("NULL")) {
            return true;
        }
        return false;
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
        String device_value_length="";

        for (TableRegionDevice RegionDevice :list){

            TableDevice tableDevice = tableDeviceMapper.selectByDevice_guid(RegionDevice.getTable_device_guid());
            if (!tableDevice.getDevice_type().equals("sensor")){
                device_type = tableDevice.getDevice_type();
                device_value_length=String.valueOf(tableDevice.getDevice_value().length());

            }else {
                continue;
            }
        }


        String device_type1 = tableDevice1.getDevice_type();
        String device_value_length1=String.valueOf(tableDevice1.getDevice_value().length());

        if (device_type.equals(device_type1)&&device_value_length.equals(device_value_length1)) {
            return true;
        }
        return false;
    }

    //
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

        JSONObject jsonResult = null;
        Set<String> keyset = jsonObject.keySet();

        JSONArray list= new JSONArray();
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

                jsonResult=reslut;
                String status = String.valueOf(reslut.get("Status"));
                if (!status.equals("0")){
                    return reslut;
                }
            }

        }

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
