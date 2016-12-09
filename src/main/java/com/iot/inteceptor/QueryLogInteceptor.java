package com.iot.inteceptor;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.InputStreamReader;

// Title：记录接口访问日志
//
// Description:
//
// Author:black
//
// Createtime:2016-07-29 13:46
//
// Version:1.0
//
// 修改历史:版本号 修改日期 修改人 修改说明
//
// 1.0 2016-07-29 13:46 black 创建文档

public class QueryLogInteceptor extends HandlerInterceptorAdapter {
    //日志文件
    private static Logger logger = Logger.getLogger(QueryLogInteceptor.class);

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String uri = request.getRequestURI();

        String method = request.getMethod();

        String logString = "请求地址："+uri;

        logString += "，请求方式：" + method;

        if ("GET".equals(method)) {
            String queryString = request.getQueryString();

            logString += "，data："+ queryString;
        }else{
            BufferedReader in = new BufferedReader(new InputStreamReader(request.getInputStream(), "utf-8"));
            String line = "";
            String empJson = "";
            while ((line = in.readLine()) != null) {
                empJson += line;
            }

            logString += "，data："+ empJson;
        }

        if (logger.isDebugEnabled()) {
            logger.debug(logString);
        }

        return true;
    }
}
