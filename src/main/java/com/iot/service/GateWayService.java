package com.iot.service;

import com.alibaba.fastjson.JSONArray;
import com.iot.pojo.UserGateway;

/**
 * Created by xulingo on 16/4/7.
 */


public interface GateWayService {

    int insert(UserGateway userGateway);

    JSONArray findGatewayOfDevice(String gatewayId,String userId);
}
