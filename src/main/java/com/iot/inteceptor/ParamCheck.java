package com.iot.inteceptor;

import com.alibaba.fastjson.JSONObject;

/**
 * Created by xulingo on 16/3/25.
 */
public class ParamCheck {

    public static JSONObject checkParam (String queryString){

        String [] queries =  queryString.split("&");

        JSONObject jsonobject  = new JSONObject();
        for (int i = 0; i <queries.length ; i++) {
            String []  kv = queries[i].split("=");
            jsonobject.put(kv[0],kv[1]);
        }
        return jsonobject;
    }


}
