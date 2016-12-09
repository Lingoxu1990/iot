package com.iot.md5Util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by adminchen on 16/8/1.
 */
public class Md5Util {

    /***
     * MD5加码 生成32位md5码
     */


    public static String stringMd5(String str){
      String reStr = null;
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] bytes = md5.digest(str.getBytes());
            StringBuffer stringBuffer = new StringBuffer();
            for (byte b : bytes){
                int bt = b&0xff;
                if (bt < 16){
                    stringBuffer.append(0);
                    }
                stringBuffer.append(Integer.toHexString(bt));
                }
            reStr = stringBuffer.toString();
            } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            }
        return reStr;


    }

    public static void main(String[] args){
        System.out.println(stringMd5("aaaaaaaa"));
    }
}
