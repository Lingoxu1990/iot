package com.iot.newService;

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

public interface NewRegionDeviceService {


    List<TableChannel> findChannel(TableChannel tableChannel);

    boolean FindDeviceRepeat(TableRegionDevice tableRegionDevice);

    boolean insert(List<TableRegionDevice> list,TableRegionDevice TableRegionDevice);

    TableRegion findRegion(String region_guid);

    TableDevice findDevice(String table_device_guid);

    int  deleteByRegion_AddrIdAndDevice_Addr(TableRegionDevice tableRegionDevice);
    //
    List<Map> fingRegionDevice(TableRegionDevice tableRegionDevice);

    TableChannel selectChannelsOne(TableChannel tableChannel);

    boolean IsNullRegionValue(String region_guid);

    //查找region_guid下所有RegionDevice

    boolean IsTypeRegionDevices(TableRegionDevice tableRegionDevice);

    Message ModifyByDrviceOfname(List<TableRegionDevice> list);


    int insertRegionDevice(TableRegionDevice tableRegionDevice);

    JSONObject socketAddRegionDevice(JSONObject jsonObject, String DestinationID, String SourceID, int packegType);

    JSONObject socketDeleteRegionDevice(JSONObject jsonObject,String DestinationID,String SourceID,int packegType);






}
