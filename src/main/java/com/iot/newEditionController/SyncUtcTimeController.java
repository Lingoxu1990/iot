package com.iot.newEditionController;

import com.alibaba.fastjson.JSONObject;
import com.iot.message.Message;
import com.iot.message.MessageNoContent;
import com.iot.newEditionService.RecipeService;
import com.iot.newEditionService.SyncUycTimeService;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;

/**
 *
 * 该接口仅用于将传感器历史数据(非utc时间)转化为utc时间.
 *
 * Created by xulingo on 16/9/23.
 */

@Controller
public class SyncUtcTimeController {


    private static Logger logger = Logger.getLogger(RecipeController.class);


    @Resource
    private SyncUycTimeService syncUycTimeService;



    @RequestMapping(value = "/utc", method = RequestMethod.GET)
    @ResponseBody
    public Object updateRecordTime(HttpServletRequest httpServletRequest) throws Exception {

        String date = httpServletRequest.getParameter("date");


//        SimpleDateFormat simpleDateFormat =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//
//        simpleDateFormat.parse(date);


        syncUycTimeService.updateRecordTime(date);


        MessageNoContent messageNoContent = new MessageNoContent();
        messageNoContent.setCode("0");
        messageNoContent.setMessage("success");


        return messageNoContent;

    }

    @RequestMapping(value = "/timestamp", method = RequestMethod.GET)
    @ResponseBody
    public Object getLocalTime() throws Exception {


        JSONObject result = new JSONObject();
        result.put("contentEncrypt","");
        result.put("content",System.currentTimeMillis()/1000);
        result.put("message","请求成功");
        result.put("code",0);



        return result;

    }



}
