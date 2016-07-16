package neu.dreamerajni.utils;

/**
 * Created by 10405 on 2016/6/6.
 */

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HttpConnectionUtil {
    private static final String GET = "GET";
    private static final String POST = "POST";
    private static final String PUT = "PUT";
    private static final String PATCH = "PATCH";
    private static final String DELETE = "DELETE";
    public static  String result = "";
    public static String jsonString = "";

    public static final String SERVERURL = "http://139.129.209.183:5678/cross/";
    public static final String PLACEURL = SERVERURL+"place/";


    private static String prepareParam(Map<String,Object> paramMap){
        StringBuffer sb = new StringBuffer();
        if(paramMap.isEmpty()){
            return "" ;
        }else{
            for(String key: paramMap.keySet()){
                String value = paramMap.get(key).toString();//modified
                if(sb.length()<1){
                    sb.append(key).append("=").append(value);
                }else{
                    sb.append("&").append(key).append("=").append(value);
                }
            }
            return sb.toString();
        }
    }

    public static void doGet(String urlStr, Map<String,Object> paramMap) throws Exception{
        String paramStr = prepareParam(paramMap);
        if(paramStr == null || paramStr.trim().length()<1){

        }else{
            urlStr +="?"+paramStr;
        }
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setRequestMethod(GET);
        conn.setRequestProperty("Content-Type","text/html; charset=UTF-8");
        conn.connect();
        if (conn.getResponseCode() != 200) {
            result = "failed";
            throw new RuntimeException("request failed");
        } else {
            result = "success";
            changeInputToString(conn);
        }
    }

    private static void changeInputToString(HttpURLConnection conn) throws Exception{
        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;
        while ((line = br.readLine()) != null) {
            jsonString += line;
        }
        br.close();
    }

    public static void sendRequest(String urlStr, Map<String,Object> paramMap, String method) throws Exception{
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setRequestMethod(method);
        String paramStr = prepareParam(paramMap);
        conn.setDoInput(true);
        conn.setDoOutput(true);
        OutputStream os = conn.getOutputStream();
        os.write(paramStr.toString().getBytes("utf-8"));
        os.close();

        changeInputToString(conn);
    }

    public static void doPost(String urlStr,Map<String,Object> paramMap) throws Exception{
        sendRequest(urlStr, paramMap, POST);
    }

    public static void doPut(String urlStr,Map<String,Object> paramMap) throws Exception{
        sendRequest(urlStr, paramMap, PUT);
    }

    public static void doPatch(String urlStr,Map<String,Object> paramMap) throws Exception{
        sendRequest(urlStr, paramMap, PATCH);
    }

    public static void doDelete(String urlStr) throws Exception{
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod(DELETE);

        if(conn.getResponseCode() == 204){ //204 means server return nothing
            result = "success";
        }else{
            System.out.println(conn.getResponseCode());
        }
    }


    /**
     * 检查当前网络是否可用
     *
     * @param activity
     * @return true or false
     */

    public static boolean isNetworkAvailable(Activity activity) {
        Context context = activity.getApplicationContext();
        // 获取手机所有连接管理对象（包括对wi-fi,net等连接的管理）
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return false;
        } else {
            // 获取NetworkInfo对象
            NetworkInfo[] networkInfo = connectivityManager.getAllNetworkInfo();
            if (networkInfo != null && networkInfo.length > 0) {
                for (int i = 0; i < networkInfo.length; i++) {
//                    System.out.println(i + "===状态===" + networkInfo[i].getState());
//                    System.out.println(i + "===类型===" + networkInfo[i].getTypeName());
                    // 判断当前网络状态是否为连接状态
                    if (networkInfo[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
