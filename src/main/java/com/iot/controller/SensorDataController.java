package com.iot.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.iot.exception.BussinessException;
import com.iot.exception.ParameterException;
import com.iot.message.Message;
import com.iot.pojo.TableRegionDevice;
import com.iot.pojo.TableSensorRecord;
import com.iot.pojo.UserGateway;
import com.iot.service.SensorDataService;
import com.iot.service.UserService;
import com.iot.spitUtil.Param;

import com.iot.utils.CreateSimpleExcelToDisk;
import com.sun.org.apache.bcel.internal.generic.IF_ACMPEQ;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;



/**
 * 该类作为区域传感器数据获取接口的控制器
 * 1 获取区域下的历史数据获取接口
 * 2 获取区域下的实时传感器数据接口
 * Created by xulingo on 16/5/4.
 */
@Controller
@RequestMapping("/sensor")
public class SensorDataController {

    @Resource
    private UserService userService;
    @Resource
    private SensorDataService sensorDataService;

    private static Logger logger = Logger.getLogger(com.iot.controller.SensorDataController.class);


    @RequestMapping(value = "old",method = RequestMethod.GET)//xiaoxin
    @ResponseBody
    public Object historyData(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse){


        Message message = new Message();

        String user_id = httpServletRequest.getParameter("user_id");
        String region_guid = httpServletRequest.getParameter("region_guid");
        String region_name = httpServletRequest.getParameter("region_name");
        String gateway_id = httpServletRequest.getParameter("gateway_id");
        String start_time = null;
        try {
            start_time = java.net.URLDecoder.decode(httpServletRequest.getParameter("start_time"),   "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String end_time = null;
        try {
            end_time = java.net.URLDecoder.decode(httpServletRequest.getParameter("end_time"),   "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String type = httpServletRequest.getParameter("type");
        int size = Integer.parseInt(httpServletRequest.getParameter("size"));


        if (start_time==null && end_time==null){

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date now  = new Date(System.currentTimeMillis());

            end_time = simpleDateFormat.format(now);

            start_time =  simpleDateFormat.format(new Date(System.currentTimeMillis()-3600*1000));

        }


        List<UserGateway> tableDeviceList = userService.selectGatewayByUserId(user_id);
        String account_id =tableDeviceList.get(0).getAccount_id();
        String SourceId= account_id+ Param.WEBAPPID+String.valueOf(System.currentTimeMillis()/1000000000);
        String DestinationId = gateway_id;

        TableRegionDevice tableRegionDevice=new TableRegionDevice();

        tableRegionDevice.setAccount_id(account_id);

        tableRegionDevice.setRegion_guid(region_guid);

        tableRegionDevice.setGateway_id(gateway_id);

        List<TableRegionDevice> sensors = sensorDataService.getRegionSensor(tableRegionDevice);
        //System.out.println("sensors:"+sensors.size());
        Queue<String> senorQueue = new LinkedList<String>();

        for (TableRegionDevice sensor:sensors) {
            senorQueue.offer(sensor.getTable_device_guid());
        }


        if (senorQueue.size()<1){
            message.setCode("0");
            message.setContent(new JSONArray());
            message.setMessage("there is no sensor in the region!");
            return message;
        }

        int queueSize = senorQueue.size();

        JSONArray reslut_list = new JSONArray();

        for (int i = 0; i <queueSize ; i++) {
            String sensor_guid = senorQueue.poll();
            if (type.equals("history")){
                JSONArray history  =sensorDataService.getSenorData(sensor_guid,account_id,start_time,end_time);

                reslut_list.add(history);
            }else {
                JSONArray realTime = sensorDataService.getRealTime(sensor_guid,account_id,DestinationId,SourceId);
                reslut_list.add(realTime);

            }
        }

        //获取最小数据长度
        JSONArray t = (JSONArray) reslut_list.get(0);
        if (t==null){
//            message.setCode("-1");
//            message.setMessage("gateway error,please try later");
//            message.setContent("[]");
//            return message;
            throw new BussinessException("-1","gateway error,please try later");
        }
        int min_siza = t.size();
        for (int i = 0; i <reslut_list.size() ; i++) {
            JSONArray temp = (JSONArray) reslut_list.get(i);
            if (min_siza>temp.size()){
                min_siza = temp.size();
            }
        }

        //数据矩阵转置
        List<List<JSONObject>> data_temp_result = new LinkedList<List<JSONObject>>();

        for (int i = 0; i <min_siza ; i++) {
            List<JSONObject> meta = new ArrayList<JSONObject>();
            for (int j = 0; j <reslut_list.size() ; j++) {
                JSONArray J = (JSONArray) reslut_list.get(j);
                meta.add((JSONObject) J.get(i));
            }
            data_temp_result.add(meta);
        }

        //整理后
        JSONArray result = new JSONArray();

        for (int i = 0; i <data_temp_result.size() ; i++) {
            List<JSONObject> Meta =data_temp_result.get(i);
            System.out.println("Meta:"+data_temp_result.get(i).toString());

            float air_temperature =0;
            float air_humidity =0;
            float soil_temperature =0;
            float soil_humidity = 0;
            float soil_PH_value=0;
            float carbon_dioxide=0;
            float illuminance=0;
            float soil_conductivity=0;
            float photons=0;
            float liquid_PH_value=0;
            float lai_value=0;

            for (int j = 0; j <Meta.size() ; j++) {
                JSONObject jj= Meta.get(j);
                air_temperature+=Float.parseFloat((String) jj.get("air_temperature"));
                air_humidity+=Float.parseFloat((String) jj.get("air_humidity"));
                soil_temperature+=Float.parseFloat((String) jj.get("soil_temperature"));
                soil_humidity+=Float.parseFloat((String) jj.get("soil_humidity"));
                soil_PH_value+=Float.parseFloat((String) jj.get("soil_PH_value"));
                carbon_dioxide+=Float.parseFloat((String) jj.get("carbon_dioxide"));
                illuminance+=Float.parseFloat((String) jj.get("illuminance"));
                soil_conductivity+=Float.parseFloat((String) jj.get("soil_conductivity"));
                photons+=Float.parseFloat((String) jj.get("photons"));
                liquid_PH_value+=Float.parseFloat((String) jj.get("liquid_PH_value"));

//                lai_value+=Float.parseFloat((String) jj.get("lai_value"));
                lai_value+=0;

                if (j==Meta.size()-1){
                    air_temperature=air_temperature/Meta.size();
                    air_humidity=air_humidity/Meta.size();
                    soil_temperature=soil_temperature/Meta.size();
                    soil_humidity=soil_humidity/Meta.size();
                    soil_PH_value=soil_PH_value/Meta.size();
                    carbon_dioxide=carbon_dioxide/Meta.size();
                    illuminance=illuminance/Meta.size();
                    soil_conductivity=soil_conductivity/Meta.size();
                    photons=photons/Meta.size();
                    liquid_PH_value=liquid_PH_value/Meta.size();
                    soil_humidity=soil_humidity/Meta.size();
                    lai_value=lai_value/Meta.size();
                    JSONObject JJJ = new JSONObject();
                    JJJ.put("air_temperature",air_temperature);
                    JJJ.put("air_humidity",air_humidity);
                    JJJ.put("soil_temperature",soil_temperature);
                    JJJ.put("soil_humidity",soil_humidity);
                    JJJ.put("soil_PH_value",soil_PH_value);
                    JJJ.put("carbon_dioxide",carbon_dioxide);
                    JJJ.put("illuminance",illuminance);
                    JJJ.put("soil_conductivity",soil_conductivity);
                    JJJ.put("photons",photons);
                    JJJ.put("liquid_PH_value",liquid_PH_value);
                    JJJ.put("soil_humidity",soil_humidity);
                    JJJ.put("lai_value",lai_value);
                    result.add(JJJ);
                }
            }
        }


        int arr_len = result.size();

        //总数据除插件
        int let = arr_len/size;
        int hash = arr_len%size;//

        JSONArray hashJson = new JSONArray();


        //当数据查询记过的数据量少于360行时,返回的
       if (let<1){
           hashJson=result;
           for (int i = 0; i <hash ; i++) {
               JSONObject object = (JSONObject) hashJson.get(i);
               float f_soil_PH_value = (Float) object.get("soil_PH_value");
               float f_carbon_dioxide =(Float)  object.get("carbon_dioxide");
               float f_illuminance =(Float) object.get("illuminance");
               float f_photons =(Float) object.get("photons");
               float f_liquid_PH_value =(Float) object.get("liquid_PH_value") ;

               object.put("soil_PH_value",10*f_soil_PH_value);
               object.put("carbon_dioxide",f_carbon_dioxide/10);
               object.put("illuminance",f_illuminance/100);
               object.put("photons",f_photons/100);
               object.put("liquid_PH_value",10*f_liquid_PH_value);

           }
       }else {
           for (int i = 0; i <size ; i++) {
               int tom=let;
               if (i<hash){
                  tom =let+1;
               }
               Float [] air_temperatures = new Float[tom];
               Float [] air_humidity = new Float[tom];
               Float [] soil_temperature = new Float[tom];
               Float [] soil_humidity = new Float[tom];
               Float [] soil_PH_value = new Float[tom];
               Float [] carbon_dioxide = new Float[tom];
               Float [] illuminance = new Float[tom];
               Float [] soil_conductivity = new Float[tom];
               Float [] photons = new Float[tom];
               Float [] liquid_PH_value = new Float[tom];
               Float [] lai_value=new Float[tom];

               for (int j = i; j <i+tom ; j++) {
                   JSONObject jtemp = (JSONObject) result.get(j);
                   air_temperatures[j-i] = (Float) jtemp.get("air_temperature");
                   air_humidity[j-i] =(Float)jtemp.get("air_humidity");
                   soil_temperature[j-i]= (Float)jtemp.get("soil_temperature");
                   soil_humidity[j-i] = (Float)jtemp.get("soil_humidity");
                   soil_PH_value[j-i]=(Float) jtemp.get("soil_PH_value");
                   carbon_dioxide[j-i] =(Float)jtemp.get("carbon_dioxide");
                   illuminance[j-i]=(Float)jtemp.get("illuminance");
                   soil_conductivity[j-i]=(Float)jtemp.get("soil_conductivity");
                   photons[j-i]=(Float)jtemp.get("photons");
                   liquid_PH_value[j-i]=(Float)jtemp.get("liquid_PH_value");
                   lai_value[j-i]=(Float)jtemp.get("lai_value");
               }

               float f_air_temperature =0;
               float f_air_humidity =0;
               float f_soil_temperature =0;
               float f_soil_humidity = 0;
               float f_soil_PH_value=0;
               float f_carbon_dioxide=0;
               float f_illuminance=0;
               float f_soil_conductivity=0;
               float f_photons=0;
               float f_liquid_PH_value=0;
               float f_lai_value=0;

               for (int j = 0; j < tom; j++) {
                   f_air_temperature+=air_temperatures[j];
                   f_air_humidity+=air_humidity[j];
                   f_soil_temperature+=soil_temperature[j];
                   f_soil_humidity+=soil_humidity[j];
                   f_soil_PH_value+=soil_PH_value[j];
                   f_carbon_dioxide+=carbon_dioxide[j];
                   f_illuminance+=illuminance[j];
                   f_soil_conductivity+=soil_conductivity[j];
                   f_photons+=photons[j];
                   f_lai_value+=lai_value[j];
               }
               JSONObject temp = new JSONObject();
               temp.put("air_temperature",f_air_temperature/tom);
               temp.put("air_humidity",f_air_humidity/tom);
               temp.put("soil_temperature",f_soil_temperature/tom);
               temp.put("soil_humidity",f_soil_humidity/tom);
               temp.put("soil_PH_value",10*f_soil_PH_value/tom);
               temp.put("carbon_dioxide",(f_carbon_dioxide/tom)/10);
               temp.put("illuminance",(f_illuminance/tom)/100);
               temp.put("soil_conductivity",f_soil_conductivity/tom);
               temp.put("photons",(f_photons/tom)/100);
               temp.put("liquid_PH_value",10*f_liquid_PH_value/tom);
               temp.put("lai_value",f_lai_value/tom);

               hashJson.add(temp);
           }
       }




        float [] air_temperatures= new float[hashJson.size()];
        float [] air_humiditys= new float[hashJson.size()];
        float [] soil_temperatures= new float[hashJson.size()];
        float [] soil_humiditys= new float[hashJson.size()];
        float [] soil_PH_values= new float[hashJson.size()];
        float [] carbon_dioxides = new float[hashJson.size()];
        float [] illuminances= new float[hashJson.size()];
        float [] soil_conductivitys= new float[hashJson.size()];
        float [] photonss= new float[hashJson.size()];
        float [] liquid_PH_values= new float[hashJson.size()];
        float [] lai_values= new float[hashJson.size()];
        String [] time =new String[hashJson.size()];


        SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
        Date startTime = null;
        Date endTime = null;
        try {
            startTime=sdf.parse(start_time);
            System.out.println("startTime:"+startTime);
            endTime=sdf.parse(end_time);
            System.out.println("endTime:"+endTime);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        long startlong = startTime.getTime();
        long endlong = endTime.getTime();
        long sed = endlong-startlong;
        long ddd = sed/size;
        System.out.println("ddd:"+ddd);

        for (int i = 0; i < hashJson.size(); i++) {

            JSONObject temp = (JSONObject) hashJson.get(i);

            Date tempDate = new Date(startlong);

            time[i]=sdf.format(tempDate);

            air_temperatures[i] = (Float) temp.get("air_temperature");
            air_humiditys[i]    =  (Float)temp.get("air_humidity");
            soil_temperatures[i] = (Float)temp.get("soil_temperature");
            soil_humiditys[i]   = (Float)temp.get("soil_humidity");
            soil_PH_values[i] = (Float)temp.get("soil_PH_value");
            carbon_dioxides[i]  = (Float)temp.get("carbon_dioxide");
            illuminances[i] = (Float)temp.get("illuminance");
            soil_conductivitys[i] = (Float)temp.get("soil_conductivity");
            photonss[i]        = (Float) temp.get("photons");
            liquid_PH_values[i]    = (Float)temp.get("liquid_PH_value");
            lai_values[i] = (Float) temp.get("lai_value");




            startlong+=ddd;
        }



        JSONObject air_temperature=new JSONObject();
        air_temperature.put("name","air_temperature(°C)");
        air_temperature.put("data",air_temperatures);
        //
        float[] original_air_temperature=new float[air_temperatures.length];
        for (int i=0;i<air_temperatures.length;i++){
            original_air_temperature[i]=air_temperatures[i];
        }
        air_temperature.put("original_data",original_air_temperature);


        JSONObject air_humidity=new JSONObject();
        air_humidity.put("name","air_humidity(%)");
        air_humidity.put("data",air_humiditys);
        //
        float[] original_air_humidity=new float[air_humiditys.length];
        for (int i=0;i<air_humiditys.length;i++){
            original_air_humidity[i]=air_humiditys[i];
        }
        air_humidity.put("original_data",original_air_humidity);


        JSONObject soil_temperature=new JSONObject();
        soil_temperature.put("name","soil_temperature(°C)");
        soil_temperature.put("data",soil_temperatures);
        //
        float[] original_soil_temperature=new float[soil_temperatures.length];
        for (int i=0;i<soil_temperatures.length;i++){
            original_soil_temperature[i]=soil_temperatures[i];
        }
        soil_temperature.put("original_data",original_soil_temperature);

        JSONObject soil_humidity = new JSONObject();
        soil_humidity.put("name","soil_humidity(%)");
        soil_humidity.put("data",soil_humiditys);
        //
        float[] original_soil_humidity=new float[soil_humiditys.length];
        for (int i=0;i<soil_humiditys.length;i++){
            original_soil_humidity[i]=soil_humiditys[i];
        }
        soil_humidity.put("original_data",original_soil_humidity);

        JSONObject soil_PH_value=new JSONObject();
        soil_PH_value.put("name","soil_PH_value(PH)");
        soil_PH_value.put("data",soil_PH_values);
        //原数据
        float[] original_soil_PH_values=new float[soil_PH_values.length];
        for (int i=0;i<soil_PH_values.length;i++){
            original_soil_PH_values[i]=soil_PH_values[i]/10;
        }
        soil_PH_value.put("original_data",original_soil_PH_values);

        JSONObject carbon_dioxide=new JSONObject();
        carbon_dioxide.put("name","carbon_dioxide(ppm)");
        carbon_dioxide.put("data",carbon_dioxides);
        //原数据
        float[] original_carbon_dioxide=new float[carbon_dioxides.length];
        for (int i=0;i<carbon_dioxides.length;i++){
            original_carbon_dioxide[i]=carbon_dioxides[i]*10;
        }
        carbon_dioxide.put("original_data",original_carbon_dioxide);

        JSONObject illuminance=new JSONObject();
        illuminance.put("name","illuminance(100*lux)");
        illuminance.put("data",illuminances);
        //原数据
        float[] original_illuminance=new float[illuminances.length];
        for (int i=0;i<illuminances.length;i++){
            original_illuminance[i]=illuminances[i]*100;
        }
        illuminance.put("original_data",original_illuminance);

        JSONObject soil_conductivity=new JSONObject();
        soil_conductivity.put("name","soil_conductivity(mS/cm)");
        soil_conductivity.put("data",soil_conductivitys);
        //原数据
        float[] original_soil_conductivity=new float[soil_conductivitys.length];
        for (int i=0;i<soil_conductivitys.length;i++){
            original_soil_conductivity[i]=soil_conductivitys[i];
        }
        soil_conductivity.put("original_data",original_soil_conductivity);

        JSONObject photons=new JSONObject();
        photons.put("name","photons(μmol/s/m²)");
        photons.put("data",photonss);
        //原数据
        float[] original_photons=new float[photonss.length];
        for (int i=0;i<photonss.length;i++){
            original_photons[i]=photonss[i]*100;
        }
        photons.put("original_data",original_photons);

        JSONObject liquid_PH_value= new JSONObject();
        liquid_PH_value.put("name","liquid_PH_value(PH)");
        liquid_PH_value.put("data",liquid_PH_values);
        //原数据
        float[] original_liquid_PH_value=new float[liquid_PH_values.length];
        for (int i=0;i<liquid_PH_values.length;i++){
            original_liquid_PH_value[i]=liquid_PH_values[i]/10;
        }
        liquid_PH_value.put("original_data",original_liquid_PH_value);


        JSONObject lai_value = new JSONObject();
        lai_value.put("name","lai_value()");
        lai_value.put("data",lai_values);
        //原数据
        float[] original_lai_value=new float[lai_values.length];
        for (int i=0;i<lai_values.length;i++){
            original_lai_value[i]=lai_values[i]/10;
        }
        lai_value.put("original_data",original_lai_value);


        JSONArray lines = new JSONArray();
        lines.add(air_temperature);
        lines.add(air_humidity);
        lines.add(soil_temperature);
        lines.add(soil_humidity);
        lines.add(soil_PH_value);
        lines.add(carbon_dioxide);
        lines.add(illuminance);
        lines.add(soil_conductivity);
        lines.add(photons);
        lines.add(liquid_PH_value);
        lines.add(lai_value);

        JSONObject jresult = new JSONObject();


        jresult.put("time",time);

        jresult.put("region_name",region_name);
        jresult.put("data",lines);

        message.setCode("0");
        message.setMessage("Search Successfully");
        message.setContent(jresult);


        System.out.println(jresult.toString());
        return message;
    }

    //实时数据
    @RequestMapping( value = "/nowData",method = RequestMethod.GET)
    @ResponseBody
    public Object nowData(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse){

        Message message = new Message();

        String user_id = httpServletRequest.getParameter("user_id");
        String region_guid = httpServletRequest.getParameter("region_guid");
        String region_name = httpServletRequest.getParameter("region_name");
        String gateway_id = httpServletRequest.getParameter("gateway_id");
        //String account_id = httpServletRequest.getParameter("account_id");
//        String start_time = null;
//        String end_time = null;
        //System.out.println(start_time);
        //String type = httpServletRequest.getParameter("type");
        //int size = Integer.parseInt(httpServletRequest.getParameter("size"));

            //SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            //Date now  = new Date(System.currentTimeMillis());
            //end_time = simpleDateFormat.format(now);
            //System.out.println("end_time:"+end_time);
            //start_time =  simpleDateFormat.format(new Date(System.currentTimeMillis()-3600*1000));

        List<UserGateway> tableDeviceList = userService.selectGatewayByUserId(user_id);
        String account_id =tableDeviceList.get(0).getAccount_id();

        String DestinationId = gateway_id;

        TableRegionDevice tableRegionDevice=new TableRegionDevice();

        tableRegionDevice.setAccount_id(account_id);

        tableRegionDevice.setRegion_guid(region_guid);

        tableRegionDevice.setGateway_id(gateway_id);

        List<TableRegionDevice> sensors = sensorDataService.getRegionSensor(tableRegionDevice);
        //System.out.println("sensors:"+sensors.size());
        Queue<String> senorQueue = new LinkedList<String>();

        for (TableRegionDevice sensor:sensors) {
            senorQueue.offer(sensor.getTable_device_guid());
        }

        if (senorQueue.size()<1){

            throw new BussinessException("-1","there is no sensor in the region!");
        }

        int queueSize = senorQueue.size();

        JSONArray reslut_list = new JSONArray();
//        System.out.println("end_time:"+end_time);
//        System.out.println("start_time:"+start_time);
        String sensor_guid="";
        for (int i = 0; i <queueSize ; i++) {
             sensor_guid = senorQueue.poll();


                JSONArray realTime = sensorDataService.getDataNowTime(sensor_guid,account_id,DestinationId);
                //System.out.println(realTime.toString());
                if (realTime.size()<1){
                    throw new BussinessException("0","the data does not exist");
                }
                JSONObject jsonObject=(JSONObject)realTime.get(0);
                jsonObject.remove("reserve01");
//                jsonObject.remove("table_device_guid");
                jsonObject.remove("record_guid");
                jsonObject.remove("record_time");
                //System.out.println("jsonObject:"+jsonObject.toString());
                //reslut_list=new JSONArray(jsonObject.size());

                JSONObject jsonObject1=new JSONObject(jsonObject);
                Set<String> mapes=jsonObject1.keySet();
                for (String  keyes:mapes){
                    if (!jsonObject1.get(keyes).toString().equals("")){
                        if (keyes.equals("air_Humidity")){
                            //keyes="air_humidity";
                            jsonObject.put(keyes,jsonObject1.get(keyes)+"%");

                        }
                        if (keyes.equals("air_Temperature")){

                            jsonObject1.put(keyes,jsonObject1.get(keyes)+"°C");
                        }
                        if (keyes.equals("carbon_Dioxide")){

                            jsonObject.put(keyes,jsonObject1.get(keyes)+"ppm");
                        }
                        if (keyes.equals("illuminance")){
                            jsonObject.put(keyes,jsonObject1.get(keyes)+"lux");
                        }
                        if (keyes.equals("lai")){

                            jsonObject.put(keyes,jsonObject1.get(keyes)+"");
                        }
                        if (keyes.equals("liquid_Conductivity")){

                            jsonObject.put(keyes,jsonObject1.get(keyes)+"mS/cm");
                        }
                        if (keyes.equals("liquid_DOC")){

                            jsonObject.put(keyes,jsonObject1.get(keyes)+"mg/l");
                        }
                        if (keyes.equals("liquid_PH")){

                            jsonObject.put(keyes,jsonObject1.get(keyes)+"PH");
                        }
                        if (keyes.equals("ppfd")){

                            jsonObject.put(keyes,jsonObject1.get(keyes)+"μmol ㎡/s");
                        }
                        if (keyes.equals("substrate_Conductivity")){

                            jsonObject.put(keyes,jsonObject1.get(keyes)+"mS/cm");
                        }

                        if (keyes.equals("substrate_DOC")){

                            jsonObject.put(keyes,jsonObject1.get(keyes)+"mg/l");
                        }
                        if (keyes.equals("substrate_Humidity")){

                            jsonObject.put(keyes,jsonObject1.get(keyes)+"%");
                        }
                        if (keyes.equals("substrate_PH")){

                            jsonObject.put(keyes,jsonObject1.get(keyes)+"PH");
                        }
                        if (keyes.equals("substrate_Temperature")){

                            jsonObject.put(keyes,jsonObject1.get(keyes)+"°C");
                        }
                    }else {
                        jsonObject.put(keyes,"no data");
                    }


                }
                jsonObject.put("region_guid",region_guid);
                jsonObject.put("minTime",realTime.get(realTime.size()-1));
                jsonObject.put("table_device_guid",sensor_guid);



                System.out.println(jsonObject1.toString());
                reslut_list.add(jsonObject);

        }
        message.setCode("0");
        message.setMessage("Query real time data success");
        message.setContent(reslut_list);
        return message;

    }

    @RequestMapping(method = RequestMethod.GET)//曲线图最新版
    @ResponseBody
    public Object historyDatas(HttpServletRequest request, HttpServletResponse response){
        Message message = new Message();
        String user_id = request.getParameter("user_id");
        String region_guid = request.getParameter("region_guid");
        String region_name = request.getParameter("region_name");
        String gateway_id = request.getParameter("gateway_id");
        String data_type=request.getParameter("data_type");
        String start_time = null;
        //start_time="2016-08-06 20:00:00";
        try {
            start_time = java.net.URLDecoder.decode(request.getParameter("start_time"),   "utf-8");
            //System.out.println("start_time:"+start_time);
            //start_time="2016-07-08 00:00:00";
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        //System.out.println("start_time:"+start_time);
        String end_time = null;
        //end_time="2016-08-08 20:00:00";
        try {
            end_time = java.net.URLDecoder.decode(request.getParameter("end_time"),   "utf-8");
            //System.out.println("end_time:"+end_time);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        //System.out.println(start_time);
        String type = request.getParameter("type");
        //int size = Integer.parseInt(request.getParameter("size"));
        int size=100;


        if (start_time==null && end_time==null){

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date now  = new Date(System.currentTimeMillis());

            end_time = simpleDateFormat.format(now);

            start_time =  simpleDateFormat.format(new Date(System.currentTimeMillis()-3600*1000));

        }

        List<UserGateway> tableDeviceList = userService.selectGatewayByUserId(user_id);
        String account_id =tableDeviceList.get(0).getAccount_id();
        String SourceId= account_id+ Param.WEBAPPID+String.valueOf(System.currentTimeMillis()/1000000000);
        String DestinationId = gateway_id;

        TableRegionDevice tableRegionDevice=new TableRegionDevice();

        tableRegionDevice.setAccount_id(account_id);

        tableRegionDevice.setRegion_guid(region_guid);

        tableRegionDevice.setGateway_id(gateway_id);

        List<TableRegionDevice> sensors = sensorDataService.getRegionSensor(tableRegionDevice);
        //System.out.println("sensors:"+sensors.size());
        Queue<String> senorQueue = new LinkedList<String>();

        for (TableRegionDevice sensor:sensors) {
            senorQueue.offer(sensor.getTable_device_guid());
        }


        if (senorQueue.size()<1){

            throw new  BussinessException("-1","there is no sensor in the region!");
        }

        int queueSize = senorQueue.size();

        JSONArray reslut_list = new JSONArray();

        for (int i = 0; i <queueSize ; i++) {
            String sensor_guid = senorQueue.poll();
            if (type.equals("history")){
                JSONArray history  =sensorDataService.getSenorData(sensor_guid,account_id,start_time,end_time);
                if (history.size()==0){
                    JSONObject historyObject=new JSONObject();
                    historyObject.put("carbon_Dioxide","0");
                    historyObject.put("substrate_PH","0");
                    historyObject.put("substrate_Conductivity","0");
                    historyObject.put("substrate_Temperature","0");
                    historyObject.put("air_Temperature","0");
                    historyObject.put("ppfd","0");

                    historyObject.put("liquid_PH","0");
                    historyObject.put("substrate_Humidity","0");
                    historyObject.put("liquid_DOC","0");
                    historyObject.put("liquid_Conductivity","0");

                    historyObject.put("air_Humidity","0");
                    historyObject.put("substrate_DOC","0");
                    historyObject.put("illuminance","0");
                    historyObject.put("lai","0");

                    historyObject.put("record_time",start_time);
                    history.add(historyObject);
                    reslut_list.add(history);
                }else {
                    //System.out.println("history:"+history.toString());
                    reslut_list.add(history);
                }

            }else {
                throw new BussinessException("-1","The sensor has no data");
            }
        }
        //System.out.println("reslut_list:"+reslut_list.toString());
        //System.out.println("reslut_list:"+reslut_list.toString());

        //获取最小数据长度
        JSONArray t = (JSONArray) reslut_list.get(0);
        if (t==null){

            throw new BussinessException("-1","gateway error,please try later");
        }
        //多个传感器取数据最小那个数据
        int min_siza = t.size();
        int index = 0;
        for (int i = 0; i <reslut_list.size() ; i++) {
            JSONArray temp = (JSONArray) reslut_list.get(i);
            if (min_siza>temp.size()){
                min_siza = temp.size();
                index=i;
            }
        }

        //数据矩阵转置
        List<List<JSONObject>> data_temp_result = new LinkedList<List<JSONObject>>();

        for (int i = 0; i <min_siza ; i++) {
            List<JSONObject> meta = new ArrayList<JSONObject>();
            for (int j = 0; j <reslut_list.size() ; j++) {
                JSONArray J = (JSONArray) reslut_list.get(j);
                meta.add((JSONObject) J.get(i));
            }
            data_temp_result.add(meta);
        }
//

        //整理后
        JSONArray result = new JSONArray();

        for (int i = 0; i <data_temp_result.size() ; i++) {
            List<JSONObject> Meta =data_temp_result.get(i);
            //System.out.println("Meta:"+data_temp_result.get(i).toString());
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

            for (int j = 0; j <Meta.size() ; j++) {
                JSONObject jj= Meta.get(j);
                carbon_Dioxide=Float.parseFloat((String) jj.get("carbon_Dioxide"));
                substrate_PH=Float.parseFloat((String) jj.get("substrate_PH"));
                substrate_Conductivity=Float.parseFloat((String) jj.get("substrate_Conductivity"));
                substrate_Temperature=Float.parseFloat((String) jj.get("substrate_Temperature"));
                air_Temperature=Float.parseFloat((String) jj.get("air_Temperature"));
                ppfd=Float.parseFloat((String) jj.get("ppfd"));
                liquid_PH=Float.parseFloat((String) jj.get("liquid_PH"));
                substrate_Humidity=Float.parseFloat((String) jj.get("substrate_Humidity"));
                if (jj.get("liquid_DOC").toString().toString().equals("")){
                    liquid_DOC=0;
                }else {
                    liquid_DOC=Float.parseFloat( jj.get("liquid_DOC").toString());
                }
                if (jj.get("liquid_Conductivity").toString().toString().equals("")){
                    liquid_Conductivity=0;
                }else {
                    liquid_Conductivity=Float.parseFloat((String) jj.get("liquid_Conductivity"));
                }

                air_Humidity=Float.parseFloat((String) jj.get("air_Humidity"));
                if (jj.get("substrate_DOC").toString().toString().equals("")){
                    substrate_DOC=0;
                }else {
                    substrate_DOC=Float.parseFloat((String) jj.get("substrate_DOC"));
                }

                illuminance=Float.parseFloat((String) jj.get("illuminance"));

                timess=(String) jj.get("record_time");

//                lai_value+=Float.parseFloat((String) jj.get("lai_value"));
                lai=0;

                JSONObject JJJ = new JSONObject();
                JJJ.put("carbon_Dioxide",carbon_Dioxide);
                JJJ.put("substrate_PH",substrate_PH);
                JJJ.put("substrate_Conductivity",substrate_Conductivity);
                JJJ.put("substrate_Temperature",substrate_Temperature);
                JJJ.put("air_Temperature",air_Temperature);
                JJJ.put("ppfd",ppfd);
                JJJ.put("liquid_PH",liquid_PH);
                JJJ.put("substrate_Humidity",substrate_Humidity);
                JJJ.put("liquid_DOC",liquid_DOC);
                JJJ.put("liquid_Conductivity",liquid_Conductivity);
                JJJ.put("air_Humidity",air_Humidity);
                JJJ.put("substrate_DOC",substrate_DOC);
                JJJ.put("illuminance",illuminance);
                JJJ.put("illuminance",illuminance);
                JJJ.put("lai",lai);
                JJJ.put("timess",timess);
                result.add(JJJ);
            }
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date startTime = null;
        Date endTime = null;
        try {
            startTime=sdf.parse(start_time);
            //System.out.println("startTime:"+startTime);
            endTime=sdf.parse(end_time);
            //System.out.println("endTime:"+endTime);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        long startlong = startTime.getTime();
        long endlong = endTime.getTime();
        long sed = endlong-startlong;
        long ddd = sed/size;
        if (ddd<30000){
            ddd=30000;
            long sizes=sed/ddd;
            size=(int)sizes;
        }

        //取时间段的平均值
        JSONArray result_point_count=new JSONArray();//所有时间点值的平均值集合
        JSONArray result_times_count=new JSONArray();//所有时间点的集合

        for (int i=0;i<size;i++){
            //时间自增
            long time_point=startlong+ddd;

            result_times_count.add(startlong);//时间点
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

            int indexs=0;//获取时间段内满足条件个数
            LinkedHashMap times_point_avg=new LinkedHashMap();
            for (int j=0;j<result.size();j++){
                JSONObject jsonObject=(JSONObject) result.get(j);
                String times=(String) jsonObject.get("timess");
                Date times_date = null;
                try {
                    times_date=sdf.parse(times);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                long times_long=times_date.getTime();

                if (startlong<=times_long&&time_point>=times_long){
                    indexs++;
                    carbon_Dioxide+=(Float) jsonObject.get("carbon_Dioxide");
                    substrate_PH+=(Float) jsonObject.get("substrate_PH");
                    substrate_Conductivity+=(Float) jsonObject.get("substrate_Conductivity");
                    substrate_Temperature+=(Float) jsonObject.get("substrate_Temperature");
                    air_Temperature+=(Float) jsonObject.get("air_Temperature");
                    ppfd+=(Float) jsonObject.get("ppfd");
                    liquid_PH+=(Float) jsonObject.get("liquid_PH");
                    substrate_Humidity+=(Float) jsonObject.get("substrate_Humidity");
                    liquid_DOC+=(Float) jsonObject.get("liquid_DOC");
                    liquid_Conductivity+=(Float) jsonObject.get("liquid_Conductivity");
                    air_Humidity+=(Float) jsonObject.get("air_Humidity");
                    substrate_DOC+=(Float) jsonObject.get("substrate_DOC");
                    illuminance+=(Float) jsonObject.get("illuminance");
                    lai=0;

                    times_point_avg.put("air_Temperature",air_Temperature);
                    times_point_avg.put("air_Humidity",air_Humidity);
                    times_point_avg.put("substrate_Temperature",substrate_Temperature);
                    times_point_avg.put("substrate_Humidity",substrate_Humidity);
                    times_point_avg.put("substrate_PH",substrate_PH);
                    times_point_avg.put("carbon_Dioxide",carbon_Dioxide);
                    times_point_avg.put("illuminance",illuminance);
                    times_point_avg.put("substrate_Conductivity",substrate_Conductivity);
                    times_point_avg.put("ppfd",ppfd);
                    times_point_avg.put("liquid_PH",liquid_PH);
                    times_point_avg.put("lai",lai);
                    times_point_avg.put("liquid_Conductivity",liquid_Conductivity);
                    times_point_avg.put("liquid_DOC",liquid_DOC);
                    times_point_avg.put("substrate_DOC",substrate_DOC);

                    //times_point_avg.put("timess",timess);
                }
            }
            //

            //获取时间段内的平均值
            Set<String> map=times_point_avg.keySet();
            for (String key:map){
                times_point_avg.put(key,((Float)times_point_avg.get(key))/indexs);
            }
            //System.out.println("times_point_avg:"+times_point_avg.toString());

            //如果不存在值则该点为0
            if (times_point_avg.size()<1){
                JSONObject temp=(JSONObject) result.get(0);
                //JSONObject temps=new JSONObject(temp);
                //times_point_avg=temps;
                Set<String> maps=temp.keySet();
                for (String key:maps){
                    times_point_avg.put(key,"0");
                }
            }
            //System.out.println("times_point_avg:"+times_point_avg.toString());
            result_point_count.add(times_point_avg);
                //时间点自增
                startlong=time_point;

            //System.out.println(time_point);

        }
        //System.out.println("result_point_count:"+result_point_count.toString());
        LinkedHashMap result_point_last=(LinkedHashMap) result_point_count.get(size-1);
        Set<String> maps_last=result_point_last.keySet();
        float maxKeyValue=0;
        for (String key:maps_last){
            if (maxKeyValue==0){
                maxKeyValue=Float.parseFloat(result_point_last.get(key).toString());
            }
            if (maxKeyValue<=Float.parseFloat(result_point_last.get(key).toString())){
                maxKeyValue=Float.parseFloat(result_point_last.get(key).toString());
            }
        }
        if (maxKeyValue==0){

            result_point_last=(LinkedHashMap) result_point_count.get(size-2);
            result_point_count.set(size-1,result_point_last);
        }
//        System.out.println(result_point_last.toString());
//        System.out.println(result_point_count.get(size-1).toString());
        //转换数据出参

        JSONArray data=new JSONArray();
        LinkedHashMap temp=(LinkedHashMap) result_point_count.get(0);
        temp.remove("timess");
        Set<String> maps=temp.keySet();
        for (String key:maps){

            JSONArray datas=new JSONArray();
            JSONArray original_data=new JSONArray();
            for (int p=0;p<result_point_count.size();p++){
                LinkedHashMap temps=(LinkedHashMap) result_point_count.get(p);
                //原始数据
                original_data.add(Float.parseFloat(temps.get(key).toString()));
                if (data_type!=null){

                    if (data_type.equals(key)){
                        if (key.equals("substrate_PH")){
                            datas.add(Float.parseFloat( temps.get(key).toString())*10 );
                        }else if (key.equals("carbon_Dioxide")){
                            datas.add(Float.parseFloat((String)temps.get(key).toString()) /10);
                        }

                        else if (key.equals("illuminance")){
                            datas.add(Float.parseFloat((String)temps.get(key).toString()) /100);
                        }
                        else if (key.equals("ppfd")){
                            datas.add(Float.parseFloat((String)temps.get(key).toString())/100);
                        }
                        else if (key.equals("liquid_PH")){
                            datas.add( Float.parseFloat((String)temps.get(key).toString())*10);
                        }
                        else {
                            datas.add(Float.parseFloat(temps.get(key).toString()));
                        }

                    }

                }else {
                    //处理数据
                    if (key.equals("substrate_PH")){
                        datas.add(Float.parseFloat( temps.get(key).toString())*10 );
                    }else if (key.equals("carbon_Dioxide")){
                        datas.add(Float.parseFloat((String)temps.get(key).toString()) /10);
                    }

                    else if (key.equals("illuminance")){
                        datas.add(Float.parseFloat((String)temps.get(key).toString()) /100);
                    }
                    else if (key.equals("ppfd")){
                        datas.add(Float.parseFloat((String)temps.get(key).toString())/100);
                    }
                    else if (key.equals("liquid_PH")){
                        datas.add( Float.parseFloat((String)temps.get(key).toString())*10);
                    }
                    else {
                        datas.add(Float.parseFloat(temps.get(key).toString()));
                    }
                }



            }
            JSONObject jsonObject=new JSONObject();

            if (data_type!=null){
                if (data_type.equals(key)){
                    if (key.equals("air_Humidity")){
                        key="air_Humidity(%)";
                        jsonObject.put("original_name",(new StringBuffer()).append(Character.toUpperCase(key.charAt(0))).append(key.substring(1)).toString());
                    }
                    if (key.equals("air_Temperature")){
                        key="air_Temperature(°C)";
                        jsonObject.put("original_name",(new StringBuffer()).append(Character.toUpperCase(key.charAt(0))).append(key.substring(1)).toString());
                    }
                    if (key.equals("illuminance")){

                        jsonObject.put("original_name",(new StringBuffer()).append(Character.toUpperCase(key.charAt(0))).append(key.substring(1)).toString()+"(lux)");
                        key="illuminance(100*lux)";
                    }
                    if (key.equals("lai")){
                        key="lai()";
                        jsonObject.put("original_name",(new StringBuffer()).append(Character.toUpperCase(key.charAt(0))).append(key.substring(1)).toString());
                    }

                    //
                    if (key.equals("carbon_Dioxide")){
                        key="carbon_Dioxide(ppm)";
                        jsonObject.put("original_name",(new StringBuffer()).append(Character.toUpperCase(key.charAt(0))).append(key.substring(1)).toString());
                    }
                    if (key.equals("substrate_Conductivity")){
                        key="substrate_Conductivity(mS/cm)";
                        jsonObject.put("original_name",(new StringBuffer()).append(Character.toUpperCase(key.charAt(0))).append(key.substring(1)).toString());
                    }

                    if (key.equals("substrate_Temperature")){

                        jsonObject.put("original_name",(new StringBuffer()).append(Character.toUpperCase(key.charAt(0))).append(key.substring(1)).toString());
                        key="substrate_Temperature(°C)";
                    }
                    if (key.equals("ppfd")){
                        jsonObject.put("original_name",(new StringBuffer()).append(Character.toUpperCase(key.charAt(0))).append(key.substring(1)).toString()+"(μmol ㎡/s)");
                        key="ppfd(100*μmol ㎡/s )";

                    }
                    if (key.equals("liquid_PH")){

                        key=key+"(PH)";
                        jsonObject.put("original_name",(new StringBuffer()).append(Character.toUpperCase(key.charAt(0))).append(key.substring(1)).toString());
                    }

                    if (key.equals("substrate_Humidity")){
                        key="substrate_Humidity(%)";
                        jsonObject.put("original_name",(new StringBuffer()).append(Character.toUpperCase(key.charAt(0))).append(key.substring(1)).toString());
                    }
                    if (key.equals("liquid_DOC")){
                        key="liquid_DOC(mg/l)";
                        jsonObject.put("original_name",(new StringBuffer()).append(Character.toUpperCase(key.charAt(0))).append(key.substring(1)).toString());
                    }
                    if (key.equals("liquid_Conductivity")){
                        key="liquid_Conductivity(mS/cm)";
                        jsonObject.put("original_name",(new StringBuffer()).append(Character.toUpperCase(key.charAt(0))).append(key.substring(1)).toString());
                    }
                    if (key.equals("substrate_DOC")){
                        key="substrate_DOC(mg/l)";
                        jsonObject.put("original_name",(new StringBuffer()).append(Character.toUpperCase(key.charAt(0))).append(key.substring(1)).toString());
                    }
                    if (key.equals("substrate_PH")){
                        key="substrate_PH(PH)";
                        jsonObject.put("original_name",(new StringBuffer()).append(Character.toUpperCase(key.charAt(0))).append(key.substring(1)).toString());
                    }
                    jsonObject.put("name",(new StringBuffer()).append(Character.toUpperCase(key.charAt(0))).append(key.substring(1)).toString());
                    jsonObject.put("original_data",original_data);
                    jsonObject.put("data",datas);
                    data.add(jsonObject);

                }
            }else {
                //添加传感器单位
                if (key.equals("air_Humidity")){
                    key="air_Humidity(%)";
                    jsonObject.put("original_name",(new StringBuffer()).append(Character.toUpperCase(key.charAt(0))).append(key.substring(1)).toString());
                }
                if (key.equals("air_Temperature")){
                    key="air_Temperature(°C)";
                    jsonObject.put("original_name",(new StringBuffer()).append(Character.toUpperCase(key.charAt(0))).append(key.substring(1)).toString());
                }
                if (key.equals("illuminance")){

                    jsonObject.put("original_name",(new StringBuffer()).append(Character.toUpperCase(key.charAt(0))).append(key.substring(1)).toString()+"(lux)");
                    key="illuminance(100*lux)";
                }
                if (key.equals("lai")){
                    key="lai()";
                    jsonObject.put("original_name",(new StringBuffer()).append(Character.toUpperCase(key.charAt(0))).append(key.substring(1)).toString());
                }

                //
                if (key.equals("carbon_Dioxide")){
                    key="carbon_Dioxide(ppm)";
                    jsonObject.put("original_name",(new StringBuffer()).append(Character.toUpperCase(key.charAt(0))).append(key.substring(1)).toString());
                }
                if (key.equals("substrate_Conductivity")){
                    key="substrate_Conductivity(mS/cm)";
                    jsonObject.put("original_name",(new StringBuffer()).append(Character.toUpperCase(key.charAt(0))).append(key.substring(1)).toString());
                }

                if (key.equals("substrate_Temperature")){

                    jsonObject.put("original_name",(new StringBuffer()).append(Character.toUpperCase(key.charAt(0))).append(key.substring(1)).toString());
                    key="substrate_Temperature(°C)";
                }
                if (key.equals("ppfd")){
                    jsonObject.put("original_name",(new StringBuffer()).append(Character.toUpperCase(key.charAt(0))).append(key.substring(1)).toString()+"(μmol ㎡/s)");
                    key="ppfd(100*μmol ㎡/s )";

                }
                if (key.equals("liquid_PH")){

                    key=key+"(PH)";
                    jsonObject.put("original_name",(new StringBuffer()).append(Character.toUpperCase(key.charAt(0))).append(key.substring(1)).toString());
                }

                if (key.equals("substrate_Humidity")){
                    key="substrate_Humidity(%)";
                    jsonObject.put("original_name",(new StringBuffer()).append(Character.toUpperCase(key.charAt(0))).append(key.substring(1)).toString());
                }
                if (key.equals("liquid_DOC")){
                    key="liquid_DOC(mg/l)";
                    jsonObject.put("original_name",(new StringBuffer()).append(Character.toUpperCase(key.charAt(0))).append(key.substring(1)).toString());
                }
                if (key.equals("liquid_Conductivity")){
                    key="liquid_Conductivity(mS/cm)";
                    jsonObject.put("original_name",(new StringBuffer()).append(Character.toUpperCase(key.charAt(0))).append(key.substring(1)).toString());
                }
                if (key.equals("substrate_DOC")){
                    key="substrate_DOC(mg/l)";
                    jsonObject.put("original_name",(new StringBuffer()).append(Character.toUpperCase(key.charAt(0))).append(key.substring(1)).toString());
                }
                if (key.equals("substrate_PH")){
                    key="substrate_PH(PH)";
                    jsonObject.put("original_name",(new StringBuffer()).append(Character.toUpperCase(key.charAt(0))).append(key.substring(1)).toString());
                }
                jsonObject.put("name",(new StringBuffer()).append(Character.toUpperCase(key.charAt(0))).append(key.substring(1)).toString());
                jsonObject.put("original_data",original_data);
                jsonObject.put("data",datas);
                data.add(jsonObject);
            }

        }

        //时间转换为String型
        JSONArray result_times_counts=new JSONArray();
        for (int k=0;k<result_times_count.size();k++){
            long times=(Long) result_times_count.get(k);
            Date times_date=new Date(times);
            result_times_counts.add(sdf.format(times_date));
        }

        JSONObject object=new JSONObject();
        object.put("data",data);
        object.put("region_name","");
        object.put("size",size);
        object.put("time",result_times_counts);
        //System.out.println("object:"+object.toString());

        message.setCode("0");
        message.setMessage("query history data success");
        message.setContent(object);
        return message;

    }

    //查询传感器一天数据
    @RequestMapping( value ="/sensorOneDay",method = RequestMethod.GET)
    @ResponseBody
    public Message getHistorySensorDateDay(HttpServletRequest request,HttpServletResponse response){

        String user_id = request.getParameter("user_id");
        String device_guid = request.getParameter("table_device_guid");
        //String device_name = request.getParameter("device_name");
        String gateway_id = request.getParameter("gateway_id");
        String data_type=request.getParameter("data_type");

        String start_time = null;
        try {
            start_time = java.net.URLDecoder.decode(request.getParameter("start_time"),   "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String end_time = null;
        try {
            end_time = java.net.URLDecoder.decode(request.getParameter("end_time"),   "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        if (user_id==null||user_id.equals("")){
            throw new ParameterException("-1","user_id does not exist");
        }
        if (device_guid==null||device_guid.equals("")){
            throw new ParameterException("-1","table_device_guid does not exist");
        }
//        if (region_name==null||region_name.equals("")){
//            throw new ParameterException("-1","region_name does not exist");
//        }
        if (gateway_id==null||gateway_id.equals("")){
            throw new ParameterException("-1","gateway_id does not exist");
        }

        if (start_time==null||start_time.equals("")){
            throw new ParameterException("-1","start_time does not exist");
        }if (end_time==null||end_time.equals("")){
            throw new ParameterException("-1","end_time does not exist");
        }
        JSONObject object=sensorDataService.getSensorDayData(device_guid,user_id,start_time,end_time,data_type);
        Message message=new Message();
        message.setCode("0");
        message.setMessage("Query one day sensor data success");
        message.setContent(object);
        return message;
    }


    @RequestMapping(value = "/history/export",method = RequestMethod.GET)
    @ResponseBody
    public String historyDataExport(HttpServletRequest httpServletRequest, HttpServletResponse response,Model model){


        String user_id = httpServletRequest.getParameter("user_id");
        String region_guid = httpServletRequest.getParameter("region_guid");
        String gateway_id = httpServletRequest.getParameter("gateway_id");

        String start_time = null;
        try {
            start_time = java.net.URLDecoder.decode(httpServletRequest.getParameter("start_time"),   "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String end_time = null;
        try {
            end_time = java.net.URLDecoder.decode(httpServletRequest.getParameter("end_time"),   "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }


        if (start_time==null && end_time==null){

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date now  = new Date(System.currentTimeMillis());

            end_time = simpleDateFormat.format(now);

            start_time =  simpleDateFormat.format(new Date(System.currentTimeMillis()-3600*1000));

        }

        List<UserGateway> tableDeviceList = userService.selectGatewayByUserId(user_id);
        String account_id =tableDeviceList.get(0).getAccount_id();
        String SourceId= account_id+ Param.WEBAPPID+String.valueOf(System.currentTimeMillis()/1000000000);
        String DestinationId = gateway_id;

        TableRegionDevice tableRegionDevice=new TableRegionDevice();

        tableRegionDevice.setAccount_id(account_id);

        tableRegionDevice.setRegion_guid(region_guid);

        tableRegionDevice.setGateway_id(gateway_id);

        List<TableRegionDevice> sensors = sensorDataService.getRegionSensor(tableRegionDevice);

        Queue<String> senorQueue = new LinkedList<String>();

        for (TableRegionDevice sensor:sensors) {
            senorQueue.offer(sensor.getTable_device_guid());
        }

        if (senorQueue.size()<1){
            model.addAttribute("msg","there is no sensor in the region!");
            return "excelerror";
        }

        int queueSize = senorQueue.size();

        JSONArray reslut_list = new JSONArray();

        for (int i = 0; i <queueSize ; i++) {
            String sensor_guid = senorQueue.poll();

            JSONArray history  =sensorDataService.getSenorData(sensor_guid,account_id,start_time,end_time);

            System.out.println(history.toString());
            reslut_list.add(history);

        }


        //获取最小数据长度
        JSONArray t = (JSONArray) reslut_list.get(0);
        if (t==null){
            model.addAttribute("msg","there is no data in the region!");
            return "excelerror";
        }
        int min_siza = t.size();
        int min_index = 0;
        for (int i = 0; i <reslut_list.size() ; i++) {
            JSONArray temp = (JSONArray) reslut_list.get(i);
            if (min_siza>temp.size()){
                min_siza = temp.size();
                min_index=i;
            }
        }

        //数据矩阵转置
        List<List<JSONObject>> data_temp_result = new LinkedList<List<JSONObject>>();

        for (int i = 0; i <min_siza ; i++) {
            List<JSONObject> meta = new ArrayList<JSONObject>();
            for (int j = 0; j <reslut_list.size() ; j++) {
                JSONArray J = (JSONArray) reslut_list.get(j);
                meta.add((JSONObject) J.get(i));
            }
            data_temp_result.add(meta);
        }

        //整理后
        JSONArray result = new JSONArray();

        for (int i = 0; i <data_temp_result.size() ; i++) {
            List<JSONObject> Meta =data_temp_result.get(i);

            float air_Temperature =0;
            float air_Humidity =0;
            float substrate_Temperature =0;
            float substrate_Humidity = 0;
            float substrate_PH=0;
            float carbon_Dioxide=0;
            float illuminance=0;
            float substrate_Conductivity=0;
            float ppfd=0;
            float liquid_PH=0;
            float lai=0;
            float liquid_Conductivity=0;
            float liquid_DOC=0;
            float substrate_DOC=0;
            float reserve01=0;
            float reserve02=0;

            for (int j = 0; j <Meta.size() ; j++) {
                JSONObject jj= Meta.get(j);
                air_Temperature+=Float.parseFloat((String) jj.get("air_Temperature"));
                air_Humidity+=Float.parseFloat((String) jj.get("air_Humidity"));
                substrate_Temperature+=Float.parseFloat((String) jj.get("substrate_Temperature"));
                substrate_Humidity+=Float.parseFloat((String) jj.get("substrate_Humidity"));
                substrate_PH+=Float.parseFloat((String) jj.get("substrate_PH"));
                carbon_Dioxide+=Float.parseFloat((String) jj.get("carbon_Dioxide"));
                illuminance+=Float.parseFloat((String) jj.get("illuminance"));
                substrate_Conductivity+=Float.parseFloat((String) jj.get("substrate_Conductivity"));
                ppfd+=Float.parseFloat((String) jj.get("ppfd"));
                liquid_PH+=Float.parseFloat((String) jj.get("liquid_PH"));

                try{
                    lai+=Float.parseFloat((String) jj.get("lai"));
                }catch (Exception e){
                    lai+=0;
                }

                try {
                    liquid_Conductivity+=Float.parseFloat((String) jj.get("liquid_Conductivity"));
                }catch (Exception e){
                    liquid_Conductivity+=0;
                }

                try{
                    liquid_DOC+=Float.parseFloat((String) jj.get("liquid_DOC"));
                }catch (Exception e){
                    liquid_DOC+=0;
                }

                try{
                    substrate_DOC+=Float.parseFloat((String) jj.get("substrate_DOC"));
                }catch (Exception e){
                    substrate_DOC+=0;
                }
                try{
                    reserve01+=Float.parseFloat((String) jj.get("reserve01"));
                }catch (Exception e){
                    reserve01+=0;
                }
                try{
                    reserve02+=Float.parseFloat((String) jj.get("reserve02"));
                }catch (Exception e){
                    reserve02+=0;
                }


                if (j==Meta.size()-1){
                    air_Temperature=air_Temperature/Meta.size();
                    air_Humidity=air_Humidity/Meta.size();
                    substrate_Temperature=substrate_Temperature/Meta.size();
                    substrate_Humidity=substrate_Humidity/Meta.size();
                    substrate_PH=substrate_PH/Meta.size();
                    carbon_Dioxide=carbon_Dioxide/Meta.size();
                    illuminance=illuminance/Meta.size();
                    substrate_Conductivity=substrate_Conductivity/Meta.size();
                    ppfd=ppfd/Meta.size();
                    liquid_PH=liquid_PH/Meta.size();
                    lai=lai/Meta.size();
                    liquid_Conductivity=liquid_Conductivity/Meta.size();
                    liquid_DOC=liquid_DOC/Meta.size();
                    substrate_DOC=substrate_DOC/Meta.size();
                    reserve01=reserve01/Meta.size();
                    reserve02=reserve02/Meta.size();



                    JSONObject JJJ = new JSONObject();
                    JJJ.put("air_Temperature",air_Temperature);
                    JJJ.put("air_Humidity",air_Humidity);
                    JJJ.put("substrate_Temperature",substrate_Temperature);
                    JJJ.put("substrate_Humidity",substrate_Humidity);
                    JJJ.put("substrate_PH",substrate_PH);
                    JJJ.put("carbon_Dioxide",carbon_Dioxide);
                    JJJ.put("illuminance",illuminance);
                    JJJ.put("substrate_Conductivity",substrate_Conductivity);
                    JJJ.put("ppfd",ppfd);
                    JJJ.put("liquid_PH",liquid_PH);
                    JJJ.put("lai",lai);
                    JJJ.put("liquid_Conductivity",liquid_Conductivity);
                    JJJ.put("liquid_DOC",liquid_DOC);
                    JJJ.put("substrate_DOC",substrate_DOC);
                    JJJ.put("reserve01",reserve01);
                    JJJ.put("reserve02",reserve02);


                    JSONObject tempMin= Meta.get(min_index);

                    JJJ.put("record_time",tempMin.get("record_time"));
                    result.add(JJJ);

                }
            }
        }

        System.out.println(result.toString());


        //设置excel表头
        String[] tableHeaders = new String[]{
                "Time",
                "air_Temperature(°C)",
                "air_Humidity(%)",
                "substrate_Temperature(°C)",
                "substrate_Humidity(%)",
                "substrate_PH(PH)",
                "carbon_Dioxide(ppm)",
                "illuminance(lux)",
                "substrate_Conductivity(mS/cm)",
                "ppfd(μmol/s/m²)",
                "liquid_PH(PH)",
                "lai()",
                "liquid_Conductivity()",
                "liquid_DOC()",
                "substrate_DOC()",
                "reserve01()",
                "reserve02()"
        };
        //数据集合
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        //数据key值，用于工具类内部获取
        String[] dataNames = new String[]{
                "Time",
                "air_Temperature(°C)",
                "air_Humidity(%)",
                "substrate_Temperature(°C)",
                "substrate_Humidity(%)",
                "substrate_PH(PH)",
                "carbon_Dioxide(ppm)",
                "illuminance(lux)",
                "substrate_Conductivity(mS/cm)",
                "ppfd(μmol/s/m²)",
                "liquid_PH(PH)",
                "lai()",
                "liquid_Conductivity()",
                "liquid_DOC()",
                "substrate_DOC()",
                "reserve01()",
                "reserve02()"
        };

        try {

            for (int i = 0; i <result.size() ; i++) {
                JSONObject object = (JSONObject) result.get(i);

                Map<String, String> data = new HashMap<String, String>();
                data.put("Time",object.getString("record_time"));
                data.put("air_Temperature(°C)",object.getString("air_Temperature"));
                data.put("air_Humidity(%)",object.getString("air_Humidity"));
                data.put("substrate_Temperature(°C)",object.getString("substrate_Temperature"));
                data.put("substrate_Humidity(%)",object.getString("substrate_Humidity"));
                data.put("substrate_PH(PH)",object.getString("substrate_PH"));
                data.put("carbon_Dioxide(ppm)",object.getString("carbon_Dioxide"));
                data.put("illuminance(lux)",object.getString("illuminance"));
                data.put("substrate_Conductivity(mS/cm)",object.getString("substrate_Conductivity"));
                data.put("ppfd(μmol/s/m²)",object.getString("ppfd"));
                data.put("liquid_PH(PH)",object.getString("liquid_PH"));
                data.put("lai()",object.getString("lai"));
                data.put("liquid_Conductivity()",object.getString("liquid_Conductivity"));
                data.put("liquid_DOC()",object.getString("liquid_DOC"));
                data.put("substrate_DOC()",object.getString("substrate_DOC"));



                list.add(data);
            }

            response.setHeader("content-disposition", "attachment;filename=" + URLEncoder.encode("region_environment.xls", "UTF-8"));

            //创建输出流
            OutputStream out = response.getOutputStream();

            CreateSimpleExcelToDisk.createExcel(tableHeaders, dataNames, list, out);

            //关闭输出流
            out.close();
        }catch (UnsupportedEncodingException e) {
            model.addAttribute("msg", "system error");
            return "excelerror";
        } catch (IOException e) {
            model.addAttribute("msg", "system error");
            return "excelerror";
        } catch (Exception e) {
            model.addAttribute("msg", "system error");
            return "excelerror";
        }

        return "excelnodata";
    }

}
