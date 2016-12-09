package com.iot.serviceImp;

import com.alibaba.fastjson.JSONObject;
import com.iot.mapper.AccountDataInfoMapper;
import com.iot.pojo.AccountDataInfo;
import com.iot.service.FtpService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * Created by xulingo on 16/4/10.
 */

@Service
public class FtpServiceImpl implements FtpService {


    @Resource
    private AccountDataInfoMapper accountDataInfoMapper;


    public int deleteInitData(String account_id) {

       return  accountDataInfoMapper.deleteByNullGateway(account_id);

    }

    public int updateByPrimaryKeySelective(AccountDataInfo accountDataInfo) {

        AccountDataInfo accountDataInfo1 = accountDataInfoMapper.selectByAccountIdAndGatewayId(accountDataInfo);

        if (accountDataInfo!=null){
            return accountDataInfoMapper.updateByAccountIdAndGatewayId(accountDataInfo);
        }else {
            accountDataInfo.setId(UUID.randomUUID().toString());
            return accountDataInfoMapper.insertSelective(accountDataInfo);
        }
    }

    public int addAccountDataInfo(AccountDataInfo accountDataInfo) {

        return accountDataInfoMapper.insertSelective(accountDataInfo);
    }

    public List<JSONObject> findAccountDataInfo() {
        List<AccountDataInfo> list=accountDataInfoMapper.findAll();

       List<JSONObject> result = new LinkedList<JSONObject>();
        for (AccountDataInfo a: list) {
            JSONObject j = (JSONObject) JSONObject.toJSON(a);
            result.add(j);
        }
        return result;
    }
}
