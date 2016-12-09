package com.iot.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.iot.exception.ParameterException;
import com.iot.message.Message;
import com.iot.pojo.UserGateway;
import com.iot.service.GateWayService;
import com.iot.spitUtil.Param;
import com.iot.utils.ParamUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.UUID;

/**
 * 该接口已经废弃
 * Created by xulingo on 16/4/7.
 */

@Controller
@RequestMapping("/gateway")
public class HardWareController {

    @Resource
    private GateWayService gateWayService;

    @RequestMapping(method = RequestMethod.POST)
    public Object init(@RequestBody @JsonFormat UserGateway userGateway) {

        Message message = new Message();

//        int id = Integer.parseInt(UUID.randomUUID());
//        userGateway.setId(id);
        userGateway.setAccount_id(String.valueOf(System.currentTimeMillis() / 100000));

        if (userGateway.getGateway_id() == null || "".equals(userGateway.getGateway_id())) {
            message.setCode("-1");
            message.setMessage("missing gateway_id");
            message.setContent("[]");
            return message;
        }

        int num = gateWayService.insert(userGateway);

        if (num != 1) {
            message.setCode("-1");
            message.setMessage("Insert gateway success!");
            //message.setContent(id);
        } else {
            message.setCode("0");
            message.setMessage("Insert gateway failed!");
            message.setContent("[]");
        }

        return message;
    }

    //查询网关下的设备列表
    @RequestMapping(value = "/device",method = RequestMethod.GET)
    @ResponseBody
    public Message getGatewayOfDevice(HttpServletRequest request) throws IOException {
        String gatewayId=request.getParameter("gateway_id");
        String userId=request.getParameter("user_id");
        if (gatewayId==null||gatewayId.equals("")){
            throw new ParameterException("-1","gateway_id does not exist");
        }
        if (userId==null||userId.equals("")){
            throw new ParameterException("-1","user_id does not exist");
        }
        JSONArray result=gateWayService.findGatewayOfDevice(gatewayId,userId);

        Message message=new Message();
        message.setCode("0");
        message.setMessage("select gateway device success");
        message.setContent(result);
        return message;
    }


}
