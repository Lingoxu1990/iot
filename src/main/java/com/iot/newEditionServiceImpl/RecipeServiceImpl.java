package com.iot.newEditionServiceImpl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.iot.dbUtil.PropsUtil;
import com.iot.dbUtil.SQLiteUtil;
import com.iot.exception.BussinessException;
import com.iot.mapper.*;
import com.iot.pojo.*;
import com.iot.socketUtil.OutPutSocketMessage;
import com.iot.message.Message;
import com.iot.newEditionService.RecipeService;
import com.iot.spitUtil.Param;
import com.iot.sqlControlUtil.SqlControlUtil;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

/**
 * Created by Jacob on 2016-04-07.
 */

@Service
public class RecipeServiceImpl implements RecipeService {

    @Resource
    private UserGatewayMapper userGatewayMapper;
    @Resource
    private PrivateRecipeIndexMapper privateRecipeIndexMapper;

    @Resource
    private PublicRecipeMapper publicRecipeMapper;

    @Resource
    private PublicRecipeDataMapper publicRecipeDataMapper;

    @Resource
    private PrivateRecipeDataMapper privateRecipeDataMapper;

    @Resource
    private PrivateRecipeMapper privateRecipeMapper;

    @Resource
    private TableRegionDeviceMapper tableRegionDeviceMapper;

    @Resource
    private TableSensorRecordMapper tableSensorRecordMapper;

    @Resource
    private TableDeviceMapper tableDeviceMapper;

    private static Logger logger = Logger.getLogger(RecipeServiceImpl.class);

    //修改配方
    public Message modifyRecipe(JSONObject jsonObject, String DestinationID, String SourceID, int packegType) {
        Message message = new Message();
        Set<String> keyset = jsonObject.keySet();
        for (String key : keyset){
            String [] result = SqlControlUtil.putObjects(key, (JSONArray) jsonObject.get(key));
            for (int i = 0; i<result.length;i++){
                OutPutSocketMessage outPutSocketMessage = new OutPutSocketMessage();
                outPutSocketMessage.setPackegType(packegType);
                outPutSocketMessage.setDestinationID(DestinationID);
                outPutSocketMessage.setSourceID(SourceID);
                outPutSocketMessage.setType("NULL");
                outPutSocketMessage.setMessage(key);
                outPutSocketMessage.setSql(result[i]);
                JSONObject jsonResult = outPutSocketMessage.sendMessag(SourceID);
                String status = (String) jsonResult.get("Status");
                if(!"0".equals(status)){
                    message.setCode("-1");
                    message.setContent(jsonResult);
                    message.setMessage("配方修改失败");
                }
            }
        }
        message.setCode("0");
        message.setContent("[]");
        message.setMessage("配方修改成功");
        return null;
    }

    //获取配方
    public Message findRecipe(JSONObject jsonObject, String DestinationID, String SourceID, int packegType) {
        Message message = new Message();
        String result = "";
        JSONObject list = new JSONObject();
        Set<String> keyset = jsonObject.keySet();
        for (String key : keyset){
            OutPutSocketMessage outPutSocketMessage = new OutPutSocketMessage();
            outPutSocketMessage.setPackegType(packegType);
            outPutSocketMessage.setDestinationID(DestinationID);
            outPutSocketMessage.setSourceID(SourceID);
            outPutSocketMessage.setMessage(key);
            outPutSocketMessage.setType(key);
            outPutSocketMessage.setSql(result);
            //发送数据包
            JSONObject jsonResult = outPutSocketMessage.sendMessag(SourceID);
            String status = (String) jsonResult.get("Status");
            if(!"0".equals(status)){
                message.setCode("-1");
                message.setContent(jsonResult);
                message.setMessage("配方查询失败");
            }
            list.put(key,jsonResult.get("List"));
        }
        message.setCode("0");
        message.setMessage("配方查询成功");
        message.setContent(list.toString());
        return message;
    }

    //添加配方
    public Message addRecipe(JSONObject jsonObject, String DestinationID, String SourceID, int packegType) {
        Message message = new Message();
        Set<String> keyset = jsonObject.keySet();
        for (String key : keyset){
            String [] result = SqlControlUtil.addObjects(key, (JSONArray) jsonObject.get(key));
            for (int i = 0; i < result.length; i ++){
                OutPutSocketMessage outPutSocketMessage = new OutPutSocketMessage();
                outPutSocketMessage.setPackegType(packegType);
                outPutSocketMessage.setDestinationID(DestinationID);
                outPutSocketMessage.setSourceID(SourceID);
                outPutSocketMessage.setType("NULL");
                outPutSocketMessage.setMessage(key);
                outPutSocketMessage.setSql(result[i]);
                JSONObject jsonResult = outPutSocketMessage.sendMessag(SourceID);
                String status = (String) jsonResult.get("Status");
                if(!"0".equals(status)){
                    message.setCode("-1");
                    message.setContent(jsonResult);
                    message.setMessage("配方添加失败");
                }
            }
        }
        message.setCode("0");
        message.setContent("[]");
        message.setMessage("配方添加成功");
        return message;
    }

    //删除配方
    public Message deleteRecipe(JSONObject jsonObject, String DestinationID, String SourceID, int packegType) {
        Message message = new Message();
        Set<String> keyset = jsonObject.keySet();
        for (String key : keyset){
            String [] result = SqlControlUtil.deleteObjects(key, (JSONArray) jsonObject.get(key));
            for (int i = 0 ; i < result.length ; i ++){
                OutPutSocketMessage outPutSocketMessage = new OutPutSocketMessage();
                outPutSocketMessage.setPackegType(packegType);
                outPutSocketMessage.setDestinationID(DestinationID);
                outPutSocketMessage.setSourceID(SourceID);
                outPutSocketMessage.setType("NULL");
                outPutSocketMessage.setMessage(key);
                outPutSocketMessage.setSql(result[i]);
                JSONObject jsonResult = outPutSocketMessage.sendMessag(SourceID);
                String status = (String) jsonResult.get("Status");
                if(!"0".equals(status)){
                    message.setCode("-1");
                    message.setContent(jsonResult);
                    message.setMessage("配方删除失败");
                }
            }
        }
        message.setCode("0");
        message.setContent("[]");
        message.setMessage("配方删除成功");
        return message;
    }

    public JSONObject startRecipe(JSONObject jsonObject, String DestinationID, String SourceID, int packegType) throws IOException {

        OutPutSocketMessage outPutSocketMessage = new OutPutSocketMessage();
        outPutSocketMessage.setPackegType(packegType);
        outPutSocketMessage.setDestinationID(DestinationID);
        outPutSocketMessage.setSourceID(SourceID);
        outPutSocketMessage.setType("NULL");
        outPutSocketMessage.setMessage("Recipe Start!");
        outPutSocketMessage.setSql(jsonObject.toString());



        JSONObject jsonResult = outPutSocketMessage.testSendMessage(SourceID);

        return jsonResult;
    }


    public String createRecipeConfig(String userId, String regionId, String recipeId) throws IOException, ClassNotFoundException {

        List<UserGateway> list=userGatewayMapper.selectByUserId(userId);

        if (list.size()<1){
            throw new BussinessException("-1","user does not exist");
        }

        String accountId=list.get(0).getAccount_id();

        String dbFilePath = new SQLiteUtil().createRecipeConfig(accountId, regionId, recipeId);

        if (dbFilePath == null || "".equals(dbFilePath)) {
            throw new BussinessException("-1", "data generate fail");
        }

        return dbFilePath;
    }

    public void recipePrivateStart(String userId, String regionId, String gatewayId, String recipeId){



        List<UserGateway> list=userGatewayMapper.selectByUserId(userId);

        if (list.size()<1){
            throw new BussinessException("-1","user does not exist");
        }

        String accountId=list.get(0).getAccount_id();

        // 装配配方状态数据
        PrivateRecipeIndex privateRecipeIndex = new PrivateRecipeIndex();
        privateRecipeIndex.setAccount_id(accountId);
        privateRecipeIndex.setPrivate_recipe_id(recipeId);
        privateRecipeIndex.setRegion_guid(regionId);
        privateRecipeIndex.setSeq("1");
        privateRecipeIndex.setStatus("1");

        SimpleDateFormat simpleDateFormat =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        String startTime =  simpleDateFormat.format(new Date());

        privateRecipeIndex.setStart_time(startTime);


//        privateRecipeIndexMapper.updateStartTimeByPrivateId(privateRecipeIndex);



        String sourceId = accountId + Param.WEBAPPID + String.valueOf(System.currentTimeMillis() / 1000000000);

        // 装配下发给子网关的数据包
        OutPutSocketMessage outPutSocketMessage = new OutPutSocketMessage();
        outPutSocketMessage.setPackegType(Param.PACKEGTYPE);//包类型，int型2
        outPutSocketMessage.setDestinationID(gatewayId);//调度者地址（字符串）
        outPutSocketMessage.setType("NULL");//填写“NULL”

        String fwdMessage =userId+","+regionId+","+gatewayId+","+recipeId;
        outPutSocketMessage.setMessage("applyRecipe");//下发的具体数据（拼接后的字符串）
        outPutSocketMessage.setSourceID(sourceId);//消息发送者的ID（前8位与调度者地址相同）
        outPutSocketMessage.setSql(fwdMessage);//配方操作指令 apply/cancel

        // 更新数据库中的配方状态数据
        JSONObject result = outPutSocketMessage.sendMessag(sourceId);

        String status = String.valueOf(result.get("Status"));

        if (!status.equals("0")) {
            throw new BussinessException("-1","gateway error");
        }
        // 更新数据库中的配方状态数据
        privateRecipeIndexMapper.updateStartTimeByPrivateId(privateRecipeIndex);

        return;
    }

    public void recipePrivateStopt(String userId, String regionId, String gatewayId, String recipeId) {
        List<UserGateway> list=userGatewayMapper.selectByUserId(userId);

        if (list.size()<1){
            throw new BussinessException("-1","user does not exist");
        }

        String accountId=list.get(0).getAccount_id();

        // 装配配方状态数据
        PrivateRecipeIndex privateRecipeIndex = new PrivateRecipeIndex();
        privateRecipeIndex.setAccount_id(accountId);
        privateRecipeIndex.setPrivate_recipe_id(recipeId);
        privateRecipeIndex.setRegion_guid(regionId);
        privateRecipeIndex.setStatus("0");

        SimpleDateFormat simpleDateFormat =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        String startTime =  simpleDateFormat.format(new Date());


        privateRecipeIndex.setStart_time(startTime);


//        privateRecipeIndexMapper.updateStartTimeByPrivateId(privateRecipeIndex);


        String sourceId = accountId + Param.WEBAPPID + String.valueOf(System.currentTimeMillis() / 1000000000);

        // 装配下发给子网关的数据包
        OutPutSocketMessage outPutSocketMessage = new OutPutSocketMessage();
        outPutSocketMessage.setPackegType(Param.PACKEGTYPE);//包类型，int型2
        outPutSocketMessage.setDestinationID(gatewayId);//调度者地址（字符串）
        outPutSocketMessage.setType("NULL");//填写“NULL”

        String fwdMessage =userId+","+regionId+","+gatewayId+","+recipeId;
        outPutSocketMessage.setMessage("stopRecipe");//下发的具体数据（拼接后的字符串）
        outPutSocketMessage.setSourceID(sourceId);//消息发送者的ID（前8位与调度者地址相同）
        outPutSocketMessage.setSql(fwdMessage);//配方操作指令 apply/cancel
        // 发送数据包,并获得子网关响应
        JSONObject result = outPutSocketMessage.sendMessag(sourceId);

        String status = String.valueOf(result.get("Status"));

        if (!status.equals("0")) {
            throw new BussinessException("-1","gateway error");
        }
        // 更新数据库中的配方状态数据
        privateRecipeIndexMapper.updateStartTimeByPrivateId(privateRecipeIndex);

        return;

    }



    public int updateStatus(PrivateRecipeIndex privateRecipeIndex) {

        return privateRecipeIndexMapper.updateStatus(privateRecipeIndex);

    }

    //获取公共配方
    public JSONArray findPublicRecipes(String user_id) {
        List<UserGateway> list=userGatewayMapper.selectByUserId(user_id);
        if (list.size()<1){
            throw new BussinessException("-1","user does not exist");
        }
        String account_id=list.get(0).getAccount_id();
        List<PublicRecipe> result=publicRecipeMapper.selectRecipesByAll();
        JSONArray publicRecipeList=(JSONArray) JSONArray.toJSON(result);

        return publicRecipeList;

    }

    //查询用户私有配方
    public JSONArray findPrivateRecipeList(String user_id) {
        List<UserGateway> list=userGatewayMapper.selectByUserId(user_id);
        if (list.size()<1){
            throw new BussinessException("-1","user does not exist");
        }
        String accountId=list.get(0).getAccount_id();

        List<PrivateRecipe> list1=privateRecipeMapper.selectPrivateRecipeByAccountId(accountId);
        if (list1.size()<1){
            throw new BussinessException("-1","query privateRecipe  is empty");
        }
        JSONArray prcipeDays=new JSONArray();
        for (PrivateRecipe recipe:list1){
            JSONObject dayes=new JSONObject();
            String privatePrcipeId=recipe.getPrivate_recipe_id();
            PrivateRecipeData privateRecipeData=new PrivateRecipeData();
            privateRecipeData.setAccount_id(accountId);
            privateRecipeData.setPrivate_recipe_id(privatePrcipeId);

            LinkedList<PrivateRecipeData> privateRecipeDataLinkedList=privateRecipeDataMapper.selectRecipeDataByprivateRecipeIdAndAccountId(privateRecipeData);
            JSONObject days=new JSONObject();
            if (privateRecipeDataLinkedList.size()>0){
                for (PrivateRecipeData recipeData:privateRecipeDataLinkedList){
                    days.put(recipeData.getDay(),"days");

                }
            }
            dayes.put("daysSize",days.size());
            dayes.put("private_recipe_id",privatePrcipeId);
            prcipeDays.add(dayes);
        }
        JSONArray objects=(JSONArray) JSONArray.toJSON(list1);
        for (int i=0;i<objects.size();i++){
            JSONObject temps=(JSONObject) objects.get(i);
            for (int a=0;a<prcipeDays.size();a++){
                JSONObject dayess=(JSONObject) prcipeDays.get(a);
                if (temps.get("private_recipe_id").equals(dayess.get("private_recipe_id"))){
                    //JSONObject dayss=(JSONObject) dayess.get("days");
                    temps.put("days",dayess.get("daysSize"));
                }
            }

        }
        return objects;
    }

    //下载配方
    public void addPrivateRecipe(String user_id, String public_recipe_id) {
        List<UserGateway> list=userGatewayMapper.selectByUserId(user_id);
        if (list.size()<1){
            throw new BussinessException("-1","user does not exist");
        }
        String account_id=list.get(0).getAccount_id();
        PrivateRecipe privateRecipe=new PrivateRecipe();
        String private_recipe_id=UUID.randomUUID().toString();
        privateRecipe.setPrivate_recipe_id(private_recipe_id);
        privateRecipe.setAccount_id(account_id);
        PublicRecipe publicRecipe=publicRecipeMapper.selectRecipeOne(public_recipe_id);
        if (publicRecipe==null){
            throw new BussinessException("0","public recipe not data");
        }
        privateRecipe.setCrop_name(publicRecipe.getCrop_name());
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String times=sdf.format(new Date());
        //System.out.println(times);
        privateRecipe.setCreate_time(times);

        //添加私有配方表

        PrivateRecipe privateRecipe1=privateRecipeMapper.selectRecipeBycropNameAndAccountId(privateRecipe);
        if (privateRecipe1!=null){
            throw new BussinessException("-1","The recipe name already exists");
        }
        int n=privateRecipeMapper.insert(privateRecipe);
        if (n<1){
            throw new BussinessException("-1","add privateRecepe faild");
        }

        List<PublicRecipeData> publicRecipeDataList=publicRecipeDataMapper.selectBypublicRecipeId(public_recipe_id);
        if (publicRecipeDataList.size()<1){
            throw new BussinessException("0","query publicRecipeData is empty ");
        }
        PrivateRecipeData recipeDatas=new PrivateRecipeData();
        recipeDatas.setAccount_id(account_id);
        recipeDatas.setPrivate_recipe_id(private_recipe_id);
        List<PrivateRecipeData> privateRecipeDataList=privateRecipeDataMapper.selectRecipeDataByprivateRecipeIdAndAccountId(recipeDatas);
        if (privateRecipeDataList.size()>0){
            throw new BussinessException("-1","The privateRecipeData already exists");
        }
        //List<PrivateRecipeData> privateRecipeDataList=new ArrayList<PrivateRecipeData>();
        for (PublicRecipeData publicRecipeData:publicRecipeDataList){
            PrivateRecipeData privateRecipeData=new PrivateRecipeData();

            privateRecipeData.setPrivate_recipe_id(private_recipe_id);
            privateRecipeData.setCrop_name(publicRecipe.getCrop_name());
            privateRecipeData.setAccount_id(account_id);

            privateRecipeData.setDay(publicRecipeData.getDay());
            privateRecipeData.setEnd_time(publicRecipeData.getEnd_time());
            privateRecipeData.setId(UUID.randomUUID().toString());

            privateRecipeData.setReserve07(publicRecipeData.getReserve07());
            privateRecipeData.setReserve06(publicRecipeData.getReserve06());
            privateRecipeData.setReserve05(publicRecipeData.getReserve05());
            privateRecipeData.setReserve04(publicRecipeData.getReserve04());
            privateRecipeData.setReserve03(publicRecipeData.getReserve03());
            privateRecipeData.setStart_time(Integer.parseInt(publicRecipeData.getStart_time()));
            privateRecipeData.setChannel_combination(publicRecipeData.getChannel_combination());

            privateRecipeData.setSubstrate_Temperature_start(publicRecipeData.getSubstrate_Temperature_start());
            privateRecipeData.setSubstrate_Temperature_end(publicRecipeData.getSubstrate_Temperature_end());
            privateRecipeData.setSubstrate_PH_start(publicRecipeData.getSubstrate_PH_start());
            privateRecipeData.setSubstrate_PH_end(publicRecipeData.getSubstrate_PH_end());
            privateRecipeData.setSubstrate_Humidity_start(publicRecipeData.getSubstrate_Humidity_start());
            privateRecipeData.setSubstrate_Humidity_end(publicRecipeData.getSubstrate_Humidity_end());
            privateRecipeData.setSubstrate_Conductivity_start(publicRecipeData.getSubstrate_Conductivity_start());
            privateRecipeData.setSubstrate_Conductivity_end(publicRecipeData.getSubstrate_Conductivity_end());
            privateRecipeData.setPpfd_start(publicRecipeData.getPpfd_start());
            privateRecipeData.setPpfd_end(publicRecipeData.getPpfd_end());
            privateRecipeData.setLiquid_DOC_start(publicRecipeData.getLiquid_DOC_start());
            privateRecipeData.setLiquid_DOC_end(publicRecipeData.getLiquid_DOC_end());
            privateRecipeData.setSubstrate_DOC_start(publicRecipeData.getSubstrate_DOC_start());
            privateRecipeData.setSubstrate_DOC_end(publicRecipeData.getSubstrate_DOC_end());
            privateRecipeData.setLiquid_Conductivity_start(publicRecipeData.getLiquid_Conductivity_start());
            privateRecipeData.setLiquid_Conductivity_end(publicRecipeData.getLiquid_Conductivity_end());
            privateRecipeData.setAir_Temperature_start(publicRecipeData.getAir_Temperature_start());
            privateRecipeData.setAir_Temperature_end(publicRecipeData.getAir_Temperature_end());
            privateRecipeData.setCarbon_Dioxide_start(publicRecipeData.getCarbon_Dioxide_start());
            privateRecipeData.setCarbon_Dioxide_end(publicRecipeData.getCarbon_Dioxide_end());
            privateRecipeData.setAir_Humidity_start(publicRecipeData.getAir_Humidity_start());
            privateRecipeData.setAir_Humidity_end(publicRecipeData.getAir_Humidity_end());
            privateRecipeData.setIlluminance_start(publicRecipeData.getIlluminance_start());
            privateRecipeData.setIlluminance_end(publicRecipeData.getIlluminance_end());
            privateRecipeData.setLai_start(publicRecipeData.getLai_start());
            privateRecipeData.setLai_end(publicRecipeData.getLai_end());
            privateRecipeData.setLiquid_PH_start(publicRecipeData.getLiquid_PH_start());
            privateRecipeData.setLiquid_PH_end(publicRecipeData.getLiquid_PH_end());

            int a=privateRecipeDataMapper.insertSelective(privateRecipeData);
            if (a<1){
                throw new BussinessException("-1","add privateRecepeData faild");
            }

        }

    }

    //查询用户私有配方详情
    public JSONArray findPrivateRecipeDetil(String user_id,  String private_recipe_id) {
        List<UserGateway> list=userGatewayMapper.selectByUserId(user_id);
        if (list.size()<1){
            throw new BussinessException("-1","user does not exist");
        }
        String account_id=list.get(0).getAccount_id();
        PrivateRecipeData privateRecipeData=new PrivateRecipeData();
        privateRecipeData.setAccount_id(account_id);
        privateRecipeData.setPrivate_recipe_id(private_recipe_id);
        LinkedList<PrivateRecipeData> privateRecipeDataList=privateRecipeDataMapper.selectRecipeDataByprivateRecipeIdAndAccountId(privateRecipeData);
        if (privateRecipeDataList.size()==0){
            throw new BussinessException("-1","the privateRecipeData is empty");
        }

        String[] dayes=new String[privateRecipeDataList.size()];
        for (int i=0;i<privateRecipeDataList.size();i++){
            PrivateRecipeData temps= privateRecipeDataList.get(i);
            dayes[i]=temps.getDay();
        }
        //排序

        for (int p=0;p<dayes.length;p++){
            for (int j=p+1;j<dayes.length;j++){
                if (Integer.parseInt(dayes[p].toString())>Integer.parseInt(dayes[j].toString())){
                    String tempess=dayes[p].toString();
                    dayes[p]=dayes[j].toString();
                    dayes[j]=tempess;
                }
            }
            //System.out.println("dayes:"+dayes[p]);
        }
        LinkedHashMap linkedHashMap=new LinkedHashMap();
        for (int d=0;d<dayes.length;d++){
            linkedHashMap.put(dayes[d],"days");
        }


        JSONArray jsonArray=(JSONArray) JSONArray.toJSON(privateRecipeDataList);
        //数据处理

        for (int i=0;i<jsonArray.size();i++){
            JSONObject temp=(JSONObject) jsonArray.get(i);
            temp.remove("account_id");
            temp.remove("channel_combination");
            temp.remove("reserve03");
            temp.remove("reserve04");
            temp.remove("reserve05");
            temp.remove("reserve06");
            temp.remove("reserve07");
            temp.remove("private_recipe_id");
            temp.remove("crop_name");
        }


        JSONArray objectsArray=new JSONArray();
        Set<String> maps=linkedHashMap.keySet();
        for (String keys:maps){
            JSONObject day=new JSONObject();
            JSONArray objects=new JSONArray();
            for (int j=0;j<jsonArray.size();j++){
                JSONObject tempes=(JSONObject) jsonArray.get(j);
                if (keys.equals(tempes.get("day"))){
                    String  endTimes=(String) tempes.get("end_time");
                    objects.add(tempes);
                }
            }
//            LinkedList<JSONObject> linkedList=new LinkedList<JSONObject>();
//            for (int k=0;k<objects.size();k++){
//                JSONObject objectsJson=(JSONObject) objects.get(k);
//
//                linkedList.add(objectsJson);
//            }
//            for (int k=0;k<linkedList.size();k++){
//                JSONObject objectsJson=(JSONObject) linkedList.get(k);
//                int endTimes=Integer.parseInt(objectsJson.get("end_time").toString());
//                for (int d=k+1;d<linkedList.size();d++){
//                    JSONObject objectsJson1=(JSONObject) linkedList.get(d);
//                    int endTimes1=Integer.parseInt(objectsJson1.get("end_time").toString());
//                    if (endTimes>endTimes1) {
//                        JSONObject jsonObjectTemp=(JSONObject) linkedList.get(k);
//                        //System.out.println("linkedList+k:"+linkedList.get(k).toString());
//                        linkedList.set(k,linkedList.get(d));
//                        //System.out.println("linkedList+k:"+linkedList.get(k).toString());
//                        linkedList.set(d,jsonObjectTemp);
//                    }
//                }
//            }
//            System.out.println("objects:"+objects.toString());
//            System.out.println("linkedList:"+linkedList.toString());
            day.put("data",objects);
            day.put("day",keys);
            objectsArray.add(day);
            //System.out.println(keys);
        }


        return objectsArray;
    }

    //添加用户私有配方添加到区域
    public void addUserRecipeRegion(String user_id,PrivateRecipeIndex privateRecipeIndex){
        List<UserGateway> list=userGatewayMapper.selectByUserId(user_id);
        if (list.size()<1){
            throw new BussinessException("-1","user does not exist");
        }
        String account_id=list.get(0).getAccount_id();
        privateRecipeIndex.setAccount_id(account_id);
        List<PrivateRecipeIndex> list1=privateRecipeIndexMapper.selectByRegionAndAccountIdAndPrivateRecipeId(privateRecipeIndex);
        if (list1.size()>0){
            throw new  BussinessException("-1"," The  privateRecipeIndex already exists ");
        }
        int n=privateRecipeIndexMapper.insert(privateRecipeIndex);
        if (n<1){
            throw new BussinessException("-1","add privateRecipeIndex faild");
        }


    }

    //查询区域配方
    public JSONArray findRegionRecipe(String user_id,PrivateRecipeIndex privateRecipeIndex){
        List<UserGateway> list=userGatewayMapper.selectByUserId(user_id);
        if (list.size()<1){
            throw new BussinessException("-1","user does not exist");
        }
        String account_id=list.get(0).getAccount_id();
        //查找区域设备
        TableRegionDevice tableRegionDevice=new TableRegionDevice();
        tableRegionDevice.setRegion_guid(privateRecipeIndex.getRegion_guid());
        tableRegionDevice.setAccount_id(account_id);

        List<TableRegionDevice> tableRegionDeviceList=tableRegionDeviceMapper.selectByAccountIdAndRegiongGuid(tableRegionDevice);
        if (tableRegionDeviceList.size()<1){
            throw new BussinessException("0","the device does not exist");
        }
        String device_type="";
        String device_guid="";
        for (TableRegionDevice tableRegionDevice1:tableRegionDeviceList){
            TableDevice tableDevice=new TableDevice();
            tableDevice.setAccount_id(account_id);
            tableDevice.setDevice_guid(tableRegionDevice1.getTable_device_guid());
            TableDevice tableDevice1=tableDeviceMapper.selectByDevice_guidAndAccount_id(tableDevice);
            if (tableDevice1.getDevice_type().equals("sensor")){
                device_type=tableDevice1.getDevice_type();
                device_guid=tableDevice1.getDevice_guid();
            }

        }

        privateRecipeIndex.setAccount_id(account_id);
        List<PrivateRecipeIndex> list1=privateRecipeIndexMapper.selectByRegionAndAccountId(privateRecipeIndex);
        if (list1.size()<1){
            throw new BussinessException("-1","query privateRecipeIndex is empty");
        }

        JSONArray objects=new JSONArray();
        for (PrivateRecipeIndex recipeIndex:list1 ){

            JSONObject recipeIndexjson=(JSONObject) JSONObject.toJSON(recipeIndex);
            JSONObject days=new JSONObject();
            String private_recipe_id=recipeIndex.getPrivate_recipe_id();
            PrivateRecipe privateRecipe=new PrivateRecipe();
            privateRecipe.setAccount_id(account_id);
            privateRecipe.setPrivate_recipe_id(private_recipe_id);
            PrivateRecipe privateRecipe1=privateRecipeMapper.selectRecipeOne(privateRecipe);
            //获取私有配方天数
            PrivateRecipeData privateRecipeData=new PrivateRecipeData();
            privateRecipeData.setPrivate_recipe_id(private_recipe_id);
            privateRecipeData.setAccount_id(privateRecipeIndex.getAccount_id());
            List<PrivateRecipeData> recipeDataList=privateRecipeDataMapper.selectRecipeDataByprivateRecipeIdAndAccountId(privateRecipeData);
            for (PrivateRecipeData privateRecipeData1:recipeDataList){
                //去掉重复天数
                days.put(privateRecipeData1.getDay(),"day");
            }
            JSONArray dayss=new JSONArray();
           Set<String> map=days.keySet();
            for (String key:map){
                dayss.add(key);
            }
            recipeIndexjson.put("days",dayss);
            recipeIndexjson.put("name",privateRecipe1.getCrop_name());
            recipeIndexjson.put("device_guid",device_guid);
            objects.add(recipeIndexjson);
        }

        return objects;
    }

    //配方曲线图
    public JSONObject findRegionAndSensorRecords(String user_id, String region_guid, String gateway_id,int size,String startTime,String endTime) {
        List<UserGateway> list=userGatewayMapper.selectByUserId(user_id);
        if (list.size()<1){
            throw new BussinessException("-1","user does not exist");
        }

        String account_id=list.get(0).getAccount_id();
        //获取执行区域配方
        PrivateRecipeIndex privateRecipeIndex=new PrivateRecipeIndex();
        privateRecipeIndex.setAccount_id(account_id);
        privateRecipeIndex.setRegion_guid(region_guid);
        //privateRecipeIndex.setStatus("1");
        List<PrivateRecipeIndex> privateRecipeIndexList=privateRecipeIndexMapper.selectByRegionAndAccountId(privateRecipeIndex);
        if (privateRecipeIndexList.size()<1){
            throw new BussinessException("0","the region not recipe ");
        }
        String private_recipe_id="";
        String seq="";
        String start_up_time="";
        for (PrivateRecipeIndex privateRecipeIndex1:privateRecipeIndexList){
            if (!privateRecipeIndex1.getStatus().equals("0")){
                private_recipe_id=privateRecipeIndex1.getPrivate_recipe_id();
                seq=privateRecipeIndex1.getSeq();
                //开始时间
                start_up_time=privateRecipeIndex1.getStart_time();
            }else {
                throw new BussinessException("-1","The region is not running the recipe");
            }
        }
        //查询配方可执行天数
        PrivateRecipeData privateRecipeDataDay=new PrivateRecipeData();
        privateRecipeDataDay.setAccount_id(account_id);
        privateRecipeDataDay.setPrivate_recipe_id(private_recipe_id);
        privateRecipeDataDay.setDay(seq);
        List<PrivateRecipeData> privateRecipeDataListDays=privateRecipeDataMapper.selectRecipeDataByprivateRecipeIdAndAccountIdAndDayBig(privateRecipeDataDay);
        if (privateRecipeDataListDays.size()<1){
            throw new BussinessException("0","the recipe is empty");
        }
        JSONObject daysess=new JSONObject();
        for (PrivateRecipeData recipeData:privateRecipeDataListDays){
            daysess.put(recipeData.getDay(),recipeData.getDay());
        }

        SimpleDateFormat sdfs=new SimpleDateFormat("yyyy-MM-dd");
        String start_up_times="";
        try {
            start_up_times=sdfs.format(sdfs.parse(start_up_time));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //开始时间
        try {
            startTime=java.net.URLDecoder.decode(startTime,   "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        try {
            endTime=java.net.URLDecoder.decode(endTime,"utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        long startTimes=0;
        try {
             startTimes=sdf.parse(startTime).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long long_start_up_time=0;

        try {
            long_start_up_time=sdf.parse(start_up_time).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (long_start_up_time>startTimes){
            startTimes =long_start_up_time;
        }

        //结束时间
        long endTimes=0;
        try {
            endTimes=sdf.parse(endTime).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long nowTimeses=System.currentTimeMillis();
        //System.out.println("nowTimeses:"+nowTimeses);
        long daysesss=daysess.size();
        long recipeLastTimeses=long_start_up_time+daysesss*24*60*60*1000;
//        System.out.println("long_start_up_time:"+long_start_up_time);
//        System.out.println("recipeLastTimeses:"+recipeLastTimeses);

        if (endTimes>=nowTimeses){
            endTimes=nowTimeses;
        }
        if(endTimes>=recipeLastTimeses){
            endTimes=recipeLastTimeses;
        }
//        System.out.println("startTimes:"+startTimes);
//        System.out.println("endTimes:"+endTimes);
        if (endTimes<startTimes){
            throw new BussinessException("-1","Time input error");
        }

        //查询传感器
        TableRegionDevice tableRegionDevice=new TableRegionDevice();
        tableRegionDevice.setAccount_id(account_id);
        tableRegionDevice.setRegion_guid(region_guid);
        tableRegionDevice.setGateway_id(gateway_id);
        List<TableRegionDevice> sensors=tableRegionDeviceMapper.getRegionSensors(tableRegionDevice);

        Queue<String> senorQueue = new LinkedList<String>();

        for (TableRegionDevice sensor:sensors) {
            senorQueue.offer(sensor.getTable_device_guid());
        }

        if (senorQueue.size()<1){
            throw new BussinessException("-1","there is no sensor in the region!");
        }
        int queueSize = senorQueue.size();
        JSONArray reslut_list = new JSONArray();
        for (int i = 0; i <queueSize ; i++) {
            String sensor_guid = senorQueue.poll();
            Map<String,String> map=new HashMap<String, String>();
            map.put("table_device_guid",sensor_guid);
            map.put("account_id",account_id);
            map.put("start_time",startTime);
            map.put("end_time",endTime);
            List<TableSensorRecord> tableSensorRecordList =tableSensorRecordMapper.getSenorData(map);
            JSONArray history=new JSONArray();
                if (tableSensorRecordList.size()==0){
                    JSONObject historyObject=new JSONObject();
                    historyObject.put("carbon_Dioxide","0");
                    historyObject.put("substrate_PH","0");
                    historyObject.put("substrate_Conductivity","0");
                    historyObject.put("substrate_Temperature","0");
                    historyObject.put("air_Temperature","0");
                    historyObject.put("ppfd","0");
                    historyObject.put("liquid_PH","0");
                    historyObject.put("substrate_Humidity","0");
                    historyObject.put("liquid_DOC","0");
                    historyObject.put("liquid_Conductivity","0");
                    historyObject.put("air_Humidity","0");
                    historyObject.put("substrate_DOC","0");
                    historyObject.put("illuminance","0");
                    historyObject.put("lai","0");
                    historyObject.put("record_time",startTime);
                    history.add(historyObject);
                }else {
                    history=(JSONArray)JSONArray.toJSON(tableSensorRecordList);
                    reslut_list.add(history);
                }
                reslut_list.add(history);


        }
        //取的数据最少的传感器
        int minSize=0;
        for (int i=0;i<reslut_list.size();i++){
            JSONArray tem=(JSONArray) reslut_list.get(i);
            if (minSize==0){
                minSize=((JSONArray) reslut_list.get(i)).size();
            }
            if (minSize>tem.size()){
                minSize=tem.size();
            }
        }

        //获取最小的数据
        JSONArray results_array=new JSONArray();
        for (int i=0;i<reslut_list.size();i++){
            JSONArray tem=(JSONArray) reslut_list.get(i);
            if (tem.size()==minSize){
                results_array=tem;
            }
        }

        //转为Float
        JSONArray floatDataArray=new JSONArray();
        for (int n=0;n<results_array.size();n++){
            float carbon_Dioxide =0;
            float substrate_PH =0;
            float substrate_Conductivity =0;
            float substrate_Temperature = 0;
            float air_Temperature=0;
            float ppfd=0;
            float liquid_PH=0;
            float substrate_Humidity=0;
            float liquid_DOC=0;
            float liquid_Conductivity=0;
            float air_Humidity=0;
            float substrate_DOC=0;
            float illuminance=0;
            float lai=0;
            JSONObject temp=(JSONObject) results_array.get(n);

            JSONObject  floatdatas=new JSONObject();
            for (int p=0;p<temp.size();p++){
                if (temp.get("carbon_Dioxide").toString().equals("")){
                    carbon_Dioxide=Float.parseFloat("0");
                }else {
                    carbon_Dioxide=Float.parseFloat(temp.get("carbon_Dioxide").toString());
                }
                if (temp.get("substrate_PH").toString().equals("")){
                    substrate_PH=Float.parseFloat("0");
                }else {
                    substrate_PH=Float.parseFloat(temp.get("substrate_PH").toString());
                }
                if (temp.get("substrate_Conductivity").toString().equals("")){
                    substrate_Conductivity=Float.parseFloat("0");
                }else {
                    substrate_Conductivity=Float.parseFloat(temp.get("substrate_Conductivity").toString());
                }
                if (temp.get("substrate_Temperature").toString().equals("")){
                    substrate_Temperature=Float.parseFloat("0");
                }else {
                    substrate_Temperature=Float.parseFloat(temp.get("substrate_Temperature").toString());
                }
                if (temp.get("air_Temperature").toString().equals("")){
                    air_Temperature=Float.parseFloat("0");
                }else {
                    air_Temperature=Float.parseFloat(temp.get("air_Temperature").toString());
                }
                if (temp.get("ppfd").toString().equals("")){
                    ppfd=Float.parseFloat("0");
                }else {
                    ppfd=Float.parseFloat(temp.get("ppfd").toString());
                }
                if (temp.get("liquid_PH").toString().equals("")){
                    liquid_PH=Float.parseFloat("0");
                }else {
                    liquid_PH=Float.parseFloat(temp.get("liquid_PH").toString());
                }
                if (temp.get("substrate_Humidity").toString().equals("")){
                    substrate_Humidity=Float.parseFloat("0");
                }else {
                    substrate_Humidity=Float.parseFloat(temp.get("substrate_Humidity").toString());
                }
                if (temp.get("liquid_DOC").toString().equals("")){
                    liquid_DOC=Float.parseFloat("0");
                }else {
                    liquid_DOC=Float.parseFloat(temp.get("liquid_DOC").toString());
                }
                if (temp.get("liquid_Conductivity").toString().equals("")){
                    liquid_Conductivity=Float.parseFloat("0");
                }else {
                    liquid_Conductivity=Float.parseFloat(temp.get("liquid_Conductivity").toString());
                }
                if (temp.get("air_Humidity").toString().equals("")){
                    air_Humidity=Float.parseFloat("0");
                }else {
                    air_Humidity=Float.parseFloat(temp.get("air_Humidity").toString());
                }
                if (temp.get("substrate_DOC").toString().equals("")){
                    substrate_DOC=Float.parseFloat("0");
                }else {
                    substrate_DOC=Float.parseFloat(temp.get("substrate_DOC").toString());
                }
                if (temp.get("illuminance").toString().equals("")){
                    illuminance=Float.parseFloat("0");
                }else {
                    illuminance=Float.parseFloat(temp.get("illuminance").toString());
                }


//                carbon_Dioxide=Float.parseFloat(temp.get("carbon_Dioxide").toString());
//                substrate_PH=Float.parseFloat(temp.get("substrate_PH").toString());
//                substrate_Conductivity=Float.parseFloat(temp.get("substrate_Conductivity").toString());
//                substrate_Temperature=Float.parseFloat(temp.get("substrate_Temperature").toString());
//                air_Temperature=Float.parseFloat(temp.get("air_Temperature").toString());
//                ppfd=Float.parseFloat(temp.get("ppfd").toString());
//                liquid_PH=Float.parseFloat(temp.get("liquid_PH").toString());
//                //System.out.println("substrate_Humidity:"+temp.get("substrate_Humidity").toString());
//                substrate_Humidity=Float.parseFloat(temp.get("substrate_Humidity").toString());
//                //System.out.println("liquid_DOC:"+temp.get("liquid_DOC").toString());
//                liquid_DOC=Float.parseFloat(temp.get("liquid_DOC").toString());
//                liquid_Conductivity=Float.parseFloat(temp.get("liquid_Conductivity").toString());
//                air_Humidity=Float.parseFloat(temp.get("air_Humidity").toString());
//                substrate_DOC=Float.parseFloat(temp.get("substrate_DOC").toString());
//                illuminance=Float.parseFloat(temp.get("illuminance").toString());
                String time=(String)temp.get("record_time");
                lai=0;

                floatdatas.put("carbon_Dioxide",carbon_Dioxide);
                floatdatas.put("substrate_PH",substrate_PH);
                floatdatas.put("substrate_Conductivity",substrate_Conductivity);

                floatdatas.put("substrate_Temperature",substrate_Temperature);
                floatdatas.put("air_Temperature",air_Temperature);
                floatdatas.put("ppfd",ppfd);
                floatdatas.put("liquid_PH",liquid_PH);
                floatdatas.put("substrate_Humidity",substrate_Humidity);
                floatdatas.put("liquid_DOC",liquid_DOC);
                floatdatas.put("liquid_Conductivity",liquid_Conductivity);
                floatdatas.put("air_Humidity",air_Humidity);
                floatdatas.put("substrate_DOC",substrate_DOC);
                floatdatas.put("illuminance",illuminance);
                floatdatas.put("times",time);
                floatdatas.put("lai",lai);
            }
            floatDataArray.add(floatdatas);
        }
        //System.out.println("floatDataArray:"+floatDataArray.toString());
        //

        long startlongses=startTimes;
        //System.out.println("startlongses:"+startlongses);
        long sed = endTimes-startTimes;
        //System.out.println("sed:"+sed);
        long ddd = sed/size;
        if (ddd<(30000)){
            ddd=30000;

        }
        size=(int) (sed/ddd);
        //System.out.println("ddd:"+ddd);

        //获取传感器时间段内的平均值
        JSONArray result_point_count=new JSONArray();//所有时间点值的平均值集合
        JSONArray result_times_count=new JSONArray();//所有时间点的集合
        for (int i=0;i<size;i++){
            long time_point=startTimes+ddd;

            result_times_count.add(startTimes);//时间点
            float carbon_Dioxide =0;
            float substrate_PH =0;
            float substrate_Conductivity =0;
            float substrate_Temperature = 0;
            float air_Temperature=0;
            float ppfd=0;
            float liquid_PH=0;
            float substrate_Humidity=0;
            float liquid_DOC=0;
            float liquid_Conductivity=0;
            float air_Humidity=0;
            float substrate_DOC=0;
            float illuminance=0;
            float lai=0;
            String timess="";
            int indexs=0;//获取时间段内满足条件个数
            JSONObject times_point_avg=new JSONObject();
            for (int j=0;j<floatDataArray.size();j++){
                JSONObject jsonObject=(JSONObject) floatDataArray.get(j);
                String times=(String) jsonObject.get("times");
                //System.out.println("times:"+times);
                Date times_date = null;
                try {
                    times_date=sdf.parse(times);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                long times_long=times_date.getTime();

                if (startTimes<=times_long&&time_point>=times_long){
                    indexs++;
                    carbon_Dioxide+=(Float) jsonObject.get("carbon_Dioxide");
                    substrate_PH+=(Float) jsonObject.get("substrate_PH");
                    substrate_Conductivity+=(Float) jsonObject.get("substrate_Conductivity");
                    substrate_Temperature+=(Float) jsonObject.get("substrate_Temperature");
                    air_Temperature+=(Float) jsonObject.get("air_Temperature");
                    ppfd+=(Float) jsonObject.get("ppfd");
                    liquid_PH+=(Float) jsonObject.get("liquid_PH");
                    substrate_Humidity+=(Float) jsonObject.get("substrate_Humidity");
                    liquid_DOC+=(Float) jsonObject.get("liquid_DOC");
                    liquid_Conductivity+=(Float) jsonObject.get("liquid_Conductivity");
                    air_Humidity+=(Float) jsonObject.get("air_Humidity");
                    substrate_DOC+=(Float) jsonObject.get("substrate_DOC");
                    illuminance+=(Float) jsonObject.get("illuminance");
                    lai=0;
                    times_point_avg.put("carbon_Dioxide",carbon_Dioxide);
                    times_point_avg.put("substrate_PH",substrate_PH);
                    times_point_avg.put("substrate_Conductivity",substrate_Conductivity);
                    times_point_avg.put("substrate_Temperature",substrate_Temperature);
                    times_point_avg.put("air_Temperature",air_Temperature);
                    times_point_avg.put("ppfd",ppfd);
                    times_point_avg.put("liquid_PH",liquid_PH);
                    times_point_avg.put("substrate_Humidity",substrate_Humidity);
                    times_point_avg.put("liquid_DOC",liquid_DOC);
                    times_point_avg.put("liquid_Conductivity",liquid_Conductivity);
                    times_point_avg.put("air_Humidity",air_Humidity);
                    times_point_avg.put("substrate_DOC",substrate_DOC);
                    times_point_avg.put("illuminance",illuminance);
                    times_point_avg.put("lai",lai);

                    //times_point_avg.put("timess",timess);
                }
            }
            //System.out.println("times_point_avg:"+times_point_avg.toString());
            //获取时间段内的平均值
            Set<String> map=times_point_avg.keySet();
            for (String key:map){
                times_point_avg.put(key,((Float)times_point_avg.get(key))/indexs);
            }
            //System.out.println("times_point_avg:"+times_point_avg.toString());

            //如果不存在值则该点为0
            if (times_point_avg.size()<1){
                JSONObject temp=(JSONObject) floatDataArray.get(0);
                Set<String> maps=temp.keySet();
                for (String key:maps){
                    times_point_avg.put(key,"0");
                }
            }
            result_point_count.add(times_point_avg);
            //时间点自增
            startTimes=time_point;
        }

        //System.out.println("result_point_count:"+result_point_count.toString());

        //获取配方数据
        PrivateRecipeData privateRecipeData=new PrivateRecipeData();
        privateRecipeData.setPrivate_recipe_id(private_recipe_id);
        privateRecipeData.setAccount_id(account_id);
        privateRecipeData.setDay(seq);
        List<PrivateRecipeData> privateRecipeDataList =privateRecipeDataMapper.selectRecipeDataByprivateRecipeIdAndAccountIdAndDayBig(privateRecipeData);
        //System.out.println("privateRecipeDataList:"+privateRecipeDataList.size());
        JSONArray recipeDatearray=new JSONArray();
        JSONArray recipeDatas=(JSONArray) JSONArray.toJSON(privateRecipeDataList);
        //System.out.println("recipeDatas:"+recipeDatas.toString());
        for (int j=0;j<recipeDatas.size();j++){
            JSONObject tempss=(JSONObject) recipeDatas.get(j);
            JSONObject jsonObject=new JSONObject();
            jsonObject.put("substrate_PH",(Float.parseFloat(tempss.get("substrate_PH_start").toString())+Float.parseFloat(tempss.get("substrate_PH_end").toString()))/2);
            jsonObject.put("substrate_Conductivity",(Float.parseFloat(tempss.get("substrate_Conductivity_start").toString())+Float.parseFloat(tempss.get("substrate_Conductivity_end").toString()))/2);
            jsonObject.put("substrate_Temperature",(Float.parseFloat(tempss.get("substrate_Temperature_start").toString())+Float.parseFloat(tempss.get("substrate_Temperature_end").toString()))/2);
            jsonObject.put("ppfd",(Float.parseFloat(tempss.get("ppfd_start").toString())+Float.parseFloat(tempss.get("ppfd_end").toString()))/2);
            jsonObject.put("liquid_PH",(Float.parseFloat(tempss.get("liquid_PH_start").toString())+Float.parseFloat(tempss.get("liquid_PH_end").toString()))/2);
            jsonObject.put("substrate_Humidity",(Float.parseFloat(tempss.get("substrate_Humidity_start").toString())+Float.parseFloat(tempss.get("substrate_Humidity_end").toString()))/2);
            jsonObject.put("liquid_DOC",(Float.parseFloat(tempss.get("liquid_DOC_start").toString())+Float.parseFloat(tempss.get("liquid_DOC_end").toString()))/2);
            jsonObject.put("liquid_Conductivity",(Float.parseFloat(tempss.get("liquid_Conductivity_start").toString())+Float.parseFloat(tempss.get("liquid_Conductivity_end").toString()))/2);
            jsonObject.put("substrate_DOC",(Float.parseFloat(tempss.get("substrate_DOC_start").toString())+Float.parseFloat(tempss.get("substrate_DOC_end").toString()))/2);
            jsonObject.put("lai",(Float.parseFloat(tempss.get("lai_start").toString())+Float.parseFloat(tempss.get("lai_end").toString()))/2);
            jsonObject.put("carbon_Dioxide",(Float.parseFloat(tempss.get("carbon_Dioxide_start").toString())+Float.parseFloat(tempss.get("carbon_Dioxide_end").toString()))/2);
            jsonObject.put("illuminance",(Float.parseFloat(tempss.get("illuminance_start").toString())+Float.parseFloat(tempss.get("illuminance_end").toString()))/2);
            jsonObject.put("air_Temperature",(Float.parseFloat(tempss.get("air_Temperature_start").toString())+Float.parseFloat(tempss.get("air_Temperature_end").toString()))/2);
            jsonObject.put("air_Humidity",(Float.parseFloat(tempss.get("air_Humidity_start").toString())+Float.parseFloat(tempss.get("air_Humidity_end").toString()))/2);

            jsonObject.put("start_time",tempss.get("start_time").toString());
            jsonObject.put("end_time",tempss.get("end_time").toString());
            jsonObject.put("day",tempss.get("day").toString());
            recipeDatearray.add(jsonObject);
            //System.out.println("recipeDatearray:"+recipeDatearray.toString());
        }
        //获取配方的平均点值
//
        JSONArray result_point_count_recipe=new JSONArray();//所有时间点值的平均值集合
        //System.out.println("size:"+size);
        for (int k=0;k<size;k++){
            long time_point=startlongses+ddd;

            float carbon_Dioxide =0;
            float substrate_PH =0;
            float substrate_Conductivity =0;
            float substrate_Temperature = 0;
            float air_Temperature=0;
            float ppfd=0;
            float liquid_PH=0;
            float substrate_Humidity=0;
            float liquid_DOC=0;
            float liquid_Conductivity=0;
            float air_Humidity=0;
            float substrate_DOC=0;
            float illuminance=0;
            float lai=0;
            JSONObject times_point_value=new JSONObject();
            //SimpleDateFormat sdfs=new SimpleDateFormat("yyyy-MM-dd");
            for (int y=0;y<recipeDatearray.size();y++){
                JSONObject recipeDatajs=(JSONObject) recipeDatearray.get(y);
                String days=(String) recipeDatajs.get("day");
                String oneDyaStart=(String) recipeDatajs.get("start_time");
                String ontDayEnd=(String) recipeDatajs.get("end_time");
                long dayStart=0;
                long dayEnds=0;
                try {
                     dayStart=(Long.parseLong(days)-Long.parseLong(seq))*24*60*60*1000+Long.parseLong(oneDyaStart)*60*60*1000+sdfs.parse(start_up_times).getTime();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                try {
                    dayEnds=(Long.parseLong(days)-Long.parseLong(seq))*24*60*60*1000+sdfs.parse(start_up_times).getTime()+Long.parseLong(ontDayEnd)*60*60*1000;
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                if (time_point>dayStart&&time_point<=dayEnds){
                    times_point_value.put("substrate_PH",Float.parseFloat(recipeDatajs.get("substrate_PH").toString()));
                    times_point_value.put("substrate_Conductivity",Float.parseFloat(recipeDatajs.get("substrate_Conductivity").toString()));
                    times_point_value.put("substrate_Temperature",Float.parseFloat(recipeDatajs.get("substrate_Temperature").toString()));
                    times_point_value.put("ppfd",Float.parseFloat(recipeDatajs.get("ppfd").toString()));
                    times_point_value.put("liquid_PH",Float.parseFloat(recipeDatajs.get("liquid_PH").toString()));
                    times_point_value.put("substrate_Humidity",Float.parseFloat(recipeDatajs.get("substrate_Humidity").toString()));
                    times_point_value.put("liquid_DOC",Float.parseFloat(recipeDatajs.get("liquid_DOC").toString()));
                    times_point_value.put("liquid_Conductivity",Float.parseFloat(recipeDatajs.get("liquid_Conductivity").toString()));
                    times_point_value.put("substrate_DOC",Float.parseFloat(recipeDatajs.get("substrate_DOC").toString()));
                    times_point_value.put("lai",Float.parseFloat(recipeDatajs.get("lai").toString()));
                    times_point_value.put("carbon_Dioxide",Float.parseFloat(recipeDatajs.get("carbon_Dioxide").toString()));
                    times_point_value.put("illuminance",Float.parseFloat(recipeDatajs.get("illuminance").toString()));
                    times_point_value.put("air_Temperature",Float.parseFloat(recipeDatajs.get("air_Temperature").toString()));
                    times_point_value.put("air_Humidity",Float.parseFloat(recipeDatajs.get("air_Humidity").toString()));
                }

            }
            if (times_point_value.size()<1){
                times_point_value.put("substrate_PH","0");
                times_point_value.put("substrate_Conductivity","0");
                times_point_value.put("substrate_Temperature","0");
                times_point_value.put("ppfd","0");
                times_point_value.put("liquid_PH","0");
                times_point_value.put("substrate_Humidity","0");
                times_point_value.put("liquid_DOC","0");
                times_point_value.put("liquid_Conductivity","0");
                times_point_value.put("substrate_DOC","0");
                times_point_value.put("lai","0");
                times_point_value.put("carbon_Dioxide","0");
                times_point_value.put("illuminance","0");
                times_point_value.put("air_Temperature","0");
                times_point_value.put("air_Humidity","0");
            }
            startlongses=time_point;
            result_point_count_recipe.add(times_point_value);
        }
        //System.out.println("result_point_count_recipe:"+result_point_count_recipe.toString());
//        //数据总成
        //System.out.println("result_point_count:"+result_point_count.toString());
        JSONObject paramsAll=(JSONObject) result_point_count.get(0);
        paramsAll.remove("times");
        //System.out.println(paramsAll.toString());
        //System.out.println(result_point_count_recipe.get(0).toString());
        Set<String> mapss=paramsAll.keySet();

        JSONArray datases=new JSONArray();
        for (String key:mapss){
            JSONObject datas=new JSONObject();
            JSONArray data=new JSONArray();

            JSONArray SersorArray=new JSONArray();
            JSONArray recipeArray=new JSONArray();
            for (int n=0;n<result_point_count_recipe.size();n++){
                JSONObject tempesRecipe =(JSONObject) result_point_count_recipe.get(n);

                recipeArray.add(Float.parseFloat(tempesRecipe.get(key).toString()));
            }
            for (int q=0;q<result_point_count.size();q++){
                JSONObject tempesSersor=(JSONObject) result_point_count.get(q);
                SersorArray.add(Float.parseFloat(tempesSersor.get(key).toString()));
            }
            JSONObject sensorJs=new JSONObject();
            sensorJs.put("name","sensorData");
            sensorJs.put("data",SersorArray);
            data.add(sensorJs);
            JSONObject recipeJs=new JSONObject();
            recipeJs.put("name","recipeData");
            recipeJs.put("data",recipeArray);
            data.add(recipeJs);

            datas.put("data",data);
            datas.put("name",(new StringBuffer()).append(Character.toUpperCase(key.charAt(0))).append(key.substring(1)).toString());
            datases.add(datas);
        }

        //时间
        JSONArray times=new JSONArray();
        String timeStr="";
        for (int i=0;i<result_times_count.size();i++){
             timeStr= sdf.format(result_times_count.get(i));
            times.add(timeStr);
        }
        //数据
        JSONObject result_objects=new JSONObject();
        result_objects.put("data",datases);
        result_objects.put("time",times);
        return result_objects;





    }

    //配方数据
    @Override
    public JSONObject findRegionAndSensorRecordses(String user_id, String region_guid, String gateway_id, int size, String startTime, String endTime, int days) {
        //System.out.println("et:"+endTime);
        List<UserGateway> list=userGatewayMapper.selectByUserId(user_id);
        if (list.size()<1){
            throw new BussinessException("-1","user does not exist");
        }

        String account_id=list.get(0).getAccount_id();
        //获取执行区域配方
        PrivateRecipeIndex privateRecipeIndex=new PrivateRecipeIndex();
        privateRecipeIndex.setAccount_id(account_id);
        privateRecipeIndex.setRegion_guid(region_guid);

        List<PrivateRecipeIndex> privateRecipeIndexList=privateRecipeIndexMapper.selectByRegionAndAccountId(privateRecipeIndex);
        if (privateRecipeIndexList.size()<1){
            throw new BussinessException("0","the region not recipe ");
        }

        String private_recipe_id="";
        String seq="";
        String start_up_time="";
        for (PrivateRecipeIndex privateRecipeIndex1:privateRecipeIndexList){
            if (!privateRecipeIndex1.getStatus().equals("0")){
                private_recipe_id=privateRecipeIndex1.getPrivate_recipe_id();
                seq=privateRecipeIndex1.getSeq();
                //开始时间
                start_up_time=privateRecipeIndex1.getStart_time();
            }else {
                throw new BussinessException("-1","The region is not running the recipe");
            }
        }

        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long startTimess=0;
        long endTimess=0;
        long privateRecipeTimes=0;
        try {
             startTimess=(sdf.parse(startTime)).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        try {
            endTimess=sdf.parse(endTime).getTime();
            //System.out.println("endT0:"+sdf.format(endTimess));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        try {
            privateRecipeTimes=sdf.parse(start_up_time).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (endTimess<privateRecipeTimes){
            throw new BussinessException("0","There is no recipe data in this time");
        }
        //查询开始时间
        long stTimes=0;
        if (startTimess>privateRecipeTimes){
            stTimes=startTimess;
        }else {
            stTimes=privateRecipeTimes;
        }

        //查询结束时间
        long dayLong=(endTimess-privateRecipeTimes)/(24*60*60*1000);
//
        if ((endTimess-privateRecipeTimes)%(24*60*60*1000)>0){
            dayLong=dayLong+1;
        }

        //查询数据
        PrivateRecipeData privateRecipeDataDay=new PrivateRecipeData();
        privateRecipeDataDay.setAccount_id(account_id);
        privateRecipeDataDay.setPrivate_recipe_id(private_recipe_id);

        privateRecipeDataDay.setDay(String.valueOf(dayLong));
        List<PrivateRecipeData> privateRecipeDataList=privateRecipeDataMapper.selectRecipeDataByprivateRecipeIdAndAccountIdAndDaymin(privateRecipeDataDay);
        JSONArray recipeDatearray=new JSONArray();
        JSONArray recipeDatas=(JSONArray) JSONArray.toJSON(privateRecipeDataList);

        for (int j=0;j<recipeDatas.size();j++){
            JSONObject tempss=(JSONObject) recipeDatas.get(j);
            JSONObject jsonObject=new JSONObject();
            jsonObject.put("substrate_PH",(Float.parseFloat(tempss.get("substrate_PH_start").toString())+Float.parseFloat(tempss.get("substrate_PH_end").toString()))/2);
            jsonObject.put("substrate_Conductivity",(Float.parseFloat(tempss.get("substrate_Conductivity_start").toString())+Float.parseFloat(tempss.get("substrate_Conductivity_end").toString()))/2);
            jsonObject.put("substrate_Temperature",(Float.parseFloat(tempss.get("substrate_Temperature_start").toString())+Float.parseFloat(tempss.get("substrate_Temperature_end").toString()))/2);
            jsonObject.put("ppfd",(Float.parseFloat(tempss.get("ppfd_start").toString())+Float.parseFloat(tempss.get("ppfd_end").toString()))/2);
            jsonObject.put("liquid_PH",(Float.parseFloat(tempss.get("liquid_PH_start").toString())+Float.parseFloat(tempss.get("liquid_PH_end").toString()))/2);
            jsonObject.put("substrate_Humidity",(Float.parseFloat(tempss.get("substrate_Humidity_start").toString())+Float.parseFloat(tempss.get("substrate_Humidity_end").toString()))/2);
            jsonObject.put("liquid_DOC",(Float.parseFloat(tempss.get("liquid_DOC_start").toString())+Float.parseFloat(tempss.get("liquid_DOC_end").toString()))/2);
            jsonObject.put("liquid_Conductivity",(Float.parseFloat(tempss.get("liquid_Conductivity_start").toString())+Float.parseFloat(tempss.get("liquid_Conductivity_end").toString()))/2);
            jsonObject.put("substrate_DOC",(Float.parseFloat(tempss.get("substrate_DOC_start").toString())+Float.parseFloat(tempss.get("substrate_DOC_end").toString()))/2);
            jsonObject.put("lai",(Float.parseFloat(tempss.get("lai_start").toString())+Float.parseFloat(tempss.get("lai_end").toString()))/2);
            jsonObject.put("carbon_Dioxide",(Float.parseFloat(tempss.get("carbon_Dioxide_start").toString())+Float.parseFloat(tempss.get("carbon_Dioxide_end").toString()))/2);
            jsonObject.put("illuminance",(Float.parseFloat(tempss.get("illuminance_start").toString())+Float.parseFloat(tempss.get("illuminance_end").toString()))/2);
            jsonObject.put("air_Temperature",(Float.parseFloat(tempss.get("air_Temperature_start").toString())+Float.parseFloat(tempss.get("air_Temperature_end").toString()))/2);
            jsonObject.put("air_Humidity",(Float.parseFloat(tempss.get("air_Humidity_start").toString())+Float.parseFloat(tempss.get("air_Humidity_end").toString()))/2);

            jsonObject.put("start_time",tempss.get("start_time").toString());
            jsonObject.put("end_time",tempss.get("end_time").toString());
            jsonObject.put("day",tempss.get("day").toString());
            recipeDatearray.add(jsonObject);

        }

        long pointTime=(endTimess-startTimess)/(size*days);
        JSONArray result_point_count_recipe=new JSONArray();
        JSONArray result_point_times=new JSONArray();
        //System.out.println("size:"+size);
        for (int i=0;i<size*days;i++){
            JSONObject times_point_value=new JSONObject();
            for (int y=0;y<recipeDatearray.size();y++){
                JSONObject recipeDatajs=(JSONObject) recipeDatearray.get(y);
                String daysInt=(String) recipeDatajs.get("day");
                String oneDyaStart=(String) recipeDatajs.get("start_time");
                String ontDayEnd=(String) recipeDatajs.get("end_time");
                long dayStart=0;
                long dayEnds=0;

                dayStart=(Long.parseLong(daysInt)-Long.parseLong(seq))*24*60*60*1000+Long.parseLong(oneDyaStart)*60*60*1000+privateRecipeTimes;


                dayEnds=(Long.parseLong(daysInt)-Long.parseLong(seq))*24*60*60*1000+privateRecipeTimes+Long.parseLong(ontDayEnd)*60*60*1000;

                if (startTimess>dayStart&&startTimess<=dayEnds){
                    times_point_value.put("substrate_PH",Float.parseFloat(recipeDatajs.get("substrate_PH").toString()));
                    times_point_value.put("substrate_Conductivity",Float.parseFloat(recipeDatajs.get("substrate_Conductivity").toString()));
                    times_point_value.put("substrate_Temperature",Float.parseFloat(recipeDatajs.get("substrate_Temperature").toString()));
                    times_point_value.put("ppfd",Float.parseFloat(recipeDatajs.get("ppfd").toString()));
                    times_point_value.put("liquid_PH",Float.parseFloat(recipeDatajs.get("liquid_PH").toString()));
                    times_point_value.put("substrate_Humidity",Float.parseFloat(recipeDatajs.get("substrate_Humidity").toString()));
                    times_point_value.put("liquid_DOC",Float.parseFloat(recipeDatajs.get("liquid_DOC").toString()));
                    times_point_value.put("liquid_Conductivity",Float.parseFloat(recipeDatajs.get("liquid_Conductivity").toString()));
                    times_point_value.put("substrate_DOC",Float.parseFloat(recipeDatajs.get("substrate_DOC").toString()));
                    times_point_value.put("lai",Float.parseFloat(recipeDatajs.get("lai").toString()));
                    times_point_value.put("carbon_Dioxide",Float.parseFloat(recipeDatajs.get("carbon_Dioxide").toString()));
                    times_point_value.put("illuminance",Float.parseFloat(recipeDatajs.get("illuminance").toString()));
                    times_point_value.put("air_Temperature",Float.parseFloat(recipeDatajs.get("air_Temperature").toString()));
                    times_point_value.put("air_Humidity",Float.parseFloat(recipeDatajs.get("air_Humidity").toString()));
                    //System.out.println("times_point_value"+times_point_value.toString());
                }

            }
            if (times_point_value.size()<1){
                times_point_value.put("substrate_PH",0);
                times_point_value.put("substrate_Conductivity",0);
                times_point_value.put("substrate_Temperature",0);
                times_point_value.put("ppfd",0);
                times_point_value.put("liquid_PH",0);
                times_point_value.put("substrate_Humidity",0);
                times_point_value.put("liquid_DOC",0);
                times_point_value.put("liquid_Conductivity",0);
                times_point_value.put("substrate_DOC",0);
                times_point_value.put("lai",0);
                times_point_value.put("carbon_Dioxide",0);
                times_point_value.put("illuminance",0);
                times_point_value.put("air_Temperature",0);
                times_point_value.put("air_Humidity",0);
            }
            startTimess=pointTime+startTimess;
            result_point_count_recipe.add(times_point_value);
            result_point_times.add(startTimess);
        }
        System.out.println("result_point_count_recipe:"+result_point_count_recipe.toString());

        JSONArray datases=new JSONArray();


        JSONObject temps=(JSONObject)result_point_count_recipe.get(0);

        Set<String> maps=temps.keySet();
        for (String key:maps){
            JSONObject datas=new JSONObject();
            JSONArray data=new JSONArray();


            JSONArray recipeArray=new JSONArray();
            for (int n=0;n<result_point_count_recipe.size();n++){
                JSONObject tempess=(JSONObject) result_point_count_recipe.get(n);
                recipeArray.add(tempess.get(key));
            }


            JSONObject recipeJs=new JSONObject();
            recipeJs.put("name","recipeData");
            recipeJs.put("data",recipeArray);
            data.add(recipeJs);

            datas.put("data",data);
            datas.put("name",(new StringBuffer()).append(Character.toUpperCase(key.charAt(0))).append(key.substring(1)).toString());
            datases.add(datas);
        }

        //时间
        JSONArray times=new JSONArray();
        for (int i=0;i<result_point_times.size();i++){
            long timeLong=(Long) result_point_times.get(i);
            String timeStr= sdf.format(timeLong);
            times.add(timeStr);
        }
        //数据
        JSONObject result_objects=new JSONObject();
        result_objects.put("data",datases);
        result_objects.put("time",times);
        return result_objects;
    }


    //修改配方数据
    public void modifyUserRecipe(String userId, List<PrivateRecipeData> privateRecipeDataList) {
        List<UserGateway> list=userGatewayMapper.selectByUserId(userId);
        if (list.size()<1){
            throw new BussinessException("-1","user does not exist");
        }
        String accountId=list.get(0).getAccount_id();
        for (PrivateRecipeData privateRecipeDatas:privateRecipeDataList){
            privateRecipeDatas.setAccount_id(accountId);
            privateRecipeDatas.setReserve03("1");
            privateRecipeDatas.setReserve04("1");
            privateRecipeDatas.setReserve05("1");
            privateRecipeDatas.setReserve06("1");
            privateRecipeDatas.setReserve07("1");

        }
        for (int i=0;i<privateRecipeDataList.size();i++){
            if (!privateRecipeDataList.get(i).getCrop_name().equals("")) {
                PrivateRecipe privateRecipe=new PrivateRecipe();
                privateRecipe.setCrop_name(privateRecipeDataList.get(i).getCrop_name());
                privateRecipe.setPrivate_recipe_id(privateRecipeDataList.get(i).getPrivate_recipe_id());
                privateRecipe.setAccount_id(accountId);
                int a=privateRecipeMapper.updateByPrimaryKeyAndAccountId(privateRecipe);
                if(a<1){
                    throw new BussinessException("-1","update privateRecipe faild");
                }
            }
            int n=privateRecipeDataMapper.updateByPrimaryKeyAndAccountId(privateRecipeDataList.get(i));
            if (n<1){
                throw new BussinessException("-1","update recipe faild");
            }
        }

    }

    //删除用户私有配方
    public void dropUserRecipe(String userId, PrivateRecipeData privateRecipeData) {
        List<UserGateway> list=userGatewayMapper.selectByUserId(userId);
        if (list.size()<1){
            throw new BussinessException("-1","user does not exist");
        }
        String accountId=list.get(0).getAccount_id();
        privateRecipeData.setAccount_id(accountId);

        PrivateRecipeIndex privateRecipeIndex=new PrivateRecipeIndex();
        privateRecipeIndex.setAccount_id(accountId);
        privateRecipeIndex.setPrivate_recipe_id(privateRecipeData.getPrivate_recipe_id());
        List<PrivateRecipeIndex> privateRecipeIndexList=privateRecipeIndexMapper.selectByPrivateRecipeIdAndAccountIds(privateRecipeIndex);
        //PrivateRecipeIndex recipeIndex=privateRecipeIndexMapper.selectByPrivateRecipeIdAndAccountId(privateRecipeIndex);
        if (privateRecipeIndexList.size()<0){
            throw new BussinessException("-1","The recipe is empty");
        }
        for (PrivateRecipeIndex privateRecipeIndex1s:privateRecipeIndexList){

                if (privateRecipeIndex1s.getStatus().equals("1")){
                    throw new BussinessException("-1","The recipe is running,Please stop running recipe first");
                }

        }
        if (privateRecipeIndexList.size()>0){
            int n=privateRecipeIndexMapper.deleteByPrivatePrcipeIdAndAccountId(privateRecipeIndex);
            if (n<1){
                throw new BussinessException("-1","delete region not executed recipe faild");
            }
        }





        //删除区域配方

        List<PrivateRecipeData> list1=privateRecipeDataMapper.selectRecipeDataByprivateRecipeIdAndAccountId(privateRecipeData);
        if (list1.size()>1){
        int a=privateRecipeDataMapper.deleteByPrivateRecipeIdAndAccountId(privateRecipeData);
        if (a<1){
            throw new BussinessException("-1","delete privateRecipeData faild");
        }
        }

        PrivateRecipe privateRecipe=new PrivateRecipe();
        privateRecipe.setAccount_id(accountId);
        privateRecipe.setPrivate_recipe_id(privateRecipeData.getPrivate_recipe_id());
        int k=privateRecipeMapper.deleteByPrivateRecipeIdAndAccountId(privateRecipe);
        if (k<1){
            throw new BussinessException("-1","delete privateRecipe faild");
        }

    }

    //添加用户私有配方名称
    public void addUserRecipeName(String userId, PrivateRecipe privateRecipe) {
        List<UserGateway> list=userGatewayMapper.selectByUserId(userId);
        if (list.size()<1){
            throw new BussinessException("-1","user does not exist");
        }
        String accountId =list.get(0).getAccount_id();
        privateRecipe.setAccount_id(accountId);
        PrivateRecipe privateRecipe1=privateRecipeMapper.selectRecipeBycropNameAndAccountId(privateRecipe);
        if (privateRecipe1!=null){
            throw new BussinessException("-1","The crop name already exists");
        }
        int n=privateRecipeMapper.insertSelective(privateRecipe);
        if (n<1){
            throw new BussinessException("-1","add private Recipe name faild");
        }

    }

    //批量添加用户私有配方数据
    public void addUserbatchPrcipes(String userId, LinkedList<PrivateRecipeData> privateRecipeDataLinkedList,String days,String privateRecipeId,String fewDays) {
        List<UserGateway> list=userGatewayMapper.selectByUserId(userId);
        if (list.size()<1){
            throw new BussinessException("-1","user does not exist");
        }
        String accountId=list.get(0).getAccount_id();
        PrivateRecipeData privateRecipeDataOne=new PrivateRecipeData();
        privateRecipeDataOne.setAccount_id(accountId);
        privateRecipeDataOne.setPrivate_recipe_id(privateRecipeId);
        LinkedList<PrivateRecipeData> recipeDataLinkedLists=privateRecipeDataMapper.selectRecipeDataByprivateRecipeIdAndAccountId(privateRecipeDataOne);
        int maxDays=0;
        int fewDayses=0;
        if (!fewDays.equals("")){
            //System.out.println("fewDays");
            fewDayses=Integer.parseInt(fewDays);
        }

        LinkedList<PrivateRecipeData> lastPrivaterecipeDate=new LinkedList<PrivateRecipeData>(); //如果数据从中间插入,则获取截断后段数据
        if (recipeDataLinkedLists.size()>1){
            for (PrivateRecipeData privateRecipeDatases:recipeDataLinkedLists){
                String recipeDay=privateRecipeDatases.getDay();
                if (!fewDays.equals("")){
                    if (maxDays<Integer.parseInt(recipeDay)&&Integer.parseInt(recipeDay)<=fewDayses){

                        maxDays=Integer.parseInt(recipeDay);
                    }
                }else {
                    if (maxDays<Integer.parseInt(recipeDay)){
                        maxDays=Integer.parseInt(recipeDay);
                    }
                }
                if (Integer.parseInt(recipeDay)>fewDayses&&fewDayses!=0){
                    lastPrivaterecipeDate.add(privateRecipeDatases);
                }
            }
        }

        //追加几天
        int dayss=0;
        if (!days.equals("")){
             dayss=Integer.parseInt(days);
        }


        if (dayss>=1){

            for (int p=1+maxDays;p<=dayss+maxDays;p++){
                for (PrivateRecipeData privateRecipeData:privateRecipeDataLinkedList){
                    privateRecipeData.setAccount_id(accountId);
                    privateRecipeData.setId(UUID.randomUUID().toString());
                    privateRecipeData.setReserve03("1");
                    privateRecipeData.setReserve05("1");
                    privateRecipeData.setDay(String.valueOf(p));
                    int n=privateRecipeDataMapper.insert(privateRecipeData);
                    if (n<1){
                        throw new BussinessException("-1","add privateRecipedata faild");
                    }
                }
            }
        }else {
            for (PrivateRecipeData privateRecipeData:privateRecipeDataLinkedList){
                privateRecipeData.setAccount_id(accountId);
                privateRecipeData.setId(UUID.randomUUID().toString());
                privateRecipeData.setReserve03("1");
                privateRecipeData.setReserve05("1");
                privateRecipeData.setDay("1");
                int n=privateRecipeDataMapper.insert(privateRecipeData);
                if (n<1){
                    throw new BussinessException("-1","add privateRecipedata faild");
                }
            }
        }

        //追加截断后半段数据
        if (lastPrivaterecipeDate.size()>=1){

            for (PrivateRecipeData privateRecipeData:lastPrivaterecipeDate){
                int  dayInt=Integer.parseInt(privateRecipeData.getDay());
                privateRecipeData.setDay(String.valueOf(dayInt+dayss));
                int n=privateRecipeDataMapper.updateByPrimaryKeyAndAccountId(privateRecipeData);
                if (n<1){
                    throw new BussinessException("-1","add privateRecipedata faild");
                }
            }
        }


    }

    //批量删除用户私有配方数据(天)
    public void dropUserbatchPrcipes(String userId, String startDay, String endDay,String privateRecipeId) {
        List<UserGateway> list=userGatewayMapper.selectByUserId(userId);
        if (list.size()<1){
            throw new BussinessException("user does not exist");
        }
        String accountId=list.get(0).getAccount_id();
        PrivateRecipeData privateRecipeData=new PrivateRecipeData();
        privateRecipeData.setAccount_id(accountId);
        privateRecipeData.setPrivate_recipe_id(privateRecipeId);
        LinkedList<PrivateRecipeData> privateRecipeDataLinkedList=privateRecipeDataMapper.selectRecipeDataByprivateRecipeIdAndAccountId(privateRecipeData);

        int endDayInt=Integer.parseInt(endDay);
        int startDayInt=Integer.parseInt(startDay);
        int dayess=endDayInt-startDayInt+1;
        //System.out.println("dayess:"+dayess);

        LinkedList<PrivateRecipeData> recipeDataLinkedList=new LinkedList<PrivateRecipeData>();
        for (PrivateRecipeData recipeData:privateRecipeDataLinkedList){
            int daysInt=Integer.parseInt(recipeData.getDay());
            if (daysInt>endDayInt){
                recipeDataLinkedList.add(recipeData);
            }
        }

        //删除数据
        for (int i=startDayInt;i<=endDayInt;i++){
            PrivateRecipeData privateRecipeData1=new PrivateRecipeData();
            privateRecipeData1.setDay(String.valueOf(i));
            privateRecipeData1.setAccount_id(accountId);
            privateRecipeData1.setPrivate_recipe_id(privateRecipeId);
            int n=privateRecipeDataMapper.deleteByPrimaryKeyAndDayAndAccountId(privateRecipeData1);
            if (n<1){
                throw new BussinessException("-1","delete privateRecipe faild");
            }
        }

        for (int j=0;j<recipeDataLinkedList.size();j++){
            PrivateRecipeData recipeDatas=recipeDataLinkedList.get(j);
            recipeDatas.setDay(String.valueOf(Integer.parseInt(recipeDatas.getDay())-dayess));
            int a=privateRecipeDataMapper.updateByPrimaryKeyAndAccountId(recipeDatas);
            if (a<1){
                throw new BussinessException("-1","update privateRecipe faild");
            }
        }


    }

    //修改用户私有配方名称
    public void modifyUserRecipeName(String userId, String privateRecipeId, String cropName) {
        List<UserGateway> list=userGatewayMapper.selectByUserId(userId);
        if (list.size()<1){
            throw new BussinessException("-1","user does not exist");
        }
        String accountId=list.get(0).getAccount_id();

        PrivateRecipeData privateRecipeData=new PrivateRecipeData();
        privateRecipeData.setCrop_name(cropName);
        privateRecipeData.setPrivate_recipe_id(privateRecipeId);
        privateRecipeData.setAccount_id(accountId);
        LinkedList<PrivateRecipeData> privateRecipeDataLinkedList=privateRecipeDataMapper.selectRecipeDataByprivateRecipeIdAndAccountId(privateRecipeData);
        if (privateRecipeDataLinkedList.size()>0){
            int p=privateRecipeDataMapper.updateByPrivateRecipeIdAndAccountId(privateRecipeData);
            if (p<1){
                throw new BussinessException("-1","update privateRecipeData faild");
            }
        }

        PrivateRecipe privateRecipe=new PrivateRecipe();
        privateRecipe.setAccount_id(accountId);
        privateRecipe.setCrop_name(cropName);;
        privateRecipe.setPrivate_recipe_id(privateRecipeId);
        int n=privateRecipeMapper.updateByPrimaryKeyAndAccountId(privateRecipe);
        if (n<1){
            throw new BussinessException("-1","update privateRecpe faild");
        }

    }

    //修改批量用户私有配方数据
    public void modifyUserRecipebatchData(String userId,String privateRecipeId, LinkedList<PrivateRecipeData> recipeDataLinkedList,String startDay,String endDay) {
        List<UserGateway> list=userGatewayMapper.selectByUserId(userId);
        if (list.size()<1){
            throw new BussinessException("-1","user does not exist");
        }
        String accountId=list.get(0).getAccount_id();
        int startDayInt=Integer.parseInt(startDay);
        int endDayInt=Integer.parseInt(endDay);

        for (int p=startDayInt;p<=endDayInt;p++){
            PrivateRecipeData privateRecipeDatas=new PrivateRecipeData();
            privateRecipeDatas.setAccount_id(accountId);
            privateRecipeDatas.setDay(String.valueOf(p));
            privateRecipeDatas.setPrivate_recipe_id(privateRecipeId);
            int k=privateRecipeDataMapper.deleteByPrimaryKeyAndDayAndAccountId(privateRecipeDatas);
            if (k<1){
                throw new BussinessException("-1","delete privateRecipeData faild");
            }
            for (PrivateRecipeData privateRecipeData:recipeDataLinkedList){
                privateRecipeData.setAccount_id(accountId);
                privateRecipeData.setDay(String.valueOf(p));
                privateRecipeData.setId(UUID.randomUUID().toString());
                privateRecipeData.setReserve05("1");
                privateRecipeData.setPrivate_recipe_id(privateRecipeId);
                int n=privateRecipeDataMapper.insert(privateRecipeData);
                if (n<1){
                    throw new BussinessException("-1","insert privateRecipe faild");
                }
            }

        }

    }

    //删除区域配方
    @Override
    public void deleteRegionRecipe(String userId, PrivateRecipeIndex privateRecipeIndex) {
        List<UserGateway> list=userGatewayMapper.selectByUserId(userId);
        if (list.size()<1){
            throw new BussinessException("-1","user does not exist");
        }
        String accountId=list.get(0).getAccount_id();
        privateRecipeIndex.setAccount_id(accountId);
        int n=privateRecipeIndexMapper.deleteByPrivatePrcipeIdAndAccountIdAndRegionId(privateRecipeIndex);
        if (n<1){
            throw new BussinessException("-1","delete region recipe faild");
        }
    }

    //获取传感器历史数据
    public JSONArray getSenorData(String sensor_guid, String accuont_id, String start_time, String end_time) {

        String sql = "select * from table_sensor_record " +
                "where table_device_guid = '" + sensor_guid + "' AND " + "account_id ='" + accuont_id + "'AND " +
                "record_time between STR_TO_DATE('" + start_time + "','%Y-%m-%d %H:%i:%s') and STR_TO_DATE('" + end_time + "','%Y-%m-%d %H:%i:%s')" +
                "ORDER BY record_time";

        logger.debug(sql);
        Connection connection = null;
        Statement statement = null;
        JSONArray jsonArray = new JSONArray();

        try {
            PropsUtil propsUtil = new PropsUtil("jdbc.properties");

            String driver = propsUtil.get("driver");
            String url = propsUtil.get("url");
            String username = propsUtil.get("username");
            String password = propsUtil.get("password");
            Class.forName(driver);

            connection = DriverManager.getConnection(url, username, password);
            statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);


            while (resultSet.next()) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("record_guid", resultSet.getString("record_guid"));
                jsonObject.put("table_device_guid", resultSet.getString("table_device_guid"));
                jsonObject.put("record_time", resultSet.getString("record_time"));
                jsonObject.put("air_temperature", resultSet.getString("air_temperature"));
                jsonObject.put("air_humidity", resultSet.getString("air_humidity"));
                jsonObject.put("soil_temperature", resultSet.getString("soil_temperature"));
                jsonObject.put("soil_humidity", resultSet.getString("soil_humidity"));
                jsonObject.put("soil_PH_value", resultSet.getString("soil_PH_value"));
                jsonObject.put("carbon_Dioxide", resultSet.getString("carbon_Dioxide"));
                jsonObject.put("illuminance", resultSet.getString("illuminance"));
                jsonObject.put("soil_conductivity", resultSet.getString("soil_conductivity"));
                jsonObject.put("photons", resultSet.getString("photons"));
                jsonObject.put("liquid_PH_value", resultSet.getString("liquid_PH_value"));
                jsonObject.put("lai_value", resultSet.getString("lai_value"));
                jsonArray.add(jsonObject);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return jsonArray;
    }


}
