package com.iot.socketUtil;


public class ToByte {

    /**
     * 大端int转小段byte[]   (可用)
     */
    public static byte[] intToMinByteArray(int i) {
        byte[] result = new byte[4];
        //由高位到低位
        result[3] = (byte) ((i >> 24) & 0xFF);
        result[2] = (byte) ((i >> 16) & 0xFF);
        result[1] = (byte) ((i >> 8) & 0xFF);
        result[0] = (byte) (i & 0xFF);
        return result;
    }


    /**
     * byte[]转int(大端模式)
     *
     * @param bytes
     * @return
     */
    public static int byteArrayToInt(byte[] bytes) {
        int value = 0;
        //由高位到低位
        for (int i = 0; i < 4; i++) {

            int shift = i * 8;

            value += (bytes[i] & 0x000000FF) << shift;//往低位游

        }
        return value;
    }

}