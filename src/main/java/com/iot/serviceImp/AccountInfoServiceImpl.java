package com.iot.serviceImp;

import com.iot.mapper.AccountDataInfoMapper;
import com.iot.pojo.AccountDataInfo;
import com.iot.service.AccountInfoService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;

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

    @Override
    public int removeBankData(String accountId) {

        List<AccountDataInfo> accountDataInfoList = accountDataInfoMapper.selectByAccountId(accountId);

        if (accountDataInfoList.size()==0){
            return 0;
        }
        AccountDataInfo accountDataInfo = accountDataInfoList.get(0);

        if (accountDataInfoList.size()==1 &&
                (accountDataInfo.getGateway_id()==null || "".equals(accountDataInfo.getGateway_id()) )
                ){
            return accountDataInfoMapper.deleteByNullGateway(accountId);
        }else {
            return 0;
        }
    }

    @Override
    public int updateData(AccountDataInfo accountDataInfo) {

        AccountDataInfo searchResult = accountDataInfoMapper.selectByAccountIdAndGatewayId(accountDataInfo);

        if (searchResult==null){
            accountDataInfo.setBasis_data_file_last_modified("");
            accountDataInfo.setRecord_data_file_last_modified("");
            accountDataInfo.setTable_dcgs_record("");
            accountDataInfo.setTable_control_record("");
            accountDataInfo.setTable_sensor_record("");
            accountDataInfo.setId(UUID.randomUUID().toString());
            return accountDataInfoMapper.insertSelective(accountDataInfo);
        }

        return 0;
    }
}
