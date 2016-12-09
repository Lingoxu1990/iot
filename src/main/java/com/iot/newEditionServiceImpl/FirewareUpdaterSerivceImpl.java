package com.iot.newEditionServiceImpl;

import com.alibaba.fastjson.JSONObject;
import com.iot.exception.BussinessException;
import com.iot.mapper.DispatcherLocationMapper;
import com.iot.newEditionService.FirmwareUpdaterService;
import com.iot.pojo.DispatcherLocation;
import com.iot.socketUtil.OutPutSocketMessage;
import com.iot.spitUtil.Param;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 *
 * 该控制器接口用于固件自动升级
 * Created by xulingo on 16/8/24.
 */

@Service
public class FirewareUpdaterSerivceImpl  implements FirmwareUpdaterService{


    @Resource
    private DispatcherLocationMapper dispatcherLocationMapper;


    /**
     * 该方法用于根据网关id 查找出调度进程位置
     * @param gatewayId
     * @return
     */
    public DispatcherLocation selectByGatewayId(String gatewayId) {

        return dispatcherLocationMapper.selectByGatewayId(gatewayId);

    }

    /**
     *
     * 该方法用于根据用户id,调度进程位置,默认下发一个更新命令,触发网关更新固件
     *
     * @param account_id
     * @param dispatcherLocation
     */
    public void sendByFwdGateway(String account_id, DispatcherLocation dispatcherLocation) {
        String dispatcher = dispatcherLocation.getDispatcher_gateway();

        OutPutSocketMessage outPutSocketMessage = new OutPutSocketMessage();
        outPutSocketMessage.setPackegType(Param.PACKEGTYPE);//包类型，int型2
        outPutSocketMessage.setDestinationID(dispatcher);//调度者地址（字符串）
        outPutSocketMessage.setType("NULL");//填写“NULL”

        String sourceId = account_id + Param.WEBAPPID + String.valueOf(System.currentTimeMillis() / 1000000000);

        outPutSocketMessage.setMessage("update");//下发的具体数据（拼接后的字符串）
        outPutSocketMessage.setSourceID(sourceId);//消息发送者的ID（前8位与调度者地址相同）
        outPutSocketMessage.setSql("");//配方操作指令 apply/cancel

        JSONObject result = outPutSocketMessage.sendMessag(sourceId);

        String status = String.valueOf(result.get("Status"));

        if (!status.equals("0")) {
            throw new BussinessException("-1","gateway error");
        }

        return ;
    }
}
