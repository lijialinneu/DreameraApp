package neu.dreamerajni.activity;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;

import java.io.File;
import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import neu.dreamerajni.R;
import neu.dreamerajni.utils.AsyncGetDataUtil;
import neu.dreamerajni.utils.FileCacheUtil;
import neu.dreamerajni.utils.ImgToolKits;
import neu.dreamerajni.view.SquaredFrameLayout;

import static android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
import static android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;


@SuppressWarnings("deprecation")
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class HandleActivity extends AppCompatActivity  {

    @Bind(R.id.photoView)
    ImageView photoView; //拍摄的照片
    @Bind(R.id.squareFrameLayout)
    SquaredFrameLayout squaredFrameLayout;
    @Bind(R.id.id_alpha)
    SeekBar alphaSeekBar;
    @Bind(R.id.id_blur)
    SeekBar blurSeekBar;
    @Bind(R.id.btnNextActivity)
    ImageButton nextButton;

    private WindowManager wm;
    private WindowManager.LayoutParams wmParams; //屏幕参数
    private String id;   //图片的id
    private Matrix matrix = new Matrix();        //前一个Activity传回的矩阵参数
    private float[] matrixValues = new float[9]; //用于获取矩阵的参数
    private Bitmap photoBitmap;         //拍摄的照片
    private Bitmap picFromFile;         //从文件中获取的老照片
    private Bitmap copyPicFromFile;     //老照片的副本
    private Bitmap borderBitmap;        //边缘检测后的图片
    private SurfaceView oldPictureView; //显示老照片的SurfaceView
    private int borderWidth = 0;        //边缘图片的宽
    private int borderHeight = 0;       //边缘图片的高
    private int left = 0;               //老照片的left
    private int top = 0;                //老照片的top
    private float screenWidth = 0;      //屏幕宽度，相机预览画面的宽度与屏幕的宽度相等
    private float xTrans = 0;           //变换矩阵中的x方向位移的值
    private float yTrans = 0;           //变换矩阵中的y方向位移的值
    private Bitmap maskBitmap;          //遮罩mask处理
    private Bitmap resultBitmap;        //最终结果图片
    private final int WITHOUT = -1;
    private static final int MASK = 1;
    private int xOffset = 0;            //悬浮窗中图片的x偏移量
    private int yOffset = 0;            //悬浮窗中图片的y偏移量
    private int alpha = 255;            //老照片的透明度
    private float addX = 0;             //x方向的补充值
    private float addY = 0;             //y方向的补充值
    private int type = 0;               //0 表示横向图片，1表示竖向图片
    private int w;                      //copyPicFromFile的width
    private int h;                      //copyPicFromFile的height
    private int[] picPixels;            //用于存储copyPicFromFile像素值得矩阵
    private int[] maskPixels;           //用于存储maskBitmap像素值得矩阵

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_handle);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        id = bundle.getString("id");           //获取新拍摄的照片的id
        matrixValues = bundle.getFloatArray("matrix");
        matrix.setValues(matrixValues);
        wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        screenWidth = wm.getDefaultDisplay().getWidth(); //屏幕的宽度1080
        addX = ImgToolKits.addHeight * matrixValues[Matrix.MSCALE_X];
        addY = ImgToolKits.addHeight * matrixValues[Matrix.MSCALE_Y];

        initPhoto();       //初始化拍摄照片
        initOldPicture();  //初始化老照片
//        showOldPixel(); //图像融合，显示老照片的像素

    }

    @Override
    protected void onResume() {
        super.onResume();
        adjustAlpha(); //初始化滑块,调整老照片的alpha值
        adjustBlur();  //调整模糊范围
    }

    /**
     * 初始化拍摄照片
     * @author 10405
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void initPhoto() {
        photoBitmap = AsyncGetDataUtil.getPhotoFromFile(); //取出拍摄的图片;
        photoView.setBackground(new BitmapDrawable(photoBitmap));
    }


    /**
     * 初始化边缘图
     * @author 10405
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void initOldPicture() {
        picFromFile = AsyncGetDataUtil.getPicFromFile(id); //从缓存中取出图片
        if(!judgePicStyle(picFromFile)) {
            type = 1; //如果是竖向图片，type=1
        }

        //先把图片变成初始大小，然后再通过matrix变换
        borderBitmap = ImgToolKits.initBorderPic(picFromFile, screenWidth, screenWidth, false);
        borderBitmap = Bitmap.createBitmap(borderBitmap, 0, 0,
                borderBitmap.getWidth(), borderBitmap.getHeight(), matrix, true);
        borderWidth = borderBitmap.getWidth();
        borderHeight = borderBitmap.getHeight();

        copyPicFromFile = ImgToolKits.changeBitmapSize(picFromFile,
                borderWidth - 2 * addX * type, borderHeight - 2 * addY * (1 - type));
        w = copyPicFromFile.getWidth();
        h = copyPicFromFile.getHeight();
        float[] matrixValues = new float[9];    //读取变换矩阵
        matrix.getValues(matrixValues);
        xTrans = matrixValues[Matrix.MTRANS_X]; //x位移值
        yTrans = matrixValues[Matrix.MTRANS_Y]; //y位移值

        left = (int) xTrans;
        top = (int) (yTrans + photoView.getTop());

        oldPictureView = new SurfaceView(this);
        oldPictureView.setBackground(new BitmapDrawable(copyPicFromFile));
        oldPictureView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                switch (event.getKeyCode()) {
                    case KeyEvent.KEYCODE_BACK:
                        clickBack();
                }
                return false;
            }
        });

        int leftw = ((int)screenWidth - copyPicFromFile.getWidth()) / 2;
        int topw = ((int)screenWidth - copyPicFromFile.getHeight()) / 2;

        wmParams = new WindowManager.LayoutParams();
        wmParams.x = (int)(left - leftw + type * addX);
        wmParams.y = (int)(top - topw + (1 - type) * addY);

        xOffset = wmParams.x + leftw;
        yOffset = wmParams.y + topw;

        wmParams.width = copyPicFromFile.getWidth();
        wmParams.height = copyPicFromFile.getHeight();
        wmParams.flags = FLAG_NOT_TOUCHABLE | FLAG_LAYOUT_NO_LIMITS;

        ViewGroup parent = (ViewGroup) oldPictureView.getParent();
        if (parent != null) {
            parent.removeAllViews();
        }
        wm.addView(oldPictureView, wmParams);

        addMask(); //添加mask
    }

    /**
     * 添加mask
     */
    public void addMask() {
        maskBitmap = BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_mask);
        maskBitmap = Bitmap.createScaledBitmap(maskBitmap,
                copyPicFromFile.getWidth(), copyPicFromFile.getHeight(), false);

        resultBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);

        //前置相片添加蒙板效果
        picPixels = new int[w * h];
        maskPixels = new int[w * h];

        copyPicFromFile.getPixels(picPixels, 0, w, 0, 0, w, h);
        maskBitmap.getPixels(maskPixels, 0, w, 0, 0, w, h);

        composite();
    }

    /**
     * 老照片与mask的融合
     */
    public void composite() {
        int x, y, px, py;
        for(int i = 0; i < maskPixels.length; i++) {
            y = i / w;
//            x = i - (y - 1) * w;
            py = yOffset + y + photoView.getTop();
//            px = xOffset + x;
            if(py <= photoView.getTop() || py >= photoView.getTop() + screenWidth) {
                picPixels[i] = 0;
            }else {
                if(maskPixels[i] == 0xff000000){ //黑色
                    picPixels[i] = 0;
                }else if(maskPixels[i] == 0){ //透明色
                    //pass
                }else{
                    //把mask的a通道与picBitmap与
                    maskPixels[i] &= 0xff000000; //高两位位表示透明度，ff表示完全不透明
                    //mask是中间透明，四周不透明；copyPicFromFile是中间不透明，四周透明
                    //所以需要做一个减法
                    maskPixels[i] = 0xff000000 - maskPixels[i];
                    picPixels[i] &= 0x00ffffff; //提取出copyPicFromFile某点的alpha值
                    picPixels[i] |= maskPixels[i]; //做 “或”运算，合成alpha
                }
            }
        }

        //生成前置图片添加蒙板后的bitmap:resultBitmap
        resultBitmap.setPixels(picPixels, 0, w, 0, 0, w, h);
        oldPictureView.setBackground(new BitmapDrawable(resultBitmap));
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        //当activity 被destory时需要立即清除之前加载的view，否则会出现窗体泄露异常
        wm.removeViewImmediate(oldPictureView);
    }

    @OnClick(R.id.btnBack)
    public void clickBack() {
        onBackPressed();
    }


    /**
     * 判断是宽 > 高的图片，还是高 > 宽的图片
     * @param bitmap
     */
    public boolean judgePicStyle(Bitmap bitmap) {
        if(bitmap.getWidth() >= bitmap.getHeight()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 转到下一个Activity
     */
    @OnClick(R.id.btnNextActivity)
    public void gotoNextActivity() {

        Bitmap picture = Bitmap.createBitmap(1080, 1080, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(picture);
        Paint paint = new Paint(); // 建立画笔
        paint.setDither(true);
        paint.setFilterBitmap(true);
        canvas.drawBitmap(photoBitmap, 0, 0, paint);
        paint.setAlpha(alpha);
        canvas.drawBitmap(resultBitmap, xOffset, yOffset, paint);

        //先把图片存到SD
        String path = FileCacheUtil.TEMPPATH; //存储JSON的路径
        // 目录下只存一个临时文件，所以在保存之前，删除其余文件
        FileCacheUtil.deleteFile( new File(path));
        FileCacheUtil fileCacheUtil = new FileCacheUtil(path);
        try {
            fileCacheUtil.savePicture(picture, "Temp");
        } catch (IOException e) {
            e.printStackTrace();
        }

        //由文件得到uri
        Uri imageUri = Uri.fromFile(new File(path + "/" + fileCacheUtil.filename));
        int[] startingLocation = new int[2];
        nextButton.getLocationOnScreen(startingLocation);
        startingLocation[0] += nextButton.getWidth() / 2;
        FilterActivity.startFilterFromLocation(startingLocation, HandleActivity.this, imageUri.toString());
        this.overridePendingTransition(0, 0);
    }


    /**
     * 第一个SeekBar调整图片的alpha值
     */
    public void adjustAlpha() {
        alphaSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                alpha = 255 - progress;
                oldPictureView.getBackground().setAlpha(alpha);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }
        });
    }

    /**
     * 第二个SeekBar调整图片的alpha值
     */
    public void adjustBlur() {
        blurSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float radius = (progress + 1) / 4.f;
                float scale = 0.1f;  //设置图片缩小的比例
                 /* 产生reSize后的Bitmap对象 */
                Matrix matrix = new Matrix();
                matrix.postScale(scale, scale);
//                Bitmap presentBitmap = ((BitmapDrawable) oldPictureView.getBackground()).getBitmap();
                Bitmap smallMask = Bitmap.createBitmap(
                        maskBitmap,0,0,maskBitmap.getWidth(),
                        maskBitmap.getHeight(), matrix, true);
                Bitmap blurMask = blurBitmap(smallMask, radius);

                 // 产生reSize后的Bitmap对象
                matrix = new Matrix();
                matrix.postScale(1/scale, 1/scale);
                Bitmap bigMask = Bitmap.createScaledBitmap(blurMask, w, h, false);

                //前置相片添加蒙板效果
                picPixels = new int[w * h];
                maskPixels = new int[w * h];
                copyPicFromFile.getPixels(picPixels, 0, w, 0, 0, w, h);
                bigMask.getPixels(maskPixels, 0, w, 0, 0, w, h);
                composite();
                oldPictureView.getBackground().setAlpha(alpha); //别忘了设置透明度
//                presentBitmap.recycle();
//                smallMask.recycle();
//                blurMask.recycle();
//                bigMask.recycle();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }
        });
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public Bitmap blurBitmap(Bitmap bitmap, float radius){

        //Let's create an empty bitmap with the same size of the bitmap we want to blur
        Bitmap outBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);

        //Instantiate a new Renderscript
        RenderScript rs = RenderScript.create(getApplicationContext());

        //Create an Intrinsic Blur Script using the Renderscript
        ScriptIntrinsicBlur blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));

        //Create the Allocations (in/out) with the Renderscript and the in/out bitmaps
        Allocation allIn = Allocation.createFromBitmap(rs, bitmap);
        Allocation allOut = Allocation.createFromBitmap(rs, outBitmap);

        //Set the radius of the blur
        blurScript.setRadius(radius);

        //Perform the Renderscript
        blurScript.setInput(allIn);
        blurScript.forEach(allOut);

        //Copy the final bitmap created by the out Allocation to the outBitmap
        allOut.copyTo(outBitmap);

        //recycle the original bitmap
//        bitmap.recycle();

        //After finishing everything, we destroy the Renderscript.
        rs.destroy();

        return outBitmap;


    }





}
