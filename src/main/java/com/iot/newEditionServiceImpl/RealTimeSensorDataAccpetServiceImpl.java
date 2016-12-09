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

    @Override
    public int replaceclassMany(List<TableChannel> list) {
        int result=0;
        for (TableChannel channel :list) {
            if(channel.getAccount_id()==null || "".equals(channel.getAccount_id())){
                throw new ParameterException("-1","account_id does not exist");
            }
            if(channel.getChannel_guid()==null || "".equals(channel.getChannel_guid())){
                throw new ParameterException("-1","channel_guid does not exist");
            }
            if (channel.getGateway_id()==null || "".equals(channel.getGateway_id())){
                throw new ParameterException("-1","gateway does not exist");
            }
            channel.setId(UUID.randomUUID().toString());
            tableChannelMapper.deleteByGuidAndAccountId(channel);
            result+=tableChannelMapper.insertSelective(channel);
        }
        return result;
    }
}
