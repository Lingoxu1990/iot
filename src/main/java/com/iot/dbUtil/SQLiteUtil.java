package com.iot.dbUtil;


import com.alibaba.fastjson.JSONObject;
import com.iot.exception.BussinessException;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by surfacepc on 2016/3/15.
 */
public class SQLiteUtil {


    //用于存储sqlite数据表中的最迟更新纪录
    public static BlockingQueue<JSONObject> jsonObjectList = new LinkedBlockingDeque<JSONObject>();
    private static Logger logger = Logger.getLogger(SQLiteUtil.class);
    private static final String PRIVATERECIPEDATA="private_recipe_data";
    private static final String STARTTIME="start_time";
    private static final String ENDTIME="end_time";
    /**
     * sql数据读取的入口方法
     *
     * @param tableNames
     * @param dbpath
     * @param accountInfo
     * @return
     */
    public static Map<String, String[]> getAllTableData(String[] tableNames, String dbpath, JSONObject accountInfo) throws SQLException {


        Map<String, String[]> result = new HashMap<String, String[]>();

        for (int i = 0; i < tableNames.length; i++) {


            Object tableNameTemp = accountInfo.get(tableNames[i]);
            String tableLastMoifiedTime = "";
            if (tableNameTemp == null) {
                //表名不是记录型的,默认最后修改时间为空(走全量更新)
                tableLastMoifiedTime = "";
            } else {
                //表名是记录型的,直接获取表名键所对应的值(最后更新时间)
                tableLastMoifiedTime = (String) tableNameTemp;
            }

            String[] temp = getData(tableNames[i], dbpath, tableLastMoifiedTime);
            result.put(tableNames[i], temp);
        }
        return result;
    }

    /**
     * 增量及全量的判断
     *
     * @param tablename
     * @param dbpath
     * @param lastmodifiedtime
     * @return
     */
    public static String[] getData(String tablename, String dbpath, String lastmodifiedtime) throws SQLException {

        String[] result = null;

        if (tablename.toUpperCase().contains("RECORD") && !"".equals(lastmodifiedtime)) {
            result = getIncreaceData(tablename, dbpath, lastmodifiedtime);

        } else {
            result = getAllData(tablename, dbpath);
        }


        return result;
    }


    /**
     * 全量更新的方法
     *
     * @param tablename 表名
     * @param dbpath    db路径
     * @return
     */
    private static String[] getAllData(String tablename, String dbpath) throws SQLException {

        if ("table_sensor_record".equals(tablename)) {
            logger.debug(tablename + ": getAllData");
            System.out.println(tablename + ": getAllData");
        }

        String[] nametemp = dbpath.split("/");
        String dbfileName = nametemp[nametemp.length - 1];
        String gateway_id = dbfileName.split("_")[0];
        String account_id = nametemp[nametemp.length - 2];

        Connection connection = null;

        String result[] = null;
        Statement statement = null;
        try {
            logger.debug(dbpath);
            System.out.println(dbpath);
            Class.forName("org.sqlite.JDBC");

            connection = DriverManager.getConnection("jdbc:sqlite:" + dbpath);

            statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.
            String sql = "";
            if (tablename.toUpperCase().contains("RECORD")) {
                sql = "SELECT * from " + tablename + " ORDER BY datetime(record_time) ASC";
            } else {
                sql = "SELECT * FROM " + tablename;
            }
            logger.debug(sql);
            System.out.println(sql);

            ResultSet resultSet = statement.executeQuery(sql);
            ResultSetMetaData rsmd = resultSet.getMetaData();

            int columnCount = rsmd.getColumnCount();

            String[] columnsname = new String[columnCount];

            //获取sql语句头部
            StringBuilder sb = getupdatetitle(tablename, rsmd, columnCount, columnsname);
            //获取sql插入语句数据部分
            List<StringBuilder> valueList = getupdatecontent(columnCount, resultSet, account_id);


            if (tablename.toUpperCase().contains("RECORD") && valueList.size() > 0) {
                StringBuilder stringBuilder = valueList.get(valueList.size() - 1);

                StringBuilder lastmodifiedtime = new StringBuilder();
                lastmodifiedtime.append(stringBuilder.toString());
                lastmodifiedtime.deleteCharAt(lastmodifiedtime.lastIndexOf(","));
                lastmodifiedtime.deleteCharAt(lastmodifiedtime.lastIndexOf("("));
                lastmodifiedtime.deleteCharAt(lastmodifiedtime.lastIndexOf(")"));

                String[] lastRecords = lastmodifiedtime.toString().split(",");
                String lastRecorsTime = "";

                for (int i = 0; i < columnsname.length; i++) {

                    if ("record_time".equals(columnsname[i])) {
                        lastRecorsTime = lastRecords[i + 1];
                    }
                }
                String[] pathTemps = dbpath.split(File.separator);

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("tablename", tablename);
                jsonObject.put("lastRecordTime", lastRecorsTime);
                jsonObject.put("account_id", pathTemps[pathTemps.length - 2]);
                jsonObject.put("gateway_id", gateway_id);
                jsonObjectList.offer(jsonObject);
                logger.debug(jsonObject.toString());
                System.out.println(jsonObject.toJSONString());
            }
            if (tablename.toUpperCase().contains("RECORD") && valueList.size() == 0) {

                JSONObject jsonObject = new JSONObject();
                String[] pathTemps = dbpath.split(File.separator);
                jsonObject.put("account_id", pathTemps[pathTemps.length - 2]);
                jsonObject.put("tablename", tablename);
                jsonObject.put("lastRecordTime", "");
                jsonObject.put("gateway_id", gateway_id);
                System.out.println(jsonObject.toJSONString());
                jsonObjectList.add(jsonObject);
            }

            //生成分页插入语句 每条sql语句插入1000条,不足1000条的直接插入.
            String[] sqls = getSqls(sb, valueList);

            result = sqls;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                // connection close failed.
                System.err.println(e);
            }
        }

        return result;

    }

    /**
     * @param tablename 表名
     * @param dbpath    db路径
     * @return
     */
    private static String[] getIncreaceData(String tablename, String dbpath, String lastmodifiedtime) throws SQLException {


        if ("table_sensor_record".equals(tablename)) {
            logger.debug(tablename + ": getIncreaseData");
            System.out.println(tablename + ": getIncreaseData");
        }

        String[] nametemp = dbpath.split("/");
        String dbfileName = nametemp[nametemp.length - 1];
        String gateway_id = dbfileName.split("_")[0];
        String account_id = nametemp[nametemp.length - 2];

        String[] result = null;
        Connection connection = null;
        Statement statement = null;
        try {
            logger.debug(dbpath);
            System.out.println(dbpath);
            Class.forName("org.sqlite.JDBC");

            connection = DriverManager.getConnection("jdbc:sqlite:" + dbpath);

            logger.debug("sqlite connect successfully!");
            statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.


            //获取记录的最后更新时间;
            String date = lastmodifiedtime;

            String sql = "SELECT * from " + tablename + " WHERE datetime('" + date + "')< datetime(record_time)";
            logger.debug(sql);

            ResultSet resultSet = statement.executeQuery(sql);
            ResultSetMetaData rsmd = resultSet.getMetaData();

            int columnCount = rsmd.getColumnCount();

            String[] columnsname = new String[columnCount];

            //获取sql插入语句头部
            StringBuilder sb = getupdatetitle(tablename, rsmd, columnCount, columnsname);
            //获取sql插入语句数据部分
            List<StringBuilder> valueList = getupdatecontent(columnCount, resultSet, account_id);


            if (tablename.toUpperCase().contains("RECORD") && valueList.size() > 0) {
                StringBuilder stringBuilder = valueList.get(valueList.size() - 1);

                StringBuilder lastIncreaseTime = new StringBuilder();
                lastIncreaseTime.append(stringBuilder.toString());
                lastIncreaseTime.deleteCharAt(lastIncreaseTime.lastIndexOf(","));
                lastIncreaseTime.deleteCharAt(lastIncreaseTime.lastIndexOf("("));
                lastIncreaseTime.deleteCharAt(lastIncreaseTime.lastIndexOf(")"));

                String[] lastRecords = lastIncreaseTime.toString().split(",");
                String lastRecorsTime = "";

                for (int i = 0; i < columnsname.length; i++) {
                    if ("record_time".equals(columnsname[i])) {
                        lastRecorsTime = lastRecords[i + 1];
                    }
                }
                logger.debug("recordtime  = " + lastRecorsTime);
                System.out.println("recordtime  = " + lastRecorsTime);
                String[] pathTemps = dbpath.split(File.separator);

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("tablename", tablename);
                jsonObject.put("lastRecordTime", lastRecorsTime);
                jsonObject.put("account_id", pathTemps[pathTemps.length - 2]);
                jsonObject.put("gateway_id", gateway_id);
                jsonObjectList.offer(jsonObject);

                logger.debug(jsonObject.toJSONString());
                System.out.println(jsonObject.toJSONString());
            }

            if (tablename.toUpperCase().contains("RECORD") && valueList.size() == 0) {
                JSONObject jsonObject = new JSONObject();
                String[] pathTemps = dbpath.split(File.separator);
                jsonObject.put("account_id", pathTemps[pathTemps.length - 2]);
                jsonObject.put("tablename", tablename);
                jsonObject.put("lastRecordTime", "");
                jsonObject.put("gateway_id", gateway_id);
                logger.debug(jsonObject.toJSONString());
                System.out.println(jsonObject.toJSONString());
                jsonObjectList.add(jsonObject);
            }

            //分页插入 每条sql语句插入1000条,不足1000条的直接插入.
            String[] sqls = getSqls(sb, valueList);

            result = sqls;


        } catch (ClassNotFoundException e) {
            logger.debug(e);
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    // connection close failed.
                    System.err.println(e);
                }

            }
        }


        return result;
    }


    private static String toUpperCaseFirstOne(String s) {

        if (Character.isUpperCase(s.charAt(0)))
            return s;
        else
            return (new StringBuilder()).append(Character.toUpperCase(s.charAt(0))).append(s.substring(1)).toString();

    }

    private static String casttablename(String tablename) {

        String[] names = tablename.split("_");
        String result = "";

        for (int i = 0; i < names.length; i++) {
            result += toUpperCaseFirstOne(names[i]);
        }
        return result;
    }


    /**
     * @param tablename   查询表名
     * @param rsmd        jdbc结果集头部
     * @param columnCount 查询结果集列数
     * @param columnsname 查询结果集列名数组
     * @return 插入的表头
     * @throws SQLException
     */
    public static StringBuilder getupdatetitle(String tablename, ResultSetMetaData rsmd, int columnCount, String[] columnsname) throws SQLException {
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO " + tablename + " (" + "\n");
        for (int i = 1; i <= columnCount; i++) {

            columnsname[i - 1] = rsmd.getColumnName(i);

            if (i == columnCount) {
                sb.append(columnsname[i - 1] + ",\n account_id)" + "\n" + "VALUES" + "\n");
            } else if (i == 1) {
                sb.append("id," + columnsname[i - 1] + "," + "\n");
            } else {
                sb.append(columnsname[i - 1] + "," + "\n");
            }
        }
        return sb;
    }

    /**
     * @param columnCount 查询结果的列数
     * @param resultSet   查询结果集
     * @return
     * @throws SQLException
     */
    public static List<StringBuilder> getupdatecontent(int columnCount, ResultSet resultSet, String account_id) throws SQLException {
        List<StringBuilder> list = new ArrayList<StringBuilder>();


        while (resultSet.next()) {

            StringBuilder values = new StringBuilder();

            String id = UUID.randomUUID().toString();
            for (int j = 1; j <= columnCount; j++) {

                String temp = " ";
                if (j == columnCount && "".equals(resultSet.getString(j))) {
                    values.append("'" + temp + "'" + ",'" + account_id + "'" + "),\n");
                } else if (j == columnCount && !"".equals(resultSet.getString(j))) {
                    values.append("'" + resultSet.getString(j) + "'" + ",'" + account_id + "'" + "),\n");
                } else if (j == 1) {
                    values.append("('" + id + "'," + "'" + resultSet.getString(j) + "'" + ",");
                } else if ("".equals(resultSet.getString(j))) {
                    values.append("'" + temp + "'" + ",");
                } else {
                    values.append("'" + resultSet.getString(j) + "'" + ",");
                }
            }

            list.add(values);

        }

        return list;
    }


    /**
     * @param sb        数据插入语句头部
     * @param valueList 数据插入语句数据列表
     * @return
     */
    public static String[] getSqls(StringBuilder sb, List<StringBuilder> valueList) {
        String[] sqls = new String[1 + (valueList.size() / 1000)];

        if (valueList.size() == 0) {
            String[] error = new String[1];
            error[0] = "";
            return error;
        }

        int end = 0;
        for (int i = 0; i < sqls.length; i++) {
            StringBuilder sql = new StringBuilder();
            sql.append(sb.toString());

            if (i == (sqls.length - 1)) {
                end = valueList.size();
            } else {
                end = 1000 * (i + 1);
            }
            logger.debug(end);
            System.out.println(end);

            for (int j = i * 1000; j < end; j++) {
                if (j == (end - 1)) {
                    logger.debug(valueList.get(j).toString());
                    System.out.println(valueList.get(j).toString());
                }
                sql.append(valueList.get(j).toString());
            }
            sql.deleteCharAt(sql.lastIndexOf(","));
            sqls[i] = sql.toString();
        }

        return sqls;

    }

    /**
     * 策略是先获取需要对哪些表的数据进行操作，然后根据selectsql和insertsql将数据表生成出来
     *
     * @param accountId
     * @param regionId
     * @param recipeId
     * @return 生成的sqlite数据库路径
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public String createRecipeConfig(String accountId, String regionId, String recipeId) throws IOException, ClassNotFoundException {

        PropsUtil configProps = new PropsUtil("config.properties");

        String tablesString = configProps.get("recipe.talbes");
        String[] tables = tablesString.split(",");

        if (tables.length == 0) {
            throw new BussinessException("config error");
        }

        Class.forName("org.sqlite.JDBC");

        UUID uuid = UUID.randomUUID();

        PropsUtil dbProps = new PropsUtil("jdbc.properties");

        String dbFileName = uuid.toString() + ".db";
        String dbPath = configProps.get("recipe.dbdir") + dbFileName;

        Connection sqliteConn = null;
        Statement sqliteStatement = null;
        PreparedStatement sqliteInsertStatement = null;

        String driver = dbProps.get("driver");
        String url = dbProps.get("url");
        String username = dbProps.get("username");
        String password = dbProps.get("password");

        Class.forName(driver);

        Connection mysqlConn = null;
        PreparedStatement mysqlStatement = null;
        ResultSet mysqlResult = null;

        try {
            sqliteConn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            mysqlConn = DriverManager.getConnection(url, username, password);

            for (String table : tables) {
                String columns = configProps.get(table + ".columns");
                String insertSql = configProps.get(table + ".insertSql");
                String selectSql = configProps.get(table + ".selectSql").replace("$columns", columns).replace("$accountId", "'"+accountId+"'").replace("$regionId", "'"+regionId+"'").replace("$recipeId", "'"+recipeId+"'");

                logger.debug("生成配方数据查询sql:"+selectSql);
                logger.debug("生成配方数据插入sql:"+insertSql);

                //先创建库表
                sqliteStatement = sqliteConn.createStatement();



                if (PRIVATERECIPEDATA.equals(table)){


                    String createSql = "create table " + table + " (";
                    StringBuilder sb = new StringBuilder();
                    sb.append(createSql);

                    String  []  columnsArry = columns.split(",");

                    for (int i = 0; i <columnsArry.length ; i++) {

                        if (STARTTIME.equals(columnsArry[i]) || ENDTIME.equals(columnsArry[i])){
                            sb.append(columnsArry[i]+" integer ,");
                        }else {
                            sb.append(columnsArry[i]+",");
                        }

                        if (i==(columnsArry.length-1)){
                            sb.deleteCharAt(sb.lastIndexOf(","));
                            sb.append(") ");
                        }

                    }
                    logger.debug("sqlite建表sql: " +sb.toString());

                    sqliteStatement.executeUpdate(sb.toString());

                }else {

                    logger.debug("sqlite建表sql: " +"create table " + table + " (" + columns + ")");
                    sqliteStatement.executeUpdate("create table " + table + " (" + columns + ")");
                }

                //从云端数据库从获取数据


                mysqlStatement = mysqlConn.prepareStatement(selectSql);
                mysqlResult = mysqlStatement.executeQuery();

                //获取查询列数量
                ResultSetMetaData resultSetMetaData = mysqlResult.getMetaData();
                int columnCount = resultSetMetaData.getColumnCount();

                sqliteInsertStatement = sqliteConn.prepareStatement(insertSql);

                while (mysqlResult.next()) {
                    //插入sqlite数据库
                    for (int i = 1; i <= columnCount; i++) {
                        String value = mysqlResult.getString(i);
                        if (value == null) {
                            value = "";
                        }
                        sqliteInsertStatement.setString(i, value);
                    }

                    sqliteInsertStatement.addBatch();
                }

                sqliteInsertStatement.executeBatch();

                sqliteStatement.close();
                sqliteInsertStatement.close();
                mysqlResult.close();
                mysqlStatement.close();
            }
        } catch (SQLException e) {
            logger.error("生成底层配方数据时发生错误，信息如下：", e);
            return "";
        } finally {
            try {
                sqliteConn.close();
            } catch (SQLException e) {
                logger.error("生成底层配方数据时发生错误，信息如下：", e);
                return "";
            }

            try {
                mysqlConn.close();
            } catch (SQLException e) {
                logger.error("生成底层配方数据时发生错误，信息如下：", e);
                return "";
            }
        }

        return dbPath;
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        new SQLiteUtil().createRecipeConfig("", "", "");
    }
}
