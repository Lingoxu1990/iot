package com.iot.service;

import com.alibaba.fastjson.JSONObject;
import com.iot.pojo.AccountDataInfo;

import java.util.List;

/**
 * Created by xulingo on 16/4/10.
 */
public interface FtpService {

    int deleteInitData(String account_id);
    int updateByPrimaryKeySelective(AccountDataInfo accountDataInfo);
    int addAccountDataInfo(AccountDataInfo accountDataInfo);
    List<JSONObject> findAccountDataInfo();

}
