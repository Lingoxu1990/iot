package com.iot.mapper;

import com.iot.pojo.UserTable;

import java.util.List;

public interface UserTableMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table user_table
     *
     * @mbggenerated
     */
    int deleteByPrimaryKey(String user_id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table user_table
     *
     * @mbggenerated
     */
    int insert(UserTable record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table user_table
     *
     * @mbggenerated
     */
    int insertSelective(UserTable record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table user_table
     *
     * @mbggenerated
     */
    UserTable selectByPrimaryKey(String user_id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table user_table
     *
     * @mbggenerated
     */
    int updateByPrimaryKeySelective(UserTable record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table user_table
     *
     * @mbggenerated
     */
    int updateByPrimaryKey(UserTable record);

    UserTable selectByEmail(String e_mail);


    List<UserTable> findChildUsers(String user_id);

    UserTable findUsers(String user_id);

}