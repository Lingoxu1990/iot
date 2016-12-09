package com.iot.newEditionServiceImpl;

import com.iot.mapper.AppInfoMapper;
import com.iot.newEditionService.AppInfoService;
import com.iot.pojo.AppInfo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Created by adminchen on 16/8/5.
 */
@Service
public class AppInfoServiceImpl implements AppInfoService{

    @Resource
    private AppInfoMapper appInfoMapper;


    public String getLastVersion(String app_name, String platform) {
        AppInfo appInfo=new AppInfo();
        appInfo.setApp_name(app_name);
        appInfo.setApp_type(platform);
        AppInfo appInfo1=appInfoMapper.selectByappInfo(appInfo);
        String lastest_version=appInfo1.getApp_version();
        return lastest_version;
    }
}
