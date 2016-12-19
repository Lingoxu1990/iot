package com.iot.newEditionServiceImpl;

import com.iot.exception.ParameterException;
import com.iot.mapper.TableChannelMapper;
import com.iot.mapper.TableDeviceMapper;
import com.iot.mapper.TableSensorRecordMapper;
import com.iot.newEditionService.RealTimeSensorDataAccpetService;
import com.iot.pojo.TableChannel;
import com.iot.pojo.TableDevice;
import com.iot.pojo.TableSensorRecord;
import org.apache.poi.util.StringUtil;
import org.springframework.stereotype.Component;
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
    @Resource
    private TableChannelMapper tableChannelMapper;

    public int insertMany(List<TableSensorRecord> list) {

        return tableSensorRecordMapper.insertMany(list);
    }


    public int replaceMany(List<TableDevice> list){

        if (list!=null && list.size()!=0){
            tableDeviceMapper.deleteByGatewayId(list.get(0));
        }
        int result = tableDeviceMapper.insertMany(list);

        return result;
    }

    @Override
    public int replaceclassMany(List<TableChannel> list) {

        if (list!=null && list.size()!=0){
            tableChannelMapper.deleteByGatewayId(list.get(0));
        }
        int result = tableChannelMapper.insertMany(list);

        return result;
    }

    @Override
    public int insertManyDevices(List<TableDevice> list) {

        int result = tableDeviceMapper.insertMany(list);
        return result;
    }

    @Override
    public int insertManyClasses(List<TableChannel> list) {
        int result = tableChannelMapper.insertMany(list);
        return result;
    }
}
