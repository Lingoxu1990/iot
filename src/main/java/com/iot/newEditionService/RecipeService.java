package com.iot.newEditionService;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.iot.message.Message;
import com.iot.pojo.PrivateRecipe;
import com.iot.pojo.PrivateRecipeData;
import com.iot.pojo.PrivateRecipeIndex;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Jacob on 2016-04-07.
 */
public interface RecipeService {

    Message modifyRecipe(JSONObject jsonObject, String DestinationID, String SourceID, int packegType);//    修改配方
    Message findRecipe(JSONObject jsonObject, String DestinationID, String SourceID, int packegType);//    获取配方信息
    Message addRecipe(JSONObject jsonObject, String DestinationID, String SourceID, int packegType); //  添加配方
    Message deleteRecipe(JSONObject jsonObject, String DestinationID, String SourceID, int packegType);//  删除配方
    JSONObject startRecipe(JSONObject jsonObject, String DestinationID, String SourceID, int packegType) throws IOException;

    /***
     * 为底层提供配方需要的sqlite数据文件
     * @param userId
     * @param regionId
     * @param recipeId
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    String createRecipeConfig(String userId, String regionId, String recipeId) throws IOException, ClassNotFoundException;

    /***
     * 将配方应用到区域，并下发命令给网关，使得网关可以进行后续步骤
     * @param userId
     * @param regionId
     * @param gatewayId
     * @param recipeId
     */
    void recipePrivateStart(String userId, String regionId, String gatewayId, String recipeId);

    /***
     * 将配方取消，并下发命令给网关，使得网关可以进行后续步骤
     * @param userId
     * @param regionId
     * @param gatewayId
     * @param recipeId
     */
    void recipePrivateStopt(String userId, String regionId, String gatewayId, String recipeId);

    /***
     * 更新配方运行状态
     * @param privateRecipeIndex
     */
    int updateStatus(PrivateRecipeIndex privateRecipeIndex);

    /**
     *
     * @param user_id
     *
     * @return
     */
    JSONArray findPublicRecipes(String user_id);

    JSONArray findPrivateRecipeList(String user_id);

    void addPrivateRecipe(String user_id,String public_recipe_id);

    JSONArray findPrivateRecipeDetil(String user_id,String private_recipe_id);

    void addUserRecipeRegion(String user_id,PrivateRecipeIndex privateRecipeIndex);

    JSONArray findRegionRecipe(String user_id,PrivateRecipeIndex privateRecipeIndex);

    JSONObject findRegionAndSensorRecords(String user_id, String region_guid, String gateway_id,int size,String startTime,String endTime);
    JSONObject findRegionAndSensorRecordses(String user_id, String region_guid, String gateway_id,int size,String startTime,String endTime,int days);


    void modifyUserRecipe(String userId, List<PrivateRecipeData> privateRecipeDataList);

    void dropUserRecipe(String userId,PrivateRecipeData privateRecipeData);

    void  addUserRecipeName(String userId, PrivateRecipe privateRecipe);

    void  addUserbatchPrcipes(String userId, LinkedList<PrivateRecipeData> privateRecipeDataLinkedList,String days,String privateRecipeId,String fewDays);

    void dropUserbatchPrcipes(String userId,String startDay,String endDay,String privateRecipeId);

    void modifyUserRecipeName(String userId,String privateRecipeId,String cropName);

    void modifyUserRecipebatchData(String userId,String privateRecipeId, LinkedList<PrivateRecipeData> recipeDataLinkedList,String startDay,String endDay);

    void deleteRegionRecipe(String userId,PrivateRecipeIndex privateRecipeIndex);

}
