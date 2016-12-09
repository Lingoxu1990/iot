package com.iot.newEditionController;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.iot.message.MessageNoContent;
import com.iot.newEditionService.NodeLocationService;
import com.iot.pojo.DispatcherLocation;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by xulingo on 16/8/19.
 */

@Controller
public class NodeLocationController {


    @Resource
    NodeLocationService nodeLocationService;


    @RequestMapping(value = "/dispatcher_location", method = RequestMethod.POST)
    @ResponseBody
    public Object initDispatcher(@RequestBody @JsonFormat List<DispatcherLocation> list) {


        MessageNoContent messageNoContent = new MessageNoContent();

        if (list == null || list.size() == 0) {

            messageNoContent.setCode("-1");
            messageNoContent.setMessage("No contents in your request body");
            return messageNoContent;

        }
        nodeLocationService.delete(list);


        int row = nodeLocationService.initDispatcher(list);


        if (row < 1) {
            messageNoContent.setCode("-1");
            messageNoContent.setMessage("Failed to insert the data");
            return messageNoContent;
        }
        messageNoContent.setCode("0");
        messageNoContent.setMessage("Init the data successfully!");

        return messageNoContent;
    }


}
