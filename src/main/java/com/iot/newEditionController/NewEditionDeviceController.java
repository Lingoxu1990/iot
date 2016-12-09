package com.iot.newEditionController;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.iot.exception.ParameterException;
import com.iot.message.Message;
import com.iot.message.MessageNoContent;
import com.iot.newEditionService.NewEditionDeviceService;
import com.iot.pojo.TableChannel;
import com.iot.pojo.TableDevice;
import com.iot.pojo.UserGateway;
import com.iot.service.UserService;
import com.iot.spitUtil.Param;
import com.iot.utils.ParamUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

/**
 * Created by adminchen on 16/6/21.
 */

@Controller
@RequestMapping()
public class NewEditionDeviceController {

    private static Logger logger = Logger.getLogger(NewEditionLoginController.class);

    @Resource
    private UserService userService;
    @Resource
    private NewEditionDeviceService newDeviceService;

    //查询用户所有设备
    @RequestMapping(value = "/table_device",method = RequestMethod.GET)
    @ResponseBody
    public Message findDevice(HttpServletRequest httpServletRequest) throws IOException {

        JSONObject jsonObject = ParamUtils.getJsonObjectFromRequest(httpServletRequest);
        System.out.println(jsonObject.toString());
        System.out.println(jsonObject.toString());
        JSONObject devicess = (JSONObject) jsonObject.get("table_device");
        String user_id = (String) devicess.get("user_id");
        String region_guid=(String) devicess.get("region_guid");
        String group_guid=(String) devicess.get("table_group_guid");
        System.out.println("group_guid:"+group_guid);
        String scene_guid=(String) devicess.get("table_scene_guid");
        String gateway_id=(String) devicess.get("gateway_id");

        JSONArray objects =newDeviceService.findDevice(user_id,region_guid,group_guid,scene_guid,gateway_id);
        Message message=new Message();
        message.setCode("0");
        message.setMessage("Query the user's device success ");
        message.setContent(objects);
        return message;
    }

    //设备通道值获取
    @RequestMapping(value = "/edi/new/device/channel", method = RequestMethod.GET)
    @ResponseBody
    public Message findDeviceOfByChannelValues(HttpServletRequest httpServletRequest) {
        Message message = new Message();
        try {
            JSONObject jsonObject = ParamUtils.getJsonObjectFromRequest(httpServletRequest);
            JSONObject jsonObject1 = (JSONObject) jsonObject.get("channel");
            String user_id = (String) jsonObject1.get("user_id");
            String gateway_id = (String) jsonObject1.get("gateway_id");
            String device_guid = (String) jsonObject1.get("table_device_guid");
            String device_type = (String) jsonObject1.get("device_type");
            List<UserGateway> list = userService.selectGatewayByUserId(user_id);
            if (list.size()<1){
                message.setCode("-1");
                message.setMessage("user does not exist");
                message.setContent("[]");
                return message;
            }
            String account_id = list.get(0).getAccount_id();
            //
            TableChannel tableChannel = new TableChannel();
            tableChannel.setGateway_id(gateway_id);
            tableChannel.setAccount_id(account_id);
            tableChannel.setTable_device_guid(device_guid);
            if (device_type.equals("sensor")) {
                message.setCode("-1");
                message.setMessage("Device is sensor");
                message.setContent("[]");
                return message;
            }
            List<TableChannel> list1 = newDeviceService.findDeviceChannelValue(tableChannel);
            if (list1.size() > 0) {
                message.setCode("0");
                message.setMessage("DeviceChannel value for success");
                JSONArray jsonArray = new JSONArray();
                jsonArray = (JSONArray) JSONArray.toJSON(list1);
                message.setContent(jsonArray);
            } else {
                message.setCode("-1");
                message.setMessage("DeviceChannel value for failed");
                message.setContent("[]");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return message;
    }

    //修改设备名称
    @RequestMapping(value = "/device/name",method = RequestMethod.PUT)
    @ResponseBody
    public MessageNoContent modifyDeviceOfname(HttpServletRequest request) throws IOException {
        JSONObject param=ParamUtils.getAttributess(request);
        String userId=(String) param.get("user_id");
        String gatewayId=(String) param.get("gateway_id");
        String deviceName=(String) param.get("device_name");
        String deviceGuid=(String) param.get("device_guid");

        if (userId==null||userId.equals("")){
            throw new ParameterException("-1","user_id does not exist");
        }
        if (gatewayId==null||gatewayId.equals("")){
            throw new ParameterException("-1","gateway_id does not exist");
        }
        if (deviceName==null||deviceName.equals("")){
            throw new ParameterException("-1","device_name does not exist");
        }
        if (deviceGuid==null||deviceGuid.equals("")){
            throw new ParameterException("-1","device_guid does not exist");
        }

        TableDevice tableDevice=new TableDevice();
        tableDevice.setDevice_guid(deviceGuid);
        tableDevice.setGateway_id(gatewayId);
        tableDevice.setDevice_name(deviceName);
        newDeviceService.modifyDeviceOfName(tableDevice,userId);
        MessageNoContent message=new MessageNoContent();
        message.setCode("0");
        message.setMessage("update device name success");
        return message;
    }





}
