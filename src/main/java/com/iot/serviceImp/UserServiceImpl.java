package com.iot.serviceImp;

import com.iot.exception.BussinessException;
import com.iot.mapper.AccountDataInfoMapper;
import com.iot.mapper.UserGatewayMapper;
import com.iot.mapper.UserTableMapper;
import com.iot.md5Util.Md5Util;
import com.iot.pojo.AccountDataInfo;
import com.iot.pojo.UserGateway;
import com.iot.pojo.UserTable;
import com.iot.service.UserService;
import com.iot.spitUtil.Param;
import com.sun.org.apache.bcel.internal.generic.IF_ACMPEQ;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

/**
 * Created by xulingo on 16/4/6.
 */

@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserTableMapper userTableMapper;
    @Resource
    private UserGatewayMapper userGatewayMapper;
    @Resource
    private AccountDataInfoMapper accountDataInfoMapper;


    public int insert(UserTable record) {


        // TODO: 16/6/12 将account_id在在user_gateway 表里实现account_id

        UserGateway userGateway = new UserGateway();
        userGateway.setUser_id(record.getUser_id());
        userGatewayMapper.insertSelective(userGateway);

        int id=userGateway.getId();

        String account_id_str=String.valueOf(id);

        int zeroLen = 8-account_id_str.length();

        String account_id = null;

        if (zeroLen==0){

            account_id=account_id_str;
        }else {
            account_id ="";
            for (int i = 0; i <zeroLen ; i++) {
                account_id+="0";
            }
            account_id+=account_id_str;

        }

        userGateway.setAccount_id(account_id);
        userGatewayMapper.updateByPrimaryKey(userGateway);


        if (record.getUser_authorization().equals("2")){
            AccountDataInfo accountDataInfo = new AccountDataInfo();
            accountDataInfo.setId(UUID.randomUUID().toString());
            accountDataInfo.setAccount_id(userGateway.getAccount_id());
            accountDataInfo.setRegion_addr("9001");
            accountDataInfo.setGroup_addr("1001");
            accountDataInfo.setSence_adde("a00a");
            accountDataInfoMapper.insertSelective(accountDataInfo);
            // TODO: 16/6/12 创建一个临时文件夹
        }

        ///System.out.println("e_mail:"+record.getE_mail());
        int num = userTableMapper.insert(record);

        return num;
    }
    //删除子用户
    public int deleteByPrimaryKey(String user_id) {
        int n=userGatewayMapper.deleteByUser_id(user_id);
        int a=userTableMapper.deleteByPrimaryKey(user_id);
        int num;
        if (n>0&&a>0){
            num=1;
        }else {
            num=0;
        }
        return  num;
    }

    public int updateByPrimaryKeySelective(UserTable record) {

        return userTableMapper.updateByPrimaryKeySelective(record);
    }

    public List<UserTable> findChildUsers(String user_id) {

        List<UserTable> list = userTableMapper.findChildUsers(user_id);

        return list;
    }

    public UserTable selectByPrimaryKey(String user_id) {
        return userTableMapper.selectByPrimaryKey(user_id);
    }

    public List<UserGateway> selectGatewayByUserId(String user_id) {

        List<UserGateway> list = userGatewayMapper.selectByUserId(user_id);
        return list;
    }

    public List<AccountDataInfo> getALLagateway(String account_id) {

        return accountDataInfoMapper.selectByAccountId(account_id);

    }

    public UserTable findUsers(String user_id){
        return  userTableMapper.findUsers(user_id);
    }

    public String getUserSourceId(String user_id) {
        List<UserGateway> list = userGatewayMapper.selectByUserId(user_id);

        if (list.size()<1){
            return null;
        }

        String accountId = list.get(0).getAccount_id();

        String SourceId = accountId + Param.WEBAPPID + String.valueOf(System.currentTimeMillis() / 1000000000);

        return SourceId;
    }

    //修改用户密码
    public void modifyUserPassWord(UserTable userTable) {
        String md5Pw= Md5Util.stringMd5(userTable.getPassword());
        userTable.setPassword(md5Pw);
        int n=userTableMapper.updateByPrimaryKeySelective(userTable);
        if (n<1){
            throw new BussinessException("-1","update user Pass word failed");
        }
    }
}
