package com.iot.service;

import com.iot.pojo.TableChecks;

/**
 * Created by adminchen on 16/7/20.
 */
public interface ChecksService {
    int updateChecks(String checks);

    TableChecks selectChecks();
}
