package com.iot.dbUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * Created by surfacepc on 2016/3/16.
 */
public class PropsUtil {

    private Properties props;

    private InputStream isp;

    public PropsUtil(String fileName){
        props = new Properties();
        isp = PropsUtil.class.getClassLoader().getResourceAsStream(fileName);
    }

    public String get(String arg) throws IOException {
        props.load(new InputStreamReader(isp, "utf-8"));
        return props.getProperty(arg);
    }
}
