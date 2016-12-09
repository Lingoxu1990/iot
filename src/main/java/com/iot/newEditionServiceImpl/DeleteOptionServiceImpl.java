package com.iot.newEditionServiceImpl;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.iot.exception.BussinessException;
import com.iot.mapper.*;
import com.iot.newEditionService.DeleteOptionService;
import com.iot.pojo.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by adminchen on 16/7/21.
 */
@Service
public class DeleteOptionServiceImpl implements DeleteOptionService {
    @Resource
    private TableSceneMembersMapper tableSceneMembersMapper;

    @Resource
    private UserGatewayMapper userGatewayMapper;

    @Resource
    private TableRegionSceneMapper tableRegionSceneMapper;

    @Resource
    private TableGroupMembersMapper tableGroupMembersMapper;

    @Resource
    private TableRegionGroupMapper tableRegionGroupMapper;

    @Resource
    private TableRegionDeviceMapper tableRegionDeviceMapper;
    @Resource
    private TableRegionMapper tableRegionMapper;

    @Resource
    private PrivateRecipeIndexMapper privateRecipeIndexMapper;

    @Resource
    private PrivateRecipeMapper privateRecipeMapper;

    public JSONArray selectObjects(String user_id,String gateway_id,JSONObject regions, JSONArray scenes, JSONArray groups,JSONArray regionDevice) {
        List<UserGateway> list=userGatewayMapper.selectByUserId(user_id);
        if (list.size()<1){
            throw new BussinessException("-1","user does  not exist");
        }
        String account_id=list.get(0).getAccount_id();

        JSONArray objects=new JSONArray();

        String region_guid=(String) regions.get("region_guid");

        //区域配方
        JSONArray regionRecipe=new JSONArray();
        if (region_guid!=null&&!region_guid.equals("")){
            //获取场景信息
            TableRegionScene tableRegionScene=new TableRegionScene();
            tableRegionScene.setAccount_id(account_id);
            tableRegionScene.setGateway_id(gateway_id);
            tableRegionScene.setRegion_guid(region_guid);
            List<TableRegionScene> tableRegionSceneList=tableRegionSceneMapper.findRegionSceneByAccountIdAndRegionguidAndGatewayId(tableRegionScene);
            if (tableRegionSceneList.size()>0){
                scenes.remove(0);
                for (TableRegionScene regionScene:tableRegionSceneList){
                    JSONObject sceness=new JSONObject();
                    sceness.put("scene_guid",regionScene.getTable_scene_guid());
                    sceness.put("members",new JSONArray());
                    scenes.add(sceness);
                }
            }
            System.out.println("scenes:"+scenes.toString());
            //获取组信息
            TableRegionGroup tableRegionGroup=new TableRegionGroup();
            tableRegionGroup.setRegion_guid(region_guid);
            tableRegionGroup.setGateway_id(gateway_id);
            tableRegionGroup.setAccount_id(account_id);
            List<TableRegionGroup> tableRegionGroupList=tableRegionGroupMapper.findRegionGroupByAccountIdAndRegionguidAndGatewayid(tableRegionGroup);
            if (tableRegionGroupList.size()>0){
                groups.remove(0);

                for (TableRegionGroup regionGroup:tableRegionGroupList){
                    JSONObject groupss=new JSONObject();
                    groupss.put("group_guid",regionGroup.getTable_group_guid());
                    groupss.put("members",new JSONArray());
                    groups.add(groupss);
                }
            }

            //获取区域设备
            TableRegionDevice tableRegionDevice=new TableRegionDevice();
            tableRegionDevice.setAccount_id(account_id);
            tableRegionDevice.setGateway_id(gateway_id);
            tableRegionDevice.setRegion_guid(region_guid);
            List<TableRegionDevice> regionDeviceList=tableRegionDeviceMapper.selectByAccountIdAndRegiongGuidGateway_idgroupByDeviceGuid(tableRegionDevice);

            if (regionDeviceList.size()>0){
                //int c=0;
                regionDevice.remove(0);
                for (TableRegionDevice regionDevice1:regionDeviceList){
                    JSONObject regionDevices=new JSONObject() ;
                    regionDevices.put("table_device_guid",regionDevice1.getTable_device_guid());
                    regionDevices.put("region_guid",regionDevice1.getRegion_guid());
                    regionDevice.add(regionDevices);

                }

            }

            //获取区域配方
            PrivateRecipeIndex privateRecipeIndex=new PrivateRecipeIndex();
            privateRecipeIndex.setAccount_id(account_id);
            privateRecipeIndex.setRegion_guid(region_guid);
            List<PrivateRecipeIndex> privateRecipeIndexList=privateRecipeIndexMapper.selectByRegionAndAccountId(privateRecipeIndex);
            if (privateRecipeIndexList.size()>0){
                for (PrivateRecipeIndex privateRecipeIndex1:privateRecipeIndexList){
                    if (privateRecipeIndex1.getStatus().equals("1")){
                        throw new BussinessException("-1","Recipe is running, you must stop it first !");
                    }
                }

                regionRecipe=(JSONArray) JSONArray.toJSON(privateRecipeIndexList);
            }

        }
        //
        if (scenes.size()>0){
            for (int i=0;i<scenes.size();i++){
                JSONObject scene=(JSONObject) scenes.get(i);
                JSONArray members=(JSONArray) scene.get("members");

                System.out.println(members.toString());

                String scene_guid=(String) scene.get("scene_guid");
                System.out.println("scene_guid:"+scene_guid);
                TableRegionScene tableRegionScene=new TableRegionScene();
                tableRegionScene.setAccount_id(account_id);
                tableRegionScene.setGateway_id(gateway_id);
                tableRegionScene.setTable_scene_guid(scene_guid);
                //删除场景
                if ((!scene_guid.equals("")&&scene_guid!=null)){
                    TableSceneMembers tableSceneMembers=new TableSceneMembers();
                    tableSceneMembers.setGateway_id(gateway_id);
                    tableSceneMembers.setAccount_id(account_id);
                    tableSceneMembers.setTable_scene_guid(scene_guid);
                    //删除场景成员信息
                    List<TableSceneMembers> sceneMembersList=tableSceneMembersMapper.selectSceneMemberByScene_guidAndGatewayIdAndAccountId(tableSceneMembers);
                    for (TableSceneMembers tableSceneMembers1:sceneMembersList){
                        //System.out.println("删除场景成员");
                        JSONObject data=new JSONObject();
                        JSONArray table_scene_members=new JSONArray();
                        JSONObject sceneMembersData=new JSONObject();
                        sceneMembersData.put("device_addr",tableSceneMembers1.getDevice_addr());
                        sceneMembersData.put("scene_addr",tableSceneMembers1.getscene_addr());
                        sceneMembersData.put("gateway_id",tableSceneMembers1.getGateway_id());
                        table_scene_members.add(sceneMembersData);
                        data.put("user_id",user_id);
                        data.put("table_scene_members",table_scene_members);

                        JSONObject sceneMembers=new JSONObject();
                        sceneMembers.put("data",data);
                        sceneMembers.put("url","device/scene/scene_members");
                        sceneMembers.put("method","delete");
                        sceneMembers.put("message","deleting "+tableSceneMembers1.getDevice_name());
                        objects.add(sceneMembers);
                    }

                    //删除场景信息
                    TableRegionScene tableRegionScene1=tableRegionSceneMapper.findRegionSceneByAccountIdAndSceneguidAndGatewayId(tableRegionScene);
                    if (tableRegionScene1==null){
                        throw new BussinessException("-1","RegionScene does  not exist");
                    }
                    //System.out.println("删除场景");

                    JSONObject data=new JSONObject();
                    JSONArray table_region_scene=new JSONArray();
                    JSONObject sceneData=new JSONObject();
                    sceneData.put("scene_addr",tableRegionScene1.getScene_addr());
                    sceneData.put("table_scene_guid",tableRegionScene1.getTable_scene_guid());
                    sceneData.put("gateway_id",tableRegionScene1.getGateway_id());
                    table_region_scene.add(sceneData);
                    data.put("user_id",user_id);
                    data.put("table_region_scene",table_region_scene);
                    JSONObject scenejson=new JSONObject();
                    scenejson.put("data",data);
                    scenejson.put("url","region/scene");
                    scenejson.put("method","delete");
                    scenejson.put("message","deleting "+tableRegionScene1.getScene_name());
                    objects.add(scenejson);

                }

                //删除成员信息
                //System.out.println("sceness:"+members.size());
                if (members.size()>0&&!members.equals("")&&members!=null){
                    for (int p=0;p<members.size();p++){
                        JSONObject devices=(JSONObject) members.get(p);
                        //String device_addr=(String) devices.get("device_addr");
                        String scene_members_guid=(String) devices.get("scene_members_guid");
                        System.out.println("scene_members_guid:"+scene_members_guid);

                        if (scene_members_guid!=null&&!scene_members_guid.equals("")){
                            System.out.println("删除场景成员");
                            TableSceneMembers tableSceneMembers=new TableSceneMembers();
                            tableSceneMembers.setAccount_id(account_id);
                            tableSceneMembers.setGateway_id(gateway_id);
                            tableSceneMembers.setScene_members_guid(scene_members_guid);
                            TableSceneMembers tableSceneMembers1=tableSceneMembersMapper.selectSceneMemberBysceneMemberGuidAndGatewayIdAndAccountIdAnd(tableSceneMembers);
                            JSONObject data=new JSONObject();

                            JSONArray table_scene_members=new JSONArray();
                            JSONObject sceneMembersData=new JSONObject();
                            sceneMembersData.put("device_addr",tableSceneMembers1.getDevice_addr());
                            sceneMembersData.put("scene_addr",tableSceneMembers1.getscene_addr());
                            sceneMembersData.put("gateway_id",tableSceneMembers1.getGateway_id());
                            table_scene_members.add(sceneMembersData);
                            data.put("user_id",user_id);
                            data.put("table_scene_members",table_scene_members);

                            JSONObject sceneMembers=new JSONObject();
                            sceneMembers.put("data",data);
                            sceneMembers.put("url","device/scene/scene_members");
                            sceneMembers.put("method","delete");
                            sceneMembers.put("message","deleting "+tableSceneMembers1.getDevice_name());
                            objects.add(sceneMembers);
                        }
                    }
                }
            }
        }

        //删除组信息
        if (groups.size()>0){
            for (int i=0;i<groups.size();i++){
                JSONObject grop=(JSONObject) groups.get(i);
                JSONArray members=(JSONArray) grop.get("members");
                String group_guid=(String) grop.get("group_guid");

                TableRegionGroup tableRegionGroup=new TableRegionGroup();
                tableRegionGroup.setAccount_id(account_id);
                tableRegionGroup.setGateway_id(gateway_id);
                tableRegionGroup.setTable_group_guid(group_guid);

                if (group_guid!=null&&!group_guid.equals("")){
                    TableGroupMembers tableGroupMembers=new TableGroupMembers();
                    tableGroupMembers.setGateway_id(gateway_id);
                    tableGroupMembers.setAccount_id(account_id);
                    tableGroupMembers.setTable_group_guid(group_guid);
                    //删除组成员信息
                    List<TableGroupMembers> groupMembersList=tableGroupMembersMapper.selectByGroupGuidAndAccount_idAndGateway_id(tableGroupMembers);
                    for (TableGroupMembers tableGroupMembers1:groupMembersList){
                        System.out.println("删除组成员");
                        JSONObject data=new JSONObject();

                        JSONArray table_group_members=new JSONArray();
                        JSONObject goupMembersData=new JSONObject();
                        goupMembersData.put("device_addr",tableGroupMembers1.getDevice_addr());
                        goupMembersData.put("gateway_id",tableGroupMembers1.getGateway_id());
                        goupMembersData.put("table_group_guid",tableGroupMembers.getTable_group_guid());
                        goupMembersData.put("group_addr",tableGroupMembers1.getGroup_addr());
                        table_group_members.add(goupMembersData);
                        data.put("user_id",user_id);
                        data.put("table_group_members",table_group_members);
                        JSONObject groupMembers=new JSONObject();
                        groupMembers.put("data",data);
                        groupMembers.put("url","device/group/group_member");
                        groupMembers.put("method","delete");
                        groupMembers.put("message","deleting "+tableGroupMembers1.getDevice_name());
                        objects.add(groupMembers);
                    }
                    //删除组信息
                    List<TableRegionGroup> tableRegionGroupList=tableRegionGroupMapper.findRegionGroupByAccountIdAndGroupGuidAndGatewayid(tableRegionGroup);
                    if(tableRegionGroupList.size()>0){
                        TableRegionGroup tableRegionGroup1=tableRegionGroupList.get(0);
                        System.out.println("删除组");
                        JSONObject data=new JSONObject();
                        JSONArray table_region_group=new JSONArray();
                        JSONObject groupData=new JSONObject();
                        groupData.put("group_addr",tableRegionGroup1.getGroup_addr());
                        groupData.put("gateway_id",tableRegionGroup1.getGateway_id());
                        groupData.put("table_group_guid",tableRegionGroup1.getTable_group_guid());
                        table_region_group.add(groupData);
                        data.put("table_region_group",table_region_group);
                        data.put("user_id",user_id);
                        JSONObject groupjson=new JSONObject();
                        groupjson.put("data",data);
                        groupjson.put("url","region/group");
                        groupjson.put("method","delete");
                        groupjson.put("message","deleting "+tableRegionGroup1.getGroup_name());
                        objects.add(groupjson);
                    }

                }

                //删除成员
                if (members.size()>0&&members.equals("")&&members!=null){
                    for (int p=0;p<members.size();p++){
                        JSONObject devices=(JSONObject) members.get(p);
                        String device_addr=(String) devices.get("device_addr");
                        String group_members_guid=(String) devices.get("scene_members_guid");
                        System.out.println("group_members_guid:"+group_members_guid);

                        if ((device_addr!=null&&!device_addr.equals(""))&&(group_members_guid!=null&&!group_members_guid.equals(""))){

                            TableGroupMembers tableGroupMembers=new TableGroupMembers();
                            tableGroupMembers.setAccount_id(account_id);
                            tableGroupMembers.setGateway_id(gateway_id);
                            tableGroupMembers.setGroup_members_guid(group_members_guid);
                            TableGroupMembers tableGroupMembers1=tableGroupMembersMapper.selectByGroupMembersGuidAndAccount_idAndGateway_id(tableGroupMembers);
                            JSONObject data=new JSONObject();
                            JSONArray table_group_members=new JSONArray();
                            JSONObject goupMembersData=new JSONObject();
                            goupMembersData.put("device_addr",tableGroupMembers1.getDevice_addr());
                            goupMembersData.put("gateway_id",tableGroupMembers1.getGateway_id());
                            goupMembersData.put("table_group_guid",tableGroupMembers.getTable_group_guid());
                            goupMembersData.put("group_addr",tableGroupMembers1.getGroup_addr());
                            table_group_members.add(goupMembersData);
                            data.put("user_id",user_id);
                            data.put("table_group_members",table_group_members);

                            JSONObject sceneMembers=new JSONObject();
                            sceneMembers.put("data",data);
                            sceneMembers.put("url","device/group/group_member");
                            sceneMembers.put("method","delete");
                            sceneMembers.put("message","deleting "+tableGroupMembers1.getDevice_name());
                            objects.add(sceneMembers);
                        }
                    }
                }



            }
        }
        //删除区域设备
        if (regionDevice.size()>0){

            for (int p=0;p<regionDevice.size();p++){
                //System.out.println("p:"+p);
                JSONObject regionDeviceObject=(JSONObject) regionDevice.get(p);
                String region_guids=(String) regionDeviceObject.get("region_guid");
                String table_device_guid=(String) regionDeviceObject.get("table_device_guid");
                if ((region_guids!=null&&!region_guids.equals(""))&&(table_device_guid!=null&&!table_device_guid.equals(""))){
                    TableRegionDevice tableRegionDevice=new TableRegionDevice();
                    tableRegionDevice.setAccount_id(account_id);
                    tableRegionDevice.setTable_device_guid(table_device_guid);
                    tableRegionDevice.setRegion_guid(region_guids);
                    tableRegionDevice.setGateway_id(gateway_id);
                    List<TableRegionDevice> tableRegionDeviceList=tableRegionDeviceMapper.selectByAccountIdAndRegiongGuidGateway_idAndDeviceGuid(tableRegionDevice);
                    TableRegionDevice tableRegionDevice1=tableRegionDeviceList.get(0);
                    //System.out.println("删除区域设备");
                    JSONObject data=new JSONObject();
                    JSONArray table_region_device=new JSONArray();
                    JSONObject regionDeviceData=new JSONObject();
                    regionDeviceData.put("gateway_id",tableRegionDevice1.getGateway_id());
                    regionDeviceData.put("region_guid",tableRegionDevice1.getRegion_guid());
                    regionDeviceData.put("region_addr",tableRegionDevice1.getRegion_addr());
                    regionDeviceData.put("device_addr",tableRegionDevice1.getDevice_addr());
                    regionDeviceData.put("table_device_guid",tableRegionDevice1.getTable_device_guid());
                    table_region_device.add(regionDeviceData);
                    data.put("table_region_device",table_region_device);
                    data.put("user_id",user_id);
                    JSONObject devicejson=new JSONObject();
                    devicejson.put("data",data);
                    devicejson.put("url","region/device");
                    devicejson.put("method","delete");
                    devicejson.put("message","deleting "+tableRegionDevice1.getDevice_name());
                    objects.add(devicejson);

                }

            }

        }

        //删除 区域配方
        if (regionRecipe.size()>0){
            for (int k=0;k<regionRecipe.size();k++){
                JSONObject regionRecipeObject=(JSONObject) regionRecipe.get(k);
                String regionGuid=(String) regionRecipeObject.get("region_guid");
                String private_recipe_id=(String)regionRecipeObject.get("private_recipe_id");
                if (regionGuid!=null){
                    PrivateRecipe privateRecipe=new PrivateRecipe();
                    privateRecipe.setAccount_id(account_id);
                    privateRecipe.setPrivate_recipe_id(private_recipe_id);
                    PrivateRecipe privateRecipe1=privateRecipeMapper.selectRecipeOne(privateRecipe);

                        JSONObject data=new JSONObject();
                        JSONArray regionRecipes=new JSONArray();
                        JSONObject regionRecipeData=new JSONObject();
                        regionRecipeData.put("region_guid",regionGuid);
                        //regionRecipeData.put("gateway_id",gateway_id);
                        regionRecipeData.put("private_recipe_id",private_recipe_id);
                        regionRecipes.add(regionRecipeData);
                        data.put("private_recipe_index",regionRecipes);
                        data.put("user_id",user_id);
                        JSONObject recipejson=new JSONObject();

                        recipejson.put("data",data);
                        recipejson.put("url","region/recipe");
                        recipejson.put("method","delete");
                        recipejson.put("message","deleting "+privateRecipe1.getCrop_name());
                        objects.add(recipejson);


                }
            }
        }
        //删除区域
        if (region_guid!=null||!region_guid.equals("")){
            TableRegion tableRegion=new TableRegion();
            tableRegion.setAccount_id(account_id);
            tableRegion.setGateway_id(gateway_id);
            tableRegion.setRegion_guid(region_guid);
            TableRegion tableRegion1=tableRegionMapper.selectByRegionGuidAndGateway_idAndAccount_id(tableRegion);
            if (tableRegion1!=null){
                JSONObject data=new JSONObject();
                JSONArray table_region=new JSONArray();
                JSONObject regionData=new JSONObject();
                regionData.put("gateway_id",tableRegion1.getGateway_id());
                regionData.put("region_guid",tableRegion1.getRegion_guid());
                regionData.put("region_addr",tableRegion1.getRegion_addr());
                table_region.add(regionData);
                data.put("user_id",user_id);
                data.put("table_region",table_region);
                JSONObject regionjson=new JSONObject();
                regionjson.put("data",data);
                regionjson.put("url","region");
                regionjson.put("method","delete");
                regionjson.put("message","deleting "+tableRegion1.getRegion_name());
                objects.add(regionjson);

            }
        }



        return objects;
    }
}