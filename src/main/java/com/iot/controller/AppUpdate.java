package com.iot.controller;

import com.alibaba.fastjson.JSONObject;
import com.iot.dbUtil.MysqlUtil;
import com.iot.exception.BussinessException;
import com.iot.message.Message;
import com.iot.newEditionService.AppInfoService;
import com.iot.pojo.AppConfig;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import static com.iot.requestUtil.RequestCaseUtil.requestGetCase;
import static com.iot.requestUtil.RequestCaseUtil.requestPostCase;

/**
 * Created by xulingo on 16/5/31.
 */
@Controller
@RequestMapping("update")
public class AppUpdate {

    @Resource
    private AppInfoService appInfoService;

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public Object update(HttpServletRequest httpServletRequest){

        Message message =new Message();

        String platform = httpServletRequest.getParameter("platform");
        String current_version =httpServletRequest.getParameter("version");
        String app_name = httpServletRequest.getParameter("app_name");

        String lastestReleaseVersionPrex=AppConfig.LASTEST_RELEASE_VERSION_PREX;
        String commemt_prex = AppConfig.COMMENT_PREX;
        String downLoadPrx = AppConfig.DOWN_LOAD_PRX;

        String androidReleaseId=AppConfig.ANDROID_RELEASE_ID;
        String iosReleaseId=AppConfig.IOS_RELEASE_ID;
        String releaseTokenId =AppConfig.RELEASE_TOKEN_ID;
        //获取
        String lastest_version =appInfoService.getLastVersion(app_name,platform);

        if (lastest_version.equals("")){

            throw new BussinessException("-1","Failed to Update the App : There is no Record in the DataBase!");
        }
        message.setCode("0");
        message.setMessage("Update the app successfully!");
        try {
            JSONObject content = new JSONObject();
            String versionName ="V";
            for (int i = 0; i <lastest_version.length() ; i++) {
                versionName+= lastest_version.charAt(i)+".";
            }
            if(Integer.parseInt(lastest_version)<=Integer.parseInt(current_version)){
                message.setCode("0");
                message.setMessage("Now is the latest version");
                content.put("versionName", versionName);
                content.put("desc", "");
                content.put("android", "");
                content.put("desc","");
                content.put("ios","");
                content.put("upgrade",false);
                message.setContent(content);
                return message;
            }
            
            StringBuilder SB = new StringBuilder();
            SB.append(versionName);
            SB.deleteCharAt(versionName.length()-1);
            content.put("versionName",versionName);
            content.put("desc",AppConfig.WELLCOME_WORD);

            if(platform.equals("android")){
                //获取andriod_download_token
                JSONObject json_down_load_token_android =JSONObject.parseObject(requestGetCase(commemt_prex+androidReleaseId+"/download_token?api_token="+releaseTokenId));

                String down_load_token_android = (String) json_down_load_token_android.get("download_token");

                //获取下载地址
                JSONObject json_android_download_url = JSONObject.parseObject(requestPostCase(downLoadPrx+androidReleaseId+"/install?download_token="+down_load_token_android,""));
                String android_download_url=(String) json_android_download_url.get("url");
                content.put("android",android_download_url);

                content.put("ios","");
            }else{
                //获取ios_download_token
                JSONObject json_down_load_token_iod = JSONObject.parseObject(requestGetCase(commemt_prex+iosReleaseId+"/download_token?api_token="+releaseTokenId));
                String down_load_token_ios = (String) json_down_load_token_iod.get("download_token");
                //获取plist
                JSONObject json_ios_plist_id =JSONObject.parseObject(requestPostCase(downLoadPrx+iosReleaseId+"/install?download_token="+down_load_token_ios,""));
                String plist_id=(String) json_ios_plist_id.get("url");
                content.put("ios","itms-services://?action=download-manifest&url=https://fir.im/plists/"+plist_id);
                content.put("android","");
            }
            int int_current_version = Integer.parseInt(current_version);
            int int_lastest_version = Integer.parseInt(lastest_version);
            if (int_current_version<int_lastest_version){
                content.put("upgrade",true);
            }else {
                content.put("upgrade",false);
            }
            message.setContent(content);

        }catch (Exception e){
            e.printStackTrace();
            throw new BussinessException("-1","Failed to Update the App : The WEB API is Error, please try later!");
        }

        return message;
    }


}
