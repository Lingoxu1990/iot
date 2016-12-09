package com.iot.service;

import com.alibaba.fastjson.JSONObject;
import com.iot.message.Message;
import com.iot.pojo.TableGroup;

import java.util.List;

/**
 * Created by Jacob on 16/4/7.
 */

public interface GroupService {
    //  修改设备组属性
     Message modifyGroup(JSONObject jsonObject, String DestinationID, String SourceID, int packegType);
    //  获取设备组的详细信息
    Message findGroup(JSONObject jsonObject, String DestinationID, String SourceID, int packegType);
    //  增加设备组
    Message addGroup(JSONObject jsonObject, String DestinationID, String SourceID, int packegType);
    //  删除设备组
    Message deleteGroup(JSONObject jsonObject, String DestinationID, String SourceID, int packegType);
    //往组中添加设备
    Message addGroupDevice(JSONObject jsonObject, String DestinationID, String SourceID, int packegType);
    //删除组中的成员设备
    Message deleteGroupMember(JSONObject jsonObject, String DestinationID, String SourceID, int packegType);
    //修改组成员设备
    Message modifyGroupMembers(JSONObject jsonObject, String DestinationID, String SourceID, int packegType);
    //查找组成员设备
    Message findGroupMembers(JSONObject jsonObject, String DestinationID, String SourceID, int packegType);

    //关联修改区域中的组名

    //控制组
    Message controlGroup(JSONObject jsonObject, String DestinationID, String SourceID, int packegType);

}
