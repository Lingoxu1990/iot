package com.iot.sqlControlUtil;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.iot.dbUtil.PropsUtil;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

/**
 * Created by xulingo on 16/4/4.
 */
public class SqlControlUtil {


    public static String[] putObjects(String tableName, JSONArray jsonArray) {

        String coloumns = "";

        PropsUtil configProps = new PropsUtil("config.properties");

        try {
            coloumns = configProps.get(tableName);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String[] coloumnsArr = coloumns.split(",");
        String[] results = new String[jsonArray.size()];


        for (int i = 0; i < jsonArray.size(); i++) {

            StringBuilder sb = new StringBuilder();
            sb.append("UPDATE " + tableName + " SET ");
            String pk = "";

            JSONObject jsonobject = (JSONObject) jsonArray.get(i);

            Set<String> keyset = jsonobject.keySet();

            for (String key : keyset) {
                if (coloumnsArr[0].equals(key)) {
                    pk = (String) jsonobject.get(key);
                } else {
                    sb.append(key + "='" + (String) jsonobject.get(key) + "',");
                }
            }
            sb.deleteCharAt(sb.lastIndexOf(","));

            sb.append(" WHERE " + coloumnsArr[0] + "='" + pk + "'");

            results[i] = sb.toString();

        }

        return results;
    }

    public static String[] controlObjects(String tableName, JSONArray jsonArray) {
        String coloumns = "";

        PropsUtil configProps = new PropsUtil("config.properties");

        try {
            coloumns = configProps.get(tableName);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String[] coloumnsArr = coloumns.split(",");
        String[] results = new String[jsonArray.size()];


        for (int i = 0; i < jsonArray.size(); i++) {

            StringBuilder sb = new StringBuilder();
            sb.append("UPDATE " + tableName + " SET ");
            String pk = "";
            String addr_key = "";
            String addr_val = "";

            JSONObject jsonobject = (JSONObject) jsonArray.get(i);

            Set<String> keyset = jsonobject.keySet();

            for (int j = 0; j < coloumnsArr.length; j++) {
                for (String key : keyset) {
                    if (key.equals(coloumnsArr[j])) {
                        if (key.contains("addr")) {
                            addr_key = key;
                            addr_val = (String) jsonobject.get(key);
                        } else if (coloumnsArr[0].equals(key)) {
                            pk = (String) jsonobject.get(key);
                        } else {
                            sb.append(key + "='" + (String) jsonobject.get(key) + "',");
                        }
                    }

                }
            }
            sb.deleteCharAt(sb.lastIndexOf(","));

            sb.append(" WHERE " + addr_key + "='" + addr_val + "'");

            results[i] = sb.toString();

        }

        return results;
    }

    public static String[] addObjects(String tableName, JSONArray jsonArray) {
        String coloumns = "";

        PropsUtil configProps = new PropsUtil("config.properties");

        try {
            coloumns = configProps.get(tableName);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String[] coloumnsArr = coloumns.split(",");
        String[] results = new String[jsonArray.size()];

        for (int i = 0; i < jsonArray.size(); i++) {

            JSONObject jsonobject = (JSONObject) jsonArray.get(i);
            String guid = (String) jsonobject.get(coloumnsArr[0]);

            StringBuilder title = new StringBuilder();
            title.append("INSERT INTO " + tableName + " ( " + coloumnsArr[0] + " ,");

            StringBuilder body = new StringBuilder();
//            String [] guidtemp = UUID.randomUUID().toString().split("-");
//
//            StringBuilder guidbuilder = new StringBuilder();
//            for (int j = 0; j <guidtemp.length ; j++) {
//                guidbuilder.append(guidtemp[j]);
//            }

//            body.append("VALUES ( '"+ guidbuilder.toString()+"' ,");
            body.append("VALUES ( '" + guid + "' ,");


            for (int j = 1; j < coloumnsArr.length; j++) {
                title.append(coloumnsArr[j] + " ,");
                body.append("'" + (String) jsonobject.get(coloumnsArr[j]) + "' ,");

            }


//            jsonobject.remove(coloumnsArr[0]);
//
//            Set<String > keyset = jsonobject.keySet();
//
//            for (String key: keyset) {
//                title.append(key+" ,");
//                body.append("'"+(String)jsonobject.get(key)+"' ,");
//            }

            title.deleteCharAt(title.lastIndexOf(","));
            title.append(")");
            body.deleteCharAt(body.lastIndexOf(","));
            body.append(")");
            results[i] = title.toString() + body.toString();
        }


        return results;
    }

    public static String[] deleteObjects(String tableName, JSONArray jsonArray) {
        String coloumns = "";

        PropsUtil configProps = new PropsUtil("config.properties");

        try {
            coloumns = configProps.get(tableName);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String[] coloumnsArr = coloumns.split(",");
        String[] results = new String[jsonArray.size()];

        for (int i = 0; i < jsonArray.size(); i++) {

            StringBuilder sb = new StringBuilder();
            sb.append("DELETE FROM " + tableName + " WHERE ");

            JSONObject jsonobject = (JSONObject) jsonArray.get(i);

            Set<String> keyset = jsonobject.keySet();


            for (String key : keyset) {
                sb.append(key + "='" + (String) jsonobject.get(key) + "' AND ");
            }
            sb.delete(sb.lastIndexOf("A"), sb.lastIndexOf("D"));
            sb.deleteCharAt(sb.lastIndexOf("D"));

            results[i] = sb.toString();

        }

        return results;
    }

    public static String selectObjects(String tableName, JSONObject jsonobject) {
        String result = "";
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM " + tableName + " WHERE ");

        Set<String> keyset = jsonobject.keySet();

        for (String key : keyset) {
            sb.append(key + "='" + (String) jsonobject.get(key) + "' AND ");
        }
        sb.delete(sb.lastIndexOf("A"), sb.lastIndexOf("D"));
        sb.deleteCharAt(sb.lastIndexOf("D"));
        result = sb.toString();
        return result;

    }

    public static String[] controlObject(String tableName, JSONArray jsonArray) {

        String coloumns = "";

        PropsUtil configProps = new PropsUtil("config.properties");

        try {
            coloumns = configProps.get(tableName);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String[] coloumnsArr = coloumns.split(",");
        String[] results = new String[jsonArray.size()];


        for (int i = 0; i < jsonArray.size(); i++) {

            StringBuilder sb = new StringBuilder();
            sb.append("UPDATE " + tableName + " SET ");
            String addr = "";
            String addrv = "";
            JSONObject jsonobject = (JSONObject) jsonArray.get(i);

            for (int j = 0; j < coloumnsArr.length; j++) {
                Set<String> keyset = jsonobject.keySet();

                for (String key : keyset) {

                    if (key.equals(coloumnsArr[j])) {
                        if (key.contains("addr")) {
                            addr = key;
                            addrv = (String) jsonobject.get(key);
                        } else {
                            sb.append(key + "='" + (String) jsonobject.get(key) + "',");
                        }
                    }
                }

            }

            sb.deleteCharAt(sb.lastIndexOf(","));

            sb.append(" WHERE " + addr + "='" + addrv + "'");

            results[i] = sb.toString();

        }

        return results;

    }

    public static String getPk(String[] sqls) {

        StringBuilder sb = new StringBuilder();
        sb.append(sqls[0]);
        sb.delete(sb.indexOf("("), sb.indexOf(")") + 1);
        sb.delete(0, sb.lastIndexOf("("));
        sb.deleteCharAt(sb.lastIndexOf("("));
        sb.deleteCharAt(sb.lastIndexOf(")"));
        String PK = sb.toString().split(",")[0];

        return PK.substring(1, PK.length() - 1);
    }


    public static void main(String[] args) {

        String SS = "{\n" +
                "     \"region_guid\":\"b6d5131d-67e0-453e-80f4-d1aed510df7c\",\n" +
                "     \"table_group_guid\":\"132456789\"\n" +
                "}";
        JSONObject jsonObject = JSON.parseObject(SS);

        JSONArray jsonArray = new JSONArray();
        jsonArray.add(jsonObject);

        deleteObjects("table_region_group", jsonArray);


    }

}
