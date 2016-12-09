package com.iot.serviceImp;

import com.iot.mapper.AccountDataInfoMapper;
import com.iot.pojo.AccountDataInfo;
import com.iot.service.AccountInfoService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by xulingo on 16/4/13.
 */

@Service
public class AccountInfoServiceImpl implements AccountInfoService{

    @Resource
    private AccountDataInfoMapper accountDataInfoMapper;

    public AccountDataInfo selectLastAddr(AccountDataInfo accountDataInfo) {

        return  accountDataInfoMapper.selectByAccountIdAndGatewayId(accountDataInfo);

    }

    public int updataAddrInfo(AccountDataInfo accountDataInfo) {

        return  accountDataInfoMapper.updateByAccountIdAndGatewayId(accountDataInfo);
    }


}
