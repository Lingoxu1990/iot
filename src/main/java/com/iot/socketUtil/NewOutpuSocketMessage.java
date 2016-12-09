package com.iot.socketUtil;

import com.google.gson.Gson;

import java.util.List;

/**
 * Created by xulingo on 16/5/27.
 */
public class NewOutpuSocketMessage {


    private String DestinationID;
    private String Type;
    private String Message;
    private String SourceID;
    private String sql;
    private int packegType;
    private String Status;
    private List<Gson> List;
    private String Command;
    private String PackageNumber;






    public String getDestinationID() {
        return DestinationID;
    }

    public void setDestinationID(String destinationID) {
        DestinationID = destinationID;
    }

    public String getType() {
        return Type;
    }

    public void setType(String type) {
        Type = type;
    }

    public String getMessage() {
        return Message;
    }

    public void setMessage(String message) {
        Message = message;
    }

    public String getSourceID() {
        return SourceID;
    }

    public void setSourceID(String sourceID) {
        SourceID = sourceID;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public int getPackegType() {
        return packegType;
    }

    public void setPackegType(int packegType) {
        this.packegType = packegType;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public java.util.List<Gson> getList() {
        return List;
    }

    public void setList(java.util.List<Gson> list) {
        List = list;
    }

    public String getCommand() {
        return Command;
    }

    public void setCommand(String command) {
        Command = command;
    }

    public String getPackageNumber() {
        return PackageNumber;
    }

    public void setPackageNumber(String packageNumber) {
        PackageNumber = packageNumber;
    }
}
