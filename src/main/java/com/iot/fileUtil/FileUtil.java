package com.iot.fileUtil;



import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.iot.dbUtil.PropsUtil;
import org.apache.log4j.Logger;

import javax.servlet.ServletInputStream;
import java.io.*;

/**
 * Created by admin on 15/12/25.
 */
public class FileUtil {
    private static Logger logger =Logger.getLogger(FileUtil.class);


    public static boolean  saveFile(ServletInputStream inputStream, int size,Long time) {

        ServletInputStream servletInputStream=inputStream;

        String prefix= null;

        PropsUtil configProps = new PropsUtil("config.properties");

        try {
            prefix = configProps.get("prefix");
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("timestap : "+System.currentTimeMillis()+" failed to read the main root");
        }


        byte[] buffer = new byte[size]; // 用于缓存每次读取的数据
        byte[] result = new byte[size]; // 用于存放结果的数组


        int count = 0;
        int rbyte = 0;
        // 循环读取
        while (count < size)
        {
            try {
                rbyte = servletInputStream.read(buffer); // 每次实际读取长度存于rbyte中 sflj
            } catch (IOException e) {
                e.printStackTrace();
                logger.debug("timestap : "+System.currentTimeMillis()+" failed to save the binary message");
            }

            for (int i = 0; i < rbyte; i++)
            {
                result[count + i] = buffer[i];
            }
            count += rbyte;
        }

        int index = 0;
        for (int i = 0; i < result.length; i++)
        {
            byte b = result[i];

            if (b == '|')
            {
                index = i;
                break;
            }
        }
        // 存放文件名
        byte name[] = new byte[index];
        // 存放文件字节
        byte[] img = new byte[size - index];
        for (int i = 0; i < result.length; i++)
        {
            if (i < index)
            {
                name[i] = result[i];
            }
            if (i > index)
            {
                // 这时注意img数组的index要从0开始
                img[i - index - 1] = result[i];
            }
        }
        logger.debug("timestap : " +System.currentTimeMillis()+" binary message save is Ok,create the File inputSteam");


        String absluePath=new String(name);//该部分为文件的绝对路径
        logger.debug("timestap : " +System.currentTimeMillis()+" absPath : "+absluePath);
        String []  paths = absluePath.split("/");

        String fileName = paths[paths.length-1];

        logger.debug("timestap : " +System.currentTimeMillis()+" fileName : "+fileName);

        String dirname = paths[paths.length-2];

        logger.debug("timestap : " +System.currentTimeMillis()+" fatherFileName : "+dirname);


        File account = new File(prefix+File.separator+dirname);

        if (!account.exists()){
            account.mkdirs();
        }

        String outname = prefix+File.separator+dirname+File.separator+fileName;

        logger.debug("timestap : " +System.currentTimeMillis()+" FileOutPutPath : "+outname);

        try {
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("timestap : " +System.currentTimeMillis()+" Failed to closed the ServletInputStream ");
        }


            File file = new File(outname);


            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(file);
                fos.write(img);
                fos.flush();
                fos.close();
//            File newfile  = new File(outname);
//
//            newfile.setLastModified(time);

            } catch (Exception e) {
                e.printStackTrace();
                logger.error("timestap : " + System.currentTimeMillis() + " Failed to save the db file ");
                return false;

            }


        return true;
    }

    public static String []  getFilePath(String accountId){

        String prefix= null;
        String [] result= null;

        PropsUtil configProps = new PropsUtil("config.properties");

        try {
            prefix = configProps.get("prefix");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("主目录路径文件读取失败");
        }



        String dirname = prefix +File.separator + accountId;

        File dir = new File(dirname);

        if (!dir.exists()){
            dir.mkdirs();
        }

        File [] files =  dir.listFiles();
        result = new String[files.length];

        for (int i = 0; i <files.length ; i++) {

            result[i]= files[i].getAbsolutePath();

        }


        return result;
    }

    public static JSONArray getAllFile(){

        String prefix="";

        PropsUtil configProps = new PropsUtil("config.properties");

        try {
            prefix = configProps.get("prefix");
        } catch (IOException e) {
            e.printStackTrace();
        }

        File file = new File(prefix);


        if(!file.exists()){
            file.mkdir();
        }

        File []  accountDirs = file.listFiles();



        if (accountDirs.length==0){
            return null;
        }
        JSONArray arr = new JSONArray();


        for (File account: accountDirs ) {

            if (".DS_Store".equals(account.getName())){
                continue;
            }
            for (File dbfile : account.listFiles()) {

                if (".DS_Store".equals(dbfile.getName())){
                    continue;
                }

                String name = dbfile.getParentFile().getName()+File.separator +dbfile.getName();
                long time = dbfile.lastModified();
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("name",name);
                jsonObject.put("time",time);
                arr.add(jsonObject);

            }


        }

        return arr;
    }

}
