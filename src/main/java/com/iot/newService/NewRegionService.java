package com.iot.newService;

import com.alibaba.fastjson.JSONObject;
import com.iot.message.Message;
import com.iot.pojo.TableRegion;
import com.iot.pojo.TableRegionDevice;
import com.iot.pojo.TableRegionGroup;
import com.iot.pojo.TableRegionScene;

import java.util.List;

/**
 * Created by adminchen on 16/6/1.
 */
public interface NewRegionService {
    //
    int  addRegion(TableRegion tableRegion);

    int  deleteRegion(TableRegion tableRegion);

    int modify(TableRegion tableRegion);

    List<TableRegion>  findAllRegion(TableRegion tableRegion);

    List<TableRegionDevice > findTheDeleteDevice(TableRegion tableRegion);

    int addRegionScene(TableRegionScene tableRegionScene);

    int deleteRegionScene(TableRegionScene tableRegionScene);

    List<TableRegionScene> findRegionScene(TableRegionScene tableRegionScene);

    int addRegionGroup(TableRegionGroup tableRegionGroup);

    int deleteRegionGroup(TableRegionGroup tableRegionGroup);

    List<TableRegionGroup> findRegionGroup(TableRegionGroup tableRegionGroup);

    TableRegion findTheValueOftheRegion(TableRegion tableRegion);

    JSONObject socketControlReion(JSONObject jsonObject, String DestinationID, String SourceID, int packegType);

    int mysqlControlRegion(TableRegion tableRegion);

    JSONObject socketUpdateRegion(String region_value,String region_guid, String DestinationID, String SourceID, int packegType);

    JSONObject socketAddRegion(JSONObject jsonObject, String DestinationID, String SourceID, int packegType);

    JSONObject socketAdd(JSONObject jsonObject, String DestinationID, String SourceID, int packegType);

    JSONObject socketDeleteRegion(JSONObject jsonObject, String DestinationID, String SourceID, int packegType);

    List<TableRegionDevice> findDistRegionDevice(TableRegion tableRegion);

    JSONObject socketAddRegionGroup(JSONObject jsonObject, String DestinationID, String SourceID, int packegType);

    JSONObject socketAddRegionScene(JSONObject jsonObject,String DestinationID,String SourceId,int PackegType);

    TableRegionGroup selectRegionGroupByGroupGuid(TableRegionGroup regionGroup);

    TableRegionScene findRegionSceneOne(TableRegionScene tableRegionScene);

}
