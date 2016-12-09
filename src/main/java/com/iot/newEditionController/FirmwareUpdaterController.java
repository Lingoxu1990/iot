package com.iot.newEditionController;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.iot.exception.BussinessException;
import com.iot.message.MessageNoContent;
import com.iot.newEditionService.FirmwareUpdaterService;
import com.iot.pojo.DispatcherLocation;
import com.iot.pojo.UserGateway;
import com.iot.utils.MyZipUtil;
import org.omg.CORBA.PUBLIC_MEMBER;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by xulingo on 16/8/19.
 */

@Controller
public class FirmwareUpdaterController {

    @Resource
    private FirmwareUpdaterService firmwareUpdaterService;


    /**
     * 该方法提供一个了一个下载接口,供子网关获取最新的固件程序zip
     * @param response
     * @throws IOException
     */
    @RequestMapping(value = "/updater/firmware",method = RequestMethod.GET)
    @ResponseBody
    public void firmwareUpdater(HttpServletResponse response) throws IOException {

        /*
        该uuid用于生成临时文件名称
         */
        String tempUUID = UUID.randomUUID().toString();

        /*
        获取压缩工具类
         */
        MyZipUtil myZipUtil = new MyZipUtil(tempUUID);

        /*
        压缩返回zip文件对象
         */
        File file =  myZipUtil.compress();

        //判断文件是否存在如果不存在就返回默认图标
        if (!(file.exists() && file.canRead())) {
            throw new BussinessException("-1", "zip file can not be readed");
        }

        FileInputStream inputStream = new FileInputStream(file);
        byte[] data = new byte[(int) file.length()];

        inputStream.read(data);//将文件中的内容读取到字节数组中
        inputStream.close();
        OutputStream stream = response.getOutputStream();
        stream.write(data);
        stream.flush();
        stream.close();

        file.delete();

    }

    /**
     * 该方法用于下发 固件更新命令至子网关
     * usergateway 对象中包含了 account_id / gateway_id
     * @param userGateway
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/update/firmware",method = RequestMethod.POST)
    @ResponseBody
    public MessageNoContent firmwareUpdate(@RequestBody @JsonFormat UserGateway userGateway) throws IOException {

        MessageNoContent messageNoContent = new MessageNoContent();

        String gatewayId = userGateway.getGateway_id();

        if (gatewayId==null || "".equals(gatewayId)){

            messageNoContent.setCode("-1");
            messageNoContent.setMessage("Missing the param gateway");
            return messageNoContent;
        }


        String accountId = userGateway.getAccount_id();

        if (accountId==null || "".equals(accountId)){

            messageNoContent.setCode("-1");
            messageNoContent.setMessage("Missing the param account_id");
            return messageNoContent;
        }

        // 获取子网关对应的dispathcer的网关id
        DispatcherLocation dispatcherLocation = firmwareUpdaterService.selectByGatewayId(gatewayId);

        // 发送指令
        firmwareUpdaterService.sendByFwdGateway(accountId,dispatcherLocation);



        messageNoContent.setCode("0");
        messageNoContent.setMessage("the firmware update successfully!");

        return messageNoContent;
    }
}
