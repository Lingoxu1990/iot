package com.iot.controller;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonFormat;

import com.iot.fileUtil.FileUtil;
import com.iot.message.Message;
import com.iot.pojo.AccountDataInfo;
import com.iot.service.FtpService;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;



/**
 * 该控制器专用与ftp文件同步
 * Created by lingo on 2016/3/16.
 */

@Controller
@RequestMapping("/ftpfiles")
public class FtpRequestController {

    @Resource
    private FtpService ftpService;

    private static Logger logger=Logger.getLogger(FtpRequestController.class);

    /**
     * ftp将更新的文件上传至web后台所需调用的接口
     * 调用com.iot.fileUtil.fileUtil,存储db文件,并触发同步事件
     *
     * @param httpServletRequest
     * @return
     */
    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public Object getfiles(HttpServletRequest httpServletRequest) {

        Message message = new Message();

        logger.error("revicing the dbfile at "+" timestap : "+System.currentTimeMillis()+" service : "+httpServletRequest.getRequestURI()+ " method : "+RequestMethod.POST);

        String timestr = httpServletRequest.getParameter("time");

        Long time = Long.valueOf(timestr);


        try {
            boolean flag = FileUtil.saveFile(httpServletRequest.getInputStream(), httpServletRequest.getContentLength(), time);

            if (flag) {
                message.setCode("0");
                message.setContent("aaaaaa");
                message.setMessage("secesss");
            } else {
                message.setCode("-1");
                message.setContent("aaaaaa");
                message.setMessage("fafafa");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return message;

    }

    /**
     * ftp扫描服务获取用户文件信息的接口
     *
     * @param httpServletRequest
     * @return
     */
    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public Object getfis(HttpServletRequest httpServletRequest) {


        List<JSONObject> temp = ftpService.findAccountDataInfo();

        JSONArray jsonArray = new JSONArray();

        for (JSONObject jsonobject : temp) {

            JSONObject basis = new JSONObject();
            JSONObject record = new JSONObject();
            String basisName = (String) jsonobject.get("account_id") + File.separator + jsonobject.get("gateway_id") + "_Basis_Data.db";
            basis.put("name", basisName);
            String recordName = (String) jsonobject.get("account_id") + File.separator + jsonobject.get("gateway_id") + "_Record_Data.db";
            record.put("name", recordName);

            String basisTimeStr = (String) jsonobject.get("basis_data_file_last_modified");

            String recordTimeStr = (String) jsonobject.get("record_data_file_last_modified");


            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            basis.put("time", basisTimeStr);
            record.put("time", recordTimeStr);

//            long basisTime;
//            long recordTime;
//            try {
//
//                if (!"".equals(basisTimeStr)&& basisTimeStr!=null) {
//                    basisTime = simpleDateFormat.parse(basisTimeStr).getTime();
//                    basis.put("time", basisTime);
//                } else {
//                    basis.put("time", null);
//                }
//
//                if (!"".equals(recordTimeStr)&& recordTimeStr!=null) {
//                    recordTime = simpleDateFormat.parse(recordTimeStr).getTime();
//                    record.put("time", recordTime);
//                } else {
//                    record.put("time", null);
//                }
//
//
//            } catch (ParseException e) {
//                e.printStackTrace();
//            }

            jsonArray.add(basis);
            jsonArray.add(record);

        }

        return jsonArray;
    }

    /**
     * ftp扫描服务删除web上保留的初始化的空数据 调用接口
     *
     * @param httpServletRequest
     * @return
     */
    @RequestMapping(method = RequestMethod.DELETE)
    @ResponseBody
    public Object deleteInitData(HttpServletRequest httpServletRequest) {

        Message message = new Message();
        int num = ftpService.deleteInitData(httpServletRequest.getParameter("account_id"));

        if (num == 1) {
            message.setContent("");
            message.setMessage("Init data has been deleted successfully!");
            message.setCode("0");
        } else {
            message.setCode("-1");
            message.setMessage("Init data delete failed");
            message.setContent(httpServletRequest.getParameter("account_id"));
        }


        return message;


    }

    /**
     * 该接口在ftp传输方式启用后 ，已弃用
     * ftp 扫描服务 修改用户文件信息 调用接口
     *
     * @param accountDataInfo
     * @return
     */
    @RequestMapping(method = RequestMethod.PUT)
    @ResponseBody
    public Object ModifyInitData(@RequestBody @JsonFormat AccountDataInfo accountDataInfo) {

        Message message = new Message();
        int num = ftpService.updateByPrimaryKeySelective(accountDataInfo);

        if (num == 1) {
            message.setCode("0");
            message.setMessage("Update Successfully!");
            message.setContent("");
        } else {
            message.setContent("");
            message.setCode("-1");
            message.setMessage("Update failed");
        }
        return message;

    }

    /**
     * 该接口在ftp传输方式启用后 ，已弃用
     * ftp扫描服务 在初始化用户数据的操作流程中,完成了删除web服务上的空数据后,调用该接口插入真实数据
     *
     * @param accountDataInfo
     * @return
     */
    @RequestMapping(value = "/ini", method = RequestMethod.POST)
    @ResponseBody
    public Object InsertUserInfo(@RequestBody @JsonFormat AccountDataInfo accountDataInfo) {
        System.out.println(accountDataInfo.getAccount_id());

        Message message = new Message();
        int num = ftpService.addAccountDataInfo(accountDataInfo);

        if (num == 1) {
            message.setCode("0");
            message.setMessage("Update Successfully!");
            message.setContent("");
        } else {
            message.setContent("");
            message.setCode("-1");
            message.setMessage("Update failed");
        }
        return message;
    }


}
