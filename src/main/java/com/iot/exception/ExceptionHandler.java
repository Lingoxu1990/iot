package com.iot.exception;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by xulingo on 16/7/11.
 */
public class ExceptionHandler implements HandlerExceptionResolver {

    private static Logger logger = Logger.getLogger(ExceptionHandler.class);


    public ModelAndView resolveException(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) {
        ModelAndView mav = new ModelAndView();


        //开始返回
        MappingJackson2JsonView view = new MappingJackson2JsonView();
        Map attributes = new HashMap();

//        // 根据不同错误返回不同信息
        if(e instanceof BussinessException) {
            logger.error("发生业务异常，信息如下：", e);

            attributes.put("code", ((BussinessException) e).getCode());
            attributes.put("message", e.getMessage());
        }else if(e instanceof ParameterException) {
            logger.error("发生参数异常，信息如下：", e);

            attributes.put("code", ((ParameterException) e).getCode());
            attributes.put("message", e.getMessage());
        }else if (e instanceof SocketTimeoutException ){
            // todo
            //System.out.println(123456);
            logger.error(httpServletRequest.getRequestURI() +" user "+" error : ",e);
            attributes.put("code", "-1");
            attributes.put("message", "The sub-gateway is busy , please try later");
        }else {
            logger.error("发生全局无捕获异常，信息如下：", e);
            attributes.put("code", "-1");
            attributes.put("message", "The system is busy, please try again later");
        }

        view.setAttributesMap(attributes);
        mav.setView(view);
        return mav;
    }



}
