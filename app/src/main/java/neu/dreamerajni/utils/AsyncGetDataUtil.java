package neu.dreamerajni.utils;


import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by 10405 on 2016/6/6.
 */

public class AsyncGetDataUtil {

    public static String jsonString; //JSON字符串
    public static Bitmap bitmap;//图片资源
    public static boolean jsonUpdate = false;//json是否更新

    /**
     * 获取缓存的JSON数据
     * @author 10405
     */
    public static void getJSONData(){
        FileCacheUtil fileCacheUtil = new FileCacheUtil(FileCacheUtil.JSONPATH);
        String filename = null;
        try {
            filename = fileCacheUtil.getFilename();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if(filename == ""){
            getDataFromServer();//如果文件不存在，则从服务器端获取
        }else{// 否则文件存在，判断有没有到更新时间
            try {
                if (overTime(filename)){
                    getDataFromServer();//先从服务器下载数据
                    FileCacheUtil.cleanJSONCache();//清除原有JSON缓存
                    jsonUpdate = true;
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 从文件中读JSON数据
     * @author 10405
     */
    public static String getJSONFromFile(){
        //TODO 数据量大的情况下需要分段加载缓存文件中的数据
        try {
            jsonString = FileCacheUtil.getJsonFromFile(FileCacheUtil.JSONPATH);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return jsonString;
    }

    /**
     * 从服务器端Get JSON数据
     * @author 10405
     */
    public static void getDataFromServer(){
        //开启一个新线程，从服务器端下载JSON数据
        new Thread(){
            public void run(){
                try{
                    Map<String, Object> map = new HashMap();
                    HttpConnectionUtil.doGet(HttpConnectionUtil.PLACEURL, map);//下载JSON数据
                    if(HttpConnectionUtil.result == "failed"){//如果下载失败了
                      //TODO 处理下载失败的情况
                    }else if(HttpConnectionUtil.result == "success"){
                        //如果下载成功了存到文件里
                        String jsonString = HttpConnectionUtil.jsonString;//下载的JSON数据资源
                        String path = FileCacheUtil.JSONPATH; //存储JSON的路径
                        FileCacheUtil fileCacheUtil = new FileCacheUtil(path);
                        fileCacheUtil.saveJSON(jsonString);
                    }
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }


    /**
     * 解析JSON数据，picture部分不解析
     * @author 10405
     * @param jsonStr
     * @return  ArrayList<HashMap<String, Object>>
     */
    public static ArrayList<HashMap<String, Object>> decodeJsonToPoint(String jsonStr)
            throws JSONException {
        ArrayList<HashMap<String, Object>> list = new ArrayList();
        JSONArray jsonArray = new JSONArray(jsonStr);

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            HashMap<String, Object> map = new HashMap();
            map.put("name", jsonObject.getString("name"));
            map.put("longitude", jsonObject.getString("longitude"));
            map.put("latitude", jsonObject.getString("latitude"));
            map.put("cross_pictures", jsonObject.getString("cross_pictures"));
            list.add(map);
        }
        return list;
    }


    /**
     * 解析picture部分的JSON数据
     * @author 10405
     * @param jsonStr
     * @return  ArrayList<HashMap<String, Object>>
     */
    public static ArrayList<HashMap<String, Object>> decodeCrossPicturesJsonToPoint(String jsonStr)
            throws JSONException {
        ArrayList<HashMap<String, Object>> list = new ArrayList();
        JSONArray jsonArray = new JSONArray(jsonStr);
        for (int i = 0; i < jsonArray.length(); i++) {
            final JSONObject jsonObject = jsonArray.getJSONObject(i);
            final HashMap<String, Object> map = new HashMap();
            map.put("id", jsonObject.getString("id"));
            map.put("url", jsonObject.getString("picture"));
            String year = jsonObject.getString("datetime").substring(0,4);//截取datetime中的year
            map.put("date", year);
            list.add(map);
        }
        return list;
    }



    /**
     * 获取缓存的图片数据
     * @author 10405
     */
    public static void getPictureData(String id, String url){
        FileCacheUtil fileCacheUtil = new FileCacheUtil(FileCacheUtil.PICTUREPATH);
        String filename = null;
        try {
            filename = fileCacheUtil.getPicFilename(id);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if(filename == ""){//如果文件不存在，则从服务器端获取
            getPicFromServer(url, id);
        }
    }


    /**
     * 按id从文件中读图片
     * @author 10405
     */
    public static Bitmap getPicFromFile(String picId){
        try {
            bitmap = FileCacheUtil.getPicFromFile(FileCacheUtil.PICTUREPATH, picId);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return bitmap;
    }


    /**
     * 按路径从文件中读图片
     * @author 10405
     */
    public static Bitmap getPhotoFromFile(){
        try {
            bitmap = FileCacheUtil.getPhotoFromFile(FileCacheUtil.CAMERAPATH);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return bitmap;
    }



    /**
     * 判断超时的函数
     * @author 10405
     * @param string 是一个包含时间的字符串
     * @return true or false
     */
    public static boolean overTime(String string) throws ParseException{
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        Date createDate = format.parse(string);
        Date presentDate = new Date();
        long createDay = createDate.getTime();//getTime()毫秒级别
        long presentDay = presentDate.getTime();
        long oneDay = 86400000; //一天的毫秒数 24*3600*1000
        long oneHour = 3600000; //一小时
        if(presentDay - createDay >= oneDay){ // 超过一小时就更新，用于测试阶段
            return true;
        }else{
            return false;
        }
    }

    /**
     * 从服务器端Get JSON数据
     * @author 10405
     */
    public static void getPicFromServer(final String url, final String id){
        //开启一个新线程，从服务器端下载图片
        new Thread(){
            public void run(){
                try{
                    Drawable drawable = Drawable.createFromStream(new URL(url).openStream(), null);
                    if(drawable != null){
                        //如果下载成功了存到文件里
                        String path = FileCacheUtil.PICTUREPATH; //存储JSON的路径
                        FileCacheUtil fileCacheUtil = new FileCacheUtil(path);
                        BitmapDrawable bd = (BitmapDrawable) drawable;
                        fileCacheUtil.savePicture(bd.getBitmap(), id);
                    }
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
}
