package com.iot.service;

import com.iot.pojo.AccountDataInfo;
import com.iot.pojo.UserGateway;
import com.iot.pojo.UserTable;

import java.util.List;

/**
 * Created by xulingo on 16/4/6.
 */
public interface UserService {

    int insert(UserTable record);
    int deleteByPrimaryKey(String user_id);
    int updateByPrimaryKeySelective(UserTable record);
    List<UserTable> findChildUsers(String user_id);
    UserTable selectByPrimaryKey(String user_id);

    List<UserGateway> selectGatewayByUserId(String user_id);

    List<AccountDataInfo> getALLagateway(String user_id);

    UserTable findUsers(String user_id);

    String getUserSourceId(String user_id);

    void  modifyUserPassWord(UserTable userTable);


}
