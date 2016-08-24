package neu.dreamerajni.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Created by 10405 on 2016/6/7.
 */

public class FileCacheUtil {

    private String path; //路径
    private static final File STORAGE = Environment.getExternalStorageDirectory();
    static final String JSONPATH = STORAGE + "/Dreamera/resource/data";       //缓存JSON数据、该目录下只存一个文件，时间一到就更新
    static final String PICTUREPATH = STORAGE + "/Dreamera/resource/picture"; //缓存图片
    public static final String CAMERAPATH = STORAGE +  "/Dreamera/photo/camera";     //存储拍摄后的照片
    public static final String EDITPATH = STORAGE +  "/Dreamera/photo/edit";         //存储编辑后的图片
    public static final String TEMPPATH = STORAGE + "/Dreamera/photo/temp";          //存储临时文件

    public String filename;

    /**
     * 构造函数
     * @param path
     * @return void
     */
    public FileCacheUtil(String path){
        this.path = path;
    }

    /**
     * 创建文件
     * @param type
     * @param picId
     * @return file
     */
    private File createFiles(int type, String picId){
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        filename = format.format(date);
        if(type == 0){ // 0: bitmap
            filename = picId + "+" + filename + ".jpg";//id + 时间
        }else if(type == 1){ //1: json
            filename += ".txt";
        }
        File fileFolder = new File(path);
        if (!fileFolder.exists()) { // 如果目录不存在，则创建一个目录
            fileFolder.mkdirs(); // 创建多级目录，要用mkdirs
        }
        return new File(fileFolder, filename);
    }

    /**
     * 删除文件
     */
    public static void deleteFile(File file) {
        if (file.exists()) { // 判断文件是否存在
            if (file.isFile()) { // 判断是否是文件
                file.delete();
            } else if (file.isDirectory()) { // 否则如果它是一个目录
                File files[] = file.listFiles(); // 声明目录下所有的文件 files[];
                for (File file1 : files) { // 遍历目录下所有的文件
                    deleteFile(file1); // 把每个文件 用这个方法进行迭代
                }
            }
            file.delete();
        }
    }


    /**
     * 格式化时间
     */
    private static long formatDate(String string) {
        long time = 0;
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        try {
            time = format.parse(string).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return time;
    }


    //-----------------------------------JSON 部分 start----------------------------------------

    /**
     * 清除JSON缓存
     */
    static void cleanJSONCache() throws ParseException {
        File fileFloder = new File(FileCacheUtil.JSONPATH );
        File files[] = fileFloder.listFiles(); // 目录下所有的文件 files[];
        if( files.length >= 2 ) { //文件数>=2 说明下载成功
            int oldestIndex = getOldestFileIndex(files);
            FileCacheUtil.deleteFile(files[oldestIndex]); //删除最老文件
        }
    }



    /**
     * 存储JSON到文件中
     */
    void saveJSON(String string) throws IOException {
        File file = createFiles(1, "");
        FileOutputStream outputStream = new FileOutputStream(file);// 文件输出流
        outputStream.write(string.getBytes());
        outputStream.flush();
        outputStream.close(); // 关闭输出流
        file.setReadOnly();//设置文件只读
    }

    /**
     * 从文件中读取JSON字符串
     * @return String
     */
    static String getJsonFromFile(final String path) throws IOException, ParseException {
        File fileFolder = new File(path);
        if(fileFolder.exists()){
            File files[] = fileFolder.listFiles(); // 声明目录下所有的文件 files[];
            FileInputStream inputStream = null;
            if(files.length == 0) {//如果没有文件,可能是没有下载完
                return "";
            } else if(files.length == 1) {//如果只有一个文件
                inputStream = new FileInputStream(files[0]);
            } else if(files.length >= 2) { //如果有多个文件 只读最新的
                int newestIndex = getNewestFileIndex(files);
                inputStream = new FileInputStream(files[newestIndex]);
                if(inputStream.available() == 0){//如果最新的文件大小为空，可能由于其他bug引起
                    inputStream = new FileInputStream(files[1-newestIndex]);//读下一个副本
                }
            }
            byte[] bytes = new byte[1024];
            ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
            while (inputStream.read(bytes) != -1) {
                arrayOutputStream.write(bytes, 0, bytes.length);
            }
            inputStream.close();
            arrayOutputStream.close();
            String jsonString = new String(arrayOutputStream.toByteArray());
            return jsonString;
        }else{
            return "";
        }
    }


    /**
     * 循环找到最新JSON文件的index
     * @return string
     */
    private static int getNewestFileIndex(File[] files) throws ParseException {
        long newestTime = formatDate(files[0].getName());
        int newestIndex = 0;   //默认第一个文件最新
        for(int i = 1; i < files.length; i++){   //循环找到最新文件
            long newestTimei = formatDate(files[i].getName());
            if(newestTimei >= newestTime){
                newestTime = newestTimei;
                newestIndex = i;
            }
        }
        return newestIndex;
    }


    /**
     * 循环找到最老JSON文件
     * @return string
     */
    private static int getOldestFileIndex(File[] files) throws ParseException {
        long oldestTime = formatDate(files[0].getName());
        int oldestIndex = 0;    //默认第一个文件最老
        for(int i = 1; i < files.length; i++){ //循环找到最老文件
            long oldestTimei = formatDate(files[i].getName());
            if(oldestTimei < oldestTime){
                oldestTime = oldestTimei;
                oldestIndex = i;
            }
        }
        return oldestIndex;
    }

    /**
     * 获得JSON文件的文件名
     * @return string
     */
    String getFilename() throws ParseException{
         File fileFolder = new File(path);
         if(fileFolder.exists()){
             File files[] = fileFolder.listFiles(); // 目录下所有的文件 files[];
             if(files.length == 0){//如果文件不存在，则返回""
                 return "";
             } else if(files.length == 1){
                 return files[0].getName();
             } else {
                 int newestIndex = getNewestFileIndex(files);
                 return files[newestIndex].getName();
             }
         } else {
             return "";
         }
     }


//-----------------------------------图片 部分 start----------------------------------------

    /**
     * 获得图片的文件名
     * @return string
     */
    String getPicFilename(String id) throws ParseException{
        File fileFolder = new File(path);
        if(fileFolder.exists()){
            File files[] = fileFolder.listFiles(); // 目录下所有的文件 files[];
            if(files.length >= 0) {
                for (File file : files) {
                    String name = file.getName();
                    String idFromName = name.substring(0, name.indexOf('+'));
                    if (idFromName.equals(id)) {
                        return name;
                    }
                }
            }
            return "";
        }
        return "";
    }


    /**
     * 按id从文件中读取图片
     * @param path
     * @param picId
     * @return string
     */
    static Bitmap getPicFromFile(final String path, final String picId)
            throws IOException, ParseException {
        File fileFolder = new File(path);
        if(fileFolder.exists()){
            File files[] = fileFolder.listFiles(); // 声明目录下所有的文件 files[];
            Bitmap bitmap = null;
            if(files.length == 0) {//如果没有文件,可能是没有下载完
                return null;
            }else{
                for (File file : files) {
                    String name = file.getName();
                    String idFromName = name.substring(0, name.indexOf('+'));
                    if (idFromName.equals(picId)) {
                        bitmap = BitmapFactory.decodeFile(path + '/' + name);
                        break;
                    }
                }
                return bitmap;
            }
        }else { //文件不存在
            return null;
        }
    }


    /**
     * 从文件中读取照片
     * @return bitmap
     */
    static Bitmap getPhotoFromFile(final String path)
            throws IOException, ParseException {
        File fileFolder = new File(path);
        Bitmap bitmap = null;
        if(fileFolder.exists()){
            File files[] = fileFolder.listFiles(); // 声明目录下所有的文件 files[];
            if(files.length == 1) { //如果没有文件,可能是没有下载完
                String name = files[0].getName();
                bitmap = BitmapFactory.decodeFile(path + '/'+name);
            }
        }
        return bitmap;
    }


    /**
     * 按id存储图片到文件中
     * @param bitmap
     * @return void
     */
    public void savePicture(Bitmap bitmap, String id) throws IOException {
        File file = createFiles(0, id);
        FileOutputStream outputStream = new FileOutputStream(file);// 文件输出流
        bitmap.compress(Bitmap.CompressFormat.JPEG, 40, outputStream);//压缩图片60%
        outputStream.flush();
        outputStream.close(); // 关闭输出流
    }

}
