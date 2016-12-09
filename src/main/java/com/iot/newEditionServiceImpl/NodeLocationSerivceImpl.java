package com.iot.newEditionServiceImpl;

import com.iot.mapper.DispatcherLocationMapper;
import com.iot.mapper.TableDeviceMapper;
import com.iot.newEditionService.NodeLocationService;
import com.iot.pojo.DispatcherLocation;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by xulingo on 16/8/19.
 */

@Service
public class NodeLocationSerivceImpl implements NodeLocationService {

    @Resource
    DispatcherLocationMapper dispatcherLocationMapper;
    @Resource
    TableDeviceMapper tableDeviceMapper;


    public int initDispatcher(List<DispatcherLocation> list) {

        return dispatcherLocationMapper.insertMany(list);

    }

    public int updateDispather(List<DispatcherLocation> list) {
        return 0;
    }

    public int delete(List<DispatcherLocation> list) {
        return dispatcherLocationMapper.delete(list);
    }

    public DispatcherLocation getDispatcher(String subGatewayId) {

//        tableDeviceMapper.selectGatewayOfDevice(subGatewayId);
//
//        return null;

        return dispatcherLocationMapper.selectByGatewayId(subGatewayId);
    }
}
