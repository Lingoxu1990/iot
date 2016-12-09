package com.iot.sqlControlUtil;

/**
 * Created by xulingo on 16/4/13.
 */
public class String2Num {



    public static int toNum(String numStr){



        return  Integer.parseInt(numStr,16);

    }


    public static void main(String[] args) {

        int a = toNum("aa")+1;


        System.out.println(Integer.toHexString(a));


    }
}
