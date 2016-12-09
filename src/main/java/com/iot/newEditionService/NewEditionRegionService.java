package com.iot.newEditionService;

import com.alibaba.fastjson.JSONObject;
import com.iot.pojo.*;

import java.util.List;

/**
 * Created by adminchen on 16/6/1.
 */
public interface NewEditionRegionService {
    //
    void   addRegion(String user_id,TableRegion tableRegion);

    void   deleteRegion(String user_id,TableRegion tableRegion,JSONObject jsonObject);

    void modifyRegionName(TableRegion tableRegion,String userId);

    List<TableRegion>  findAllRegion(TableRegion tableRegion);

    List<TableRegionDevice > findTheDeleteDevice(TableRegion tableRegion);

    //添加区域场景
    void addRegionScene(String user_id,TableRegionScene tableRegionScene);

    void deleteRegionScene(String user_id,TableRegionScene tableRegionScene,JSONObject jsonObject);

    List<TableRegionScene> findRegionScene(TableRegionScene tableRegionScene);

    void addRegionGroup(String user_id,TableRegionGroup tableRegionGroup);

    void deleteRegionGroup(String user_id,TableRegionGroup tableRegionGroup,JSONObject jsonObject);

    List<TableRegionGroup> findRegionGroup(TableRegionGroup tableRegionGroup,String userId);

    TableRegion findTheValueOftheRegion(TableRegion tableRegion);

    JSONObject socketControlReion(JSONObject jsonObject, String DestinationID, String SourceID, int packegType);

    int mysqlControlRegion(TableRegion tableRegion);

    JSONObject socketUpdateRegion(String region_value, String region_guid, String DestinationID, String SourceID, int packegType);

    JSONObject socketAddRegion(JSONObject jsonObject, String DestinationID, String SourceID, int packegType);
    JSONObject socketDeleteRegion(JSONObject jsonObject, String DestinationID, String SourceID, int packegType);

    List<TableRegionDevice> findDistRegionDevice(TableRegion tableRegion);

    JSONObject socketAddRegionGroup(JSONObject jsonObject, String DestinationID, String SourceID, int packegType);

    JSONObject socketAddRegionScene(JSONObject jsonObject, String DestinationID, String SourceId, int PackegType);

    TableRegionGroup selectRegionGroupByGroupGuid(TableRegionGroup regionGroup);

    TableRegionScene findRegionSceneOne(TableRegionScene tableRegionScene);

    //删除区域设备
    int deleteRegionDevice(TableRegionDevice tableRegionDevice);
    //查找区域下的组
    //List<TableRegionGroup> findRegionGroup(TableRegionGroup tableRegionGroup);


    //初始化区域硬件
    void initRegion(TableRegion tableRegion,String account_id);

    //修改 区域场景名称
    void modifyRegionSceneName(TableRegionScene tableRegionScene,String userId);

    //修改区域组名称
    void  modifyRegionGroupName(TableRegionGroup tableRegionGroup,String userId);

    /**
     *  移除区域状态
     * @param regionStatus
     * @return
     */
    int removeRegionStatus(RegionStatus regionStatus);

    /**
     *  新增区域状态
     * @param regionStatus
     * @return
     */
    int insertRegionStatus(RegionStatus regionStatus);

}
