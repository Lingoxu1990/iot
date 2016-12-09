package com.iot.requestUtil;

import com.alibaba.fastjson.JSONObject;
import com.iot.pojo.AppConfig;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;


/**
 * http请求工具
 */
public class RequestCaseUtil {

    private static Logger logger=Logger.getLogger(RequestCaseUtil.class);

    /**
     * 向指定 URL 发送POST方法的请求
     *
     * @param url   发送请求的 URL
     * @param param 请求参数必须是 name1=value1&name2=value2 的形式,否则omniREST无法识别
     * @return 所代表远程资源的响应结果
     */


    public static String requestPostFileCase(String url, String param, long time) {
        String result = "";//缓存请求数据
        BufferedReader in = null;
        try {
            url += "?time=" + time;
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            HttpURLConnection conn = (HttpURLConnection) realUrl.openConnection();

            //设置 HttpURLConnection的字符编码
            conn.setRequestProperty("Content-Type", "application/octet-stream");
            conn.setDoOutput(true);//设置输出流
            conn.setDoInput(true);//设置输入流
            conn.setRequestMethod("POST");//设置请求方式

            conn.setUseCaches(false);//post请求不可以设置缓存,设置为false


            conn.setInstanceFollowRedirects(false);//设置是否自动处理重定向,只对当前请求有效
            conn.connect();//
            OutputStream out = conn.getOutputStream();


            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(out);

            File file = new File(param);

            System.out.println("文件传输前的修改时间: " + file.lastModified());

            FileInputStream fileInputStream = new FileInputStream(file);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);


            byte[] absolutepath = param.getBytes();

            bufferedOutputStream.write(absolutepath);
            bufferedOutputStream.write('|');
            int d = -1;
            while ((d = bufferedInputStream.read()) != -1) {

                bufferedOutputStream.write(d);
            }


            bufferedInputStream.close();
            bufferedOutputStream.close();


            // 定义BufferedReader输入流来读取URL的响应
            InputStream inputStream = conn.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
            in = new BufferedReader(inputStreamReader);
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {

            System.out.println("发送 POST 请求出现异常！" + e);
//                log.error("发送 POST 请求出现异常！url:" + url);
//
//            log.error(e);
            e.printStackTrace();
        }
        //使用finally块来关闭输出流、输入流
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return result;
    }


    /**
     * 向指定URL发送GET方法的请求
     *
     * @param url 发送请求的URL
     * @return URL 所代表远程资源的响应结果
     * @throws UnsupportedEncodingException
     */
    public static String requestGetCase(String url) {
        String result = "";

        BufferedReader in = null;
        try {
            String urlNameString = url;
//			System.out.println("请求URL:"+urlNameString);
            URL realUrl = new URL(urlNameString);
            // 打开和URL之间的连接
            URLConnection connection = realUrl.openConnection();
            // 设置通用的请求属性
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("User-agent",
                    "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:39.0) Gecko/20100101 Firefox/39.0");
            connection.setRequestProperty("Accept-Charset", "UTF-8");
            // 建立实际的连接
            connection.connect();
            // 获取所有响应头字段
            // 定义 BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream(), "utf-8"));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            logger.error("Get Request failed! \n"+e);
            e.printStackTrace();
        }
        // 使用finally块来关闭输入流
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return result;
    }

    public static String requestDeleteCase(String url, String account_id) {
        String result = "";//缓存请求数据
        BufferedReader in = null;
        try {
            url += "?account_id=" + account_id;
            URL realUrl = new URL(url);
            System.out.println("delete-url:" + url);
            // 打开和URL之间的连接
            HttpURLConnection conn = (HttpURLConnection) realUrl.openConnection();

            //设置 HttpURLConnection的字符编码
            conn.setRequestProperty("Content-Type", "application/octet-stream");
            conn.setDoOutput(true);//设置输出流
            conn.setRequestMethod("DELETE");//设置请求方式

            conn.setUseCaches(false);//post请求不可以设置缓存,设置为false

            conn.setInstanceFollowRedirects(false);//设置是否自动处理重定向,只对当前请求有效
            conn.connect();//

            // 定义BufferedReader输入流来读取URL的响应
            InputStream inputStream = conn.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
            in = new BufferedReader(inputStreamReader);
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //使用finally块来关闭输出流、输入流
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return result;
    }

    public static String requestPutCase(String url, JSONObject jsonObject) {
        String result = "";//缓存请求数据
        BufferedReader in = null;
        try {
            System.out.println("put-url:" + url);

            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            HttpURLConnection conn = (HttpURLConnection) realUrl.openConnection();
            //设置 HttpURLConnection的字符编码
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);//设置输出流
            conn.setDoInput(true);//设置输入流
            conn.setRequestMethod("PUT");//设置请求方式
            conn.connect();//
            OutputStream out = conn.getOutputStream();

            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(out, "UTF-8");

            BufferedWriter bw = new BufferedWriter(outputStreamWriter);

            System.out.println(jsonObject.toString());
            bw.write(jsonObject.toJSONString());
            bw.flush();
            bw.close();


            // 定义BufferedReader输入流来读取URL的响应
            InputStream inputStream = conn.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
            in = new BufferedReader(inputStreamReader);
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {

            System.out.println("发送 PUT 请求出现异常！" + e);
            e.printStackTrace();
        }
        //使用finally块来关闭输出流、输入流
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return result;
    }

    public static String requestPostCase(String url, String param) {
        String result = "";//缓存请求数据
        BufferedReader in = null;
        BufferedWriter out = null;
        try {
            URL realUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) realUrl.openConnection();

            //设置 HttpURLConnection的字符编码
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);//设置输出流
            conn.setDoInput(true);//设置输入流
            conn.setRequestMethod("POST");//设置请求方式

            conn.setUseCaches(false);//post请求不可以设置缓存,设置为false

            conn.setInstanceFollowRedirects(false);//设置是否自动处理重定向,只对当前请求有效
            conn.connect();//

            out = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream(), "utf-8"));
            if (!param.equals("")){
                out.write(param);
            }
            out.close();
            in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {

            System.out.println("发送 POST 请求出现异常！" + e);
//                log.error("发送 POST 请求出现异常！url:" + url);
//
//            log.error(e);
            e.printStackTrace();
        }
        //使用finally块来关闭输出流、输入流
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return result;
    }


    public static void main(String[] args) {
//        String lastestReleaseVersionPrex=AppConfig.LASTEST_RELEASE_VERSION_PREX;
//        String commemt_prex = AppConfig.COMMENT_PREX;
//        String downLoadPrx = AppConfig.DOWN_LOAD_PRX;
//
//        String androidReleaseId=AppConfig.ANDROID_RELEASE_ID;
//        String iosReleaseId=AppConfig.IOS_RELEASE_ID;
//        String releaseTokenId =AppConfig.RELEASE_TOKEN_ID;
//
//
//        JSONObject SS = JSONObject.parseObject(requestPostCase(downLoadPrx+androidReleaseId+"/install?download_token=ea65bb18f91a99e1c493ab95ca48318a",""));
//
//        System.out.println(SS.toString());
        String lastestReleaseVersionPrex=AppConfig.LASTEST_RELEASE_VERSION_PREX;
        String commemt_prex = AppConfig.COMMENT_PREX;
        String downLoadPrx = AppConfig.DOWN_LOAD_PRX;

        String androidReleaseId=AppConfig.ANDROID_RELEASE_ID;
        String iosReleaseId=AppConfig.IOS_RELEASE_ID;
        String releaseTokenId =AppConfig.RELEASE_TOKEN_ID;


//        JSONObject json_down_load_token_android =JSONObject.parseObject(requestGetCase(commemt_prex+androidReleaseId+"/download?api_token="+releaseTokenId));

        System.out.println(requestGetCase(commemt_prex+androidReleaseId+"/download?api_token="+releaseTokenId));
    }


}
