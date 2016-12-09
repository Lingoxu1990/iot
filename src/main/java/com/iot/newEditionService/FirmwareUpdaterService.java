package com.iot.newEditionService;

import com.alibaba.fastjson.JSONObject;
import com.iot.pojo.DispatcherLocation;
import com.iot.pojo.UserGateway;

/**
 * Created by xulingo on 16/8/24.
 */
public interface FirmwareUpdaterService {


    DispatcherLocation selectByGatewayId(String gatewayId);

    void sendByFwdGateway(String account_id,DispatcherLocation dispatcherLocation);

}
