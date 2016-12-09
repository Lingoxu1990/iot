package com.iot.pojo;

import java.util.List;

/**
 * Created by adminchen on 16/6/15.
 */
public class InterfaceGroupMembers {

    private String user_id;
    private List<AddGroupMembers> table_group_members;

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public List<AddGroupMembers> getTable_group_members() {
        return table_group_members;
    }

    public void setTable_group_members(List<AddGroupMembers> table_group_members) {
        this.table_group_members = table_group_members;
    }
}
