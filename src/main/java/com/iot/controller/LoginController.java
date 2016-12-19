package com.iot.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.iot.exception.BussinessException;
import com.iot.md5Util.Md5Util;
import com.iot.socketUtil.OutPutSocketMessage;
import com.iot.message.Message;
import com.iot.pojo.AccountDataInfo;
import com.iot.pojo.UserGateway;
import com.iot.pojo.UserTable;
import com.iot.service.LoginService;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Created by xulingo on 16/4/6.
 */
@Controller
@RequestMapping("/login")
public class LoginController {

    private static Logger logger = Logger.getLogger(LoginController.class);

    @Resource
    LoginService loginService;


    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public Object login(HttpServletRequest httpServletRequest) {

        //System.out.println("12346818631463156123156");
        Message message = new Message();

        BufferedReader in = null;
        JSONObject result=null;
        try {

            in = new BufferedReader(new InputStreamReader(httpServletRequest.getInputStream(), "utf-8"));
            String line = "";
            String empJson = "";
            while ((line = in.readLine()) != null) {
                empJson += line;
            }
            System.out.println(empJson);
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


        String e_mail = (String) result.get("e_mail");
        String password = (String) result.get("password");


        if (e_mail == null || e_mail.equals("")) {
//            message.setCode("-1");
//            message.setMessage("Missing the param 'e_mail'");
//            message.setContent("[]");
//            return message;
            throw new BussinessException("-1","Missing the param 'e_mail'");
        }
        if (password == null || password.equals("")) {
//            message.setContent("[]");
//            message.setMessage("Missing the param 'password'");
//            message.setCode("-1");
//            return message;
            throw new BussinessException("-1","Missing the param 'password'");
        }


        UserTable userTable = loginService.login(e_mail, password);


        if (userTable == null ) {
//            message.setCode("-1");
//            message.setMessage("Error: Login failed (name/password refused)");
//            message.setContent("");
            throw new BussinessException("-1","Error: Login failed (name/password refused)");
        } else {
            List<UserGateway> userGateways = loginService.geAccountId(userTable.getUser_id());
            if (userTable.getPassword().equals(password) && !userTable.getUser_authorization().equals("0")) {
                message.setCode("0");
                message.setMessage("Login Success!");
                JSONObject jsonObject = (JSONObject) JSONObject.toJSON(userTable);
                jsonObject.remove("password");
                jsonObject.put("account_id", userGateways.get(0).getAccount_id());
                message.setContent(jsonObject);
                HttpSession httpSession = httpServletRequest.getSession();


            } else if (userTable.getPassword().equals(password) && userTable.getUser_authorization().equals("0")) {
//                message.setCode("-1");
//                message.setMessage("Error: Disabled Account (Your account has been disabled,please contract your administrator)");
//                message.setContent("");
                throw new BussinessException("-1","Error: Disabled Account (Your account has been disabled,please contract your administrator)");
            } else {
//                message.setCode("-1");
//                message.setMessage("Error: Login failed (name/password refused)");
//                message.setContent("");
                throw new BussinessException("-1","Error: Login failed (name/password refused)");
            }
        }
        return message;

    }

    @RequestMapping(value = "/test", method = RequestMethod.POST)
    @ResponseBody
    public Object test(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        Message message = new Message();
        String e_mail = httpServletRequest.getParameter("e_mail");
        String password = httpServletRequest.getParameter("password");

        UserTable userTable = loginService.login(e_mail, password);

        if (userTable == null) {
            message.setCode("-1");
            message.setMessage("Error: User dosen't exist");
            message.setContent("");
        } else if (userTable.getUser_authorization().equals("-1")) {
            message.setCode("0");
            message.setMessage("offline");
            message.setContent("");
        } else {
            OutPutSocketMessage outPutSocketMessage = new OutPutSocketMessage();
            try {
                JSONObject login_result = outPutSocketMessage.logintest();
                String result = (String) login_result.get("Message");

                if (result.equals("login successful")) {

                    message.setCode("0");
                    message.setMessage("online");
                    message.setContent("");
                } else {

                    message.setCode("0");
                    message.setMessage("offline");
                    message.setContent("");
                }
            } catch (Exception e) {
                message.setCode("0");
                message.setMessage("offline");
                message.setContent("");
                e.printStackTrace();
            }

        }
        return message;

    }

    //查询
    @RequestMapping(value = "/user_info", method = RequestMethod.GET)
    @ResponseBody
    public Object userdata(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        Message message = new Message();

        String user_id = httpServletRequest.getParameter("user_id");

        List<AccountDataInfo> list = loginService.getUserInfo(user_id);

        if (list.size() < 1) {
            message.setCode("0");
            message.setMessage("No Result");
            message.setContent(new JSONArray());
            return message;
        }


        JSONArray jsonArray = new JSONArray();
        for (AccountDataInfo usergateway : list) {

            JSONObject json = (JSONObject) JSONObject.toJSON(usergateway);
            json.remove("account_id");
            jsonArray.add(json);
        }
        message.setCode("0");
        message.setMessage("Search successfully!");
        message.setContent(jsonArray);


        return message;

    }


}
