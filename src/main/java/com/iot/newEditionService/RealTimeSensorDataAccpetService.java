package com.iot.newEditionService;

import com.iot.pojo.TableChannel;
import com.iot.pojo.TableDevice;
import com.iot.pojo.TableSensorRecord;

import java.util.List;

/**
 * Created by xulingo on 16/8/11.
 */
public interface RealTimeSensorDataAccpetService {

    int insertMany(List<TableSensorRecord> list);
    int replaceMany(List<TableDevice> list);
    int replaceclassMany(List<TableChannel> list);
}
