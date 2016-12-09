package com.iot.mapper;

import com.iot.pojo.TableRegionGroup;

import java.util.List;

public interface TableRegionGroupMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table table_region_group
     *
     * @mbggenerated
     */
    int deleteByPrimaryKey(String id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table table_region_group
     *
     * @mbggenerated
     */
    int insert(TableRegionGroup record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table table_region_group
     *
     * @mbggenerated
     */
    int insertSelective(TableRegionGroup record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table table_region_group
     *
     * @mbggenerated
     */
    TableRegionGroup selectByPrimaryKey(String id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table table_region_group
     *
     * @mbggenerated
     */
    int updateByPrimaryKeySelective(TableRegionGroup record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table table_region_group
     *
     * @mbggenerated
     */
    int updateByPrimaryKey(TableRegionGroup record);

    List<TableRegionGroup> findRegionGroupByAccountIdAndRegionId(TableRegionGroup record);

    int deleteGroup(TableRegionGroup record);
    int updateByAccountIdAndGroupGuid(TableRegionGroup record);

    TableRegionGroup findRegionGroupByAccountIdAndGroupId(TableRegionGroup record);

    /**
     *
     * @param record
     * @return
     */
    int deleteGroupOfAccount_idAndGroup_guidAndAddr(TableRegionGroup record);

    TableRegionGroup findRegionGroupByAccountIdAndGroupaddrAndGatewayid(TableRegionGroup record);

    int deleteGroupOfAccount_idAndgateway_idAndAddr(TableRegionGroup record);

    List<TableRegionGroup> findRegionGroupByAccountIdAndRegionguidAndGatewayid(TableRegionGroup record);

    int deleteRegionGroupOfAccount_idAndGroup_guidAndgatewayId(TableRegionGroup record);

    int deleteRegionGroupOfAccount_idAndregion_guidAndgatewayId(TableRegionGroup record);

    List<TableRegionGroup> findRegionGroupByAccountIdAndGroupGuidAndGatewayid(TableRegionGroup record);

    List<TableRegionGroup> findRegionGroupByGroupAddr(TableRegionGroup record);

    TableRegionGroup findRegionGroupByGroupGuid(TableRegionGroup record);

    TableRegionGroup findRegionGroupByAccountIdAndGroupGuidAndGatewayidAndRegionGuid(TableRegionGroup record);

    int updateByAccountIdAndGroupGuidAndGatewayId(TableRegionGroup record);


}