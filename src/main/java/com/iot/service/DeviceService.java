package com.iot.service;

import com.alibaba.fastjson.JSONObject;
import com.iot.message.Message;

/**
 * Created by Jacob on 16/4/6.
 */

/*
* 设备服务
* */
public interface DeviceService {

    Message modifyDevice(JSONObject jsonObject, String DestinationID, String SourceID, int packegType);//    修改设备属性
    Message findDevice(JSONObject jsonObject, String DestinationID, String SourceID, int packegType);//    获取所有设备的详细信息
    Message addDevice(JSONObject jsonObject, String DestinationID, String SourceID, int packegType); //  增加设备
    Message deleteDevice(JSONObject jsonObject, String DestinationID, String SourceID, int packegType);//  删除设备

    Message findDevice(String account_id);

}
