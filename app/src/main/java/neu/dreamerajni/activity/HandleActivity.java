package neu.dreamerajni.activity;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;

import java.io.File;
import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import neu.dreamerajni.R;
import neu.dreamerajni.adapter.PhotoFiltersAdapter;
import neu.dreamerajni.utils.AsyncGetDataUtil;
import neu.dreamerajni.utils.FileCacheUtil;
import neu.dreamerajni.utils.ImgToolKits;
import neu.dreamerajni.view.SquaredFrameLayout;

import static android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
import static android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;


@SuppressWarnings("deprecation")
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class HandleActivity extends AppCompatActivity  {

    @Bind(R.id.photoView)
    ImageView photoView; //拍摄的照片
    @Bind(R.id.squareFrameLayout)
    SquaredFrameLayout squaredFrameLayout;
//    @Bind(R.id.rvFilters)
//    RecyclerView rvFilters;
    @Bind(R.id.id_alpha)
    SeekBar alphaSeekBar;

    private String id; //图片的id
    private Matrix matrix = new Matrix();//前一个Activity传回的矩阵参数
    private float[] matrixValues = new float[9]; //用于获取矩阵的参数

    private Bitmap photoBitmap, copyPhotoBitmap; //新拍摄的照片和副本
    private Bitmap picFromFile, copyPicFromFile; //从文件中获取的源照片和副本
    private Bitmap borderBitmap; //边缘检测后的图片
    private int borderWidth, borderHeight; // 边缘图片的宽和高

    private WindowManager wm;
    private WindowManager.LayoutParams wmParams;
    private float screenWidth; //屏幕宽度，相机预览画面的宽度与屏幕的宽度相等
    private float xTrans, yTrans; // x位移、y位移、surfaceView距顶部的宽度、放缩倍数
    private int left, top; // borderView的left、top

    private int a, b, c; //椭圆的参数
    private int midx, midy;
    private int powa, powb;

    private SurfaceView oldPictureView;
    private SurfaceHolder surfaceHolder;

    // mask
    private ImageView maskView;
    private Bitmap maskBitmap;
    private final int WITHOUT = -1;
    private static final int MASK = 1;
    private Bitmap resultBitmap;


    private int[] resIds = new int[]{       //渐变
            WITHOUT,
            R.mipmap.ic_mask,
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_handle);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        // 获取新拍摄的照片
        id = bundle.getString("id");//获取id
        matrixValues = bundle.getFloatArray("matrix");
        matrix.setValues(matrixValues);

        wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        screenWidth = wm.getDefaultDisplay().getWidth();

        /**
         * 初始化拍摄照片
         */
        initPhoto();

        /**
         * 初始化老照片
         */
        initOldPicture();

        /**
         * 图像融合，显示老照片的像素
         */
//        showOldPixel();

        /**
         * 显示过滤图片列表
         */
//        setupPhotoFilters();

//        Bitmap b = createCircleImage(copyPicFromFile, 20);
//        oldPictureView.setBackground(new BitmapDrawable(b));

    }



    /**
     * 根据原图和变长绘制圆形图片
     */
//    private Bitmap createCircleImage(Bitmap source, int min) {
//        final Paint paint = new Paint();
//        paint.setAntiAlias(true);
//        // 注意一定要用ARGB_8888，否则因为背景不透明导致遮罩失败
//        Bitmap target = Bitmap.createBitmap(min, min, Bitmap.Config.ARGB_8888);
//// 产生一个同样大小的画布
//        Canvas canvas = new Canvas(target);
//// 首先绘制圆形
//        canvas.drawCircle(min / 2, min / 2, min / 2, paint);
//// 使用SRC_IN
//        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
//// 绘制图片
//        canvas.drawBitmap(source, 0, 0, paint);
//        return target;
//    }




    @Override
    protected void onResume() {
        super.onResume();
        /**
         * 初始化滑块,调整老照片的alpha值
         */
        adjustAlpha();
    }


    /**
     *
     * @author 10405
     */
//    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
//    private void setupPhotoFilters() {
//        PhotoFiltersAdapter photoFiltersAdapter =
//                new PhotoFiltersAdapter(this, photoView, photoBitmap);
//        rvFilters.setHasFixedSize(true);
//        rvFilters.setAdapter(photoFiltersAdapter);
//        rvFilters.setLayoutManager(
//                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
//    }


    /**
     * 初始化拍摄照片
     * @author 10405
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void initPhoto() {
        copyPhotoBitmap = AsyncGetDataUtil.getPhotoFromFile(); //从缓存中取出图片;
        photoBitmap = copyPhotoBitmap.copy(Bitmap.Config.ARGB_8888, true);
        photoView.setBackground(new BitmapDrawable(photoBitmap));
    }


    /**
     * 初始化边缘图
     * @author 10405
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void initOldPicture() {

        picFromFile = AsyncGetDataUtil.getPicFromFile(id); //从缓存中取出图片
        //先把图片变成初始大小，然后再通过matrix变换
        borderBitmap = ImgToolKits.initBorderPic(picFromFile, screenWidth, screenWidth, false);
        borderBitmap = Bitmap.createBitmap(borderBitmap,
                0,0,borderBitmap.getWidth(),borderBitmap.getHeight(),
                matrix, true);
        borderWidth = borderBitmap.getWidth();
        borderHeight = borderBitmap.getHeight();

        if(judgePicStyle(picFromFile)) {
            copyPicFromFile = ImgToolKits.changeBitmapSize(
                    picFromFile, borderWidth,
                    borderHeight - 2 * ImgToolKits.addHeight * matrixValues[Matrix.MSCALE_Y]);
        } else {
            copyPicFromFile = ImgToolKits.changeBitmapSize(
                    picFromFile,
                    borderWidth - 2 * ImgToolKits.addHeight * matrixValues[Matrix.MSCALE_X],
                    borderHeight);
        }

        float[] matrixValues = new float[9];
        matrix.getValues(matrixValues);
        xTrans = matrixValues[Matrix.MTRANS_X];
        yTrans = matrixValues[Matrix.MTRANS_Y];

        left = (int) xTrans;
        top = (int) (yTrans + photoView.getTop());

        oldPictureView = new SurfaceView(this);
        surfaceHolder = oldPictureView.getHolder();
//        oldPictureView.setBackground(new BitmapDrawable(copyPicFromFile));

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

        oldPictureView.layout(left,
                (int) (top + ImgToolKits.addHeight * matrixValues[Matrix.MSCALE_Y]),
                left + photoBitmap.getWidth(), top + photoBitmap.getHeight());

        wmParams = new WindowManager.LayoutParams();
        wmParams.width = copyPicFromFile.getWidth();
        wmParams.height = copyPicFromFile.getHeight();
        wmParams.flags = FLAG_NOT_TOUCHABLE;

        ViewGroup parent = (ViewGroup) oldPictureView.getParent();
        if (parent != null) {
            parent.removeAllViews();
        }
        wm.addView(oldPictureView, wmParams);

        addMask();
    }

    public void addMask() {
        maskBitmap = BitmapFactory.decodeResource(this.getResources(), resIds[MASK]);
        maskBitmap = Bitmap.createScaledBitmap(maskBitmap,
                copyPicFromFile.getWidth(), copyPicFromFile.getHeight(), false);
        int w = copyPicFromFile.getWidth();
        int h = copyPicFromFile.getHeight();
        int edgeColor = maskBitmap.getPixel(1, 1);
        int centerColor = maskBitmap.getPixel(w/2, h/2);
        resultBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        //前置相片添加蒙板效果
        int[] picPixels = new int[w * h];
        int[] maskPixels = new int[w * h];

        copyPicFromFile.getPixels(picPixels, 0, w, 0, 0, w, h);
        maskBitmap.getPixels(maskPixels, 0, w, 0, 0, w, h);

        for(int i = 0; i < maskPixels.length; i++) {
            if(maskPixels[i] == 0xff000000){ //黑色
                picPixels[i] = 0;
            }else if(maskPixels[i] == 0){ //透明色
                //donothing
            }else{
                //把mask的a通道应用与picBitmap
                maskPixels[i] &= 0xff000000;
                maskPixels[i] = 0xff000000 - maskPixels[i];
                picPixels[i] &= 0x00ffffff;
                picPixels[i] |= maskPixels[i];
            }
        }
        //生成前置图片添加蒙板后的bitmap:resultBitmap
        resultBitmap.setPixels(picPixels, 0, w, 0, 0, w, h);
        oldPictureView.setBackground(new BitmapDrawable(resultBitmap));


//        composedBitmap = Bitmap.createBitmap(fengjingBitmap.getWidth(), fengjingBitmap.getHeight(), Bitmap.Config.ARGB_8888);
//        Canvas cv = new Canvas(copyPicFromFile);
//        cv.drawBitmap(fengjingBitmap, 0, 0, null);
//        cv.drawBitmap(resultBitmap, 100, 100, null);
//
//        cv.save(Canvas.ALL_SAVE_FLAG);
//        cv.restore();
//        resultView.setImageBitmap(composedBitmap);
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
     * 显示老照片对应的像素
     * @author 10405
     */
//    public void showOldPixel(){
//        calEllipseParam(copyPicFromFile); //计算椭圆参数
//        int addtemp; //计算偏移量
//        if(judgePicStyle(picFromFile)) {
//            addtemp = (int) (top + ImgToolKits.addHeight * matrixValues[Matrix.MSCALE_Y]);
//        } else {
//            addtemp = (int) (left + ImgToolKits.addHeight * matrixValues[Matrix.MSCALE_X]);
//        }
//
//        for(int i = 0; i <= b; i++ ) { // i表示列
//            boolean flag = false;
//            int setX,setY;
//            for(int j = 0; j <= a; j += 2) { // j 表示行
//                if(judgePicStyle(picFromFile)) {
//                    setX = j + left;
//                    setY = i + addtemp;
//                } else {
//                    setX = j + addtemp;
//                    setY = i + top;
//                }
//                int xt = (a - j) * 2;
//                int yt = (b - i) * 2;
//
//                float t = 0; //减少计算次数
//                if(j == 0) { flag = true;}
//                if(flag) { t = calculateEllipse(i, j);}
//
//                if(t <= 1) { // t <= 1 表示在椭圆范围内
//                    flag = true;
//                    int[] param = {setX, setY, i, j, xt, yt};
//                    pixelReplace(param);
//                }
//            }
//        }
//        copyPhotoBitmap = photoBitmap; //保存一份副本
//        photoView.setBackground(new BitmapDrawable(photoBitmap));
//    }
//
//    /**
//     * 计算椭圆参数
//     */
//    public void calEllipseParam(Bitmap bitmap) {
//        a = (int)(bitmap.getWidth() * 0.5f);
//        b = (int)(bitmap.getHeight() * 0.5f);
//        c = (int)Math.sqrt(Math.pow(a,2) - Math.pow(b,2));
//
//        midx = (int)(bitmap.getWidth() * 0.5f);
//        midy = (int)(bitmap.getHeight() * 0.5f);
//        powa = a * a;
//        powb = b * b;
//    }

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
     *  判断是否超出屏幕
     *  @param i
     *  @param j
     */
//    public boolean outScreen(int i, int j) {
//        if(i < 0 || i > screenWidth || j < 0 || j > screenWidth) {
//            return true;
//        }
//        return false;
//    }

    /**
     *  计算椭圆表达式
     */
//    public float calculateEllipse(int i, int j) {
//        return ((float)(j - midx) * (j - midx)) / powa + ((float)(i - midy) * (i - midy)) / powb;
//    }

    /**
     *  像素替换
     */
//    public void pixelReplace(int[] param) {
//        try {
//            if(!outScreen(param[0], param[1])) {
//                photoBitmap.setPixel(param[0], param[1],
//                        copyPicFromFile.getPixel(param[3], param[2]));
//            }
//            if(!outScreen(param[0] + param[4], param[1])) {
//                photoBitmap.setPixel(param[0] + param[4], param[1],
//                        copyPicFromFile.getPixel(param[3] + param[4], param[2]));
//            }
//            if(!outScreen(param[0] + param[4], param[1] + param[5])) {
//                photoBitmap.setPixel(param[0] + param[4], param[1] + param[5],
//                        copyPicFromFile.getPixel(param[3] + param[4], param[2] + param[5]));
//            }
//            if(!outScreen(param[0], param[1] + param[5])) {
//                photoBitmap.setPixel(param[0], param[1] + param[5],
//                        copyPicFromFile.getPixel(param[3], param[2] + param[5]));
//            }
//        } catch (IllegalArgumentException e) {
////                        e.printStackTrace(); //pass
//        }
//    }


    /**
     * 点击分享按钮
     */
    @OnClick(R.id.btnAccept)
    public void share() {
//        //先把编辑后的图片存到SD
//        String path = FileCacheUtil.EDITPATH; //存储JSON的路径
//        FileCacheUtil fileCacheUtil = new FileCacheUtil(path);
//
//        try {
//            if(PhotoFiltersAdapter.dstBitmap != null) {
//                fileCacheUtil.savePicture(PhotoFiltersAdapter.dstBitmap, "Edit");
//            }else {
//                fileCacheUtil.savePicture(photoBitmap, "Edit");
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        //由文件得到uri
//        Uri imageUri = Uri.fromFile(new File(path + "/" + fileCacheUtil.filename));
//        Intent shareButtonIntent = new Intent();
//        shareButtonIntent.setAction(Intent.ACTION_SEND);
//        shareButtonIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
//        shareButtonIntent.setType("image/*");
//        startActivity(Intent.createChooser(shareButtonIntent, "分享到"));
    }

    /**
     * 撤销之前的操作
     */
//    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
//    @OnClick(R.id.btnRedo)
//    public void redo() { //撤销
//        PhotoFiltersAdapter.dstBitmap = null;
//        photoView.setBackground(new BitmapDrawable(copyPhotoBitmap));
//    }

    /**
     * 调整图片的alpha值
     */
    public void adjustAlpha() {

        alphaSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                oldPictureView.getBackground().setAlpha(255 - progress);
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


    public  Bitmap getTransparentBitmap(Bitmap sourceImg, int number){
        int[] argb = new int[sourceImg.getWidth() * sourceImg.getHeight()];

        sourceImg.getPixels(argb, 0, sourceImg.getWidth(), 0, 0, sourceImg
                .getWidth(), sourceImg.getHeight());// 获得图片的ARGB值

        number = number * 255 / 100;

        for (int i = 0; i < argb.length; i++) {
            argb[i] = (number << 24) | (argb[i] & 0x00FFFFFF);
        }

        sourceImg = Bitmap.createBitmap(argb, sourceImg.getWidth(), sourceImg
                .getHeight(), Bitmap.Config.ARGB_8888);

        return sourceImg;
    }


}
