package com.iot.serviceImp;

import com.iot.mapper.AccountDataInfoMapper;
import com.iot.mapper.UserGatewayMapper;
import com.iot.mapper.UserTableMapper;
import com.iot.pojo.AccountDataInfo;
import com.iot.pojo.UserGateway;
import com.iot.pojo.UserTable;
import com.iot.service.LoginService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by xulingo on 16/4/6.
 */

@Service
public class LoginServiceImpl implements LoginService {

    @Resource
    private UserTableMapper userTableMapper;
    @Resource
    private UserGatewayMapper userGatewayMapper;
    @Resource
    private AccountDataInfoMapper accountDataInfoMapper;

    public UserTable login(String e_mail, String password) {

        UserTable userTable =  userTableMapper.selectByEmail(e_mail);

        return userTable;
    }

    public List<AccountDataInfo> getUserInfo(String user_id) {

        List<UserGateway> userGateway = userGatewayMapper.selectByUserId(user_id);

        List<AccountDataInfo> list= accountDataInfoMapper.selectByAccountId(userGateway.get(0).getAccount_id());

        return list;
    }

    public List<UserGateway> geAccountId(String user_id) {

        List<UserGateway> userGateway = userGatewayMapper.selectByUserId(user_id);

        return userGateway ;
    }
}
