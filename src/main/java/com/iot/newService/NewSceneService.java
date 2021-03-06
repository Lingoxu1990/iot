package com.iot.newService;

import com.alibaba.fastjson.JSONObject;
import com.iot.message.Message;
import com.iot.pojo.*;

import java.util.List;

/**
 * Created by adminchen on 16/6/16.
 */
public interface NewSceneService {
    int insertScene(TableScene tableScene);

    int updateScene(TableScene tableScene);

    List<TableScene> findScene(TableScene tableScene);

    int deleteScene(TableScene tableScene);

    int insertSceneMembers(TableSceneMembers tableSceneMembers);

    int deleteSceneMembers(TableSceneMembers tableSceneMembers);

    int deleteSceneByMembers(TableSceneMembers tableSceneMembers);

    List<TableSceneMembers> selectSceneMembers(TableSceneMembers tableSceneMemberst);

    TableSceneMembers  selectSceneMemberByDevice(TableSceneMembers tableSceneMembers);

    int updateSceneMembers(TableSceneMembers tableSceneMembers);

    int sceneApplication(TableScene tableScene);

    JSONObject socketAddScene(JSONObject jsonObject, String DestinationID, String SourceID, int packegType);

    JSONObject socketAddSceneMembers(JSONObject jsonObject,String DestinationID,String SourceID,int packegType);

    JSONObject socketDeleteSceneMembers(JSONObject jsonObject,String DestinationID,String SourceID,int packegType);

    TableRegionScene findRegionScene(TableRegionScene tableRegionScene);

    //删除区域场景
    int deleteRegionScene(TableRegionScene tableRegionScene);

    //删除场景socket
    JSONObject socketDeleteScene(JSONObject jsonObject,String DestiantionID,String SourceID, int packegType);
    //修改场景成员
    JSONObject socketModifySceneMembers(JSONObject jsonObject,String DestiantionID,String SourceID,int packegType);

    //删除区域场景
    JSONObject socketDeleteRegionScene(JSONObject jsonObject,String DestiantionID,String SourceID, int packegType);

    //查询区域场景下的场景(单个)
    TableRegionScene findRegionSceneone(TableRegionScene tableRegionScene);

    //查询场景成员通道
    List<TableChannel> selectSceneMembersByChannel(TableChannel tableChannel);

    //更新区域场景
    int  updateRegionScene(TableRegionScene tableRegionScene);

    //修改场景
    JSONObject socketmodifyScene(JSONObject jsonObject,String DestiantionID,String SourceID,int packegType);

    //修改区域场景
    JSONObject socketmodifyRegionScene(JSONObject jsonObject,String DestiantionID,String SourceID,int packegType);

    //场景控制
    JSONObject socketApplication(JSONObject jsonObject,String DestiantionID,String SourceID,int packegType);

    //
    TableDevice selectDevice(TableDevice tableDevice);

}
