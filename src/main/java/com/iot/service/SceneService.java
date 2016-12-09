package com.iot.service;

import com.alibaba.fastjson.JSONObject;
import com.iot.message.Message;
import com.iot.pojo.TableScene;

import java.util.List;

/**
 * Created by chenweixiang on 16/3/23.
 */

public interface SceneService {
    //    修改场景属性(批量)
    Message modifyScene(JSONObject jsonObject, String DestinationID, String SourceID, int packegType);
    //    获取场景的详细信息
    Message findScene(JSONObject jsonObject, String DestinationID, String SourceID, int packegType);
    //  增加场景(批量)
    Message addScene(JSONObject jsonObject, String DestinationID, String SourceID, int packegType);
    //  删除场景(批量)
    Message deleteScene(JSONObject jsonObject, String DestinationID, String SourceID, int packegType);
    //   控制场景开关
    Message controlScene(JSONObject jsonObject, String DestinationID, String SourceID, int packegType);

    //新增场景成员
    Message addSceneMember(JSONObject jsonObject, String DestinationID, String SourceID, int packegType);
    //修改场景成员
    Message modifySceneMembers(JSONObject jsonObject, String DestinationID, String SourceID, int packegType);
    //获取场景成员
    Message findSceneMembers(JSONObject jsonObject, String DestinationID, String SourceID, int packegType);
    //删除场景成员
    Message deleteSceneMember(JSONObject jsonObject, String DestinationID, String SourceID, int packegType);



}
