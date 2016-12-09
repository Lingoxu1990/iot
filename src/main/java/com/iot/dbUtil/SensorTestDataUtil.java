package com.iot.dbUtil;

import com.iot.pojo.TableSensorRecord;

import java.io.IOException;
import java.sql.*;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by xulingo on 16/6/2.
 */
public class SensorTestDataUtil {

    private static  void UpdateTestSensorData(){

        String driver="";
        String url="";
        String username ="";
        String password="";
        Connection connection=null;

        PropsUtil propsUtil = new PropsUtil("jdbc.properties");

        try {
            driver = propsUtil.get("driver");
            url= propsUtil.get("url");
            username=propsUtil.get("username");
            password=propsUtil.get("password");


        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            Class.forName(driver);
            connection = DriverManager.getConnection(
                    url,
                    username,
                    password
            );
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String querySql = "SELECT * FROM table_sensor_record where table_device_guid = '1110D337583C4E18B306368E84AE9639' ORDER BY record_time DESC ";

        Statement statement=null;

        try {
            statement= connection.createStatement();

            ResultSet resultSet = statement.executeQuery(querySql);

            java.util.Date d = new Date(System.currentTimeMillis());


            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            Calendar calendar =Calendar.getInstance();
            int year =calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH)+1;
            int day = calendar.get(Calendar.DATE);
            int hour=calendar.get(Calendar.HOUR_OF_DAY);

            String str_month="";
            if (month<10){
                str_month = "0"+String.valueOf(month);
            }else {
                str_month=String.valueOf(month);
            }
            String str_date ="";
            if (day<10){
                str_date = "0"+String.valueOf(day);
            }else {
                str_date=String.valueOf(day);
            }
            String str_hour="";
            if (hour<10){
                str_hour="0"+String.valueOf(hour);
            }else {
                str_hour=String.valueOf(hour);
            }


            List<TableSensorRecord> list  = new ArrayList<TableSensorRecord>();

            String aa="";

            int det= 0;

            while (resultSet.next()){

                TableSensorRecord tableSensorRecord = new TableSensorRecord();

                tableSensorRecord.setId(
                        resultSet.getString("id")
                );
                String old_reocrd_time =resultSet.getString("record_time");

                String[]  spit_str = old_reocrd_time.split(" ");

                String time_str = spit_str[1];
                StringBuilder stringBuilder =new StringBuilder();
                stringBuilder.append(time_str);
                String AA = time_str.split(":")[0];

                if ("".equals(aa)){
                    aa=AA;
                    det=Integer.parseInt(str_hour)-Integer.parseInt(aa);
                }

                stringBuilder.delete(0,2);


//                if (AA.equals("0-2")){
//
//                    System.out.println(resultSet.getString("record_guid"));
//
//                }
                String prex="";
                if ((det+Integer.parseInt(AA)+1)<10){
                    prex = "0"+String.valueOf(det+Integer.parseInt(AA)+1);
                }else {
                    prex = String.valueOf(det+Integer.parseInt(AA)+1);
                }

                String prex_date =String.valueOf(year)+"-"+str_month+"-"+str_date;


                tableSensorRecord.setRecord_time(
                        prex_date+" "+prex+stringBuilder.toString()
                );
                list.add(tableSensorRecord);
            }


            for (TableSensorRecord record: list) {

                String updateSql = "UPDATE table_sensor_record set record_time='"+record.getRecord_time()+"' WHERE id = '"+record.getId()+"'";
                System.out.println(updateSql);
                statement.executeUpdate(updateSql);

            }

            statement.close();



        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            if (connection!=null){
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }


    }

    public static void main(String[] args) {
        UpdateTestSensorData();
    }

}
