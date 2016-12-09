package com.iot.service;

import com.iot.pojo.AccountDataInfo;
import com.iot.pojo.UserGateway;
import com.iot.pojo.UserTable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by surfacepc on 2016/3/15.
 */

public interface LoginService {

     UserTable login(String email , String password);

     List<AccountDataInfo> getUserInfo(String user_id);

     List<UserGateway> geAccountId(String user_id);


}
