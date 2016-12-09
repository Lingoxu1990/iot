package com.iot.controller;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.iot.exception.BussinessException;
import com.iot.exception.ParameterException;
import com.iot.md5Util.Md5Util;
import com.iot.message.Message;
import com.iot.message.MessageNoContent;
import com.iot.pojo.UserTable;
import com.iot.service.UserService;
import com.iot.utils.ParamUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.UUID;


/**
 * Created by liusheng on 16/2/17.
 */
@Controller
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;


    /**
     * @param
     * @param
     * @return
     */
    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public Object addChildUser(@RequestBody @JsonFormat UserTable userTable) {

        System.out.println(userTable.toString());

        MessageNoContent message = new MessageNoContent();
        String user_id = UUID.randomUUID().toString();
        userTable.setUser_id(user_id);

        JSONObject aa = (JSONObject) JSONObject.toJSON(userTable);

        UserTable fatherUser = userService.selectByPrimaryKey(userTable.getFather_user());
        JSONObject abba = (JSONObject) JSONObject.toJSON(fatherUser);

        userTable.setUser_authorization(String.valueOf(Integer.parseInt(fatherUser.getUser_authorization()) - 1));
        //加密
        String passwords=userTable.getPassword();
        userTable.setPassword(Md5Util.stringMd5(passwords));
        //System.out.println("加密密码:"+userTable.getPassword());
        //userTable.setFather_user(user_id_f);
        int num = userService.insert(userTable);

        if (num == 1) {
            message.setCode("0");
            message.setMessage("Build Success");

        } else {
//            message.setCode("-1");
//            message.setMessage("Build Failed");
            throw new BussinessException("-1","Build Failed");

        }

        return message;
    }

    //查询用户
    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public Object getChildUsers(HttpServletRequest httpServletRequest) {
        Message message = new Message();
        String user_id = httpServletRequest.getParameter("user_id");
        List<UserTable> list = userService.findChildUsers(user_id);
        UserTable userTable=userService.findUsers(user_id);
        if (list.size() == 0&&userTable==null) {
//            message.setCode("0");
//            message.setContent(new JSONArray());
//            message.setMessage("No Result!");
            throw new BussinessException("0","No Result!");
        } else {
            JSONArray jlist = (JSONArray) JSONObject.toJSON(list);
            JSONObject users=(JSONObject)JSONObject.toJSON(userTable);
            users.remove("father_user");
            jlist.add(users);
            message.setCode("0");
            message.setContent(jlist);
            message.setMessage("Search Success");

        }
        return message;
    }

    @RequestMapping(method = RequestMethod.PUT)
    @ResponseBody
    public Object ModifyChildUsers(HttpServletRequest req) {

        MessageNoContent message = new MessageNoContent();
        JSONObject result = null;
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(req.getInputStream(), "utf-8"));
            String line = "";
            String empJson = "";
            while ((line = in.readLine()) != null) {
                empJson += line;
            }
            result = (JSONObject) JSONObject.parse(empJson);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String user_id = (String) result.get("user_id");
        String child_user_id = (String) result.get("child_user_id");
        result.remove("child_user_id");
        result.put("user_id", child_user_id);

        List<UserTable> list = userService.findChildUsers(user_id);

        if (list.size() < 1) {
//            message.setCode("-1");
//            message.setMessage("this user has no child user");
//
//            return message;
            throw new BussinessException("-1","this user has no child user");
        }
        boolean flag = true;
        for (UserTable userTable : list) {
            if (userTable.getUser_id().equals(child_user_id)) {
                flag = false;
                break;
            }
        }

        if (flag) {
//            message.setCode("-1");
//
//            message.setMessage("the child user can't match the father user");
//            return message;
            throw new BussinessException("-1","the child user can't match the father user");
        }

        System.out.println(result.toString());

        UserTable userTable = JSONObject.parseObject(result.toString(), UserTable.class);
        //加密
//        String passwords=Md5Util.stringMd5(userTable.getPassword());
//        userTable.setPassword(passwords);

        int num = userService.updateByPrimaryKeySelective(userTable);

        if (num == 1) {
            message.setCode("0");
            message.setMessage("Update user Success");

        } else {
//            message.setCode("-1");
//            message.setMessage("Update user Failed");
            throw new BussinessException("-1","Update user Failed");

        }

        return message;
    }

    @RequestMapping(method = RequestMethod.DELETE)
    @ResponseBody
    public Object disableChildUser(HttpServletRequest req) {

        JSONObject result = null;
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(req.getInputStream(), "utf-8"));
            String line = "";
            String empJson = "";
            while ((line = in.readLine()) != null) {
                empJson += line;
            }
            result = (JSONObject) JSONObject.parse(empJson);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String user_id = (String) result.get("user_id");
        String child_user_id = (String) result.get("child_user_id");

        Message message = new Message();
        List<UserTable> list = userService.findChildUsers(user_id);

        if (list.size() < 1) {

            throw new BussinessException("-1","this user has no child user");
        }
        boolean flag = true;
        for (UserTable userTable : list) {
            if (userTable.getUser_id().equals(child_user_id)) {
                flag = false;
                break;
            }
        }

        if (flag) {

            throw new BussinessException("-1","the child user can't match the father user");
        }

        int num = userService.deleteByPrimaryKey(child_user_id);
        if (num == 1) {
            message.setCode("0");
            message.setMessage("Delete user Success");
            message.setContent(num);
        } else {
//            message.setCode("-1");
//            message.setMessage("Delete user Failed");
//            message.setContent("[]");
            throw new BussinessException("-1","Delete user Failed");
        }
        return message;
    }

    //修改用户密码
    @RequestMapping(value = "/passWord",method = RequestMethod.PUT)
    @ResponseBody
    public MessageNoContent modifyUserPassword(HttpServletRequest request) throws IOException {
        JSONObject param= ParamUtils.getAttributess(request);
        String userId=(String) param.get("user_id");
        String passWord=(String) param.get("pass_word");
        if (userId==null||userId.equals("")){
            throw new ParameterException("-1","user_id does not exist");
        }
        if (passWord==null||passWord.equals("")){
            throw new ParameterException("-1","pass_word does not exist");
        }

        UserTable userTable=new UserTable();
        userTable.setPassword(passWord);
        userTable.setUser_id(userId);
        userService.modifyUserPassWord(userTable);
        MessageNoContent message=new MessageNoContent();
        message.setCode("0");
        message.setMessage("update user password success");
        return message;
    }
}
