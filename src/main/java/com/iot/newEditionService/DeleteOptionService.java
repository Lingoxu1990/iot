package com.iot.newEditionService;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * Created by adminchen on 16/7/21.
 */
public interface DeleteOptionService {

    JSONArray selectObjects(String user_id,String gateway_id,JSONObject regions, JSONArray scenes, JSONArray groups,JSONArray regionDevice);
}