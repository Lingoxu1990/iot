package com.iot.filters;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.iot.exception.BussinessException;
import com.iot.mapper.UserTableMapper;
import com.iot.pojo.UserTable;

import com.iot.utils.ParamUtils;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Set;

/**
 * Created by adminchen on 16/9/14.
 */

public class UrlInterceptor implements HandlerInterceptor {
    @Resource
    private UserTableMapper userTableMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestUri = request.getRequestURI();
        System.out.println("uri = "+requestUri);
        System.out.println("url = "+request.getRequestURL());
        //String contextPath = request.getContextPath();
        //String url = request.getScheme()+"://"+request.getHeader("host")+request.getRequestURI();

        JSONArray requetstUrles=new JSONArray();
//设备
        JSONObject objects7=new JSONObject();
        objects7.put("/device/name","put");
        requetstUrles.add(objects7);

        JSONObject objects10=new JSONObject();
        objects10.put("/region/device","post");
        requetstUrles.add(objects10);

        JSONObject objects11=new JSONObject();
        objects11.put("/device/group/group_member","post");
        requetstUrles.add(objects11);

        JSONObject objects12=new JSONObject();
        objects12.put("/device/group/group_member","delete");
        requetstUrles.add(objects12);

        JSONObject objects14=new JSONObject();
        objects14.put("/region/device","delete");
        requetstUrles.add(objects14);

        JSONObject objects18=new JSONObject();
        objects18.put("/device/scene/scene_members","delete");
        requetstUrles.add(objects18);

        JSONObject objects20=new JSONObject();
        objects20.put("/device/scene/scene_members","post");
        requetstUrles.add(objects20);

        JSONObject objects23=new JSONObject();
        objects23.put("/regionName","put");
        requetstUrles.add(objects23);

        JSONObject objects24=new JSONObject();
        objects24.put("/region/scene","put");
        requetstUrles.add(objects24);

        JSONObject objects25=new JSONObject();
        objects25.put("/region/group","put");
        requetstUrles.add(objects25);

        JSONObject objects26=new JSONObject();
        objects26.put("/region/group","delete");
        requetstUrles.add(objects26);

        JSONObject objects27=new JSONObject();
        objects27.put("/deleteOptions","post");
        requetstUrles.add(objects27);

        JSONObject objects28=new JSONObject();
        objects28.put("/device/group","post");
        requetstUrles.add(objects28);

        JSONObject objects31=new JSONObject();
        objects31.put("/device/scene","post");
        requetstUrles.add(objects31);

        JSONObject objects32=new JSONObject();
        objects32.put("/region/group","post");
        requetstUrles.add(objects32);

        JSONObject objects33=new JSONObject();
        objects33.put("/region/scene","delete");
        requetstUrles.add(objects33);

        JSONObject objects34=new JSONObject();
        objects34.put("/region/scene","post");
        requetstUrles.add(objects34);

        JSONObject objects35=new JSONObject();
        objects35.put("/region","delete");
        requetstUrles.add(objects35);

        JSONObject objects36=new JSONObject();
        objects36.put("/region","post");
        requetstUrles.add(objects36);

        JSONObject objects38=new JSONObject();
        objects38.put("/table_device","put");
        requetstUrles.add(objects38);

        JSONObject objects45=new JSONObject();
        objects45.put("/recipe/addRegionRecipe","post");
        requetstUrles.add(objects45);

        JSONObject objects47=new JSONObject();
        objects47.put("/recipe/modifyUserRecipeName","put");
        requetstUrles.add(objects47);

        JSONObject objects48=new JSONObject();
        objects48.put("/recipe/modifyUserBatchPrcipeData","put");
        requetstUrles.add(objects48);

        JSONObject objects49=new JSONObject();
        objects49.put("/recipe/addUserBatchPrcipe","post");
        requetstUrles.add(objects49);

        JSONObject objects50=new JSONObject();
        objects50.put("/recipe/dropUserBatchPrcipe","delete");
        requetstUrles.add(objects50);

        JSONObject objects51=new JSONObject();
        objects51.put("/recipe/dropUserRecipe","delete");
        requetstUrles.add(objects51);

        JSONObject objects53=new JSONObject();
        objects53.put("/recipe/addUserPecipeName","post");
        requetstUrles.add(objects53);

        JSONObject objects54=new JSONObject();
        objects54.put("/recipe/addPrivateRecipe","post");
        requetstUrles.add(objects54);


        String userId="";
        JSONObject param=null;


        //判断接收模式
        if (request.getMethod().equals("GET")){
            return true;
        }else {
            String keys="";
            String values="";
            for(int p=0;p<requetstUrles.size();p++){
                JSONObject temp=(JSONObject) requetstUrles.get(p);
                Set<String> mapes=temp.keySet();
                for (String key:mapes){
                    if (requestUri.equals(key)&&request.getMethod().toLowerCase().equals(temp.get(key))){
                        keys=key;
                        values=(String) temp.get(key);
                    }
                }
            }

            if (!keys.equals("")&&values.toUpperCase().equals(request.getMethod())){
                param=(JSONObject) ParamUtils.getJsonObjectFromStream(request.getInputStream());
                System.out.println(param);
                userId=(String) param.get("user_id");
            }else {
                return true;
            }

        }

        UserTable userTable=userTableMapper.selectByPrimaryKey(userId);
        if (userTable==null){
            throw new BussinessException("-1","user does not exit");
        }

        String userAuthorization=userTable.getUser_authorization();
        //判断用户权限
        if (Integer.parseInt(userAuthorization)<2){
            throw new BussinessException("-1","The user does not have permission");

        }
        if (Integer.parseInt(userAuthorization)>=2){
            request.setAttribute("params",param);
            return true;
        }


        return false;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {


    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }

    public static String jsonPost(String strURL, String params) {

        try {
            URL url = new URL(strURL);// 创建连接
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setUseCaches(false);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestMethod("POST"); // 设置请求方式
            connection.setRequestProperty("Accept", "application/json"); // 设置接收数据的格式
            connection.setRequestProperty("Content-Type", "application/json"); // 设置发送数据的格式
            connection.connect();
            OutputStreamWriter out = new OutputStreamWriter(
                    connection.getOutputStream(), "UTF-8"); // utf-8编码
            out.append(params);
            out.flush();
            out.close();
            // 读取响应
            int length = (int) connection.getContentLength();// 获取长度
            InputStream is = connection.getInputStream();
            if (length != -1) {
                byte[] data = new byte[length];
                byte[] temp = new byte[512];
                int readLen = 0;
                int destPos = 0;
                while ((readLen = is.read(temp)) > 0) {
                    System.arraycopy(temp, 0, data, destPos, readLen);
                    destPos += readLen;
                }
                String result = new String(data, "UTF-8"); // utf-8编码
                //System.out.println(result);
                return result;
            }
        } catch (IOException e) {

            e.printStackTrace();
        }
        return "error"; // 自定义错误信息
    }
}
