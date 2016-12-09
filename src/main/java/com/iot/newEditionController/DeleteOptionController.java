package com.iot.newEditionController;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.iot.exception.ParameterException;
import com.iot.message.Message;
import com.iot.newEditionService.DeleteOptionService;
import com.iot.utils.ParamUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Created by adminchen on 16/7/21.
 */
@Controller
public class DeleteOptionController {
    private JSONObject jsonObject;

    @Resource
    private DeleteOptionService deleteOptionService;

    @RequestMapping(value = "/deleteOptions",method = RequestMethod.POST)
    @ResponseBody
    public Message deleteOptions(HttpServletRequest httpServletRequest) throws IOException {
        Message message=new Message();
        JSONObject jsonObject = ParamUtils.getAttributess(httpServletRequest);
        JSONObject regions=(JSONObject) jsonObject.get("region");
        String user_id=(String) jsonObject.get("user_id");
        String gateway_id=(String) jsonObject.get("gateway_id");
        if (user_id==null||user_id.equals("")){
            throw new  ParameterException("-1","user_id does not exist");
        }

        if (gateway_id==null||gateway_id.equals("")){
            throw new  ParameterException("-1","gateway_id does not exist");
        }

        //查询是否有场景及场景成员
        JSONArray scenes=(JSONArray) regions.get("scene");

        JSONArray groups=(JSONArray) regions.get("group");

        JSONArray regionDevice=(JSONArray) regions.get("regionDevice");

        JSONArray object=deleteOptionService.selectObjects(user_id,gateway_id,regions,scenes,groups,regionDevice);
        message.setCode("0");
        message.setMessage("delete data success");
        message.setContent(object);
        return message;
    }
}

