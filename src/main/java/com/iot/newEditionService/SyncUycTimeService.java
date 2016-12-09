package com.iot.newEditionService;

import java.text.ParseException;

/**
 * Created by xulingo on 16/9/23.
 */
public interface SyncUycTimeService {
    void updateRecordTime(String date) throws ParseException;

}
