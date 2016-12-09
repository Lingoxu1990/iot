package com.iot.newEditionService;

import com.alibaba.fastjson.JSONObject;
import com.iot.message.Message;
import com.iot.pojo.TableChannel;
import com.iot.pojo.TableDevice;
import com.iot.pojo.TableRegion;
import com.iot.pojo.TableRegionDevice;

import java.util.List;
import java.util.Map;

/**
 * Created by adminchen on 16/6/3.
 */

public interface NewEditionRegionDeviceService {


    List<TableChannel> findChannel(TableChannel tableChannel);

    List<TableRegionDevice> FindDeviceRepeat(TableRegionDevice tableRegionDevice);

    //插入区域设备
    JSONObject insertRegionDevice(String user_id, TableRegionDevice tableRegionDevice,JSONObject jsonObject);

    TableRegion findRegion(String region_guid);

    TableDevice findDevice(String table_device_guid);

    void  deleteByRegion_AddrIdAndDevice_Addr(String user_id,TableRegionDevice tableRegionDevice,JSONObject jsonObject);
    //
    List<Map> fingRegionDevice(TableRegionDevice tableRegionDevice);

    boolean IsNullRegionValue(TableRegion tableRegion);

    //查找region_guid下所有RegionDevice

    boolean IsTypeRegionDevices(TableRegionDevice tableRegionDevice);

    Message ModifyByDrviceOfname(List<TableRegionDevice> list);


    //int insertRegionDevice(TableRegionDevice tableRegionDevice);

    JSONObject socketAddRegionDevice(JSONObject jsonObject, String DestinationID, String SourceID, int packegType);

    JSONObject socketDeleteRegionDevice(JSONObject jsonObject, String DestinationID, String SourceID, int packegType);

    TableRegion findRegionOfNow(TableRegion tableRegion);






}
