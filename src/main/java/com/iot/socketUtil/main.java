package com.iot.socketUtil;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.iot.dbUtil.PropsUtil;

import java.io.*;
import java.net.Socket;

/**
 * Created by xulingo on 16/3/31.
 */
public class main {

    public static JSONObject singoTest(String sql) {
        JSONObject re = null;
        String gatewayIP = "";
        String gatewayPort = "";

        PropsUtil configProps = new PropsUtil("config.properties");

        try {
            gatewayIP = configProps.get("gatewayIP");
            gatewayPort = configProps.get("gatewayPort");
        } catch (IOException e) {
            System.out.println("连接异常");
            e.printStackTrace();
        }

        Socket socket = null;
        try {
            socket = new Socket(gatewayIP, Integer.parseInt(gatewayPort));
//            socket.setReceiveBufferSize(100000);
        } catch (IOException e) {
            e.printStackTrace();
        }
        OutPutSocketMessage outPutSocketMessage = new OutPutSocketMessage();
        outPutSocketMessage.setPackegType(2);
        outPutSocketMessage.setDestinationID("000000011234asdf");
        outPutSocketMessage.setType("table_region_device");
        outPutSocketMessage.setMessage("applyRecipe");
        outPutSocketMessage.setSourceID("0000000112345677");
        outPutSocketMessage.setSql("xxxxxxxxx,d6e20855-d14d-40ff-a7f6-533caffc256b,000000011234asdf,3bdbc110-dd90-4812-b473-470f026b61bb");

        byte[] message = outPutSocketMessage.getbytes();


        JSONObject jsonObject = new JSONObject();

        jsonObject.put("PackageNumber", "fffd:WhiteListIDTail");
        jsonObject.put("DestinationID", "0000000000000005");
        jsonObject.put("Type", "  ");
        jsonObject.put("Message", "*:jennet");
        jsonObject.put("Status", 1);
        jsonObject.put("SourceID", "0000000112345677");
        jsonObject.put("List", new JSONArray());
        jsonObject.put("Command", "login");


        int jsonlen = jsonObject.toString().getBytes().length;
        int packegLen = jsonlen + 8;

        byte[] lenbyte = ToByte.intToMinByteArray(packegLen);
        byte[] typeByte = ToByte.intToMinByteArray(2);
        byte[] jsonstr = jsonObject.toString().getBytes();
        byte[] result = new byte[packegLen];

        for (int j = 0; j < packegLen; j++) {
            if (j < lenbyte.length) {
                result[j] = lenbyte[j];
            } else if (j >= lenbyte.length && j < lenbyte.length + typeByte.length) {
                result[j] = typeByte[j - lenbyte.length];
            } else {
                result[j] = jsonstr[j - lenbyte.length - typeByte.length];
            }
        }


        BufferedOutputStream bufferedOutputStream = null;
        InputStream inputStream = null;
        try {
            bufferedOutputStream = new BufferedOutputStream(socket.getOutputStream());
            bufferedOutputStream.write(result);
            bufferedOutputStream.flush();

            inputStream = socket.getInputStream();

            re = InPutSocketMessage.getJson(inputStream);

            bufferedOutputStream.write(message);
            bufferedOutputStream.flush();

            JSONObject a = InPutSocketMessage.getJson(inputStream);


        } catch (IOException e) {
            e.printStackTrace();
        }
        return re;

    }


    public static void main(String[] args) {
//
//        JSONObject j= singoTest("insert into table_region_device (region_device_guid,region_guid,region_addr,region_name,table_device_guid,gateway_id,device_addr,device_name,channel_class,channel_guid,channel_name,channel_type,channel_bit_num) values('123456789',' baf57091fc9a47e4becd64f17903f52e','ff15::9037’,'123','1110D337583C4E18B306368E84AE9639','02158d00007e170a','sensor_addr','test_sensor_name','channel_class','channel_guid','channel_name','chanel_type','channel_bit_num');");
//        JSONObject j = singoTest("DELETE FROM table_region_device WHERE device_addr='fd04:bd3:80e8:3:215:8d00:35:e3a9' AND region_addr='ff15::90bc'");
        JSONObject j = singoTest("applyRecipe");

    }


}
