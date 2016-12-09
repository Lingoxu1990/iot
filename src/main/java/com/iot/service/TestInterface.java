package com.iot.service;

import com.iot.pojo.TableCdtsList;

/**
 * Created by liusheng on 16/2/17.
 */
public interface TestInterface {
    public TableCdtsList searchId(String guId);

    public int updateTableCdsList(TableCdtsList cdsList);

    public int deleteTableCdsList(TableCdtsList cdsList);

    public int addTableCdsList(TableCdtsList cdsList);
}
