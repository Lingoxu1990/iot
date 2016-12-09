package com.iot.newEditionService;

import com.iot.pojo.DispatcherLocation;

import java.util.List;

/**
 * Created by xulingo on 16/8/19.
 */
public interface NodeLocationService {

    int initDispatcher(List<DispatcherLocation> list);

    int updateDispather(List<DispatcherLocation> list);

    int delete(List<DispatcherLocation> list);
    DispatcherLocation getDispatcher(String subGatewayId);

}
