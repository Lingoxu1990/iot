package com.iot.service;

import com.alibaba.fastjson.JSONObject;
import com.iot.message.Message;

/**
 * Created by Jacob on 2016-04-07.
 */
public interface TermService {

    Message modifyTerm(JSONObject jsonObject, String DestinationID, String SourceID, int packegType);//    修改条件
    Message findTerm(JSONObject jsonObject, String DestinationID, String SourceID, int packegType);//    查找条件
    Message addTerm(JSONObject jsonObject, String DestinationID, String SourceID, int packegType);//  增加条件
    Message deleteTerm(JSONObject jsonObject, String DestinationID, String SourceID, int packegType);//  删除条件

    //增加条件至条件集合
    Message addConditions(JSONObject jsonObject, String DestinationID, String SourceID, int packegType);
    Message modifyConditions(JSONObject jsonObject, String DestinationID, String SourceID, int packegType);
    Message deleteConditions(JSONObject jsonObject, String DestinationID, String SourceID, int packegType);

    //增加动作序列至条件集合
    Message addSequence(JSONObject jsonObject, String DestinationID, String SourceID, int packegType);
    Message modifySequence(JSONObject jsonObject, String DestinationID, String SourceID, int packegType);
    Message deleteSequence(JSONObject jsonObject, String DestinationID, String SourceID, int packegType);

    //增加控制动作至条件集合
    Message addControls(JSONObject jsonObject, String DestinationID, String SourceID, int packegType);
    Message modifyControls(JSONObject jsonObject, String DestinationID, String SourceID, int packegType);
    Message deleteControls(JSONObject jsonObject, String DestinationID, String SourceID, int packegType);
}
