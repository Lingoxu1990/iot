package com.iot.serviceImp;

import com.iot.mapper.TableCdtsListMapper;
import com.iot.pojo.TableCdtsList;
import com.iot.service.TestInterface;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Created by liusheng on 16/2/17.
 */
@Service
public class TestInterfaceImpl implements TestInterface {

    @Resource
    private TableCdtsListMapper user;

    public TableCdtsList searchId(String num) {
        return user.selectByPrimaryKey(num);
    }

    public int updateTableCdsList(TableCdtsList cdsList) {
        return user.updateByPrimaryKey(cdsList);
    }

    public int deleteTableCdsList(TableCdtsList cdsList) {

        return user.deleteByPrimaryKey(cdsList.getCdts_list_guid());
    }

    public int addTableCdsList(TableCdtsList cdsList) {
        return user.insert(cdsList);
    }
}
