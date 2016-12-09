package com.iot.newEditionController;


import com.alibaba.fastjson.JSONObject;
import com.iot.message.Message;
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
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;


/**
 * Created by adminchen on 16/5/31.
 */
@Controller
@RequestMapping("/edition/new/login")
public class NewEditionLoginController {

    private static Logger logger= Logger.getLogger(NewEditionLoginController.class);
    @Resource
    LoginService loginService;

    @RequestMapping(method= RequestMethod.POST)
    @ResponseBody
    public Object Login(HttpServletRequest req){
        Message message=new Message();
        BufferedReader in=null;
        JSONObject result=null;
        try{
            in=new BufferedReader(new InputStreamReader(req.getInputStream(),"utf-8"));
            String line="";
            String empJson="";
            while((line=in.readLine())!=null){
                empJson+=line;

            }
            result=(JSONObject)JSONObject.parse(empJson);

        }catch (Exception e){
        e.printStackTrace();
        }finally{
            if (in!=null){
                try{
                    in.close();
                }catch(IOException e){
                    e.printStackTrace();;
                }
            }
        }

        System.out.println(result.toString());
        String e_mail=(String) result.get("e_mail");
        String password=(String) result.get("password");


        if(e_mail==null||e_mail.equals("")){
            message.setCode("-1");
            message.setMessage("Missing the param e_mail");
            message.setContent("[]");
        }
        if(password==null||password.equals("")){
            message.setCode("-1");
            message.setMessage("Missing the param passage");
            message.setContent("-1");
        }

        UserTable userTable=loginService.login(e_mail,password);

        if (userTable==null){
            message.setCode("-1");
            message.setMessage("Error:login name no exits;");
            message.setContent("");
        }else {
            List<UserGateway> userGateWay=loginService.geAccountId(userTable.getUser_id());
            if(userTable.getPassword().equals(password)&&!userTable.getUser_authorization().equals("0")){
                message.setCode("0");
                message.setMessage("login success");
                JSONObject jsonObject=(JSONObject) JSONObject.toJSON(userTable);
                jsonObject.remove("password");
                jsonObject.put("accout_id",userGateWay.get(0).getAccount_id());
                message.setContent(jsonObject);
                HttpSession session=req.getSession();
            }else if (userTable.getPassword().equals(password)&&userTable.getUser_authorization().equals("0")){
                message.setCode("-1");
                message.setMessage("you no authorization");
                message.setContent("");
            }else {
                message.setCode("-1");
                message.setMessage("you password false!");
            }
        }
        System.out.println(message.getMessage());

        return message;
    }
}
