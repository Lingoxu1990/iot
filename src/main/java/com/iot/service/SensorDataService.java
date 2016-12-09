package com.iot.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.iot.pojo.TableRegionDevice;
import com.iot.pojo.TableSensorRecord;

import java.util.List;

/**
 * Created by xulingo on 16/5/5.
 */
public interface SensorDataService {

    JSONArray getSenorData(String sensor_guid,String account_id,String start_time,String end_time);

    JSONArray getRealTime(String sensor_guid,String accuont_id,String gateway_id,String SourceId);
    //实时数据
    JSONArray getDataNowTime(String sensor_guid,String accuont_id,String gateway_id);

    List<TableRegionDevice> getRegionSensor( TableRegionDevice tableRegionDevice);

    //查询一天数据
    JSONObject getSensorDayData(String deviceGuid,String userId,String startTime,String endTime,String data_type);


}
