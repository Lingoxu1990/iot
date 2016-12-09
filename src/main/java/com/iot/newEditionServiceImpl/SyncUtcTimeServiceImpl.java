package com.iot.newEditionServiceImpl;

import com.iot.mapper.TableSensorRecordMapper;
import com.iot.newEditionService.SyncUycTimeService;
import com.iot.pojo.TableSensorRecord;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static com.iot.utils.GetUTCTimeUtil.getUTCTimeStr;

/**
 * Created by xulingo on 16/9/23.
 */

@Service
public class SyncUtcTimeServiceImpl implements SyncUycTimeService {


    @Resource
    TableSensorRecordMapper tableSensorRecordMapper;

    public void updateRecordTime(String date) throws ParseException {

        HashMap<String,String> map = new HashMap<String, String>();
        map.put("start",date+" 00:00:00");
        map.put("end",date+" 23:59:59");


        int total = tableSensorRecordMapper.getAllRecordNumber(map);
        System.out.println(total);

        int pageSize=3000;



        int remainder = total%pageSize;


        int index = total/pageSize;


        for (int i = 0; i <index+1 ; i++) {


            int num =(i+1)*pageSize-pageSize;
            int size =pageSize;

            map.put("num",String.valueOf(num));
            map.put("size",String.valueOf(size));

            List<TableSensorRecord> list = tableSensorRecordMapper.selectDailyData(map);


            for (TableSensorRecord record: list) {
                String record_time =  record.getRecord_time();
                System.out.println(record_time);

                Calendar cal =  Calendar.getInstance();

                SimpleDateFormat simpleDateFormat =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                Date recordTime = simpleDateFormat.parse(record_time);


                cal.setTime(recordTime);
                String utcTime = getUTCTimeStr(cal);
                System.out.println("utc time: "+ utcTime);
                record.setRecord_time(utcTime);
            }


            tableSensorRecordMapper.updateRecordTime(list);


        }



//        List<TableSensorRecord> list = tableSensorRecordMapper.selectDailyData(map);
//
//
//        for (TableSensorRecord record: list) {
//            String record_time =  record.getRecord_time();
//
//            Calendar cal =  Calendar.getInstance();
//
//            SimpleDateFormat simpleDateFormat =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//
//            Date recordTime = simpleDateFormat.parse(record_time);
//
//
//            cal.setTime(recordTime);
//            String utcTime = getUTCTimeStr(cal);
//            record.setRecord_time(utcTime);
//        }
//
//        tableSensorRecordMapper.updateRecordTime(list);


    }
}
