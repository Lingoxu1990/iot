package com.iot.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.iot.message.Message;
import com.iot.pojo.AccountDataInfo;
import com.iot.pojo.UserGateway;
import com.iot.service.TermService;
import com.iot.service.UserService;
import com.iot.spitUtil.Param;
import com.iot.utils.ParamUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

/**
 * Created by Jacob on 2016-04-07.
 */

@Controller
@RequestMapping("/device/term")
public class TermController {

    private static Logger logger = Logger.getLogger(TermController.class);
    @Resource
    private TermService termService;
    @Resource
    private UserService userService;

    /**
     *
     * @return
     */
    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public Object addTerm(HttpServletRequest httpServletRequest) throws IOException {
        JSONObject jsonObject = ParamUtils.getJsonObjectFromRequest(httpServletRequest);


        JSONObject temp = new JSONObject();

        String user_id = (String) jsonObject.get("user_id");

        List<UserGateway> tableDeviceList = userService.selectGatewayByUserId(user_id);

        String account_id =tableDeviceList.get(0).getAccount_id();

        String SourceId= account_id+ Param.WEBAPPID+String.valueOf(System.currentTimeMillis()/1000000000);
        JSONArray cdts_lists = (JSONArray) jsonObject.get("table_cdts_list");

        JSONObject cdts_list = (JSONObject) cdts_lists.get(0);
        String gateway_id = (String) cdts_list.get("gateway_id");
        jsonObject.remove("user_id");

        logger.error("service : device/term"  + " action : add term " );

        Message message =termService.addTerm(jsonObject,gateway_id,SourceId,2);

        return  message;
    }

    @RequestMapping(method = RequestMethod.PUT)
    @ResponseBody
    public Object modifyTerm(HttpServletRequest httpServletRequest) throws IOException {

        JSONObject jsonObject = ParamUtils.getJsonObjectFromRequest(httpServletRequest);

        String user_id = (String) jsonObject.get("user_id");

        List<UserGateway> tableDeviceList = userService.selectGatewayByUserId(user_id);

        String account_id =tableDeviceList.get(0).getAccount_id();

        String SourceId= account_id+ Param.WEBAPPID+String.valueOf(System.currentTimeMillis()/1000000000);
        JSONArray cdts_lists = (JSONArray) jsonObject.get("table_cdts_list");

        JSONObject cdts_list = (JSONObject) cdts_lists.get(0);
        String gateway_id = (String) cdts_list.get("gateway_id");
        jsonObject.remove("user_id");
        System.out.println(jsonObject.toString());
        logger.error("service : device/term"  + " action : add term " );

        Message message =termService.modifyTerm(jsonObject,gateway_id,SourceId,2);

        return  message;
    }

    /**
     *  入参 user_id
     *  出参 和条件控制相关的信息
     *
     * @return
     */
    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public Object findTerm(HttpServletRequest httpServletRequest) throws IOException {

        Message message= new Message();

        JSONObject jsonObject = ParamUtils.getJsonObjectFromRequest(httpServletRequest);


        JSONObject term = (JSONObject) jsonObject.get("term");
        String user_id =(String)term.get("user_id");
        String cdts_list_guid =(String)term.get("cdts_list_guid");

        logger.error("user_id : "+user_id);

        List<UserGateway> tableDeviceList = userService.selectGatewayByUserId(user_id);

        String account_id =tableDeviceList.get(0).getAccount_id();

        String SourceId= account_id+ Param.WEBAPPID+String.valueOf(System.currentTimeMillis()/1000000000);

        List<AccountDataInfo> tableDeviceList1 = userService.getALLagateway(account_id);

        JSONArray rr = new JSONArray();

        for (AccountDataInfo accountinfo: tableDeviceList1) {

            JSONObject temp = new JSONObject();
            JSONObject cdts = new JSONObject();
            cdts.put("gateway_id",accountinfo.getGateway_id());
            cdts.put("cdts_list_guid",cdts_list_guid);
            temp.put("table_cdts_list",cdts);

            Message tempmessage = termService.findTerm(temp,accountinfo.getGateway_id(),SourceId,2);

            if (!tempmessage.getCode().equals("-1")){

                JSONArray content = (JSONArray) tempmessage.getContent();
                System.out.println(content.toString());

                for (int i = 0; i <content.size() ; i++) {

                    rr.add(content.get(i));

                }
            }
        }

        message.setCode("0");
        message.setMessage("success");
        message.setContent(rr);
        return message;
    }
    @RequestMapping(method = RequestMethod.DELETE)
    @ResponseBody
    private Object deleteTerm(HttpServletRequest httpServletRequest) throws IOException {
        Message message  = new Message();

        JSONObject jsonObject = ParamUtils.getJsonObjectFromRequest(httpServletRequest);

        String user_id = (String) jsonObject.get("user_id");
        JSONArray table_cdts_lists = (JSONArray) jsonObject.get("table_cdts_list");
        JSONArray table_conditionss = (JSONArray) jsonObject.get("table_conditons");
        JSONArray table_ctrl_sequences =(JSONArray) jsonObject.get("table_ctrl_sequence");
        JSONArray table_controls =(JSONArray) jsonObject.get("table_control");

        List<UserGateway> tableDeviceList = userService.selectGatewayByUserId(user_id);

        String account_id =tableDeviceList.get(0).getAccount_id();

        String SourceId= account_id+ Param.WEBAPPID+String.valueOf(System.currentTimeMillis()/1000000000);

        for (int i = 0; i <4 ; i++) {

            if (i==0){
                if (table_controls.size()>0){
                    JSONObject temp_json = new JSONObject();
                    temp_json.put("table_control",table_controls);
                    JSONObject control = (JSONObject) table_controls.get(0);
                    String gateway_id = (String) control.get("gateway_id");
                    message =termService.deleteControls(jsonObject,gateway_id,SourceId,2);
                    if (message.getCode().equals("-1")){
                        return message;
                    }
                }
            }
            if (i==1){
                if (table_ctrl_sequences.size()>0){
                    JSONObject temp_json = new JSONObject();
                    temp_json.put("table_ctrl_sequence",table_ctrl_sequences);
                    JSONObject control = (JSONObject) table_controls.get(0);
                    String gateway_id = (String) control.get("gateway_id");
                    message =termService.deleteSequence(jsonObject,gateway_id,SourceId,2);
                    if (message.getCode().equals("-1")){
                        return message;
                    }
                }
            }
            if (i==2){
                if (table_conditionss.size()>0){
                    JSONObject temp_json = new JSONObject();
                    temp_json.put("table_conditons",table_conditionss);
                    JSONObject control = (JSONObject) table_controls.get(0);
                    String gateway_id = (String) control.get("gateway_id");
                    message =termService.deleteConditions(jsonObject,gateway_id,SourceId,2);
                    if (message.getCode().equals("-1")){
                        return message;
                    }
                }
            }
            if (i==3){
                if (table_cdts_lists.size()>0){
                    JSONObject temp_json = new JSONObject();
                    temp_json.put("table_conditons",table_cdts_lists);
                    JSONObject control = (JSONObject) table_controls.get(0);
                    String gateway_id = (String) control.get("gateway_id");
                    message =termService.deleteTerm(jsonObject,gateway_id,SourceId,2);
                    if (message.getCode().equals("-1")){
                        return message;
                    }
                }
            }
        }
        message.setCode("0");
        message.setMessage("delete successfully!");
        message.setContent("[]");

        return message;
    }

    @RequestMapping(value = "/conditions",method = RequestMethod.POST)
    @ResponseBody
    public Object addConditions(HttpServletRequest httpServletRequest) throws IOException {
        JSONObject jsonObject = ParamUtils.getJsonObjectFromRequest(httpServletRequest);

        String user_id = (String) jsonObject.get("user_id");
        jsonObject.remove("user_id");

        List<UserGateway> tableDeviceList = userService.selectGatewayByUserId(user_id);

        String account_id =tableDeviceList.get(0).getAccount_id();

        String SourceId= account_id+ Param.WEBAPPID+String.valueOf(System.currentTimeMillis()/1000000000);
        String DestinationId = tableDeviceList.get(0).getGateway_id();

        JSONArray conditions = (JSONArray) jsonObject.get("table_conditons");
        JSONObject condition = (JSONObject) conditions.get(0);
        String gateway_id = (String) condition.get("gateway_id");

        Message message =termService.addConditions(jsonObject,gateway_id,SourceId,2);

        return  message;
    }

    @RequestMapping(value = "/conditions",method = RequestMethod.PUT)
    @ResponseBody
    public Object modifyConditions(HttpServletRequest httpServletRequest) throws IOException {
        JSONObject jsonObject = ParamUtils.getJsonObjectFromRequest(httpServletRequest);

        String user_id = (String) jsonObject.get("user_id");

        List<UserGateway> tableDeviceList = userService.selectGatewayByUserId(user_id);

        String account_id =tableDeviceList.get(0).getAccount_id();

        String SourceId= account_id+ Param.WEBAPPID+String.valueOf(System.currentTimeMillis()/1000000000);
        JSONArray conditions = (JSONArray) jsonObject.get("table_conditons");

        JSONObject condition = (JSONObject) conditions.get(0);
        String gateway_id = (String) condition.get("gateway_id");
        jsonObject.remove("user_id");
        System.out.println(jsonObject.toString());
        logger.error("service : device/term"  + " action : add term " );

        Message message =termService.modifyConditions(jsonObject,gateway_id,SourceId,2);

        return  message;


    }



    @RequestMapping(value = "/sequence",method = RequestMethod.POST)
    @ResponseBody
    public Object addTermSequence(HttpServletRequest httpServletRequest) throws IOException {
        JSONObject jsonObject = ParamUtils.getJsonObjectFromRequest(httpServletRequest);

        System.out.println(jsonObject.toString());
        JSONObject temp = new JSONObject();

        String user_id = (String) jsonObject.get("user_id");

        List<UserGateway> tableDeviceList = userService.selectGatewayByUserId(user_id);

        String account_id =tableDeviceList.get(0).getAccount_id();

        String SourceId= account_id+ Param.WEBAPPID+String.valueOf(System.currentTimeMillis()/1000000000);
        String DestinationId = tableDeviceList.get(0).getGateway_id();

        jsonObject.remove("user_id");
        JSONArray table_ctrl_sequences = (JSONArray) jsonObject.get("table_ctrl_sequence");
        JSONObject table_ctrl_sequence = (JSONObject)table_ctrl_sequences.get(0);
        String gateway_id = (String) table_ctrl_sequence.get("gateway_id");
        table_ctrl_sequence.remove("gateway_id");

        Message message =termService.addSequence(jsonObject,gateway_id,SourceId,2);

        return  message;
    }
    @RequestMapping(value = "/sequence",method = RequestMethod.PUT)
    @ResponseBody
    public Object modifySequence(HttpServletRequest httpServletRequest) throws IOException {
        JSONObject jsonObject = ParamUtils.getJsonObjectFromRequest(httpServletRequest);

        String user_id = (String) jsonObject.get("user_id");

        List<UserGateway> tableDeviceList = userService.selectGatewayByUserId(user_id);

        String account_id =tableDeviceList.get(0).getAccount_id();

        String SourceId= account_id+ Param.WEBAPPID+String.valueOf(System.currentTimeMillis()/1000000000);
        JSONArray conditions = (JSONArray) jsonObject.get("table_ctrl_sequence");

        JSONObject condition = (JSONObject) conditions.get(0);
        String gateway_id = (String) condition.get("gateway_id");
        jsonObject.remove("user_id");
        System.out.println(jsonObject.toString());
        logger.error("service : device/term"  + " action : add term " );

        Message message =termService.modifySequence(jsonObject,gateway_id,SourceId,2);

        return  message;


    }


    @RequestMapping(value = "/controls",method = RequestMethod.POST)
    @ResponseBody
    public Object addTermControls(HttpServletRequest httpServletRequest) throws IOException {
        JSONObject jsonObject = ParamUtils.getJsonObjectFromRequest(httpServletRequest);

        String user_id = (String) jsonObject.get("user_id");

        List<UserGateway> tableDeviceList = userService.selectGatewayByUserId(user_id);

        String account_id =tableDeviceList.get(0).getAccount_id();

        String SourceId= account_id+ Param.WEBAPPID+String.valueOf(System.currentTimeMillis()/1000000000);
        String DestinationId = tableDeviceList.get(0).getGateway_id();

        jsonObject.remove("user_id");


        // TODO: 16/5/19 暂时屏蔽开关字段
        JSONArray controls  = (JSONArray) jsonObject.get("table_control");
        String gateway_id="";

        for (int i = 0; i <controls.size() ; i++) {

            JSONObject control_temp = (JSONObject) controls.get(i);
            control_temp.remove("m_switch");
            gateway_id=(String) control_temp.get("gateway_id");
        }

        Message message =termService.addControls(jsonObject,gateway_id,SourceId,2);

        return  message;
    }
    @RequestMapping(value = "/controls",method = RequestMethod.PUT)
    @ResponseBody
    public Object modifyTermControls(HttpServletRequest httpServletRequest) throws IOException {
        JSONObject jsonObject = ParamUtils.getJsonObjectFromRequest(httpServletRequest);

        String user_id = (String) jsonObject.get("user_id");

        List<UserGateway> tableDeviceList = userService.selectGatewayByUserId(user_id);

        String account_id =tableDeviceList.get(0).getAccount_id();

        String SourceId= account_id+ Param.WEBAPPID+String.valueOf(System.currentTimeMillis()/1000000000);
        JSONArray conditions = (JSONArray) jsonObject.get("table_control");

        JSONObject condition = (JSONObject) conditions.get(0);
        String gateway_id = (String) condition.get("gateway_id");
        jsonObject.remove("user_id");
        System.out.println(jsonObject.toString());
        logger.error("service : device/term"  + " action : add term " );

        Message message =termService.modifyControls(jsonObject,gateway_id,SourceId,2);

        return  message;
    }



}
