package com.iot.inteceptor;

import com.alibaba.fastjson.JSONObject;
import com.iot.message.Message;
import com.iot.dbUtil.PropsUtil;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.security.NoSuchAlgorithmException;

/**
 * Created by xulingo on 16/3/25.
 */

public class MyInteceptor  implements HandlerInterceptor {

    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o)  {

        Message message = new Message();

        if (httpServletRequest.getRequestURI().contains("ftpfiles")){
            return true;
        }
        if (httpServletRequest.getRequestURI().contains("login")){
            return true;
        }

        String openID = httpServletRequest.getParameter("openID");
        String accessToken = httpServletRequest.getParameter("accessToken");

        if ((accessToken==null || "".equals(accessToken)) || (openID==null ||"".equals(openID))){
            message.setCode("-1");
            message.setMessage("缺少accessToken");
            message.setContent("");
            JSONObject jsonObject1 = (JSONObject) JSONObject.toJSON(message);
            try {
                httpServletResponse.getWriter().print(jsonObject1.toJSONString());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }

        String appID = null;

        PropsUtil configProps = new PropsUtil("config.properties");

        try {
            appID = configProps.get("appID");
        } catch (IOException e) {
            e.printStackTrace();
        }
        int tsTemp = (int) System.currentTimeMillis();
        int tst = tsTemp/1000;
        String ts = String.valueOf(tst);
        String token = null;
        try {
            token = configProps.get("token");
        } catch (IOException e) {
            e.printStackTrace();
        }

//        String signStr = appID+ts+token+"false";
//        String sign = null;
//        try {
//            sign = Md5Util.EncoderByMd5(signStr);
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//
//        String param = "appID="+appID+"&sign="+sign+"&ts="+ts;
//
//        String  httpadd = "http://AG服务器地址/v1/oauth?"+param;

        return true;
    }

    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {

    }

    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {

    }
}
