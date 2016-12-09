package com.iot.serviceImp;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.iot.exception.BussinessException;
import com.iot.mapper.TableDeviceMapper;
import com.iot.mapper.UserGatewayMapper;
import com.iot.pojo.TableDevice;
import com.iot.pojo.UserGateway;
import com.iot.service.GateWayService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by xulingo on 16/4/7.
 */

@Service
public class GateWayServiceImpl implements GateWayService {


    @Resource
    private UserGatewayMapper userGatewayMapper;

    @Resource
    private TableDeviceMapper tableDeviceMapper;

    public int insert(UserGateway userGateway) {

        return  userGatewayMapper.insertSelective(userGateway);
    }

    //查询网关下的设备列表
    public JSONArray findGatewayOfDevice(String gatewayId,String userId) {
        List<UserGateway> list=userGatewayMapper.selectByUserId(userId);
        if (list.size()<1){
            throw new BussinessException("-1","user does not exist");
        }

        List<TableDevice> tableDeviceList=tableDeviceMapper.selectGatewayOfDevice(gatewayId);
        if (tableDeviceList.size()<1){
            throw new BussinessException("-1","the gatewayId not exist");
        }

        JSONArray result=(JSONArray)JSONArray.toJSON(tableDeviceList);
        JSONArray results=new JSONArray();
        for (int i=0;i<result.size();i++){
            JSONObject temp=(JSONObject) result.get(i);

            if (!temp.get("device_type").toString().equals("gateway")){
                results.add(temp);
            }
        }
        return results;
    }
}
