package neu.dreamerajni.activity;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.view.View.OnTouchListener;

//import org.opencv.android.BaseLoaderCallback;
//import org.opencv.android.LoaderCallbackInterface;
//import org.opencv.android.OpenCVLoader;

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


@SuppressWarnings("deprecation")
public class HandleActivity extends AppCompatActivity  {


    @Bind(R.id.photoView)
    SurfaceView photoView; //拍摄的照片
    @Bind(R.id.squareFrameLayout)
    SquaredFrameLayout squaredFrameLayout;
    @Bind(R.id.rvFilters)
    RecyclerView rvFilters;
    private String id; //图片的id
    private Matrix matrix = new Matrix();//前一个Activity传回的矩阵参数
    private float[] matrixValues = new float[9]; //用于获取矩阵的参数

    private ImageView borderView; //边缘图
    private Bitmap photoBitmap, copyPhotoBitmap; //新拍摄的照片和副本
    private Bitmap picFromFile, copyPicFromFile; //从文件中获取的源照片和副本
    private Bitmap borderBitmap; //边缘检测后的图片
    private int borderWidth, borderHeight; // 边缘图片的宽和高

    private WindowManager wm;
    private float screenWidth; //屏幕宽度，相机预览画面的宽度与屏幕的宽度相等
    private float xTrans, yTrans, sTop, scale; // x位移、y位移、surfaceView距顶部的宽度、放缩倍数
    private int left, top, right, bottom; // borderView的left、top、right、bottom值

    private boolean borderShowed = false; //是否显示边缘
    private boolean canDraw = false;
    private int mark[][]; //用于标记(i,j)是否被涂抹过


    /**
     * 加载OpenCV的回调函数，用此函数在手机上安装OpenCV Manager
     * @author 10405
     */
//    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
//        @Override
//        public void onManagerConnected(int status) {
//            switch (status) {
//                case LoaderCallbackInterface.SUCCESS:{
//                    Log.i("CameraActivity", "Load success");
//                } break;
//                default:{
//                    super.onManagerConnected(status);
//                } break;
//            }
//        }
//    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_handle);

//        if (!OpenCVLoader.initDebug()) {  // 加载OpenCV
//            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, mLoaderCallback);
//        } else {
//            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
//        }

        ButterKnife.bind(this);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        // 获取新拍摄的照片
        id = bundle.getString("id");//获取id
        matrixValues = bundle.getFloatArray("matrix");
        matrix.setValues(matrixValues);

        wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        screenWidth = wm.getDefaultDisplay().getWidth();

        setupPhotoFilters();
        initPhoto();
        initBorder();

        photoView.setOnTouchListener(myTouchListener);

    }



    /**
     * 初始化拍摄照片
     * @author 10405
     */
    private void setupPhotoFilters() {
        PhotoFiltersAdapter photoFiltersAdapter = new PhotoFiltersAdapter(this);
        rvFilters.setHasFixedSize(true);
        rvFilters.setAdapter(photoFiltersAdapter);
        rvFilters.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
    }


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
    public void initBorder() {

        borderView = new ImageView(this);
        picFromFile = AsyncGetDataUtil.getPicFromFile(id); //从缓存中取出图片
        borderBitmap = ImgToolKits.initBorderPic(picFromFile, screenWidth, screenWidth);

//        sTop = (screenWidth - borderBitmap.getHeight()) / 2;
//        System.out.println("asdf sTop "+ sTop);

        borderBitmap = Bitmap.createBitmap(borderBitmap,
                0,0,borderBitmap.getWidth(),borderBitmap.getHeight(),
                matrix, true);
        borderView.setImageBitmap(borderBitmap);

        borderWidth = borderBitmap.getWidth();
        borderHeight = borderBitmap.getHeight();

        copyPicFromFile = ImgToolKits.changeBitmapSize(
                picFromFile, borderWidth, borderHeight - 2 * ImgToolKits.addHeight);

        float[] matrixValues = new float[9];
        matrix.getValues(matrixValues);
//        scale = matrixValues[Matrix.MSCALE_X];
        xTrans = matrixValues[Matrix.MTRANS_X];
        yTrans = matrixValues[Matrix.MTRANS_Y];

        left = (int) xTrans;
//        top = (int)(yTrans + sTop + photoView.getTop());
        top = (int) (yTrans + photoView.getTop());
        right = left + borderWidth;
        bottom = top + borderHeight;

        SquaredFrameLayout.LayoutParams lp = new SquaredFrameLayout.LayoutParams(
                borderBitmap.getWidth(), borderBitmap.getHeight());
        lp.setMargins(left,top,right,bottom);
        squaredFrameLayout.addView(borderView, lp);

        borderView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                switch (event.getKeyCode()) {
                    case KeyEvent.KEYCODE_BACK:
                        onBackPressed();
                }
                return false;
            }
        });
        borderShowed = true;

        // 初始化 Marker
        mark = new int[copyPicFromFile.getWidth()][copyPicFromFile.getHeight()];
        for(int i = 0; i < copyPicFromFile.getWidth(); i++) {
            for (int j = 0; j < copyPicFromFile.getHeight(); j++) {
                mark[i][j] = 0;
            }
        }


    }


    @OnClick(R.id.btnBack)
    public void clickBack() {
        onBackPressed();
    }


    @OnClick(R.id.btnShowBorder)
    public void showBorder() {
        if(borderShowed){ //如果已经显示出来了，则隐藏
            borderView.setVisibility(View.GONE);
            borderShowed = false;
        } else { //如果没有显示，则显示
            borderView.setVisibility(View.VISIBLE);
            borderShowed = true;
        }
    }


    @OnClick(R.id.btnAccept)
    public void share() {
        //先把编辑后的图片存到SD
        String path = FileCacheUtil.EDITPATH; //存储JSON的路径
        FileCacheUtil fileCacheUtil = new FileCacheUtil(path);

        try {
            fileCacheUtil.savePicture(photoBitmap, "Edit");
        } catch (IOException e) {
            e.printStackTrace();
        }

        //由文件得到uri
        Uri imageUri = Uri.fromFile(new File(path + "/" + fileCacheUtil.filename));
        Intent shareButtonIntent = new Intent();
        shareButtonIntent.setAction(Intent.ACTION_SEND);
        shareButtonIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
        shareButtonIntent.setType("image/*");
        startActivity(Intent.createChooser(shareButtonIntent, "分享到"));
    }


    /**
     * 触摸监听
     * @author 10405
     */
    OnTouchListener myTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch(event.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    if(canDraw){
                        try{
                            showOldPixel(event);
                        } catch (IllegalArgumentException e){
                            // pass
                        }
                    }
                case MotionEvent.ACTION_DOWN:
                    canDraw = true;
                    break;
                case MotionEvent.ACTION_OUTSIDE:
                case MotionEvent.ACTION_UP:
                    canDraw = false;
                    break;
            }
            return true;
        }
    };


    /**
     * 显示老照片对应的像素
     * @param event
     * @author 10405
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void showOldPixel(MotionEvent event)  throws  IllegalArgumentException{

        int m = 80;
        int x = (int) event.getX() - left;
        int y = (int) event.getY() - top - ImgToolKits.addHeight;

        if(inOldPicRegion(x, y)) {
            double r = (float)m / 4 * 3;
            for(int i = x - m; i < x + m; i++) {
                for (int j = y - m; j < y + m; j++) {

                    if(i <= 0 || i >= copyPicFromFile.getWidth()
                            || j <= 0 || j >= copyPicFromFile.getHeight()) break;

                    int setX = i + left;
                    int setY = j + top + ImgToolKits.addHeight;
                    if(setX <= 0 || setY <=0 || setX >= photoBitmap.getWidth()
                            || setY >= photoBitmap.getHeight()) break;

                    double distance = Math.sqrt(Math.pow((x - i), 2) + Math.pow((y - j), 2));
                    if (distance <= r) {
                        if (mark[i][j] < 1) {
                            mark[i][j] += 1;
                            photoBitmap.setPixel(setX, setY, copyPicFromFile.getPixel(i, j));
                        }
                    }else if(distance > r && distance <= m){
                        if(setX > screenWidth || setY > screenWidth){
                            break;
                        }
                        if(mark[i][j] < 1){
                            photoBitmap.setPixel(setX, setY, Color.parseColor("#C4C4C4"));
                        }
                    }
                }
            }
            photoView.setBackground(new BitmapDrawable(photoBitmap));
        }
    }


    /**
     * 判断是否在边缘图区域内
     * @return
     */
    public boolean inOldPicRegion(float x, float y) {

        if(x >= 0 && x <= copyPicFromFile.getWidth()
                && y >= 0 && y <= copyPicFromFile.getHeight()){
            return true;
        }else {
            return false;
        }
    }

}
