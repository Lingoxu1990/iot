package com.iot.serviceImp;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.iot.exception.BussinessException;
import com.iot.mapper.TableSensorRecordMapper;
import com.iot.mapper.UserGatewayMapper;
import com.iot.pojo.TableSensorRecord;
import com.iot.pojo.UserGateway;
import com.iot.socketUtil.OutPutSocketMessage;
import com.iot.dbUtil.PropsUtil;
import com.iot.mapper.TableRegionDeviceMapper;

import com.iot.pojo.TableRegionDevice;
import com.iot.service.SensorDataService;
import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;


/**
 * Created by xulingo on 16/5/5.
 */

@Service
public class SensorDataServiceImpl implements SensorDataService {

    private static Logger logger = Logger.getLogger(SensorDataServiceImpl.class);

    @Resource
    private TableRegionDeviceMapper tableRegionDeviceMapper;

    @Resource
    private TableSensorRecordMapper tableSensorRecordMapper;

    @Resource
    private UserGatewayMapper userGatewayMapper;

    public JSONArray getSenorData(String sensor_guid, String accuont_id, String start_time, String end_time) {

        Map<String,String> map=new HashMap<String, String>();
        map.put("start_time",start_time);
        map.put("end_time",end_time);
        map.put("account_id",accuont_id);
        map.put("table_device_guid",sensor_guid);

        List<TableSensorRecord> tableSensorRecordList=tableSensorRecordMapper.getSenorData(map);

        JSONArray jsonArray = new JSONArray();
        for (TableSensorRecord tableSensorRecord:tableSensorRecordList){
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("record_guid", tableSensorRecord.getRecord_guid());
            jsonObject.put("table_device_guid", tableSensorRecord.getTable_device_guid());
            jsonObject.put("record_time",tableSensorRecord.getRecord_time());
            jsonObject.put("carbon_Dioxide", tableSensorRecord.getCarbon_Dioxide());
            jsonObject.put("substrate_PH", tableSensorRecord.getSubstrate_PH());
            jsonObject.put("substrate_Conductivity", tableSensorRecord.getSubstrate_Conductivity());
            jsonObject.put("substrate_Temperature", tableSensorRecord.getSubstrate_Temperature());
            jsonObject.put("air_Temperature", tableSensorRecord.getAir_Temperature());
            jsonObject.put("ppfd", tableSensorRecord.getPpfd());
            jsonObject.put("liquid_PH", tableSensorRecord.getLiquid_PH());
            jsonObject.put("substrate_Humidity", tableSensorRecord.getSubstrate_Humidity());
            jsonObject.put("liquid_DOC", tableSensorRecord.getLiquid_DOC());
            jsonObject.put("liquid_Conductivity", tableSensorRecord.getLiquid_Conductivity());
            jsonObject.put("air_Humidity", tableSensorRecord.getAir_Humidity());
            jsonObject.put("substrate_DOC", tableSensorRecord.getSubstrate_DOC());
            jsonObject.put("illuminance", tableSensorRecord.getIlluminance());
            jsonObject.put("lai", tableSensorRecord.getLai());
            jsonArray.add(jsonObject);
        }

        return jsonArray;
    }

    public JSONArray getRealTime(String sensor_guid, String accuont_id, String gateway_id, String SourceId) {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String now = simpleDateFormat.format(new Date());
        String last_ten = simpleDateFormat.format(new Date(System.currentTimeMillis() - 10000));

        String sql = "SELECT * FROM  table_sensor_record WHERE record_time= (" +
                "SELECT max(datetime(record_time)) FROM table_sensor_record WHERE table_device_guid='" + sensor_guid + "' ORDER BY datetime(record_time) " +
                ")";

        OutPutSocketMessage outPutSocketMessage = new OutPutSocketMessage();
        outPutSocketMessage.setPackegType(2);//包类型,app写2,web写-1
        outPutSocketMessage.setDestinationID(gateway_id);//app客户端可以写为任意的16个字符串,web填写目标网关地址
        outPutSocketMessage.setSourceID(SourceId);//源ID
        outPutSocketMessage.setType("NULL");//查询就填表名,非查询填写NULL
        outPutSocketMessage.setMessage("table_device");//消息用于网关返回消息,下发命令的时候,可以写表名
        outPutSocketMessage.setSql(sql);//下发的指令(sql语句)

        JSONObject jresult = outPutSocketMessage.sendMessag(SourceId);
        String staus = String.valueOf(jresult.get("Status"));

        JSONArray returnResult = null;

        if (!staus.equals("0")) {
            returnResult = new JSONArray();
        } else {
            returnResult = (JSONArray) jresult.get("List");
        }

        return returnResult;
    }

    public JSONArray getDataNowTime(String sensor_guid,String accuont_id,String gateway_id){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String last_ten = simpleDateFormat.format(new Date());
        String start_time = simpleDateFormat.format(new Date(System.currentTimeMillis() - 10000));
        //start_time="2016-07-10 20:10:10";
        Map<String,String> map=new HashMap<String, String>();
        map.put("start_time",start_time);
        map.put("end_time",last_ten);
        map.put("table_device_guid",sensor_guid);
        map.put("account_id",accuont_id);
        map.put("gateway_id",gateway_id);

        TableSensorRecord tableSensorRecordMinTime=tableSensorRecordMapper.getNowSenorDataMinTime(map);
        if (tableSensorRecordMinTime==null){
            throw new BussinessException("-1","no data");
        }
        //System.out.println("最小时间:"+tableSensorRecordMinTime.getRecord_time());
        List<TableSensorRecord> sensorRecordList=tableSensorRecordMapper.getNowSenorData(map);

        List<TableSensorRecord> sensorRecordList1=new LinkedList<TableSensorRecord>();
         String dates="";
        for (TableSensorRecord tableSensorRecord:sensorRecordList){
            if (dates.equals("")){
                dates=sensorRecordList.get(0).getRecord_time();
            }
            long timees=0;
            long timees1=0;
            try {
                 timees=simpleDateFormat.parse(dates).getTime();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            try {
                timees1=simpleDateFormat.parse(tableSensorRecord.getRecord_time()).getTime();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (timees<timees1){
                dates= tableSensorRecord.getRecord_time();
            }

        }
        for (TableSensorRecord tableSensorRecord:sensorRecordList){

            long timees=0;
            long timees1=0;
            try {
                timees=simpleDateFormat.parse(dates).getTime();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            try {
                timees1=simpleDateFormat.parse(tableSensorRecord.getRecord_time()).getTime();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (timees==timees1){
                sensorRecordList1.add(tableSensorRecord);
            }

        }
        JSONArray returnResult = (JSONArray) JSONArray.toJSON(sensorRecordList1);
        returnResult.add(tableSensorRecordMinTime.getRecord_time());
        return returnResult;
    }

    public List<TableRegionDevice> getRegionSensor(TableRegionDevice tableRegionDevice) {

        return tableRegionDeviceMapper.getRegionSensors(tableRegionDevice);

    }

    //查询传感器一天数据
    @Override
    public JSONObject getSensorDayData(String deviceGuid, String userId, String startTime, String endTime,String data_type) {
        List<UserGateway> list=userGatewayMapper.selectByUserId(userId);
        if (list.size()<1){
            throw new BussinessException("-1","user does not exist");
        }
        String accoutId=list.get(0).getAccount_id();

        Map map=new HashMap();
        map.put("table_device_guid",deviceGuid);
        map.put("account_id",accoutId);
        map.put("start_time",startTime);
        map.put("end_time",endTime);

        List<TableSensorRecord> tableSensorRecordList=tableSensorRecordMapper.getSenorData(map);
        if (tableSensorRecordList.size()==0){
            //throw new BussinessException("0","the sensor no data");

            TableSensorRecord  tableSensorRecordss=new TableSensorRecord();
            tableSensorRecordss.setCarbon_Dioxide("0");
            tableSensorRecordss.setSubstrate_PH("0");
            tableSensorRecordss.setSubstrate_Conductivity("0");
            tableSensorRecordss.setSubstrate_Temperature("0");
            tableSensorRecordss.setAir_Temperature("0");
            tableSensorRecordss.setPpfd("0");

            tableSensorRecordss.setLiquid_PH("0");
            tableSensorRecordss.setSubstrate_Humidity("0");
            tableSensorRecordss.setLiquid_DOC("0");
            tableSensorRecordss.setLiquid_Conductivity("0");

            tableSensorRecordss.setAir_Humidity("0");
            tableSensorRecordss.setSubstrate_DOC("0");
            tableSensorRecordss.setIlluminance("0");
            tableSensorRecordss.setLai("0");

            tableSensorRecordss.setRecord_time(startTime);
            tableSensorRecordList.add(tableSensorRecordss);

        }

        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long startTimes=0;
        long endTimes=0;
        try {
             startTimes=sdf.parse(startTime).getTime();
            endTimes=sdf.parse(endTime).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        int size=4;
        JSONObject yearObject=oneDaySensorData(size,tableSensorRecordList,startTimes,data_type,endTimes);
        //System.out.println("yearObject:"+yearObject.toString());
         size=8;
        JSONObject halfYearObject=oneDaySensorData(size,tableSensorRecordList,startTimes,data_type,endTimes);
        size=16;
        JSONObject quarterObject=oneDaySensorData(size,tableSensorRecordList,startTimes,data_type,endTimes);
        //时间点
        long sizes=48;
        long slotTime=(endTimes-startTimes)/sizes;
        //System.out.println(slotTime);
        if (slotTime<30000){
            slotTime=30000;
            sizes=(endTimes-startTimes)/slotTime;
        }


        //
        JSONArray allAvgData=new JSONArray();
        JSONArray point_times=new JSONArray();

        for (int i=0;i<sizes;i++){
            float carbon_Dioxide =0;
            float substrate_PH =0;
            float substrate_Conductivity =0;
            float substrate_Temperature = 0;
            float air_Temperature=0;
            float ppfd=0;
            float liquid_PH=0;
            float substrate_Humidity=0;
            float liquid_DOC=0;
            float liquid_Conductivity=0;
            float air_Humidity=0;
            float substrate_DOC=0;
            float illuminance=0;
            float lai=0;
            String timess="";

            int index=0;
            for (TableSensorRecord tableSensorRecord:tableSensorRecordList){
                if (tableSensorRecord.getCarbon_Dioxide().equals("")){
                    tableSensorRecord.setCarbon_Dioxide("0");
                }
                if (tableSensorRecord.getLai().equals("")){
                    tableSensorRecord.setLai("0");
                }
                if (tableSensorRecord.getSubstrate_PH().equals("")){
                    tableSensorRecord.setSubstrate_PH("0");
                }
                if (tableSensorRecord.getSubstrate_Conductivity().equals("")){
                    tableSensorRecord.setSubstrate_Conductivity("0");
                }
                if (tableSensorRecord.getSubstrate_Temperature().equals("")){
                    tableSensorRecord.setSubstrate_Temperature("0");
                }
                if (tableSensorRecord.getAir_Temperature().equals("")){
                    tableSensorRecord.setAir_Temperature("0");
                }
                if (tableSensorRecord.getPpfd().equals("")){
                    tableSensorRecord.setPpfd("0");
                }
                if (tableSensorRecord.getLiquid_PH().equals("")){
                    tableSensorRecord.setLiquid_PH("0");
                }
                if (tableSensorRecord.getSubstrate_Humidity().equals("")){
                    tableSensorRecord.setSubstrate_Humidity("0");
                }
                if (tableSensorRecord.getLiquid_DOC().equals("")){
                    //System.out.println("123244");
                    tableSensorRecord.setLiquid_DOC("0");
                }
                if (tableSensorRecord.getLiquid_Conductivity().equals("")){
                    tableSensorRecord.setLiquid_Conductivity("0");
                }
                if (tableSensorRecord.getAir_Humidity().equals("")){
                    tableSensorRecord.setAir_Humidity("0");
                }
                if (tableSensorRecord.getSubstrate_DOC().equals("")){
                    tableSensorRecord.setSubstrate_DOC("0");
                }
                if (tableSensorRecord.getIlluminance().equals("")){
                    tableSensorRecord.setIlluminance("0");
                }

                long times=0;
                try {
                    times=sdf.parse(tableSensorRecord.getRecord_time()).getTime();

                } catch (ParseException e) {
                    e.printStackTrace();
                }

                if (times>=startTimes&&times<startTimes+slotTime){
                    carbon_Dioxide+= Float.parseFloat(tableSensorRecord.getCarbon_Dioxide());
                    substrate_PH+=Float.parseFloat(tableSensorRecord.getSubstrate_PH());
                    //System.out.println("sub:"+substrate_PH);
                    substrate_Conductivity+=Float.parseFloat(tableSensorRecord.getSubstrate_Conductivity());
                    substrate_Temperature+=Float.parseFloat(tableSensorRecord.getSubstrate_Temperature());
                    air_Temperature+=Float.parseFloat(tableSensorRecord.getAir_Temperature());
                    ppfd+=Float.parseFloat(tableSensorRecord.getPpfd());
                    liquid_PH+=Float.parseFloat(tableSensorRecord.getLiquid_PH());
                    substrate_Humidity+=Float.parseFloat(tableSensorRecord.getSubstrate_Humidity());
                    //System.out.println("liquid_DOC:"+tableSensorRecord.getLiquid_DOC());
                    liquid_DOC+=Float.parseFloat(tableSensorRecord.getLiquid_DOC().toString());
                    liquid_Conductivity+=Float.parseFloat(tableSensorRecord.getLiquid_Conductivity());
                    air_Humidity+=Float.parseFloat(tableSensorRecord.getAir_Humidity());
                    substrate_DOC+=Float.parseFloat(tableSensorRecord.getSubstrate_DOC());
                    illuminance+=Float.parseFloat(tableSensorRecord.getIlluminance());
                    lai+=Float.parseFloat(tableSensorRecord.getLai());
                    index++;
                }

            }
            //System.out.println("index:"+index);
            point_times.add(startTimes);
            startTimes=startTimes+slotTime;


             carbon_Dioxide =carbon_Dioxide/index;

            if (Float.isNaN(carbon_Dioxide)){
                carbon_Dioxide=0;
            }
            //System.out.println("substrate_PH:"+substrate_PH +" index:"+index);
             substrate_PH =substrate_PH/index;

            if (Float.isNaN(substrate_PH)){
                substrate_PH=0;
            }
             substrate_Conductivity =substrate_Conductivity/index;
            if (Float.isNaN(substrate_Conductivity)){
                substrate_Conductivity=0;
            }
             substrate_Temperature = substrate_Temperature/index;
            if (Float.isNaN(substrate_Temperature)){
                substrate_Temperature=0;
            }
             air_Temperature=air_Temperature/index;
            if (Float.isNaN(air_Temperature)){
                air_Temperature=0;
            }
             ppfd=ppfd/index;
            if (Float.isNaN(ppfd)){
                ppfd=0;
            }
             liquid_PH=liquid_PH/index;
            if (Float.isNaN(liquid_PH)){
                liquid_PH=0;
            }
             substrate_Humidity=substrate_Humidity/index;
            if (Float.isNaN(substrate_Humidity)){
                substrate_Humidity=0;
            }
             liquid_DOC=liquid_DOC/index;
            if (Float.isNaN(liquid_DOC)){
                liquid_DOC=0;
            }
             liquid_Conductivity=liquid_Conductivity/index;
            if (Float.isNaN(liquid_Conductivity)){
                liquid_Conductivity=0;
            }
             air_Humidity=air_Humidity/index;
            if (Float.isNaN(air_Humidity)){
                air_Humidity=0;
            }
             substrate_DOC=substrate_DOC/index;
            if (Float.isNaN(substrate_DOC)){
                substrate_DOC=0;
            }
             illuminance=illuminance/index;
            if (Float.isNaN(illuminance)){
                illuminance=0;
            }
             lai=lai/index;
            if (Float.isNaN(lai)){
                lai=0;
            }
            LinkedHashMap avgData=new LinkedHashMap();
            avgData.put("carbon_Dioxide",carbon_Dioxide);
            avgData.put("substrate_PH",substrate_PH);
            avgData.put("substrate_Conductivity",substrate_Conductivity);
            avgData.put("substrate_Temperature",substrate_Temperature);
            avgData.put("air_Temperature",air_Temperature);
            avgData.put("ppfd",ppfd);
            avgData.put("liquid_PH",liquid_PH);
            avgData.put("substrate_Humidity",substrate_Humidity);
            avgData.put("liquid_DOC",liquid_DOC);
            avgData.put("liquid_Conductivity",liquid_Conductivity);
            avgData.put("air_Humidity",air_Humidity);
            avgData.put("substrate_DOC",substrate_DOC);
            avgData.put("illuminance",illuminance);
            avgData.put("lai",lai);
            allAvgData.add(avgData);
            //allAvgData.add(startTimes);
        }
       //System.out.println(allAvgData.toString());

        JSONArray data=new JSONArray();
        LinkedHashMap temps=(LinkedHashMap) allAvgData.get(0);
        Set<String> maps=temps.keySet();
        for (String key:maps){
            JSONObject object=new JSONObject();
            JSONArray original_data=new JSONArray();
            JSONArray datas=new JSONArray();

            if (data_type!=null){
                //System.out.println("12446666666");
                if (data_type.equals(key)){
                    for (int i=0;i<allAvgData.size();i++){
                        LinkedHashMap points=(LinkedHashMap) allAvgData.get(i);
                        //原始数据

                        original_data.add(Float.parseFloat(points.get(key).toString()));


                        if (key.equals("substrate_PH")){
                            datas.add(Float.parseFloat( points.get(key).toString())*10 );
                        }else if (key.equals("carbon_Dioxide")){
                            datas.add(Float.parseFloat((String)points.get(key).toString()) /10);
                        }

                        else if (key.equals("illuminance")){
                            datas.add(Float.parseFloat((String)points.get(key).toString()) /100);
                        }
                        else if (key.equals("ppfd")){
                            datas.add(Float.parseFloat((String)points.get(key).toString())/100);
                        }
                        else if (key.equals("liquid_PH")){
                            datas.add( Float.parseFloat((String)points.get(key).toString())*10);
                        }
                        else {
                            datas.add(Float.parseFloat(points.get(key).toString()));
                        }


                    }


                    //添加传感器单位
                    if (key.equals("air_Humidity")){
                        key="air_Humidity(%)";
                        object.put("original_name",(new StringBuffer()).append(Character.toUpperCase(key.charAt(0))).append(key.substring(1)).toString());
                    }
                    if (key.equals("air_Temperature")){
                        key="air_Temperature(°C)";
                        object.put("original_name",(new StringBuffer()).append(Character.toUpperCase(key.charAt(0))).append(key.substring(1)).toString());
                    }
                    if (key.equals("illuminance")){

                        object.put("original_name",(new StringBuffer()).append(Character.toUpperCase(key.charAt(0))).append(key.substring(1)).toString()+"(lux)");
                        key="illuminance(100*lux)";
                    }
                    if (key.equals("lai")){
                        key="lai()";
                        object.put("original_name",(new StringBuffer()).append(Character.toUpperCase(key.charAt(0))).append(key.substring(1)).toString());
                    }

                    //
                    if (key.equals("carbon_Dioxide")){
                        key="carbon_Dioxide(ppm)";
                        object.put("original_name",(new StringBuffer()).append(Character.toUpperCase(key.charAt(0))).append(key.substring(1)).toString());
                    }
                    if (key.equals("substrate_Conductivity")){
                        key="substrate_Conductivity(mS/cm)";
                        object.put("original_name",(new StringBuffer()).append(Character.toUpperCase(key.charAt(0))).append(key.substring(1)).toString());
                    }

                    if (key.equals("substrate_Temperature")){

                        object.put("original_name",(new StringBuffer()).append(Character.toUpperCase(key.charAt(0))).append(key.substring(1)).toString());
                        key="substrate_Temperature(°C)";
                    }
                    if (key.equals("ppfd")){
                        object.put("original_name",(new StringBuffer()).append(Character.toUpperCase(key.charAt(0))).append(key.substring(1)).toString()+"(μmol ㎡/s)");
                        key="ppfd(100*μmol ㎡/s )";

                    }
                    if (key.equals("liquid_PH")){

                        key=key+"(PH)";
                        object.put("original_name",(new StringBuffer()).append(Character.toUpperCase(key.charAt(0))).append(key.substring(1)).toString());
                    }

                    if (key.equals("substrate_Humidity")){
                        key="substrate_Humidity(%)";
                        object.put("original_name",(new StringBuffer()).append(Character.toUpperCase(key.charAt(0))).append(key.substring(1)).toString());
                    }
                    if (key.equals("liquid_DOC")){
                        key="liquid_DOC(mg/l)";
                        object.put("original_name",(new StringBuffer()).append(Character.toUpperCase(key.charAt(0))).append(key.substring(1)).toString());
                    }
                    if (key.equals("liquid_Conductivity")){
                        key="liquid_Conductivity(mS/cm)";
                        object.put("original_name",(new StringBuffer()).append(Character.toUpperCase(key.charAt(0))).append(key.substring(1)).toString());
                    }
                    if (key.equals("substrate_DOC")){
                        key="substrate_DOC(mg/l)";
                        object.put("original_name",(new StringBuffer()).append(Character.toUpperCase(key.charAt(0))).append(key.substring(1)).toString());
                    }
                    if (key.equals("substrate_PH")){
                        key="substrate_PH(PH)";
                        object.put("original_name",(new StringBuffer()).append(Character.toUpperCase(key.charAt(0))).append(key.substring(1)).toString());
                    }
                    object.put("data",datas);
                    object.put("original_data",original_data);
                    //object.put("original_name",key);
                    object.put("name",key);
                    data.add(object);
                }

            }else {
                //System.out.println("333333");
                for (int i=0;i<allAvgData.size();i++){
                    LinkedHashMap points=(LinkedHashMap) allAvgData.get(i);
                    //原始数据

                    original_data.add(Float.parseFloat(points.get(key).toString()));


                    if (key.equals("substrate_PH")){
                        datas.add(Float.parseFloat( points.get(key).toString())*10 );
                    }else if (key.equals("carbon_Dioxide")){
                        datas.add(Float.parseFloat((String)points.get(key).toString()) /10);
                    }

                    else if (key.equals("illuminance")){
                        datas.add(Float.parseFloat((String)points.get(key).toString()) /100);
                    }
                    else if (key.equals("ppfd")){
                        datas.add(Float.parseFloat((String)points.get(key).toString())/100);
                    }
                    else if (key.equals("liquid_PH")){
                        datas.add( Float.parseFloat((String)points.get(key).toString())*10);
                    }
                    else {
                        datas.add(Float.parseFloat(points.get(key).toString()));
                    }


                }


                //添加传感器单位
                if (key.equals("air_Humidity")){
                    key="air_Humidity(%)";
                    object.put("original_name",(new StringBuffer()).append(Character.toUpperCase(key.charAt(0))).append(key.substring(1)).toString());
                }
                if (key.equals("air_Temperature")){
                    key="air_Temperature(°C)";
                    object.put("original_name",(new StringBuffer()).append(Character.toUpperCase(key.charAt(0))).append(key.substring(1)).toString());
                }
                if (key.equals("illuminance")){

                    object.put("original_name",(new StringBuffer()).append(Character.toUpperCase(key.charAt(0))).append(key.substring(1)).toString()+"(lux)");
                    key="illuminance(100*lux)";
                }
                if (key.equals("lai")){
                    key="lai()";
                    object.put("original_name",(new StringBuffer()).append(Character.toUpperCase(key.charAt(0))).append(key.substring(1)).toString());
                }

                //
                if (key.equals("carbon_Dioxide")){
                    key="carbon_Dioxide(ppm)";
                    object.put("original_name",(new StringBuffer()).append(Character.toUpperCase(key.charAt(0))).append(key.substring(1)).toString());
                }
                if (key.equals("substrate_Conductivity")){
                    key="substrate_Conductivity(mS/cm)";
                    object.put("original_name",(new StringBuffer()).append(Character.toUpperCase(key.charAt(0))).append(key.substring(1)).toString());
                }

                if (key.equals("substrate_Temperature")){

                    object.put("original_name",(new StringBuffer()).append(Character.toUpperCase(key.charAt(0))).append(key.substring(1)).toString());
                    key="substrate_Temperature(°C)";
                }
                if (key.equals("ppfd")){
                    object.put("original_name",(new StringBuffer()).append(Character.toUpperCase(key.charAt(0))).append(key.substring(1)).toString()+"(μmol ㎡/s)");
                    key="ppfd(100*μmol ㎡/s )";

                }
                if (key.equals("liquid_PH")){

                    key=key+"(PH)";
                    object.put("original_name",(new StringBuffer()).append(Character.toUpperCase(key.charAt(0))).append(key.substring(1)).toString());
                }

                if (key.equals("substrate_Humidity")){
                    key="substrate_Humidity(%)";
                    object.put("original_name",(new StringBuffer()).append(Character.toUpperCase(key.charAt(0))).append(key.substring(1)).toString());
                }
                if (key.equals("liquid_DOC")){
                    key="liquid_DOC(mg/l)";
                    object.put("original_name",(new StringBuffer()).append(Character.toUpperCase(key.charAt(0))).append(key.substring(1)).toString());
                }
                if (key.equals("liquid_Conductivity")){
                    key="liquid_Conductivity(mS/cm)";
                    object.put("original_name",(new StringBuffer()).append(Character.toUpperCase(key.charAt(0))).append(key.substring(1)).toString());
                }
                if (key.equals("substrate_DOC")){
                    key="substrate_DOC(mg/l)";
                    object.put("original_name",(new StringBuffer()).append(Character.toUpperCase(key.charAt(0))).append(key.substring(1)).toString());
                }
                if (key.equals("substrate_PH")){
                    key="substrate_PH(PH)";
                    object.put("original_name",(new StringBuffer()).append(Character.toUpperCase(key.charAt(0))).append(key.substring(1)).toString());
                }
                object.put("data",datas);
                object.put("original_data",original_data);
                //object.put("original_name",key);
                object.put("name",key);
                System.out.println(object.toString());
                data.add(object);
            }

            //if (key.equals(""))

        }
//输出数据
        JSONArray result_times_counts=new JSONArray();
        for (int p=0;p<point_times.size();p++){
            long time_pointss=Long.parseLong(point_times.get(p).toString());
            String time_pointsss=sdf.format(time_pointss);
            result_times_counts.add(time_pointsss);
        }
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("data",data);
        jsonObject.put("time",result_times_counts);
        jsonObject.put("yearObject",yearObject);
        jsonObject.put("halfYearObject",halfYearObject);
        jsonObject.put("quarterObject",quarterObject);

        //
        return jsonObject;
    }

    //传感器一天数据处理
    public JSONObject oneDaySensorData(int sizes,List<TableSensorRecord> tableSensorRecordList,long startTimes,String data_type,long endTimes){
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long slotTime=(endTimes-startTimes)/sizes;
        JSONArray allAvgData=new JSONArray();
        JSONArray point_times=new JSONArray();

        for (int i=0;i<sizes;i++){
            float carbon_Dioxide =0;
            float substrate_PH =0;
            float substrate_Conductivity =0;
            float substrate_Temperature = 0;
            float air_Temperature=0;
            float ppfd=0;
            float liquid_PH=0;
            float substrate_Humidity=0;
            float liquid_DOC=0;
            float liquid_Conductivity=0;
            float air_Humidity=0;
            float substrate_DOC=0;
            float illuminance=0;
            float lai=0;
            String timess="";

            int index=0;
            for (TableSensorRecord tableSensorRecord:tableSensorRecordList){
                if (tableSensorRecord.getCarbon_Dioxide().equals("")){
                    tableSensorRecord.setCarbon_Dioxide("0");
                }
                if (tableSensorRecord.getLai().equals("")){
                    tableSensorRecord.setLai("0");
                }
                if (tableSensorRecord.getSubstrate_PH().equals("")){
                    tableSensorRecord.setSubstrate_PH("0");
                }
                if (tableSensorRecord.getSubstrate_Conductivity().equals("")){
                    tableSensorRecord.setSubstrate_Conductivity("0");
                }
                if (tableSensorRecord.getSubstrate_Temperature().equals("")){
                    tableSensorRecord.setSubstrate_Temperature("0");
                }
                if (tableSensorRecord.getAir_Temperature().equals("")){
                    tableSensorRecord.setAir_Temperature("0");
                }
                if (tableSensorRecord.getPpfd().equals("")){
                    tableSensorRecord.setPpfd("0");
                }
                if (tableSensorRecord.getLiquid_PH().equals("")){
                    tableSensorRecord.setLiquid_PH("0");
                }
                if (tableSensorRecord.getSubstrate_Humidity().equals("")){
                    tableSensorRecord.setSubstrate_Humidity("0");
                }
                if (tableSensorRecord.getLiquid_DOC().equals("")){
                    //System.out.println("123244");
                    tableSensorRecord.setLiquid_DOC("0");
                }
                if (tableSensorRecord.getLiquid_Conductivity().equals("")){
                    tableSensorRecord.setLiquid_Conductivity("0");
                }
                if (tableSensorRecord.getAir_Humidity().equals("")){
                    tableSensorRecord.setAir_Humidity("0");
                }
                if (tableSensorRecord.getSubstrate_DOC().equals("")){
                    tableSensorRecord.setSubstrate_DOC("0");
                }
                if (tableSensorRecord.getIlluminance().equals("")){
                    tableSensorRecord.setIlluminance("0");
                }

                long times=0;
                try {
                    times=sdf.parse(tableSensorRecord.getRecord_time()).getTime();

                } catch (ParseException e) {
                    e.printStackTrace();
                }

                if (times>=startTimes&&times<startTimes+slotTime){
                    carbon_Dioxide+= Float.parseFloat(tableSensorRecord.getCarbon_Dioxide());
                    substrate_PH+=Float.parseFloat(tableSensorRecord.getSubstrate_PH());
                    //System.out.println("sub:"+substrate_PH);
                    substrate_Conductivity+=Float.parseFloat(tableSensorRecord.getSubstrate_Conductivity());
                    substrate_Temperature+=Float.parseFloat(tableSensorRecord.getSubstrate_Temperature());
                    air_Temperature+=Float.parseFloat(tableSensorRecord.getAir_Temperature());
                    ppfd+=Float.parseFloat(tableSensorRecord.getPpfd());
                    liquid_PH+=Float.parseFloat(tableSensorRecord.getLiquid_PH());
                    substrate_Humidity+=Float.parseFloat(tableSensorRecord.getSubstrate_Humidity());
                    //System.out.println("liquid_DOC:"+tableSensorRecord.getLiquid_DOC());
                    liquid_DOC+=Float.parseFloat(tableSensorRecord.getLiquid_DOC().toString());
                    liquid_Conductivity+=Float.parseFloat(tableSensorRecord.getLiquid_Conductivity());
                    air_Humidity+=Float.parseFloat(tableSensorRecord.getAir_Humidity());
                    substrate_DOC+=Float.parseFloat(tableSensorRecord.getSubstrate_DOC());
                    illuminance+=Float.parseFloat(tableSensorRecord.getIlluminance());
                    lai+=Float.parseFloat(tableSensorRecord.getLai());
                    index++;
                }

            }
            //System.out.println("index:"+index);
            point_times.add(startTimes);
            startTimes=startTimes+slotTime;


            carbon_Dioxide =carbon_Dioxide/index;

            if (Float.isNaN(carbon_Dioxide)){
                carbon_Dioxide=0;
            }
            //System.out.println("substrate_PH:"+substrate_PH +" index:"+index);
            substrate_PH =substrate_PH/index;

            if (Float.isNaN(substrate_PH)){
                substrate_PH=0;
            }
            substrate_Conductivity =substrate_Conductivity/index;
            if (Float.isNaN(substrate_Conductivity)){
                substrate_Conductivity=0;
            }
            substrate_Temperature = substrate_Temperature/index;
            if (Float.isNaN(substrate_Temperature)){
                substrate_Temperature=0;
            }
            air_Temperature=air_Temperature/index;
            if (Float.isNaN(air_Temperature)){
                air_Temperature=0;
            }
            ppfd=ppfd/index;
            if (Float.isNaN(ppfd)){
                ppfd=0;
            }
            liquid_PH=liquid_PH/index;
            if (Float.isNaN(liquid_PH)){
                liquid_PH=0;
            }
            substrate_Humidity=substrate_Humidity/index;
            if (Float.isNaN(substrate_Humidity)){
                substrate_Humidity=0;
            }
            liquid_DOC=liquid_DOC/index;
            if (Float.isNaN(liquid_DOC)){
                liquid_DOC=0;
            }
            liquid_Conductivity=liquid_Conductivity/index;
            if (Float.isNaN(liquid_Conductivity)){
                liquid_Conductivity=0;
            }
            air_Humidity=air_Humidity/index;
            if (Float.isNaN(air_Humidity)){
                air_Humidity=0;
            }
            substrate_DOC=substrate_DOC/index;
            if (Float.isNaN(substrate_DOC)){
                substrate_DOC=0;
            }
            illuminance=illuminance/index;
            if (Float.isNaN(illuminance)){
                illuminance=0;
            }
            lai=lai/index;
            if (Float.isNaN(lai)){
                lai=0;
            }
            LinkedHashMap avgData=new LinkedHashMap();
            avgData.put("carbon_Dioxide",carbon_Dioxide);
            avgData.put("substrate_PH",substrate_PH);
            avgData.put("substrate_Conductivity",substrate_Conductivity);
            avgData.put("substrate_Temperature",substrate_Temperature);
            avgData.put("air_Temperature",air_Temperature);
            avgData.put("ppfd",ppfd);
            avgData.put("liquid_PH",liquid_PH);
            avgData.put("substrate_Humidity",substrate_Humidity);
            avgData.put("liquid_DOC",liquid_DOC);
            avgData.put("liquid_Conductivity",liquid_Conductivity);
            avgData.put("air_Humidity",air_Humidity);
            avgData.put("substrate_DOC",substrate_DOC);
            avgData.put("illuminance",illuminance);
            avgData.put("lai",lai);
            allAvgData.add(avgData);
            //allAvgData.add(startTimes);
        }
        //System.out.println(allAvgData.toString());

        JSONArray data=new JSONArray();
        LinkedHashMap temps=(LinkedHashMap) allAvgData.get(0);
        Set<String> maps=temps.keySet();
        for (String key:maps){
            JSONObject object=new JSONObject();
            JSONArray original_data=new JSONArray();
            JSONArray datas=new JSONArray();

            if (data_type!=null){
                //System.out.println("12446666666");
                if (data_type.equals(key)){
                    for (int i=0;i<allAvgData.size();i++){
                        LinkedHashMap points=(LinkedHashMap) allAvgData.get(i);
                        //原始数据

                        original_data.add(Float.parseFloat(points.get(key).toString()));


                        if (key.equals("substrate_PH")){
                            datas.add(Float.parseFloat( points.get(key).toString())*10 );
                        }else if (key.equals("carbon_Dioxide")){
                            datas.add(Float.parseFloat((String)points.get(key).toString()) /10);
                        }

                        else if (key.equals("illuminance")){
                            datas.add(Float.parseFloat((String)points.get(key).toString()) /100);
                        }
                        else if (key.equals("ppfd")){
                            datas.add(Float.parseFloat((String)points.get(key).toString())/100);
                        }
                        else if (key.equals("liquid_PH")){
                            datas.add( Float.parseFloat((String)points.get(key).toString())*10);
                        }
                        else {
                            datas.add(Float.parseFloat(points.get(key).toString()));
                        }


                    }


                    //添加传感器单位
                    if (key.equals("air_Humidity")){
                        key="air_Humidity(%)";
                        object.put("original_name",(new StringBuffer()).append(Character.toUpperCase(key.charAt(0))).append(key.substring(1)).toString());
                    }
                    if (key.equals("air_Temperature")){
                        key="air_Temperature(°C)";
                        object.put("original_name",(new StringBuffer()).append(Character.toUpperCase(key.charAt(0))).append(key.substring(1)).toString());
                    }
                    if (key.equals("illuminance")){

                        object.put("original_name",(new StringBuffer()).append(Character.toUpperCase(key.charAt(0))).append(key.substring(1)).toString()+"(lux)");
                        key="illuminance(100*lux)";
                    }
                    if (key.equals("lai")){
                        key="lai()";
                        object.put("original_name",(new StringBuffer()).append(Character.toUpperCase(key.charAt(0))).append(key.substring(1)).toString());
                    }

                    //
                    if (key.equals("carbon_Dioxide")){
                        key="carbon_Dioxide(ppm)";
                        object.put("original_name",(new StringBuffer()).append(Character.toUpperCase(key.charAt(0))).append(key.substring(1)).toString());
                    }
                    if (key.equals("substrate_Conductivity")){
                        key="substrate_Conductivity(mS/cm)";
                        object.put("original_name",(new StringBuffer()).append(Character.toUpperCase(key.charAt(0))).append(key.substring(1)).toString());
                    }

                    if (key.equals("substrate_Temperature")){

                        object.put("original_name",(new StringBuffer()).append(Character.toUpperCase(key.charAt(0))).append(key.substring(1)).toString());
                        key="substrate_Temperature(°C)";
                    }
                    if (key.equals("ppfd")){
                        object.put("original_name",(new StringBuffer()).append(Character.toUpperCase(key.charAt(0))).append(key.substring(1)).toString()+"(μmol ㎡/s)");
                        key="ppfd(100*μmol ㎡/s )";

                    }
                    if (key.equals("liquid_PH")){

                        key=key+"(PH)";
                        object.put("original_name",(new StringBuffer()).append(Character.toUpperCase(key.charAt(0))).append(key.substring(1)).toString());
                    }

                    if (key.equals("substrate_Humidity")){
                        key="substrate_Humidity(%)";
                        object.put("original_name",(new StringBuffer()).append(Character.toUpperCase(key.charAt(0))).append(key.substring(1)).toString());
                    }
                    if (key.equals("liquid_DOC")){
                        key="liquid_DOC(mg/l)";
                        object.put("original_name",(new StringBuffer()).append(Character.toUpperCase(key.charAt(0))).append(key.substring(1)).toString());
                    }
                    if (key.equals("liquid_Conductivity")){
                        key="liquid_Conductivity(mS/cm)";
                        object.put("original_name",(new StringBuffer()).append(Character.toUpperCase(key.charAt(0))).append(key.substring(1)).toString());
                    }
                    if (key.equals("substrate_DOC")){
                        key="substrate_DOC(mg/l)";
                        object.put("original_name",(new StringBuffer()).append(Character.toUpperCase(key.charAt(0))).append(key.substring(1)).toString());
                    }
                    if (key.equals("substrate_PH")){
                        key="substrate_PH(PH)";
                        object.put("original_name",(new StringBuffer()).append(Character.toUpperCase(key.charAt(0))).append(key.substring(1)).toString());
                    }
                    object.put("data",datas);
                    object.put("original_data",original_data);
                    //object.put("original_name",key);
                    object.put("name",key);
                    data.add(object);
                }

            }else {
                //System.out.println("333333");
                for (int i=0;i<allAvgData.size();i++){
                    LinkedHashMap points=(LinkedHashMap) allAvgData.get(i);
                    //原始数据

                    original_data.add(Float.parseFloat(points.get(key).toString()));


                    if (key.equals("substrate_PH")){
                        datas.add(Float.parseFloat( points.get(key).toString())*10 );
                    }else if (key.equals("carbon_Dioxide")){
                        datas.add(Float.parseFloat((String)points.get(key).toString()) /10);
                    }

                    else if (key.equals("illuminance")){
                        datas.add(Float.parseFloat((String)points.get(key).toString()) /100);
                    }
                    else if (key.equals("ppfd")){
                        datas.add(Float.parseFloat((String)points.get(key).toString())/100);
                    }
                    else if (key.equals("liquid_PH")){
                        datas.add( Float.parseFloat((String)points.get(key).toString())*10);
                    }
                    else {
                        datas.add(Float.parseFloat(points.get(key).toString()));
                    }


                }


                //添加传感器单位
                if (key.equals("air_Humidity")){
                    key="air_Humidity(%)";
                    object.put("original_name",(new StringBuffer()).append(Character.toUpperCase(key.charAt(0))).append(key.substring(1)).toString());
                }
                if (key.equals("air_Temperature")){
                    key="air_Temperature(°C)";
                    object.put("original_name",(new StringBuffer()).append(Character.toUpperCase(key.charAt(0))).append(key.substring(1)).toString());
                }
                if (key.equals("illuminance")){

                    object.put("original_name",(new StringBuffer()).append(Character.toUpperCase(key.charAt(0))).append(key.substring(1)).toString()+"(lux)");
                    key="illuminance(100*lux)";
                }
                if (key.equals("lai")){
                    key="lai()";
                    object.put("original_name",(new StringBuffer()).append(Character.toUpperCase(key.charAt(0))).append(key.substring(1)).toString());
                }

                //
                if (key.equals("carbon_Dioxide")){
                    key="carbon_Dioxide(ppm)";
                    object.put("original_name",(new StringBuffer()).append(Character.toUpperCase(key.charAt(0))).append(key.substring(1)).toString());
                }
                if (key.equals("substrate_Conductivity")){
                    key="substrate_Conductivity(mS/cm)";
                    object.put("original_name",(new StringBuffer()).append(Character.toUpperCase(key.charAt(0))).append(key.substring(1)).toString());
                }

                if (key.equals("substrate_Temperature")){

                    object.put("original_name",(new StringBuffer()).append(Character.toUpperCase(key.charAt(0))).append(key.substring(1)).toString());
                    key="substrate_Temperature(°C)";
                }
                if (key.equals("ppfd")){
                    object.put("original_name",(new StringBuffer()).append(Character.toUpperCase(key.charAt(0))).append(key.substring(1)).toString()+"(μmol ㎡/s)");
                    key="ppfd(100*μmol ㎡/s )";

                }
                if (key.equals("liquid_PH")){

                    key=key+"(PH)";
                    object.put("original_name",(new StringBuffer()).append(Character.toUpperCase(key.charAt(0))).append(key.substring(1)).toString());
                }

                if (key.equals("substrate_Humidity")){
                    key="substrate_Humidity(%)";
                    object.put("original_name",(new StringBuffer()).append(Character.toUpperCase(key.charAt(0))).append(key.substring(1)).toString());
                }
                if (key.equals("liquid_DOC")){
                    key="liquid_DOC(mg/l)";
                    object.put("original_name",(new StringBuffer()).append(Character.toUpperCase(key.charAt(0))).append(key.substring(1)).toString());
                }
                if (key.equals("liquid_Conductivity")){
                    key="liquid_Conductivity(mS/cm)";
                    object.put("original_name",(new StringBuffer()).append(Character.toUpperCase(key.charAt(0))).append(key.substring(1)).toString());
                }
                if (key.equals("substrate_DOC")){
                    key="substrate_DOC(mg/l)";
                    object.put("original_name",(new StringBuffer()).append(Character.toUpperCase(key.charAt(0))).append(key.substring(1)).toString());
                }
                if (key.equals("substrate_PH")){
                    key="substrate_PH(PH)";
                    object.put("original_name",(new StringBuffer()).append(Character.toUpperCase(key.charAt(0))).append(key.substring(1)).toString());
                }
                object.put("data",datas);
                object.put("original_data",original_data);
                //object.put("original_name",key);
                object.put("name",key);
                System.out.println(object.toString());
                data.add(object);
            }

            //if (key.equals(""))

        }
//输出数据
        JSONArray result_times_counts=new JSONArray();
        for (int p=0;p<point_times.size();p++){
            long time_pointss=Long.parseLong(point_times.get(p).toString());
            String time_pointsss=sdf.format(time_pointss);
            result_times_counts.add(time_pointsss);
        }
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("data",data);
        jsonObject.put("time",result_times_counts);

        //
        JSONArray objectData=new JSONArray();
        objectData.add(jsonObject);
        return jsonObject;
    }
}
