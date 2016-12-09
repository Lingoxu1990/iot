package com.iot.listener;

import com.alibaba.fastjson.JSONObject;
import com.iot.dbUtil.MysqlUtil;
import com.iot.dbUtil.PropsUtil;
import com.iot.dbUtil.SQLiteUtil;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

/**
 * Created by xulingo on 16/4/23.
 */
public class DataBaseSynchronize implements Runnable {


    private static Logger logger = Logger.getLogger(DataBaseSynchronize.class);

    private static final String FTP_PATH="/mnt/ftpfiles";

    public void run() {
        try {
            String prefix = null;

            PropsUtil configProps = new PropsUtil("config.properties");

            try {
                prefix = configProps.get("prefix");
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("主目录路径文件读取失败");
            }

            //获取用户信息

            List<JSONObject> accountInfos = MysqlUtil.getAccountFilesInfoAndRecordInfo();

            for (JSONObject accountInfo : accountInfos) {

                System.out.println(accountInfo.toString());
                logger.debug(accountInfo.toString());

                String accountId = (String) accountInfo.get("account_id");
                String gateway_id = (String) accountInfo.get("gateway_id");
                logger.debug(gateway_id);
                System.out.println(gateway_id);
                //用户文件夹与用户id同名,拼接为临时存储文件夹路径
                String accountPath = prefix + File.separator + accountId;
                //打开用户文件夹
                File file = new File(accountPath);

                if (!file.exists()) {
                    file.mkdirs();
                    continue;
                }
                File[] dbfiles = file.listFiles();

                for (File dbfile : dbfiles) {

                    // 排除mac下的系统文件
                    if (".DS_Store".equals(dbfile.getName())) {
                        continue;
                    }
                    //排除非这个网关下的源数据
                    if (!dbfile.getName().contains(gateway_id)) {
                        continue;
                    }

                    //获取db文件中的所有表名
                    String[] tableNames;

                    try {

                        //获取db文件下包含的表名
                        String basis;
                        if (dbfile.getName().contains("Basis_Data.db")) {
                            basis = configProps.get("Basis_Data.db");
                        } else {
                            basis = configProps.get("Record_Data.db");
                        }

                        tableNames = basis.split(",");

                        for (int i = 0; i <tableNames.length ; i++) {
                            System.out.println(tableNames[i]);
                        }

                        //获取db文件的绝对路径
                        String absolutePath = dbfile.getAbsolutePath();

                        String [] absolutePathBits = absolutePath.split(File.separator);


                        String [] ftpPathBits = FTP_PATH.split(File.separator);


                        for (int i = 0; i <ftpPathBits.length ; i++) {
                            absolutePathBits[i]=ftpPathBits[i];
                        }

                        String dbPath = "/";


                        for (int i = 1; i <absolutePathBits.length ; i++) {

                            if(i==(absolutePathBits.length-1)){
                                dbPath+=absolutePathBits[i];
                            }else {
                                dbPath+=absolutePathBits[i]+File.separator;
                            }


                        }
                        logger.debug(dbPath);
                        System.out.println(dbPath);

                        Map<String, String[]> results;
                        try{
                            //获取SQL查询结果,accountInfo 包含了增量表的数据最后更新时间
                            results = SQLiteUtil.getAllTableData(tableNames, dbPath, accountInfo);
                            //mysql更新表数据
                            for (Map.Entry<String, String[]> entry : results.entrySet()) {
                                MysqlUtil.sqlexcute(entry.getValue(), entry.getKey(), accountId, gateway_id);
                            }
                        }catch (SQLException e){
                            e.printStackTrace();
                            if (e.getMessage().contains("database disk image is malformed"))
                            continue;
                        }


//                        //mysql更新表数据
//                        for (Map.Entry<String, String[]> entry : results.entrySet()) {
//                            MysqlUtil.sqlexcute(entry.getValue(), entry.getKey(), accountId, gateway_id);
//                        }
                        dbfile.delete();

                        BlockingQueue<JSONObject> jsonObjects = SQLiteUtil.jsonObjectList;

                        StringBuilder sql = new StringBuilder();
                        sql.append("UPDATE account_data_info Set ");
                        //mysql更新用户对应的表的最后更新数据的时间字段
                        int size = jsonObjects.size();


                        boolean flag = false;
                        for (int i = 0; i < size; i++) {
                            JSONObject jsonObject = jsonObjects.poll();
                            System.out.println(jsonObject.toString());
                            //用户id
                            String accout_id = (String) jsonObject.get("account_id");
                            //获取tablename
                            String tablename = (String) jsonObject.get("tablename");
                            String lastRecordTime = (String) jsonObject.get("lastRecordTime");
                            String gateway_id_ = (String) jsonObject.get("gateway_id");

                            //找到sql中用户id与本次更新用户id相同的数据
                            if (accout_id.equals(accountId) && gateway_id_.equals(gateway_id)) {
                                //生成sql执行语句

                                if (lastRecordTime != null && !"".equals(lastRecordTime)) {
                                    flag = true;
                                    sql.append(tablename + "=" + lastRecordTime);
                                }
                            }
                        }


                        if (!flag) {
                            continue;
                        }

                        sql.append(" WHERE account_id='" + accountId + "'" + " AND gateway_id ='" + gateway_id + "'");

                        String[] updatasql = new String[1];
                        updatasql[0] = sql.toString();
                        System.out.println(updatasql[0]);
                        MysqlUtil.sqlexcute(updatasql, "account_data_info", accountId, gateway_id);


                    } catch (IOException e) {
                        e.printStackTrace();
                        System.out.println("主目录路径文件读取失败");
                    }


                }


            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
