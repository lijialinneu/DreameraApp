package neu.dreamerajni.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;

//import org.opencv.android.Utils;
//import org.opencv.core.Mat;
//import org.opencv.imgproc.Imgproc;


/**
 * Created by lijialin on 16-3-7.
 */
public class ImgToolKits {

//    private static Mat src = new Mat();
//    private static Mat dst = new Mat();
    public static int addHeight;

    /**
     * 改变图片大小
     * @author 10405
     * @param bitmap 待处理的图片
     * @param width 宽度
     * @param height 高度
     */
    public static Bitmap changeBitmapSize(Bitmap bitmap, float width, float height) {
        float pw = width / (float) bitmap.getWidth();//pw 宽比
        float ph = height / (float)bitmap.getHeight();//ph 高比
        Matrix matrix = new Matrix();
        matrix.postScale(pw, ph); //长和宽放大缩小的比例

        try{
            Bitmap after = Bitmap.createBitmap(
                    bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true
            );// after是缩放后的图片
            return after;
        } catch (IllegalArgumentException e){
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 自动生成边缘图
     */
    public static Bitmap getBorder(Bitmap srcb) {

        int width = srcb.getWidth();
        int height = srcb.getHeight();

        int[] pix = new int [width * height];
        srcb.getPixels(pix, 0, width, 0, 0, width, height);
        int[] resultPixes = OpenCVHelper.Canny(pix, width, height);

//        System.out.println("asdf resultpixes "+ resultPixes);
//        Utils.bitmapToMat(srcb, src); // bitmap 转 Mat; src 是4通道图
//        Imgproc.Canny(src, dst, 100, 100);// 边缘检测 Canny算子; dst是1通道图
//        int width = dst.width();
//        int height = dst.height();

        Bitmap b = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444);
//        Utils.matToBitmap(dst, b);//Mat 转 bitmap

        b.setPixels(resultPixes, 0, width, 0, 0, width, height);

//        for(int i=0; i < b.getWidth(); i++) { //循环每个像素点
//            for(int j=0; j < b.getHeight(); j++) {
//                if(b.getPixel(i,j) != Color.BLACK ){
//                    b.setPixel(i,j,Color.GREEN);
//                    if(i >= 1){
//                        b.setPixel(i-1,j, Color.GREEN);
//                    }
//                }else {
//                    b.setPixel(i,j,Color.argb(0,0,0,0));
//                }
//            }
//        }
        return b;

    }


    /**
     * 初始化边缘图
     * @param bitmap 待处理图片
     * @param width 目标宽度
     * @param height 目标高度
     */
    public static Bitmap initBorderPic(Bitmap bitmap, float width, float height) {
        float newHeight =  (width / bitmap.getWidth()) * bitmap.getHeight();// 保持原有比例下的新高度
        Bitmap after = changeBitmapSize(bitmap, width, newHeight); // 调整比例
        after = getBorder(after);//边缘检测

        //在after上下添加补充的透明空白
        addHeight = (int) (height - newHeight) / 2;
        Bitmap add = Bitmap.createBitmap((int)width, addHeight, Bitmap.Config.ARGB_8888);
        // 构造最后结果图片
        Bitmap result = Bitmap.createBitmap((int)width, (int)height, Bitmap.Config.ARGB_8888);

        Paint p = new Paint(); // 构造画笔
        p.setStyle( Paint.Style.STROKE );
        p.setAlpha(0);//设置透明度为0,即全透明
        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(add,0, 0, p);// 开始在结果图上绘制
        canvas.drawBitmap(after, 0, add.getHeight(), null);
        canvas.drawBitmap(add,0, add.getHeight()+after.getHeight(), p);

        after.recycle();
        add.recycle();

        return result;
    }

}
