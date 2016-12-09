package com.iot.pojo;

public class PrivateRecipe {
    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column private_recipe.private_recipe_id
     *
     * @mbggenerated
     */
    private String private_recipe_id;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column private_recipe.account_id
     *
     * @mbggenerated
     */
    private String account_id;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column private_recipe.crop_name
     *
     * @mbggenerated
     */
    private String crop_name;

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column private_recipe.private_recipe_id
     *
     * @return the value of private_recipe.private_recipe_id
     *
     * @mbggenerated
     */
    public String getPrivate_recipe_id() {
        return private_recipe_id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column private_recipe.private_recipe_id
     *
     * @param private_recipe_id the value for private_recipe.private_recipe_id
     *
     * @mbggenerated
     */
    public void setPrivate_recipe_id(String private_recipe_id) {
        this.private_recipe_id = private_recipe_id == null ? null : private_recipe_id.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column private_recipe.account_id
     *
     * @return the value of private_recipe.account_id
     *
     * @mbggenerated
     */
    public String getAccount_id() {
        return account_id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column private_recipe.account_id
     *
     * @param account_id the value for private_recipe.account_id
     *
     * @mbggenerated
     */
    public void setAccount_id(String account_id) {
        this.account_id = account_id == null ? null : account_id.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column private_recipe.crop_name
     *
     * @return the value of private_recipe.crop_name
     *
     * @mbggenerated
     */
    public String getCrop_name() {
        return crop_name;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column private_recipe.crop_name
     *
     * @param crop_name the value for private_recipe.crop_name
     *
     * @mbggenerated
     */
    public void setCrop_name(String crop_name) {
        this.crop_name = crop_name == null ? null : crop_name.trim();
    }

    private String create_time;

    public String getCreate_time() {
        return create_time;
    }

    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }
}