package com.iot.utils;

import com.alibaba.fastjson.JSONObject;
import org.apache.log4j.Logger;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Title：
 * Author:black
 * Createtime:2016-08-03 16:58
 */
public class ParamUtils {

    private static Logger logger = Logger.getLogger(ParamUtils.class);

    public static JSONObject getJsonObjectFromStream(ServletInputStream inputStream) throws IOException {

        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));

        String line = "";
        String empline = "";

        while ((line = in.readLine()) != null) {
            empline += line;
        }

        return (JSONObject) JSONObject.parse(empline);
    }

    public static JSONObject getJsonObjectFromRequest(HttpServletRequest httpServletRequest) throws IOException {

        String method = httpServletRequest.getMethod();

        if ("GET".equals(method)) {
            String query = httpServletRequest.getQueryString();
            String uri = httpServletRequest.getRequestURI();
            String[] tablenames = uri.split("/");
            String tablename = tablenames[tablenames.length - 1];

            String[] queries = query.split("&");

            JSONObject jsonObject = new JSONObject();

            for (int i = 0; i < queries.length; i++) {
                String[] kv = queries[i].split("=");
                if(!kv[0].equals("appid")){
                    jsonObject.put(kv[0], kv[1]);
                }

            }

            JSONObject result = new JSONObject();
            result.put(tablename, jsonObject);

            return result;
        } else {
            BufferedReader in = new BufferedReader(new InputStreamReader(httpServletRequest.getInputStream(), "utf-8"));

            String line = "";
            String empJson = "";

            while ((line = in.readLine()) != null) {
                empJson += line;
            }

            JSONObject result = (JSONObject) JSONObject.parse(empJson);

            return result;
        }
    }

    public static JSONObject getAttributess(HttpServletRequest request) {

        JSONObject param=(JSONObject) request.getAttribute("params");
        System.out.println("param"+param.toString());

        return param;
    }

}
