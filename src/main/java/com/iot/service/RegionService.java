package com.iot.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.iot.message.Message;
import com.iot.pojo.TableRegionDevice;
import com.iot.pojo.TableRegionGroup;
import com.iot.pojo.TableRegionScene;

import java.util.List;
import java.util.Map;

/**
 * Created by xulingo on 16/4/5.
 */

public interface RegionService {

    //修改区域
    Message modifyRegion(JSONObject jsonObject, String DestinationID, String SourceID, int packegType);
    //查找区域
    JSONArray findRegion(JSONObject jsonObject, String DestinationID, String SourceID, int packegType);

    //增加区域
    Message addRegion(JSONObject jsonObject, String DestinationID, String SourceID, int packegType);
    //删除区域
    Message deleteRegion(JSONObject jsonObject, String DestinationID, String SourceID, int packegType);

    //增加区域设备
    Message addRegionDevice(JSONObject jsonObject, String DestinationID, String SourceID, int packegType);

    //特殊处理/分组处理
    Map<String,JSONObject> specialAction(JSONArray alldevies, String sourceId,String tablename,String region_gateway_id);

    //查找区域下的设备
    Message findRegionDevice(JSONObject jsonObject, String DestinationID, String SourceID, int packegType);
//    JSONArray findRegionDevice(TableRegionDevice tableRegionDevice);

    //删除区域设备
    Message deleteRegionDevice(JSONObject jsonObject,String DestinationID,String SourceID,int packegType);

    //增加区域组
    Message addRegionGroup(JSONObject jsonObject, String DestinationID, String SourceID, int packegType);

    //根据用户id与区域id查找区域下的所有组
//    JSONArray findRegionGroupByAccountIdAndRegionId(TableRegionGroup tableRegionGroup);
    Message findRegionGroupByAccountIdAndRegionId(JSONObject jsonObject, String DestinationID, String SourceID, int packegType);

    //删除区域组
    Message deleteRegionGroup(JSONObject jsonObject, String DestinationID, String SourceID, int packegType);

    //修改区域组信息
    Message modifyRegionGroup(JSONObject jsonObject, String DestinationID, String SourceID, int packegType);

    //根据用户id与组主键,获取区域组的主键
    JSONObject findRegionGroupByAccountIdAndGroupId(TableRegionGroup tableRegionGroup);

    //为区域添加场景
    Message addRegionScene(JSONObject jsonObject, String DestinationID, String SourceID, int packegType);
    //查找区域场景主键
    JSONObject findRegionSceneByAccountIdAndSceneId(TableRegionScene tableRegionScene);
    //查找区域下的场景
    Message findRegionSceneByAccountIdAndRegionId(JSONObject jsonObject, String DestinationID, String SourceID, int packegType);
    //修改区域场景信息
    Message modifyRegionScene(JSONObject jsonObject, String DestinationID, String SourceID, int packegType);
    //删除区域场景
    Message deleteRegionScene(JSONObject jsonObject, String DestinationID, String SourceID, int packegType);

    //添加区域条件控制集合
    Message addRegionCdts(JSONObject jsonObject, String DestinationID, String SourceID, int packegType);

    Message controlReion(JSONObject jsonObject, String DestinationID, String SourceID, int packegType);




}
