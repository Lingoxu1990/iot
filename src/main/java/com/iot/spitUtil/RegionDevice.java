package com.iot.spitUtil;

import org.springframework.stereotype.Service;

/**
 *  1 判断添加进入区域的多个设备是否跨网关
 *  2 如果不跨网关,直接返回入参
 *  3 如果跨网关,拆分区域设备,分别写入指定的网关
 *  4 只对单个区域生效()
 * Created by xulingo on 16/4/11.
 */

@Service
public class RegionDevice {
//
//
//
//    @Resource
//    private TableDeviceMapper tabledevicemapper;
//    @Resource
//    private TableRegionMapper tableRegionMapper;
//    @Resource
//    private TableChannelMapper tableChannelMapper;
//    /**
//     *
//     * @param alldevies
//     * {
//     * "id":"主键(非必需)",
//     * "region_device_guid":"区域设备主键",
//     * "region_guid":"区域主键",
//     * "region_addr":"区域地址",
//     * "reiong_name":"区域名称",
//     * "table_device_guid":"设备主键",
//     * "gateway_id":"设备网关地址",
//     * "device_addr":"设备地址",
//     * "device_name":"设备名称",
//     * }
//     * @return
//     */
//    public Map<String,JSONObject> deviceCopy(JSONArray alldevies,String sourceId){
//
//        int device_num =alldevies.size();
//
//        Map<String,JSONArray> tempGateway_id = new HashMap<String, JSONArray>();
//
//        //将设备按照 网关进行分组
//        for (int i = 0; i <device_num ; i++) {
//
//            JSONObject jsonobject=(JSONObject) alldevies.get(i);
//
//            String gateway_id = (String) jsonobject.get("gateway_id");
//
//            JSONArray entities= tempGateway_id.get(gateway_id);
//
//            if (entities==null){
//                entities=new JSONArray();
//                entities.add(jsonobject);
//                tempGateway_id.put(gateway_id,entities);
//            }else {
//                entities.add(jsonobject);
//                tempGateway_id.put(gateway_id,entities);
//            }
//
//        }
//        //获得按照网关分组的设备群
//        Set<Map.Entry<String,JSONArray>> entries = tempGateway_id.entrySet();
//        Map<String,JSONObject> result = new HashMap<String, JSONObject>();
//
//        for (Map.Entry<String,JSONArray> entry: entries) {
//            //key为gateway_id
//           String key=  entry.getKey();
//            //value 为设备列表
//           JSONArray value =  entry.getValue();
//
//            int size = value.size();
//
//            JSONArray region_devices_channels = new JSONArray();
//
//            for (int i = 0; i <size ; i++) {
//
//                JSONObject region_deviece = (JSONObject) value.get(i);
//                String guid = (String) region_deviece.get("table_device_guid");
//
//                List<JSONObject> list = MysqlUtil.getDeviceInfo("table_device",guid);
//
//                //根据主键查询出的设备详细信息
//                JSONObject device = list.get(0);
//
//                String device_type = (String) device.get("device_type");
//
//                if (device_type.equals(Param.DEVICETYPE)){
//
//                    String region_guid = (String) region_deviece.get("region_guid");
//
//                    TableRegion tableRegion = tableRegionMapper.selectByRegionGuid(region_guid);
//
//                    if(!tableRegion.getGateway_id().equals(key)){
//
//                        device.remove("device_valid");
//                        device.put("device_valid","A");
//                        device.remove("id");
//                        device.remove("account_id");
//                        device.remove("region_bunding");
//
//                        JSONArray specialDevics = new JSONArray();
//                        specialDevics.add(device);
//
//                        String [] sql = sqlControlUtil.addObjects("table_device",specialDevics);
//
//                        OutPutSocketMessage outPutSocketMessage= new OutPutSocketMessage();
//
//                        outPutSocketMessage.setDestinationID(tableRegion.getGateway_id());
//                        outPutSocketMessage.setPackegType(-1);
//                        outPutSocketMessage.setSql(sql[0]);
//                        outPutSocketMessage.setSourceID(sourceId);
//                        outPutSocketMessage.setMessage("Sensors copy");
//                        outPutSocketMessage.setType("NULL");
//                        System.out.println(outPutSocketMessage.test().toString());
////                        JSONObject resp =outPutSocketMessage.sendMessag(SourceID);
////
////                        if (!resp.get("Status").equals("0")){
////
////                        }
//                    }
//                }
//
//                List<TableChannel> channelList = tableChannelMapper.selectByDeviceGuid(guid);
//
//
//                // 设备通道扩展
//                for (TableChannel channel: channelList) {
//
//                    JSONObject json= new JSONObject();
//
//                    json.put("region_devece_guid", UUID.randomUUID().toString());
//                    json.put("region_guid",region_deviece.get("region_guid"));
//                    json.put("region_addr",region_deviece.get("region_addr"));
//                    json.put("reiong_name",region_deviece.get("reiong_name"));
//                    json.put("table_device_guid",region_deviece.get("table_device_guid"));
//                    json.put("gateway_id",region_deviece.get("gateway_id"));
//                    json.put("device_addr",region_deviece.get("device_addr"));
//                    json.put("device_name",region_deviece.get("device_name"));
//                    json.put("channel_class",channel.getChannel_class());
//                    json.put("channel_guid",channel.getChannel_guid());
//                    json.put("channel_name",channel.getChannel_name());
//                    json.put("channel_type",channel.getChannel_type());
//                    json.put("channel_bit_num",channel.getChannel_bit_num());
//
//                    region_devices_channels.add(json);
//                }
//
//
//            }
//
//            JSONObject temp = new JSONObject();
//            temp.put("table_region_device",region_devices_channels);
//            result.put(key,temp);
//        }
//
//
//        return  result;
//    }
//

}
