package com.iot.pojo;

public class DeviceClass {
    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column device_class.id
     *
     * @mbggenerated
     */
    private String id;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column device_class.device_type
     *
     * @mbggenerated
     */
    private String device_type;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column device_class.main_param
     *
     * @mbggenerated
     */
    private String main_param;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column device_class.main_param_device_on
     *
     * @mbggenerated
     */
    private String main_param_device_on;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column device_class.main_param_device_value
     *
     * @mbggenerated
     */
    private String main_param_device_value;

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column device_class.id
     *
     * @return the value of device_class.id
     *
     * @mbggenerated
     */
    public String getId() {
        return id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column device_class.id
     *
     * @param id the value for device_class.id
     *
     * @mbggenerated
     */
    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column device_class.device_type
     *
     * @return the value of device_class.device_type
     *
     * @mbggenerated
     */
    public String getDevice_type() {
        return device_type;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column device_class.device_type
     *
     * @param device_type the value for device_class.device_type
     *
     * @mbggenerated
     */
    public void setDevice_type(String device_type) {
        this.device_type = device_type == null ? null : device_type.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column device_class.main_param
     *
     * @return the value of device_class.main_param
     *
     * @mbggenerated
     */
    public String getMain_param() {
        return main_param;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column device_class.main_param
     *
     * @param main_param the value for device_class.main_param
     *
     * @mbggenerated
     */
    public void setMain_param(String main_param) {
        this.main_param = main_param == null ? null : main_param.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column device_class.main_param_device_on
     *
     * @return the value of device_class.main_param_device_on
     *
     * @mbggenerated
     */
    public String getMain_param_device_on() {
        return main_param_device_on;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column device_class.main_param_device_on
     *
     * @param main_param_device_on the value for device_class.main_param_device_on
     *
     * @mbggenerated
     */
    public void setMain_param_device_on(String main_param_device_on) {
        this.main_param_device_on = main_param_device_on == null ? null : main_param_device_on.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column device_class.main_param_device_value
     *
     * @return the value of device_class.main_param_device_value
     *
     * @mbggenerated
     */
    public String getMain_param_device_value() {
        return main_param_device_value;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column device_class.main_param_device_value
     *
     * @param main_param_device_value the value for device_class.main_param_device_value
     *
     * @mbggenerated
     */
    public void setMain_param_device_value(String main_param_device_value) {
        this.main_param_device_value = main_param_device_value == null ? null : main_param_device_value.trim();
    }
}