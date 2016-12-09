package com.iot.newService;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.iot.message.Message;
import com.iot.pojo.TableDevice;
import com.iot.pojo.TableGroup;
import com.iot.pojo.TableGroupMembers;
import com.iot.pojo.TableRegionGroup;

import java.util.List;

/**
 * Created by adminchen on 16/6/13.
 */
public interface NewGroupService {

    int  insertGroup(TableGroup tableGroup);

    int deleteGroup(TableGroup tableGroup);

    Message updateGoup(TableGroup tableGroup);

//    Message findRegionGroup(TableRegionGroup tableRegionGroup);

//    boolean isGroupDevice(TableGroupMembers tableGroupMembers);
//
//    boolean isDeviceSensor(TableDevice tableDevice);

    Message insertGroupMembers(TableGroupMembers tableGroupMembers);

    int deleteGroupMembers(TableGroupMembers tableGroupMembers);

    int modifyGroupMembers(TableGroupMembers tableGroupMembers);

    List<TableGroup> findGroup(TableGroup tableGroup);

    List<TableGroupMembers> findGroupMember(TableGroupMembers tableGroupMembers);

    //组控制
    int groupController(TableGroup tableGroup, JSONArray channel_value_arr);

    //子网关中 新增一个组
    JSONObject socketAddGroup(JSONObject jsonObject, String DestinationID, String SourceID, int packegType);
    //子网关中 新增一个组成员
    JSONObject socketAddGroupMembers(JSONObject jsonObject, String DestinationID, String SourceID, int packegType);
    //子网关中 删除一个组成员
    JSONObject socketDeleteGroupMembers(JSONObject jsonObject, String DestinationID, String SourceID, int packegType);

    //根据组地址查询组成员列表
    List<TableGroupMembers> findGroupMemberByGroupAddr(TableGroupMembers tableGroupMembers);

    TableRegionGroup findRegionGroupByGroupAddr(TableRegionGroup tableRegionGroup);

    JSONObject socketDeleteGroup(JSONObject jsonObject, String DestinationID, String SourceID, int packegType);

    JSONObject socketDeleteRegionGroup(JSONObject jsonObject, String DestinationID, String SourceID, int packegType);

    JSONObject socketControlGroup(JSONObject jsonObject, String DestinationID, String SourceID, int packegType);

}
