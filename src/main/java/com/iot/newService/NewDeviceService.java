package com.iot.newService;

import com.iot.pojo.TableChannel;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import com.iot.pojo.TableDevice;

import java.util.List;

/**
 * Created by adminchen on 16/6/21.
 */
public interface NewDeviceService {

    List<TableDevice> findDevice(TableDevice tableDevice);
    List<TableDevice> findInfoOfTheDevice(TableDevice tableDevice);
    List<TableChannel> findChannelInfo(TableChannel tableChannel);

    List<TableChannel> findDeviceChannelValue(TableChannel tableChannel);

    int deviceController(TableDevice tableDevice);


    int modifyDeviceName(TableDevice tableDevice);

    TableDevice findDeviceValue(TableDevice tableDevice);

    JSONObject socketControllDevice(JSONObject jsonObject, String DestinationID, String SourceID, int packegType);

}
