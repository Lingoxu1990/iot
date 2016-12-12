package com.iot.newEditionController;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.iot.exception.BussinessException;
import com.iot.exception.ParameterException;
import com.iot.message.Message;
import com.iot.message.MessageNoContent;
import com.iot.newEditionService.NodeLocationService;
import com.iot.newEditionService.RecipeService;
import com.iot.pojo.DispatcherLocation;
import com.iot.pojo.PrivateRecipe;
import com.iot.pojo.PrivateRecipeData;
import com.iot.pojo.PrivateRecipeIndex;
import com.iot.utils.ParamUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by black on 2016-07-29.
 */

@Controller
public class RecipeController {

    private static Logger logger = Logger.getLogger(RecipeController.class);

    @Resource
    private RecipeService recipeService;
    @Resource
    private NodeLocationService nodeLocationService;


    /**
     * 该接口用于 生成配方所需的db文件
     * @param user_id
     * @param region_id
     * @param recipe_id
     * @param response
     * @throws IOException
     * @throws ClassNotFoundException
     */
    @RequestMapping(value = "/recipe/dbdata", method = RequestMethod.GET)
    @ResponseBody
    public void getRecipeDbFile(String user_id, String region_id, String recipe_id, HttpServletResponse response) throws IOException, ClassNotFoundException {

        if (user_id == null || "".equals(user_id)) {
            throw new ParameterException("-1", "user_id does not exist");
        }

        if (region_id == null || "".equals(region_id)) {
            throw new ParameterException("-1", "region_id does not exist");
        }

        if (recipe_id == null || "".equals(recipe_id)) {
            throw new ParameterException("-1", "recipe_id does not exist");
        }

        response.setHeader("Content-Disposition", "attachment; filename=data.db");

        String dbFilePath = recipeService.createRecipeConfig(user_id, region_id, recipe_id);

        File file = new File(dbFilePath);

        //判断文件是否存在如果不存在就返回默认图标
        if (!(file.exists() && file.canRead())) {
            throw new BussinessException("-1", dbFilePath + " can not be readed");
        }

        FileInputStream inputStream = new FileInputStream(file);
        byte[] data = new byte[(int) file.length()];

        inputStream.read(data);//将文件中的内容读取到字节数组中
        inputStream.close();

        OutputStream stream = response.getOutputStream();
        stream.write(data);
        stream.flush();
        stream.close();

        file.delete();
    }


    /**
     * 该接口用于 下发配方开始的命令
     * @param request
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    @RequestMapping(value = "/recipe/private_start", method = RequestMethod.POST)
    @ResponseBody
    public MessageNoContent recipePrivateStart(HttpServletRequest request) throws IOException, ClassNotFoundException {

        JSONObject param = ParamUtils.getJsonObjectFromStream(request.getInputStream());

        String userId = param.getString("user_id");
        String regionGuid = param.getString("region_guid");
        String gatewayId = param.getString("gateway_id");

        System.out.println(gatewayId);

        String privateRecipeId = param.getString("private_recipe_id");

        if (userId == null || "".equals(userId)) {
            throw new ParameterException("-1", "user_id does not exist");
        }

        if (regionGuid == null || "".equals(regionGuid)) {
            throw new ParameterException("-1", "region_guid does not exist");
        }

        if (gatewayId == null || "".equals(gatewayId)) {
            throw new ParameterException("-1", "gateway_id does not exist");
        }

        if (privateRecipeId == null || "".equals(privateRecipeId)) {
            throw new ParameterException("-1", "private_recipe_id does not exist");
        }

        // 获取子网关对应的dispathcer的网关id
        DispatcherLocation dispatcherLocation = nodeLocationService.getDispatcher(gatewayId);

        String dispathcer = dispatcherLocation.getDispatcher_gateway();

        recipeService.recipePrivateStart(userId, regionGuid, dispathcer, privateRecipeId);

        MessageNoContent message = new MessageNoContent();
        message.setCode("0");
        message.setMessage("success");
        return message;
    }

    /**
     * 该接口用于 下发配方停止的命令
     * @param request
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    @RequestMapping(value = "/recipe/private_stop", method = RequestMethod.POST)
    @ResponseBody
    public MessageNoContent recipePrivateStop(HttpServletRequest request) throws IOException, ClassNotFoundException{


        JSONObject param = ParamUtils.getJsonObjectFromStream(request.getInputStream());

        String userId = param.getString("user_id");
        String regionGuid = param.getString("region_guid");
        String gatewayId = param.getString("gateway_id");

        String privateRecipeId = param.getString("private_recipe_id");

        if (userId == null || "".equals(userId)) {
            throw new ParameterException("-1", "user_id does not exist");
        }

        if (regionGuid == null || "".equals(regionGuid)) {
            throw new ParameterException("-1", "region_guid does not exist");
        }

        if (gatewayId == null || "".equals(gatewayId)) {
            throw new ParameterException("-1", "gateway_id does not exist");
        }

        if (privateRecipeId == null || "".equals(privateRecipeId)) {
            throw new ParameterException("-1", "private_recipe_id does not exist");
        }
        // 获取子网关对应的dispathcer的网关id
        DispatcherLocation dispatcherLocation = nodeLocationService.getDispatcher(gatewayId);

        String dispathcer = dispatcherLocation.getDispatcher_gateway();

        recipeService.recipePrivateStopt(userId, regionGuid, dispathcer, privateRecipeId);



        MessageNoContent message = new MessageNoContent();
        message.setCode("0");
        message.setMessage("success");
        return message;


    }

    /**
     * 用于更新配方的状态,子网关程序调用
     *
     *
     * @param request
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    @RequestMapping(value = "/recipe/private_status", method = RequestMethod.POST)
    @ResponseBody
    public MessageNoContent recipePrivateStatus(HttpServletRequest request) throws IOException, ClassNotFoundException{


        JSONObject param = ParamUtils.getJsonObjectFromStream(request.getInputStream());


        String regionGuid = param.getString("region_guid");
        String gatewayId = param.getString("gateway_id");

        String privateRecipeId = param.getString("private_recipe_id");


        if (regionGuid == null || "".equals(regionGuid)) {
            throw new ParameterException("-1", "region_guid does not exist");
        }

        if (privateRecipeId == null || "".equals(privateRecipeId)) {
            throw new ParameterException("-1", "private_recipe_id does not exist");

        }
        PrivateRecipeIndex privateRecipeIndex = JSONObject.parseObject(param.toString(),PrivateRecipeIndex.class);

        recipeService.updateStatus(privateRecipeIndex);

        MessageNoContent message = new MessageNoContent();
        message.setCode("0");
        message.setMessage("success");
        return message;

    }

    //获取所有公有配方
    @RequestMapping(value = "/recipe/publicRecipe", method = RequestMethod.GET)
    @ResponseBody
    public Message getServerRecepeAllData(HttpServletRequest request) throws IOException {
        JSONObject param = ParamUtils.getJsonObjectFromStream(request.getInputStream());
        //查询所有的配方
        String user_id = request.getParameter("user_id");
        if (user_id == null || user_id.equals("")) {
            throw new ParameterException("-1", "user_id does not exist");
        }
        JSONArray list = recipeService.findPublicRecipes(user_id);

        Message message = new Message();
        message.setCode("0");
        message.setMessage("query publicRecipe success");
        message.setContent(list);
        return message;
    }

    //查询用户私有配方
    @RequestMapping(value = "/recipe/userRecipe", method = RequestMethod.GET)
    @ResponseBody
    public Message getUserRecepe(HttpServletRequest request) throws IOException {
        String user_id = request.getParameter("user_id");
        //String account_id=request.getParameter("account_id");
        if (user_id == null || user_id.equals("")) {
            throw new ParameterException("-1", "user_id does not exist");
        }

        JSONArray result = recipeService.findPrivateRecipeList(user_id);

        Message message = new Message();
        message.setCode("0");
        message.setMessage("Query privateRecipeData success ");
        message.setContent(result);
        return message;
    }

    // 添加用户私有配方
    @RequestMapping(value = "/recipe/addPrivateRecipe", method = RequestMethod.POST)
    @ResponseBody
    public MessageNoContent addUserRecepe(HttpServletRequest request) throws IOException {
        JSONObject param = ParamUtils.getAttributess(request);

        String user_id = param.getString("user_id");
        String public_recipe_id = param.getString("public_recipe_id");
        if (user_id == null || user_id.equals("")) {
            throw new ParameterException("-1", "user_id does not exist");
        }
        if (public_recipe_id == null || public_recipe_id.equals("")) {
            throw new ParameterException("-1", "public_recipe_id does not exist");
        }

        recipeService.addPrivateRecipe(user_id, public_recipe_id);

        MessageNoContent message = new MessageNoContent();
        message.setCode("0");
        message.setMessage("add privateRecipeData success ");
        return message;
    }

    //查询用户私有配方详情
    @RequestMapping(value = "/recipe/userRecipeDetil", method = RequestMethod.GET)
    @ResponseBody
    public Message getUserRecipeDetil(HttpServletRequest request) {
        String user_id = request.getParameter("user_id");
        //String account_id=request.getParameter("account_id");
        String private_recipe_id = request.getParameter("private_recipe_id");

        if (user_id == null || user_id.equals("")) {
            throw new ParameterException("-1", "user_id does not exist");
        }

        if (private_recipe_id == null || private_recipe_id.equals("")) {
            throw new ParameterException("-1", "private_recipe_id does not exist");
        }
        JSONArray jsonArray = recipeService.findPrivateRecipeDetil(user_id, private_recipe_id);

        Message message = new Message();
        message.setCode("0");
        message.setMessage("query privateRecipeDataDetil success");
        message.setContent(jsonArray);
        return message;

    }

    //私有配方添加到区域
    @RequestMapping(value = "/recipe/addRegionRecipe", method = RequestMethod.POST)
    @ResponseBody
    public MessageNoContent addUserRecipeRegion(HttpServletRequest request) throws IOException {

        JSONObject param = ParamUtils.getAttributess(request);
        String user_id = param.getString("user_id");
        //String account_id=param.getString("account_id");
        String region_guid = param.getString("region_guid");
        String private_recipe_id = param.getString("private_recipe_id");

        if (user_id == null || user_id.equals("")) {
            throw new BussinessException("-1", "user_id does not exist");
        }
//        if (account_id==null||account_id.equals("")){
//            throw new BussinessException("-1","account_id does not exist");
//        }
        if (region_guid == null || region_guid.equals("")) {
            throw new BussinessException("-1", "region_guid does not exist");
        }
        if (private_recipe_id == null || private_recipe_id.equals("")) {
            throw new BussinessException("-1", "private_recipe_id does not exist");
        }


        PrivateRecipeIndex privateRecipeIndex = new PrivateRecipeIndex();
        privateRecipeIndex.setPrivate_recipe_id(private_recipe_id);
        //privateRecipeIndex.setAccount_id(account_id);
        privateRecipeIndex.setStart_time("");
        privateRecipeIndex.setSeq("0");
        privateRecipeIndex.setStatus("0");
        privateRecipeIndex.setId(UUID.randomUUID().toString());
        privateRecipeIndex.setRegion_guid(region_guid);
        recipeService.addUserRecipeRegion(user_id, privateRecipeIndex);
        MessageNoContent message = new MessageNoContent();
        message.setCode("0");
        message.setMessage("add privateRecipeIndex success");

//        recipeService
        return message;
    }

    //查询区域下配方
    @RequestMapping(value = "/recipe/regionRecipe", method = RequestMethod.GET)
    @ResponseBody
    public Message findRegionRecipe(HttpServletRequest request) {
        String user_id = request.getParameter("user_id");
        // String account_id=request.getParameter("account_id");
        String region_guid = request.getParameter("region_guid");
        if (user_id == null || user_id.equals("")) {
            throw new ParameterException("-1", "user_id does not exist");
        }

        if (region_guid == null || region_guid.equals("")) {
            throw new ParameterException("-1", "region_guid does not exist");
        }
        PrivateRecipeIndex privateRecipIndex = new PrivateRecipeIndex();

        privateRecipIndex.setRegion_guid(region_guid);
        JSONArray result = recipeService.findRegionRecipe(user_id, privateRecipIndex);

        Message message = new Message();
        message.setCode("0");
        message.setMessage("query region Recipe success");
        message.setContent(result);
        return message;
    }

    //查询配方曲线图
    @RequestMapping(value = "/recipe/sensorAndRecipeRecord", method = RequestMethod.GET)
    @ResponseBody
    public Message findRecipeAndSensorRecord(HttpServletRequest request) {
        String user_id = request.getParameter("user_id");
        String region_guid = request.getParameter("region_guid");
        String gateway_id = request.getParameter("gateway_id");
        String size = "100";
        String startTime = request.getParameter("start_time");
        String endTime = request.getParameter("end_time");

        if (user_id == null || user_id.equals("")) {
            throw new ParameterException("-1", "user_id does not exist");
        }

        if (region_guid == null || region_guid.equals("")) {
            throw new ParameterException("-1", "region_guid does not exist");
        }


        if (gateway_id == null || gateway_id.equals("")) {
            throw new ParameterException("-1", "gateway_id does not exist");
        }


        int sizes = Integer.parseInt(size);

        JSONObject result = recipeService.findRegionAndSensorRecords(user_id, region_guid,  gateway_id, sizes, startTime,endTime);
        Message message = new Message();
        message.setCode("0");
        message.setMessage("query recipe data success");
        message.setContent(result);
        return message;
    }

    //查询配方曲线图s
    @RequestMapping(value = "/recipe/sensorAndRecipeRecords", method = RequestMethod.GET)
    @ResponseBody
    public Message findRecipeAndSensorRecords(HttpServletRequest request) {
        String user_id = request.getParameter("user_id");
        String region_guid = request.getParameter("region_guid");
        String gateway_id = request.getParameter("gateway_id");
        String size = request.getParameter("size");
        String startTime = request.getParameter("start_time");
        String endTime = request.getParameter("end_time");
        String days=request.getParameter("days");

        if (user_id == null || user_id.equals("")) {
            throw new ParameterException("-1", "user_id does not exist");
        }

        if (region_guid == null || region_guid.equals("")) {
            throw new ParameterException("-1", "region_guid does not exist");
        }


        if (gateway_id == null || gateway_id.equals("")) {
            throw new ParameterException("-1", "gateway_id does not exist");
        }

        if (size == null || size.equals("")) {
            throw new ParameterException("-1", "size does not exist");
        }
        if (days == null || days.equals("")) {
            throw new ParameterException("-1", "days does not exist");
        }


        int sizes = Integer.parseInt(size);
        System.out.println("kkkkkk");
        JSONObject result = recipeService.findRegionAndSensorRecordses(user_id, region_guid,  gateway_id, sizes, startTime,endTime,Integer.parseInt(days));
        Message message = new Message();
        message.setCode("0");
        message.setMessage("query recipe data success");
        message.setContent(result);
        return message;
    }

    //修改用户私有配方 1
    @RequestMapping(value = "/recipe/modifyUserRecipe", method = RequestMethod.PUT)
    @ResponseBody
    public MessageNoContent modifyUserRecipe(HttpServletRequest request) throws IOException {
        JSONObject param = ParamUtils.getAttributess(request);
        List<PrivateRecipeData> list=new ArrayList<PrivateRecipeData>();
        for (int i=0;i<param.size();i++){
            JSONArray paramArray=(JSONArray) param.get("recipe");
            JSONObject paramJson=(JSONObject)paramArray.get(0);
            String privatePrecipeId = (String) paramJson.get("private_recipe_id");
            String Id = (String) paramJson.get("id");
            String cropName=(String) paramJson.get("crop_name");
            String day=(String) paramJson.get("day");
            String startTime=(String) paramJson.get("start_time");
            String endTime=(String) paramJson.get("end_time");
            String channelCombination=(String) paramJson.get("channel_combination");
            String airTemperature=(String) paramJson.get("air_temperature");
            String airHumidity=(String) paramJson.get("air_humidity");
            String soilTemperature=(String) paramJson.get("soil_temperature");
            String soilPH_value=(String) paramJson.get("soil_PH_value");
            String soilHumidity=(String) paramJson.get("soil_humidity");
            String carbonDioxide=(String) paramJson.get("carbon_dioxide");
            String illuminance=(String) paramJson.get("illuminance");
            String soilConductivity=(String) paramJson.get("soil_conductivity");
            String photons=(String) paramJson.get("photons");
            String liquidPH_value=(String) paramJson.get("liquid_PH_value");
            String laiValue=(String) paramJson.get("lai_value");
            PrivateRecipeData privateRecipeData=new PrivateRecipeData();

            privateRecipeData.setId(Id);

            list.add(privateRecipeData);

        }
        String userId = param.getString("user_id");

        recipeService.modifyUserRecipe(userId,list);

        MessageNoContent message = new MessageNoContent();
        message.setCode("0");
        message.setMessage("modify recipe success");
        return message;
    }

    //删除用户私有配方
    @RequestMapping(value = "/recipe/dropUserRecipe",method = RequestMethod.DELETE)
    @ResponseBody
    public MessageNoContent deleteUserRecipe(HttpServletRequest request) throws IOException {
        JSONObject param= ParamUtils.getAttributess(request);
        String userId=param.getString("user_id");
        String privateRecipeId=param.getString("private_recipe_id");
        String id=param.getString("id");
        if (userId==null||userId.equals("")){
            throw new ParameterException("-1","user_id does not exist");
        }
        if (privateRecipeId==null||privateRecipeId.equals("")){
            throw new ParameterException("-1","private_recipe_id");
        }
//        if (id==null||id.equals("")){
//            throw new ParameterException("-1","id does not exist");
//        }
        PrivateRecipeData privateRecipeData=new PrivateRecipeData();
        privateRecipeData.setPrivate_recipe_id(privateRecipeId);
        //privateRecipeData.setId(id);
        recipeService.dropUserRecipe(userId,privateRecipeData);
        MessageNoContent message=new MessageNoContent();
        message.setMessage("delete recipe success");
        message.setCode("0");
        return message;
    }

    //添加用户私有配方名称
    @RequestMapping(value = "/recipe/addUserPecipeName",method = RequestMethod.POST)
    @ResponseBody
    public MessageNoContent addUserPrcipeName(HttpServletRequest request) throws IOException {
        JSONObject param=ParamUtils.getAttributess(request);
        String userId=param.getString("user_id");
        String cropName=param.getString("crop_name");
        if (userId==null||userId.equals("")){
            throw new ParameterException("-1","user_id does not exist");
        }
        if (cropName==null||cropName.equals("")){
            throw new ParameterException("-1","crop_name does not exist");
        }

        //实体类
        PrivateRecipe privateRecipe=new PrivateRecipe();
        privateRecipe.setCrop_name(cropName);
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        privateRecipe.setCreate_time(sdf.format(new Date()));
        privateRecipe.setPrivate_recipe_id(UUID.randomUUID().toString());
        recipeService.addUserRecipeName(userId,privateRecipe);
        MessageNoContent message=new MessageNoContent();
        message.setCode("0");
        message.setMessage("add recipe cropName success");
        return message;
    }

    //批量添加用户私有配方数据
    @RequestMapping(value = "/recipe/addUserBatchPrcipe",method = RequestMethod.POST)
    @ResponseBody
    public MessageNoContent addUserBatchPrcipe(HttpServletRequest request) throws IOException {
        JSONObject param=ParamUtils.getAttributess(request);
        String userId=param.getString("user_id");
        String cropName=param.getString("crop_name");
        String days=param.getString("days");
        //fewDays从第几天开始添加
        String fewDays=param.getString("few_days");
        String privateRecipeId=param.getString("private_recipe_id");
        JSONArray batchPrcipe=(JSONArray) param.get("batchPrcipe");

        LinkedList<PrivateRecipeData> list=new LinkedList<PrivateRecipeData>();
        for (int i=0;i<batchPrcipe.size();i++){
            JSONObject batchs=(JSONObject) batchPrcipe.get(i);
            //String day=batchs.get("day").toString();
            String startTime=batchs.get("start_time").toString();
            String endTime=batchs.get("end_time").toString();
            String substratePHStart=batchs.get("substrate_PH_start").toString();
            String substratePHEnd=batchs.get("substrate_PH_end").toString();
            String substrateConductivityEnd=batchs.get("substrate_Conductivity_end").toString();
            String substrateConductivityStart=batchs.get("substrate_Conductivity_start").toString();
            String substrateTemperatureStart=batchs.get("substrate_Temperature_start").toString();
            String substrateTemperatureEnd=batchs.get("substrate_Temperature_end").toString();
            String ppfdStart=batchs.get("ppfd_start").toString();
            String ppfdEnd=batchs.get("ppfd_end").toString();
            String liquidPHStart=batchs.get("liquid_PH_start").toString();
            String liquidPHEnd=batchs.get("liquid_PH_end").toString();
            String substrateHumidityStart=batchs.get("substrate_Humidity_start").toString();
            String substrateHumidityEnd=batchs.get("substrate_Humidity_end").toString();
            String liquidDOCStart=batchs.get("liquid_DOC_start").toString();
            String liquidDOCEnd=batchs.get("liquid_DOC_end").toString();
            String liquidConductivityStart=batchs.get("liquid_Conductivity_start").toString();
            String liquidConductivityEnd=batchs.get("liquid_Conductivity_end").toString();
            String substrateDOCStart=batchs.get("substrate_DOC_start").toString();
            String substrateDOCEnd=batchs.get("substrate_DOC_end").toString();
            String laiStart=batchs.get("lai_start").toString();
            String laiEnd=batchs.get("lai_end").toString();
            String carbonDioxideStart=batchs.get("carbon_Dioxide_start").toString();
            String carbonDioxideEnd=batchs.get("carbon_Dioxide_end").toString();
            String illuminanceStart=batchs.get("illuminance_start").toString();
            String illuminanceEnd=batchs.get("illuminance_end").toString();
            String airTemperatureStart=batchs.get("air_Temperature_start").toString();
            String airTemperatureEnd=batchs.get("air_Temperature_end").toString();
            String airHumidityStart=batchs.get("air_Humidity_start").toString();
            String airHumidityEnd=batchs.get("air_Humidity_end").toString();

            PrivateRecipeData privateRecipeData=new PrivateRecipeData();
            privateRecipeData.setCrop_name(cropName);
            privateRecipeData.setPrivate_recipe_id(privateRecipeId);
            privateRecipeData.setStart_time(Integer.parseInt(startTime));
            //privateRecipeData.setDay(day);
            privateRecipeData.setEnd_time(endTime);
            privateRecipeData.setSubstrate_PH_start(substratePHStart);
            privateRecipeData.setSubstrate_PH_end(substratePHEnd);
            privateRecipeData.setSubstrate_Conductivity_start(substrateConductivityStart);
            privateRecipeData.setSubstrate_Conductivity_end(substrateConductivityEnd);
            privateRecipeData.setSubstrate_Temperature_start(substrateTemperatureEnd);
            privateRecipeData.setSubstrate_Temperature_start(substrateTemperatureStart);
            privateRecipeData.setSubstrate_Temperature_end(substrateTemperatureEnd);
            privateRecipeData.setPpfd_start(ppfdStart);
            privateRecipeData.setPpfd_end(ppfdEnd);
            privateRecipeData.setLiquid_PH_start(liquidPHStart);
            privateRecipeData.setLiquid_PH_end(liquidPHEnd);
            privateRecipeData.setSubstrate_Humidity_start(substrateHumidityStart);
            privateRecipeData.setSubstrate_Humidity_end(substrateHumidityEnd);
            privateRecipeData.setIlluminance_start(illuminanceStart);
            privateRecipeData.setIlluminance_end(illuminanceEnd);
            privateRecipeData.setLiquid_DOC_start(liquidDOCStart);
            privateRecipeData.setLiquid_DOC_end(liquidDOCEnd);
            privateRecipeData.setLiquid_Conductivity_start(liquidConductivityStart);
            privateRecipeData.setLiquid_Conductivity_end(liquidConductivityEnd);
            privateRecipeData.setSubstrate_DOC_start(substrateDOCStart);
            privateRecipeData.setSubstrate_DOC_end(substrateDOCEnd);
            privateRecipeData.setCarbon_Dioxide_start(carbonDioxideStart);
            privateRecipeData.setCarbon_Dioxide_end(carbonDioxideEnd);
            privateRecipeData.setAir_Temperature_start(airTemperatureStart);
            privateRecipeData.setAir_Temperature_end(airTemperatureEnd);
            privateRecipeData.setAir_Humidity_start(airHumidityStart);
            privateRecipeData.setAir_Humidity_end(airHumidityEnd);
            privateRecipeData.setLai_start(laiStart);
            privateRecipeData.setLai_end(laiEnd);
            list.add(privateRecipeData);
        }
        if (userId==null||userId.equals("")){
            throw new ParameterException("-1","user_id does not exist");
        }

        if (privateRecipeId==null||privateRecipeId.equals("")){
            throw new ParameterException("-1","private_recipe_id does not exist");
        }
        recipeService.addUserbatchPrcipes(userId,list,days,privateRecipeId, fewDays);

        MessageNoContent message=new MessageNoContent();
        message.setCode("0");
        message.setMessage("add recipe data success");
        return message;

    }

    //批量删除用户私有配方
    @RequestMapping(value = "/recipe/dropUserBatchPrcipe",method = RequestMethod.DELETE)
    @ResponseBody
    public MessageNoContent dropUserBatchPrcipes(HttpServletRequest request) throws IOException {
        JSONObject param=ParamUtils.getAttributess(request);
        String userId=param.getString("user_id");
        String privateRecipeId=param.getString("private_recipe_id");
        String startDay=param.getString("start_day");
        String endDay=param.getString("end_day");
        if (userId==null||userId.equals("")){
            throw new ParameterException("-1","user_id does not exist");
        }
        if (privateRecipeId==null||privateRecipeId.equals("")){
            throw new ParameterException("-1","private_recipe_id does not exist");
        }
        if (startDay==null||startDay.equals("")){
            throw new ParameterException("-1","start_day does not exist");
        }
        if (endDay==null||endDay.equals("")){
            throw new ParameterException("-1","end_day does not exist");
        }
        recipeService.dropUserbatchPrcipes(userId,startDay,endDay,privateRecipeId);
        MessageNoContent message=new MessageNoContent();
        message.setCode("0");
        message.setMessage("delete recipe data success");
        return message;
    }

    //修改用户私有配方名称
    @RequestMapping(value = "/recipe/modifyUserRecipeName",method = RequestMethod.PUT)
    @ResponseBody
    public MessageNoContent modifyUserRecipeName(HttpServletRequest request) throws IOException {
        JSONObject param=ParamUtils.getAttributess(request);
        String userId=param.getString("user_id");
        String privateRecipeId=param.getString("private_recipe_id");
        String cropName=param.getString("crop_name");
        if (userId==null||userId.equals("")){
            throw new ParameterException("-1","user_id does not exist");
        }
        if (privateRecipeId==null||privateRecipeId.equals("")){
            throw new ParameterException("-1","private_recipe_id does not exist");
        }
        if (cropName==null||cropName.equals("")){
            throw new ParameterException("-1","crop_name does not exist");
        }
        recipeService.modifyUserRecipeName(userId,privateRecipeId,cropName);
        MessageNoContent message=new MessageNoContent();
        message.setCode("0");
        message.setMessage("update crop name success");
        return message;

    }

    //批量修改用户配方数据 /1
    @RequestMapping(value = "/recipe/modifyUserBatchPrcipeData",method = RequestMethod.PUT)
    @ResponseBody
    public MessageNoContent modifyUserBatchRecipeDate(HttpServletRequest request) throws IOException {
        JSONObject param=ParamUtils.getAttributess(request);
        String userId=param.getString("user_id");
        String privateRecipeId=param.getString("private_recipe_id");
        JSONArray batchPrcipe=(JSONArray) param.get("batchPrcipe");
        String startDay=param.getString("start_day");
        String endDay=param.getString("end_day");
        String cropName=param.getString("crop_name");

        if (userId==null||userId.equals("")){
            throw new ParameterException("-1","user_id does not exist");
        }
        if (privateRecipeId==null||privateRecipeId.equals("")){
            throw new ParameterException("-1","private_Recipe_id does not exist");
        }
        if (startDay==null||startDay.equals("")){
            throw new ParameterException("-1","start_day does not exist");
        }
        if (endDay==null||endDay.equals("")){
            throw new ParameterException("-1","end_day does not exist");
        }
        if (cropName==null||cropName.equals("")){
            throw new ParameterException("-1","crop_name does not exist");
        }

        //获取数据
        LinkedList<PrivateRecipeData> linkedList=new LinkedList<PrivateRecipeData>();
        for (int i=0;i<batchPrcipe.size();i++){
            JSONObject  batchs=(JSONObject) batchPrcipe.get(i);
            String startTime=batchs.get("start_time").toString();
            String endTime=batchs.get("end_time").toString();
            String substratePHStart=batchs.get("substrate_PH_start").toString();
            String substratePHEnd=batchs.get("substrate_PH_end").toString();
            String substrateConductivityEnd=batchs.get("substrate_Conductivity_end").toString();
            String substrateConductivityStart=batchs.get("substrate_Conductivity_start").toString();
            String substrateTemperatureStart=batchs.get("substrate_Temperature_start").toString();
            String substrateTemperatureEnd=batchs.get("substrate_Temperature_end").toString();
            String ppfdStart=batchs.get("ppfd_start").toString();
            String ppfdEnd=batchs.get("ppfd_end").toString();
            String liquidPHStart=batchs.get("liquid_PH_start").toString();
            String liquidPHEnd=batchs.get("liquid_PH_end").toString();
            String substrateHumidityStart=batchs.get("substrate_Humidity_start").toString();
            String substrateHumidityEnd=batchs.get("substrate_Humidity_end").toString();
            String liquidDOCStart=batchs.get("liquid_DOC_start").toString();
            String liquidDOCEnd=batchs.get("liquid_DOC_end").toString();
            String liquidConductivityStart=batchs.get("liquid_Conductivity_start").toString();
            String liquidConductivityEnd=batchs.get("liquid_Conductivity_end").toString();
            String substrateDOCStart=batchs.get("substrate_DOC_start").toString();
            String substrateDOCEnd=batchs.get("substrate_DOC_end").toString();
            String laiStart=batchs.get("lai_start").toString();
            String laiEnd=batchs.get("lai_end").toString();
            String carbonDioxideStart=batchs.get("carbon_Dioxide_start").toString();
            String carbonDioxideEnd=batchs.get("carbon_Dioxide_end").toString();
            String illuminanceStart=batchs.get("illuminance_start").toString();
            String illuminanceEnd=batchs.get("illuminance_end").toString();
            String airTemperatureStart=batchs.get("air_Temperature_start").toString();
            String airTemperatureEnd=batchs.get("air_Temperature_end").toString();
            String airHumidityStart=batchs.get("air_Humidity_start").toString();
            String airHumidityEnd=batchs.get("air_Humidity_end").toString();


                PrivateRecipeData privateRecipeData=new PrivateRecipeData();
                privateRecipeData.setStart_time(Integer.parseInt(startTime));
                privateRecipeData.setEnd_time(endTime);
                privateRecipeData.setCrop_name(cropName);
                privateRecipeData.setSubstrate_PH_start(substratePHStart);
                privateRecipeData.setSubstrate_PH_end(substratePHEnd);
                privateRecipeData.setSubstrate_Conductivity_start(substrateConductivityStart);
                privateRecipeData.setSubstrate_Conductivity_end(substrateConductivityEnd);
                privateRecipeData.setSubstrate_Temperature_start(substrateTemperatureEnd);
                privateRecipeData.setSubstrate_Temperature_start(substrateTemperatureStart);
                privateRecipeData.setSubstrate_Temperature_end(substrateTemperatureEnd);
                privateRecipeData.setPpfd_start(ppfdStart);
                privateRecipeData.setPpfd_end(ppfdEnd);
                privateRecipeData.setLiquid_PH_start(liquidPHStart);
                privateRecipeData.setLiquid_PH_end(liquidPHEnd);
                privateRecipeData.setSubstrate_Humidity_start(substrateHumidityStart);
                privateRecipeData.setSubstrate_Humidity_end(substrateHumidityEnd);
                privateRecipeData.setIlluminance_start(illuminanceStart);
                privateRecipeData.setIlluminance_end(illuminanceEnd);
                privateRecipeData.setLiquid_DOC_start(liquidDOCStart);
                privateRecipeData.setLiquid_DOC_end(liquidDOCEnd);
                privateRecipeData.setLiquid_Conductivity_start(liquidConductivityStart);
                privateRecipeData.setLiquid_Conductivity_end(liquidConductivityEnd);
                privateRecipeData.setSubstrate_DOC_start(substrateDOCStart);
                privateRecipeData.setSubstrate_DOC_end(substrateDOCEnd);
                privateRecipeData.setCarbon_Dioxide_start(carbonDioxideStart);
                privateRecipeData.setCarbon_Dioxide_end(carbonDioxideEnd);
                privateRecipeData.setAir_Temperature_start(airTemperatureStart);
                privateRecipeData.setAir_Temperature_end(airTemperatureEnd);
                privateRecipeData.setAir_Humidity_start(airHumidityStart);
                privateRecipeData.setAir_Humidity_end(airHumidityEnd);
                privateRecipeData.setLai_start(laiStart);
                privateRecipeData.setLai_end(laiEnd);
                linkedList.add(privateRecipeData);

        }

        recipeService.modifyUserRecipebatchData(userId,privateRecipeId,linkedList,startDay,endDay);
        MessageNoContent message=new MessageNoContent();
        message.setCode("0");
        message.setMessage("update prvateRecipeData success");
        return message;
    }

    //删除区域配方
    @RequestMapping(value = "/region/recipe",method = RequestMethod.DELETE)
    @ResponseBody
    public MessageNoContent deleteRegionRecipe(HttpServletRequest request) throws IOException {
        JSONObject param=(JSONObject) ParamUtils.getJsonObjectFromStream(request.getInputStream());
        JSONArray regionRecipes=(JSONArray) param.get("private_recipe_index");
        JSONObject regionRecipe=(JSONObject) regionRecipes.get(0);

        String regionGuid=(String) regionRecipe.get("region_guid");
        String privateRecipeId=(String) regionRecipe.get("private_recipe_id");
        String userId=(String) param.get("user_id");
        //String gatewayId=(String) param.get("gateway_id");
        PrivateRecipeIndex privateRecipeIndex=new PrivateRecipeIndex();
        privateRecipeIndex.setPrivate_recipe_id(privateRecipeId);
        privateRecipeIndex.setRegion_guid(regionGuid);

        recipeService.deleteRegionRecipe(userId,privateRecipeIndex);

        MessageNoContent message=new MessageNoContent();
        message.setCode("0");
        message.setMessage("delete region recipe success");
        return message;
    }
}
