package com.iot.newEditionController;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.iot.message.Message;
import com.iot.message.MessageNoContent;
import com.iot.newEditionService.RealTimeSensorDataAccpetService;
import com.iot.newEditionService.RecipeService;
import com.iot.pojo.TableDevice;
import com.iot.pojo.TableSensorRecord;
import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;
import com.sun.xml.internal.ws.api.model.MEP;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * Created by xulingo on 16/8/11.
 */

@Controller

public class RealTimeSensorDataAccpetController {

    private static Logger logger = Logger.getLogger(RealTimeSensorDataAccpetController.class);

    @Resource
    private RealTimeSensorDataAccpetService realTimeSensorDataAccpetService;


    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    @ResponseBody
    public Object insertRecord(HttpServletRequest httpServletRequest) throws Exception {
        BufferedReader in = new BufferedReader(new InputStreamReader(httpServletRequest.getInputStream(), "utf-8"));
        String line = "";
        String empline = "";
        while ((line = in.readLine()) != null) {
            empline += line;
        }
        List<TableSensorRecord> tableSensorRecords =new LinkedList<TableSensorRecord>();

        JSONArray body = (JSONArray) JSONObject.parse(empline) ;


        for (int i = 0; i <body.size() ; i++) {
            JSONObject temp =(JSONObject) body.get(i);
            TableSensorRecord tempRecord = new TableSensorRecord();
            tempRecord.setId(temp.getString("id"));
            tempRecord.setRecord_guid(temp.getString("record_guid"));
            tempRecord.setTable_device_guid(temp.getString("table_device_guid"));
            tempRecord.setRecord_time(temp.getString("record_time"));
            tempRecord.setAir_Temperature(temp.getString("air_temperature"));
            tempRecord.setAir_Humidity(temp.getString("air_humidity"));
            tempRecord.setCarbon_Dioxide(temp.getString("carbon_dioxide"));
            tempRecord.setIlluminance(temp.getString("illuminance"));
            tempRecord.setPpfd(temp.getString("ppfd"));
            tempRecord.setLiquid_PH(temp.getString("liquid_ph"));
            tempRecord.setLiquid_Conductivity(temp.getString("liquid_conductivity"));
            tempRecord.setLiquid_DOC(temp.getString("liquid_doc"));
            tempRecord.setSubstrate_PH(temp.getString("substrate_ph"));
            tempRecord.setSubstrate_Conductivity(temp.getString("substrate_conductivity"));
            tempRecord.setSubstrate_DOC(temp.getString("substrate_doc"));
            tempRecord.setLai(temp.getString("lai"));
            tempRecord.setSubstrate_Temperature(temp.getString("substrate_temperature"));
            tempRecord.setSubstrate_Humidity(temp.getString("substrate_humidity"));
            tempRecord.setReserve01(temp.getString("reserve01"));
            tempRecord.setReserve02(temp.getString("reserve02"));
            tempRecord.setAccount_id(temp.getString("account_id"));

            tableSensorRecords.add(tempRecord);


        }
//        for (TableSensorRecord record: tableSensorRecords) {
//            System.out.println(record.toString());
//        }
        MessageNoContent messageNoContent = new MessageNoContent();

        if (tableSensorRecords==null || tableSensorRecords.size()==0){
            messageNoContent.setCode("-1");
            messageNoContent.setMessage("No contents in your request body");
            return messageNoContent;
        }
        try {
            int rows = realTimeSensorDataAccpetService.insertMany(tableSensorRecords);
            if (rows<tableSensorRecords.size()){
                messageNoContent.setCode("-1");
                messageNoContent.setMessage("Failed to insert the data");
                return messageNoContent;
            }

        }catch (Exception e){
            String exceMessage = e.getMessage();
            if (exceMessage.contains("Duplicate")&& exceMessage.contains("PRIMARY")){
                String [] words= exceMessage.split(" ");
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(exceMessage);
                int head =exceMessage.indexOf("'");
                stringBuilder.delete(0,head+1);
                int end = stringBuilder.indexOf("'");
                String pk = stringBuilder.substring(0,end);
                Message message = new Message();
                message.setCode("-1");
                message.setMessage("DuplicateKeyException");
                message.setContent(pk);
                return message;
            }else {
                throw e;
            }
        }

        messageNoContent.setCode("0");
        messageNoContent.setMessage("insert the data successfully!");

        return messageNoContent;
    }

    @RequestMapping(value = "/upload/basis",method = RequestMethod.POST)
    @ResponseBody
    public Object updateBasisData(HttpServletRequest httpServletRequest) throws Exception{
        Message message = new Message();
        BufferedReader in = new BufferedReader(new InputStreamReader(httpServletRequest.getInputStream(), "utf-8"));
        String line = "";
        String empline = "";
        while ((line = in.readLine()) != null) {
            empline += line;
        }
        List<TableDevice>  tableDeviceList = JSONArray.parseArray(empline, TableDevice.class);
        int num=0;
        try{
            num = realTimeSensorDataAccpetService.replaceMany(tableDeviceList);
        }catch (Exception e){
            message.setCode("-1");
            message.setMessage("replcae error");
            message.setContent(e.getMessage());
            return message;
        }
        if (num!=(tableDeviceList.size())){
            message.setCode("-1");
            message.setMessage("execute error");
            message.setContent("this operation is not executed as expected.");
            return message;
        }
        MessageNoContent messageNoContent = new MessageNoContent();
        messageNoContent.setCode("0");
        messageNoContent.setMessage("success");
        return messageNoContent;
    }
}
