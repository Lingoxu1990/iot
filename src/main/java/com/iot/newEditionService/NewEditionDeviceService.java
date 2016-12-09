package com.iot.newEditionService;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.iot.pojo.TableChannel;
import com.iot.pojo.TableDevice;

import java.util.List;

/**
 * Created by adminchen on 16/6/21.
 */
public interface NewEditionDeviceService {

    JSONArray findDevice(String user_id, String region_guid, String group_guid, String scene_guid, String gateway_id);

    List<TableDevice> findInfoOfTheDevice(TableDevice tableDevice);
    List<TableChannel> findChannelInfo(TableChannel tableChannel);

    List<TableChannel> findDeviceChannelValue(TableChannel tableChannel);

    int deviceController(TableDevice tableDevice);


    int modifyDeviceName(TableDevice tableDevice);

    TableDevice findDeviceValue(TableDevice tableDevice);

    JSONObject socketControllDevice(JSONObject jsonObject, String DestinationID, String SourceID, int packegType);

    void modifyDeviceOfName(TableDevice tableDevice,String userId);



}
