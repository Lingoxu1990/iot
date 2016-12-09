package com.iot.listener;

import com.alibaba.fastjson.JSONObject;
import com.iot.dbUtil.MysqlUtil;
import com.iot.dbUtil.PropsUtil;
import com.iot.dbUtil.SQLiteUtil;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

/**
 * Created by xulingo on 16/6/23.
 */
public class UpdateTimer {
    public static void main(String[] args) throws ClassNotFoundException, SQLException {

        Connection connection = null;
        Statement statement = null;
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:" + "/Users/xulingo/IoTServerDATA/00000001/000000010052c779_Record_Data.db");

        statement = connection.createStatement();
        statement.setQueryTimeout(30);  // set timeout to 30 sec.


        ResultSet resultSet = statement.executeQuery(
                "SELECT * from table_sensor_record");
        ResultSetMetaData rsmd = resultSet.getMetaData();

        while (resultSet.next()){


            System.out.println(resultSet.getString(1));
        }


    }

}
