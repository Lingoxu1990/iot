package com.iot.socketUtil;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.iot.dbUtil.PropsUtil;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.List;

/**
 * Created by xulingo on 16/3/31.
 */
public class OutPutSocketMessage {


    private String DestinationID;
    private String Type;
    private String Message;
    private String SourceID;
    private String sql;
    private int packegType;
    private String Status;
    private List<Gson> List;
    private String Command;
    private String PackageNumber;
    public static Object LO;



    public OutPutSocketMessage() {

    }

    public byte[] login(){

        String ii= "{\"Command\":\"login\",\"DestinationID\":\"0000000000000005\",\"List\":[{\"BackupIPAddr\":\"NULL\"},{\"RelayIPAddr\":\"NULL\"},{\"RelayTCPPort\":\"NULL\"},{\"BackupTCPPort\":\"NULL\"}],\"Message\":\"*:jennet\",\"PackageNumber\":\"fffd:WhiteListIDTail\",\"SourceID\":\"1234567802345677\",\"Status\":1,\"Type\":\"  \"}";
        JSONObject jsonObject =new JSONObject();
        int packegNumber = (int)(Math.random()*10000000);
        jsonObject.put("PackageNumber","fffd:WhiteListIDTail");
        jsonObject.put("DestinationID", "0000000000000005");
        jsonObject.put("Type","  ");
        jsonObject.put("Message","*:jennet");
        jsonObject.put("Status",1);
        jsonObject.put("SourceID","1234567802345677");
        JSONArray jsonArray = new JSONArray();

        JSONObject backaddr = new JSONObject();
        backaddr.put("BackupIPAddr","NULL");

        jsonArray.add(backaddr);

        JSONObject relayIP = new JSONObject();
        relayIP.put("RelayIPAddr","NULL");

        jsonArray.add(relayIP);

        JSONObject ReLayTcp = new JSONObject();
        ReLayTcp.put("RelayTCPPort","NULL");

        jsonArray.add(ReLayTcp);

        JSONObject BackupTcp = new JSONObject();
        BackupTcp.put("BackupTCPPort","NULL");

        jsonArray.add(BackupTcp);

        jsonObject.put("List",jsonArray);
        jsonObject.put("Command","login");
        System.out.println(jsonObject);
        int jsonlen  = jsonObject.toString().getBytes().length;
        int packegLen = jsonlen+8;

        byte [] lenbyte =ToByte.intToMinByteArray(packegLen);
        byte [] typeByte = ToByte.intToMinByteArray(packegType);
        byte [] jsonstr = jsonObject.toString().getBytes();
        byte[] result= new byte[packegLen];

        for (int j = 0; j < packegLen; j++) {
            if (j < lenbyte.length) {
                result[j] = lenbyte[j];
            } else if (j >= lenbyte.length && j < lenbyte.length + typeByte.length) {
                result[j] = typeByte[j - lenbyte.length];
            } else {
                result[j] = jsonstr[j - lenbyte.length - typeByte.length];
            }
        }

        return result;
    }

    public  JSONObject logintest() throws IOException {
        JSONObject jsonObject= new JSONObject();

        byte[] message = login();
        Socket socket = SocketPool.getSoket();

        BufferedOutputStream bufferedOutputStream=null;
        BufferedInputStream bufferedInputStream=null;
        try {
            bufferedOutputStream = new BufferedOutputStream(socket.getOutputStream());
            bufferedOutputStream.write(message);
            bufferedOutputStream.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (socket!=null){
                SocketPool.release(socket);
            }
        }
        bufferedInputStream = new BufferedInputStream(socket.getInputStream());
        jsonObject = InPutSocketMessage.getJson(bufferedInputStream);

        return jsonObject;
    }

    public JSONObject sendloginMessag(){

        JSONObject jsonObject= new JSONObject();

        byte[] message = login();
        Socket socket = SocketPool.getSoket();

        BufferedOutputStream bufferedOutputStream=null;
        BufferedInputStream bufferedInputStream=null;
        try {
            bufferedOutputStream = new BufferedOutputStream(socket.getOutputStream());
            bufferedOutputStream.write(message);
            bufferedOutputStream.flush();

            bufferedInputStream = new BufferedInputStream(socket.getInputStream());

            jsonObject = InPutSocketMessage.getJson(bufferedInputStream);

            System.out.println(jsonObject.toString());

            JSONObject  result = sendMessage(socket);
            System.out.println(result.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            SocketPool.release(socket);
        }

        return jsonObject;

    }

    public  byte[] getbytes(){


        JSONObject jsonObject =new JSONObject();
        int packegNumber = (int)(Math.random()*10000000);
        jsonObject.put("PackageNumber",String.valueOf(packegNumber));
        jsonObject.put("DestinationID", DestinationID);
        jsonObject.put("Type",Type);
        jsonObject.put("Message",Message);
        jsonObject.put("Status","1");
        jsonObject.put("SourceID",SourceID);
        JSONArray jsonArray = new JSONArray();
        jsonObject.put("List",jsonArray);
        jsonObject.put("Command",sql);
        System.out.println(jsonObject.toString());

//        NewOutpuSocketMessage newOutpuSocketMessage = new NewOutpuSocketMessage();
//
//        newOutpuSocketMessage.setPackageNumber(String.valueOf(packegNumber));
//        newOutpuSocketMessage.setDestinationID(DestinationID);
//        newOutpuSocketMessage.setType(Type);
//        newOutpuSocketMessage.setMessage(Message);
//        newOutpuSocketMessage.setStatus("1");
//        newOutpuSocketMessage.setSourceID(SourceID);
//        newOutpuSocketMessage.setList(new LinkedList<Gson>());
//        newOutpuSocketMessage.setCommand(sql);

//        int jsonlen  = new Gson().toJson(newOutpuSocketMessage).getBytes().length;


        int jsonlen = jsonObject.toString().getBytes().length;
        int packegLen = jsonlen+8;

        byte [] lenbyte =ToByte.intToMinByteArray(packegLen);
        byte [] typeByte = ToByte.intToMinByteArray(packegType);

//        byte [] jsonstr = new Gson().toJson(newOutpuSocketMessage).getBytes();

        byte [] jsonstr = jsonObject.toString().getBytes();

        byte[] result= new byte[packegLen];

        for (int j = 0; j < packegLen; j++) {
                if (j < lenbyte.length) {
                    result[j] = lenbyte[j];
                } else if (j >= lenbyte.length && j < lenbyte.length + typeByte.length) {
                    result[j] = typeByte[j - lenbyte.length];
                } else {
                    result[j] = jsonstr[j - lenbyte.length - typeByte.length];
                }
            }

        return result;
    }

    public JSONObject test(){
        JSONObject jsonObject =new JSONObject();
        int packegNumber = (int)(Math.random()*10000000);
        jsonObject.put("PackageNumber",String.valueOf(packegNumber));
        jsonObject.put("DestinationID", DestinationID);
        jsonObject.put("Type",Type);
        jsonObject.put("Message",Message);
        jsonObject.put("Status","1");
        jsonObject.put("SourceID",SourceID);
        JSONArray jsonArray = new JSONArray();
        jsonObject.put("List",jsonArray);
        jsonObject.put("Command",sql);

        return jsonObject;
    }

    public JSONObject sendMessage(Socket socket){
        JSONObject jsonObject= new JSONObject();

        String delaultMessage = "{ \"PackageNumber\": \"01\", \"Message\": \"login successful\", \"SourceID\": \"00000001007e1467\", \"DestinationID\": \"0000000000000005\", \"Type\": \"  \", \"List\": [ { \"RelayIPAddr\": \"NULL\" }, { \"RelayTCPPort\": \"NULL\" }, { \"BackupIPAddr\": \"NULL\" }, { \"BackupTCPPort\": \"NULL\" } ], \"Status\": 0, \"Command\": \"  \" }";
        JSONObject defaultJson = (JSONObject) JSONObject.parse(delaultMessage);
        if (this.getSourceID().contains("00000000")){
            return defaultJson;
        }

        byte[] message = getbytes();

        BufferedOutputStream bufferedOutputStream=null;
        BufferedInputStream bufferedInputStream=null;
        try {
            bufferedOutputStream = new BufferedOutputStream(socket.getOutputStream());
            bufferedOutputStream.write(message);
            bufferedOutputStream.flush();

            bufferedInputStream = new BufferedInputStream(socket.getInputStream());

            jsonObject = InPutSocketMessage.getJson(bufferedInputStream);

            System.out.println(jsonObject.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            SocketPool.release(socket);
        }

        return jsonObject;
    }

    public  JSONObject sendMessag (String sourceID)  {


        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(sourceID);
        String accountId = stringBuilder.substring(0,8).toString();

        if ("00000000".equals(accountId)){
            JSONObject test= new JSONObject();
            test.put("Status",0);
            return test;
        }

        JSONObject jsonObject = new JSONObject();


        byte[] message = getbytes();
        String number="";
        String gatewayIP="";
        String gatewayPort="";

        PropsUtil configProps = new PropsUtil("config.properties");

        try {
            number = configProps.get("socketpoll");
            gatewayIP= configProps.get("gatewayIP");
            gatewayPort = configProps.get("gatewayPort");
        } catch (IOException e) {

            e.printStackTrace();
        }

            Socket socket = null;
            try {
                socket = new Socket(gatewayIP,Integer.parseInt(gatewayPort));
                socket.setReceiveBufferSize(100000);
                socket.setSoTimeout(6000);
            } catch (IOException e) {
                    e.printStackTrace();
            }


//        socket =SocketPool.getSoket();


        JSONObject loginPackage=new JSONObject();

        loginPackage.put("PackageNumber","fffd:WhiteListIDTail");
        loginPackage.put("DestinationID", "0000000000000005");
        loginPackage.put("Type","  ");
        loginPackage.put("Message","*:jennet");
        loginPackage.put("Status",1);
        loginPackage.put("SourceID",sourceID);
        loginPackage.put("List",new JSONArray());
        loginPackage.put("Command","login");


        int jsonlen  = loginPackage.toString().getBytes().length;
        int packegLen = jsonlen+8;

        byte [] lenbyte =ToByte.intToMinByteArray(packegLen);
        byte [] typeByte = ToByte.intToMinByteArray(2);
        byte [] jsonstr = loginPackage.toString().getBytes();
        byte[] login= new byte[packegLen];

        for (int j = 0; j < packegLen; j++) {
            if (j < lenbyte.length) {
                login[j] = lenbyte[j];
            } else if (j >= lenbyte.length && j < lenbyte.length + typeByte.length) {
                login[j] = typeByte[j - lenbyte.length];
            } else {
                login[j] = jsonstr[j - lenbyte.length - typeByte.length];
            }
        }


        BufferedOutputStream bufferedOutputStream=null;
        InputStream inputStream=null;
        try {

            synchronized (OutPutSocketMessage.class){
            bufferedOutputStream = new BufferedOutputStream(socket.getOutputStream());
            bufferedOutputStream.write(login);
            bufferedOutputStream.flush();

            inputStream = socket.getInputStream();

            InPutSocketMessage.getJson(inputStream);

            bufferedOutputStream.write(message);
            bufferedOutputStream.flush();

            jsonObject=InPutSocketMessage.getJson(inputStream);

            }

        } catch (IOException e) {

//            if (e instanceof SocketTimeoutException){
//                throw  e
//            }
            e.printStackTrace();

        }finally {

            if (socket!=null){
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


        }

        return jsonObject;

    }




    public JSONObject testSendMessage(String sourceID) throws IOException {

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(sourceID);
        String accountId = stringBuilder.substring(0,8).toString();

        if ("00000000".equals(accountId)){
            JSONObject test= new JSONObject();
            test.put("Status",0);
            return test;
        }

        JSONObject jsonObject = new JSONObject();


        byte[] message = getbytes();
        String number="";
        String gatewayIP="";
        String gatewayPort="";

        PropsUtil configProps = new PropsUtil("config.properties");

        try {
            number = configProps.get("socketpoll");
            gatewayIP= configProps.get("gatewayIP");
            gatewayPort = configProps.get("gatewayPort");
        } catch (IOException e) {

            e.printStackTrace();
        }

        Socket socket = null;

        socket = new Socket(gatewayIP,Integer.parseInt(gatewayPort));
        socket.setReceiveBufferSize(100000);



        socket.setSoTimeout(3000);



        JSONObject loginPackage=new JSONObject();

        loginPackage.put("PackageNumber","fffd:WhiteListIDTail");
        loginPackage.put("DestinationID", "0000000000000005");
        loginPackage.put("Type","  ");
        loginPackage.put("Message","*:jennet");
        loginPackage.put("Status",1);
        loginPackage.put("SourceID",sourceID);
        loginPackage.put("List",new JSONArray());
        loginPackage.put("Command","login");


        int jsonlen  = loginPackage.toString().getBytes().length;
        int packegLen = jsonlen+8;

        byte [] lenbyte =ToByte.intToMinByteArray(packegLen);
        byte [] typeByte = ToByte.intToMinByteArray(2);
        byte [] jsonstr = loginPackage.toString().getBytes();
        byte[] login= new byte[packegLen];

        for (int j = 0; j < packegLen; j++) {
            if (j < lenbyte.length) {
                login[j] = lenbyte[j];
            } else if (j >= lenbyte.length && j < lenbyte.length + typeByte.length) {
                login[j] = typeByte[j - lenbyte.length];
            } else {
                login[j] = jsonstr[j - lenbyte.length - typeByte.length];
            }
        }


        BufferedOutputStream bufferedOutputStream=null;
        InputStream inputStream=null;
        try {

            synchronized (OutPutSocketMessage.class){
                bufferedOutputStream = new BufferedOutputStream(socket.getOutputStream());
                bufferedOutputStream.write(login);
                bufferedOutputStream.flush();

                inputStream = socket.getInputStream();

                InPutSocketMessage.getJson(inputStream);

                bufferedOutputStream.write(message);
                bufferedOutputStream.flush();

                jsonObject=InPutSocketMessage.getJson(inputStream);

            }

        } catch (IOException e) {
           throw e;
        }finally {

            if (socket!=null){
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


        }

        return jsonObject;
    }


    public String getDestinationID() {
        return DestinationID;
    }

    public void setDestinationID(String destinationID) {
        DestinationID = destinationID;
    }

    public String getType() {
        return Type;
    }

    public void setType(String type) {
        Type = type;
    }

    public String getMessage() {
        return Message;
    }

    public void setMessage(String message) {
        Message = message;
    }

    public String getSourceID() {
        return SourceID;
    }

    public void setSourceID(String sourceID) {
        SourceID = sourceID;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public int getPackegType() {
        return packegType;
    }

    public void setPackegType(int packegType) {
        this.packegType = packegType;
    }

    @Override
    public String toString() {
        return "OutPutSocketMessage{" +
                "DestinationID='" + DestinationID + '\'' +
                ", Type='" + Type + '\'' +
                ", Message='" + Message + '\'' +
                ", SourceID='" + SourceID + '\'' +
                ", sql='" + sql + '\'' +
                ", packegType=" + packegType +
                '}';
    }

    public static void main(String[] args) {
        OutPutSocketMessage O= new OutPutSocketMessage();

        JSONObject jsonObject = O.sendMessag("00000000007e1467");

        System.out.println(jsonObject.toString());
    }
}
