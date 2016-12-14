package com.iot.socketUtil;

import com.alibaba.fastjson.JSONObject;

import java.io.*;
import java.net.Socket;

/**
 * Created by Administrator on 2016/12/14.
 */
public class test {
    public static final String GATEWAY="gateway";

    public static void main(String[] args) {
        getDb("192.168.0.164",1214,"","C:\\Users\\Administrator\\Desktop","test.zip");
    }
    public static void getDb(String host,int port,String gatewayId,String savePath,String fileName){
        Socket socket = null;
        BufferedOutputStream bufferedOutputStream=null;
        InputStream inputStream = null;
        FileOutputStream fos = null;
        try {
            socket = new Socket(host,port);
            JSONObject jsonObject =  new JSONObject();
            jsonObject.put(GATEWAY,gatewayId);
            byte[] result = jsonObject.toString().getBytes();
            bufferedOutputStream = new BufferedOutputStream(socket.getOutputStream());
            bufferedOutputStream.write(result);
            bufferedOutputStream.flush();

            String absoluteName = savePath+File.separator+fileName;
            File test = new File(absoluteName);
            inputStream =socket.getInputStream();
            ByteArrayOutputStream data = getFile(inputStream);
            fos = new FileOutputStream(test);
            byte[] datas  = data.toByteArray();
            fos.write(datas);
            fos.flush();
            fos.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static ByteArrayOutputStream getFile(InputStream inputStream) throws IOException {

        ByteArrayOutputStream byteArrayInputStream = new ByteArrayOutputStream();
        BufferedInputStream bis = new BufferedInputStream(inputStream);
        byte[] data_len = new byte[4];
        byte[] data_type = new byte[4];
        byte[] data = new byte[1024*1024];
        bis.read(data_len);
        bis.read(data_type);
        int length = ToByte.byteArrayToInt(data_len);
        int contentlen = length - 8;
        int readlength = 1024*50;
        int hasRead = 0;
        while (true) {
            if (hasRead != contentlen) {
                if (contentlen - hasRead < readlength) {
                    readlength = contentlen - hasRead;
                    data=new byte[readlength];
                }
                int num = bis.read(data, 0, readlength);
                if (data.length>num){
                    byte [] temp = new byte[num];
                    System.arraycopy(data,0,temp,0,num);
                    byteArrayInputStream.write(temp);
                }else {
                    byteArrayInputStream.write(data);
                }
                hasRead = hasRead + num;
                readlength = 1024 * 50;
                data=new byte[1024*100];
            }else {
                break;
            }
        }
        return byteArrayInputStream;

    }
}
