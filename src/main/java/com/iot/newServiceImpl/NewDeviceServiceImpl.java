package com.iot.newServiceImpl;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import com.iot.socketUtil.OutPutSocketMessage;
import com.iot.mapper.TableChannelMapper;
import com.iot.mapper.TableDeviceMapper;
import com.iot.newService.NewDeviceService;
import com.iot.pojo.TableChannel;
import com.iot.pojo.TableDevice;
import com.iot.sqlControlUtil.SqlControlUtil;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Created by adminchen on 16/6/21.
 */
@Service
public class NewDeviceServiceImpl implements NewDeviceService {

    private static Logger logger = Logger.getLogger(NewDeviceServiceImpl.class);
    @Resource
    private TableDeviceMapper tableDeviceMapper;
    @Resource
    private TableChannelMapper tableChannelMapper;


    //查询用户下所有设备
    public List<TableDevice> findDevice(TableDevice tableDevice) {
        List<TableDevice> list=tableDeviceMapper.selectByAccountId(tableDevice.getAccount_id());

        List<TableDevice> list1 = new LinkedList<TableDevice>();

        for (int i = 0; i < list.size(); i++) {

            if ("gateway".equals(list.get(i).getDevice_type())){
                continue;
            }

            list1.add(list.get(i));
        }

        return list1;
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

            System.out.println("device_value : " +device_value);
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
//            int channel_valu=Integer.parseInt(channel_val,16);
//
//            String channel_value;
//            if (channel_valu<16){
//                channel_value="0"+Integer.toHexString(channel_valu);
//            }else {
//                channel_value=Integer.toHexString(channel_valu);
//            }

            tableChannel1.setChannel_value(channel_val);

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
}
