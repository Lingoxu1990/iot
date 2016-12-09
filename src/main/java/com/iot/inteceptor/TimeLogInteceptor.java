package com.iot.inteceptor;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Title：
 * Author:black
 * Createtime:2016-07-29 14:04
 */
public class TimeLogInteceptor extends HandlerInterceptorAdapter {
    //日志文件
    private static Logger logger = Logger.getLogger(TimeLogInteceptor.class);

    //before the actual handler will be executed
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response, Object handler)
            throws Exception {

        long startTime = System.currentTimeMillis();
        request.setAttribute("startTime", startTime);

        return true;
    }

    //after the handler is executed
    public void postHandle(
            HttpServletRequest request, HttpServletResponse response,
            Object handler, ModelAndView modelAndView)
            throws Exception {

        long startTime = (Long) request.getAttribute("startTime");

        long endTime = System.currentTimeMillis();

        long executeTime = endTime - startTime;

        //log it
        if (logger.isDebugEnabled()) {
            logger.debug("[" + handler + "] executeTime : " + executeTime + "ms");
        }
    }
}
