package com.iot.dbUtil;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.log4j.Logger;

import javax.activation.DataSource;
import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by surfacepc on 2016/3/16.
 */
public class MysqlUtil {

    private static BlockingQueue<Connection> pool = new LinkedBlockingQueue<Connection>(10);

    private static Logger logger =Logger.getLogger(MysqlUtil.class);

    static {
        getpool();

    }

    private static void getpool(){

        Connection connection= null;

        String driver="";
        String url="";
        String username ="";
        String password="";

        PropsUtil dbProps = new PropsUtil("jdbc.properties");

        try {
            driver = dbProps.get("driver");
            url= dbProps.get("url");
            username=dbProps.get("username");
            password=dbProps.get("password");
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            Class.forName(driver);

            for (int i = 0; i <10 ; i++) {
                connection = DriverManager.getConnection(
                        url,
                        username,
                        password
                );
                pool.offer(connection);
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static Connection getConnection() throws SQLException, ClassNotFoundException {

        Connection connection= null;

        String driver="";
        String url="";
        String username ="";
        String password="";

        PropsUtil dbProps = new PropsUtil("jdbc.properties");

        try {
            driver = dbProps.get("driver");
            url= dbProps.get("url");
            username=dbProps.get("username");
            password=dbProps.get("password");
        } catch (IOException e) {
            e.printStackTrace();
        }

        Class.forName(driver);

        return connection = DriverManager.getConnection(
                url,
                username,
                password
        );
    }

    public static void sqlexcute(String[] sqls,String tableName ,String account_id,String gateway_id){

        Connection connection = pool.poll();
        Statement statement = null;

        try {

            statement = connection.createStatement();

            if (!tableName.toUpperCase().contains("RECORD") && tableName.toUpperCase().contains("TABLE")) {
                if(!tableName.equals("table_register") && !tableName.equals("table_ctrl_sequence")){
                    String deleteall = "delete from "+tableName+" WHERE account_id='"+account_id+"'"+" AND gateway_id= '"+gateway_id+"'";

                    logger.debug(deleteall);
                    statement.executeUpdate(deleteall);
                }

            }

            for (int i = 0; i <sqls.length ; i++) {
                if ("".equals(sqls[i])){
                    continue;
                }
                logger.debug(sqls[i].toString());
                statement.executeUpdate(sqls[i]);
            }

        } catch (SQLException e) {

            System.out.println(tableName + "发生错误");
            e.printStackTrace();
        } finally {
            pool.offer(connection);
        }

    }

    public static List<JSONObject> getAccountFilesInfoAndRecordInfo(){
        List<JSONObject> list = new ArrayList<JSONObject>();

        String sql = "SELECT * FROM account_data_info";
        Connection connection = null;
        Statement statement = null;

        try {

            connection = getConnection();

            statement = connection.createStatement();

            ResultSet resultSet = statement.executeQuery(sql);

            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();

            int columnCount = resultSetMetaData.getColumnCount();

            String [] columnNames = new String[columnCount];


            for (int i = 1; i <=columnCount ; i++) {
                columnNames [i-1]= resultSetMetaData.getColumnName(i);
            }

            while(resultSet.next()){
                JSONObject jsononject = new JSONObject();

                for (int i = 1; i <=columnCount ; i++) {
                    String values = resultSet.getString(i);
                    if (values==null){
                        values="";
                    }

                    jsononject.put(resultSetMetaData.getColumnName(i),values);
                }

                list.add(jsononject);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            if(connection!=null){
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

        }
        return list;
    }
}
