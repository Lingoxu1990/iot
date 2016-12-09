package com.iot.utils;

import com.iot.dbUtil.PropsUtil;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Zip;
import org.apache.tools.ant.types.FileSet;

import java.io.File;
import java.io.IOException;

/**
 * Created by xulingo on 16/8/19.
 */

public class MyZipUtil {


    private File zipFile;
    private String firmarePath;

    public MyZipUtil(String uuid) throws IOException {

        PropsUtil props = new PropsUtil("config.properties");

        firmarePath = props.get("firmware");

        zipFile = new File(firmarePath+File.separator+uuid+".zip");
    }

    public File compress() {

        String srcPathName = firmarePath;

        File srcdir = new File(srcPathName);
        if (!srcdir.exists())
            throw new RuntimeException(srcPathName + "不存在！");

        Project prj = new Project();
        Zip zip = new Zip();
        zip.setProject(prj);
        zip.setDestFile(zipFile);
        FileSet fileSet = new FileSet();
        fileSet.setProject(prj);
        fileSet.setDir(srcdir);
        //fileSet.setIncludes("**/*.java"); 包括哪些文件或文件夹 eg:zip.setIncludes("*.java");
        //fileSet.setExcludes(...); 排除哪些文件或文件夹
        zip.addFileset(fileSet);

        zip.execute();

        return zipFile;
    }






}
