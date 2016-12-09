package com.iot.mapper;

import com.iot.pojo.TableDevice;

import java.util.List;

public interface TableDeviceMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table table_device
     *
     * @mbggenerated
     */
    int deleteByPrimaryKey(String id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table table_device
     *
     * @mbggenerated
     */
    int insert(TableDevice record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table table_device
     *
     * @mbggenerated
     */
    int insertSelective(TableDevice record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table table_device
     *
     * @mbggenerated
     */
    TableDevice selectByPrimaryKey(String id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table table_device
     *
     * @mbggenerated
     */
    int updateByPrimaryKeySelective(TableDevice record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table table_device
     *
     * @mbggenerated
     */
    int updateByPrimaryKey(TableDevice record);

    List<TableDevice> selectByAccountId(String account_id);

    //jim

    TableDevice selectByDevice_guid(String device_guid);

    int updateDevice_guidAndAccount_idAndDevice_addr(TableDevice tableDevice);

    /**
     *
     * @param record
     * @return
     */
    TableDevice selectByDevice_guidAndAccount_id(TableDevice record);

    TableDevice selectByDevice_guidAndAccount_idAndGateway_id(TableDevice record);

    List<TableDevice> findTheSensor(TableDevice record);

    int updateGateway_idAndAccount_idAndDevice_addr(TableDevice record);

    int updateGateway_idAndAccount_idAndDevice_guid(TableDevice record);

    TableDevice selectByDevice_guidAndAccount_idAndGateway_idas(TableDevice record);

    List<TableDevice> selectGatewayOfDevice(String gatewayId);

    int updateByDeviceName(TableDevice tableDevice);

    int deleteByGuidAndAccountId(TableDevice tableDevice);
}