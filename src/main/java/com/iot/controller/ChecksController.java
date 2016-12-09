package com.iot.controller;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.StringCodec;
import com.iot.exception.BussinessException;
import com.iot.message.Message;
import com.iot.pojo.TableChecks;
import com.iot.service.ChecksService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


/**
 * Created by adminchen on 16/7/20.
 */
@Controller
public class ChecksController {

    @Resource
    private ChecksService checksService;

    @RequestMapping(value = "/checks",method = RequestMethod.PUT)
    @ResponseBody
    public Message checks(HttpServletRequest httpServletRequest){
        Message message=new Message();
        BufferedReader in = null;
        JSONObject result=null;
        try {
            in = new BufferedReader(new InputStreamReader(httpServletRequest.getInputStream(), "utf-8"));
            String line = "";
            String empJson = "";
            while ((line = in.readLine()) != null) {
                empJson += line;
            }
            result = (JSONObject) JSONObject.parse(empJson);
        } catch (Exception e) {
            e.printStackTrace();

        }finally {
            if (in!=null){
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        //0是审核通过 -1未通过
        String check_in=(String) result.get("checks");
        if (check_in==null||check_in.equals("")){
//            message.setCode("-1");
//            message.setMessage("checks does not exist");
//            return message;
            throw new BussinessException("-1","checks does not exist");
        }
        int n=checksService.updateChecks(check_in);
        if (n<1){
//            message.setCode("-1");
//            message.setMessage("checks update failed");
//            return message;
            throw new BussinessException("-1","checks update failed");
        }
        message.setCode("0");
        message.setMessage("checks update success");
        return message;
    }

    @RequestMapping(value = "/checks",method = RequestMethod.GET)
    @ResponseBody
    public Message findChecks(HttpServletRequest httpServletRequest){
        BufferedReader in = null;
        JSONObject result=null;
        try {
            in = new BufferedReader(new InputStreamReader(httpServletRequest.getInputStream(), "utf-8"));
            String line = "";
            String empJson = "";
            while ((line = in.readLine()) != null) {
                empJson += line;
            }
            result = (JSONObject) JSONObject.parse(empJson);
        } catch (Exception e) {
            e.printStackTrace();

        }finally {
            if (in!=null){
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        Message message=new Message();
//        String id=(String) result.get("id");
//        if (id==null||id.equals("")){
//            message.setCode("-1");
//            message.setMessage("id does not exist");
//            return message;
//        }
        TableChecks tableChecks=checksService.selectChecks();
        if (tableChecks==null){
//            message.setCode("-1");
//            message.setMessage("TableChecks does not exist");
//            return message;
            throw new BussinessException("-1","TableChecks does not exist");
        }
        JSONObject object=(JSONObject) JSONObject.toJSON(tableChecks);
        message.setCode("0");
        message.setMessage("tableCheck select success");
        message.setContent(object);
        return message;
    }

}
