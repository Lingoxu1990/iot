package com.iot.newEditionServiceImpl;

import com.iot.mapper.TableDeviceMapper;
import com.iot.mapper.TableSensorRecordMapper;
import com.iot.newEditionService.RealTimeSensorDataAccpetService;
import com.iot.pojo.TableDevice;
import com.iot.pojo.TableSensorRecord;
import org.apache.poi.util.StringUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

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

    @Resource
    public int replaceMany(List<TableDevice> list){

        int result=0;
        for (TableDevice device :list) {
            if(device.getId()!=null && !"".equals(device.getId())){
                tableDeviceMapper.deleteByPrimaryKey(device.getId());
                result+=tableDeviceMapper.insertSelective(device);
            }
        }
        return result;
    }
}
