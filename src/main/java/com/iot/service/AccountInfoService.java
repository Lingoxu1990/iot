package com.iot.service;

import com.iot.pojo.AccountDataInfo;

import java.util.List;

/**
 * Created by xulingo on 16/4/13.
 */
public interface AccountInfoService {

    AccountDataInfo selectLastAddr(AccountDataInfo accountDataInfo);

    int updataAddrInfo(AccountDataInfo accountDataInfo);

}
