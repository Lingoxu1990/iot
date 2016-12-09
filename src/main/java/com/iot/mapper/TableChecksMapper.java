package com.iot.mapper;

import com.iot.pojo.TableChecks;

/**
 * Created by adminchen on 16/7/20.
 */
public interface TableChecksMapper {
    int updateChecks(String recode);
    TableChecks selectChecks();

}
