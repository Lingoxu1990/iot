package com.iot.socketUtil;

import com.alibaba.fastjson.JSONObject;

import java.io.*;
/**
 * Created by xulingo on 16/3/31.
 */
public class InPutSocketMessage {

    public InPutSocketMessage() {
    }


    public static JSONObject getJson(InputStream inputStream) throws IOException {

        BufferedInputStream bis = new BufferedInputStream(inputStream);
        byte[] data_len = new byte[4];
        byte[] data_type = new byte[4];
        byte[] data = new byte[1024 * 1024];
        bis.read(data_len);
        bis.read(data_type);
        int length = ToByte.byteArrayToInt(data_len);
        int contentlen = length - 8;
        int readlength = 1024*50;
        int hasRead = 0;
        StringBuilder info = new StringBuilder();
        while (true) {
            if (hasRead != contentlen) {
                if (contentlen - hasRead < readlength) {
                    readlength = contentlen - hasRead;
                }
                int num = bis.read(data, 0, readlength);
                hasRead = hasRead + num;
                info.append(new String(data, 0, num, "utf-8"));
                readlength = 1024 * 50;

            }else {
                break;
            }
        }
//        bis.close();

        String recv_msg = info.toString();
        System.out.println(recv_msg);

        return JSONObject.parseObject(recv_msg);

    }



}
