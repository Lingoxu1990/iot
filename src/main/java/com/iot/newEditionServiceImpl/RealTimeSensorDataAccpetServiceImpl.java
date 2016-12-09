package com.iot.newEditionServiceImpl;

import com.iot.exception.ParameterException;
import com.iot.mapper.TableDeviceMapper;
import com.iot.mapper.TableSensorRecordMapper;
import com.iot.newEditionService.RealTimeSensorDataAccpetService;
import com.iot.pojo.TableDevice;
import com.iot.pojo.TableSensorRecord;
import org.apache.poi.util.StringUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;

/**
 * Created by xulingo on 16/8/11.
 */


@Service
public class RealTimeSensorDataAccpetServiceImpl implements RealTimeSensorDataAccpetService {

    @Resource
    private TableSensorRecordMapper tableSensorRecordMapper;
    @Resource
    private TableDeviceMapper tableDeviceMapper;

    public int insertMany(List<TableSensorRecord> list) {

        return tableSensorRecordMapper.insertMany(list);
    }


    public int replaceMany(List<TableDevice> list){

        int result=0;
        for (TableDevice device :list) {
            if(device.getAccount_id()==null || "".equals(device.getAccount_id())){
                throw new ParameterException("-1","account_id does not exist");
            }
            if(device.getDevice_guid()==null || "".equals(device.getDevice_guid())){
                throw new ParameterException("-1","device_guid does not exist");
            }
            if (device.getGateway_id()==null || "".equals(device.getGateway_id())){
                throw new ParameterException("-1","gateway does not exist");
            }
            device.setId(UUID.randomUUID().toString());
            tableDeviceMapper.deleteByGuidAndAccountId(device);
            result+=tableDeviceMapper.insertSelective(device);
        }
        return result;
    }
}
