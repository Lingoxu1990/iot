package com.iot.serviceImp;

import com.iot.mapper.TableChecksMapper;
import com.iot.pojo.TableChecks;
import com.iot.service.ChecksService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Created by adminchen on 16/7/20.
 */

@Service
public class ChecksServiceImpl implements ChecksService {

    @Resource
    private TableChecksMapper tableChecksMapper;

    public int updateChecks(String checks) {
        int n=tableChecksMapper.updateChecks(checks);
        return n;
    }

    public TableChecks selectChecks() {
//        int ids=Integer.parseInt(id);
        TableChecks tableChecks=tableChecksMapper.selectChecks();
        return tableChecks;
    }
}
