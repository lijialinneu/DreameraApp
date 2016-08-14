package neu.dreamerajni.activity;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.view.View.OnTouchListener;

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
public class EraseActivity extends AppCompatActivity  {

    @Bind(R.id.photoView)
    ImageView photoView;            //拍摄的照片
    @Bind(R.id.squareFrameLayout)
    SquaredFrameLayout squaredFrameLayout;
    @Bind(R.id.rvFilters)
    RecyclerView rvFilters;

    private String id;                              //图片的id
    private Matrix matrix = new Matrix();           //前一个Activity传回的矩阵参数
    private float[] matrixValues = new float[9];    //用于获取矩阵的参数
    private ImageView borderView;                   //边缘图
    private Bitmap photoBitmap, copyPhotoBitmap;    //新拍摄的照片和副本
    private Bitmap picFromFile, copyPicFromFile;    //从文件中获取的源照片和副本
    private Bitmap borderBitmap;                    //边缘检测后的图片
    private int borderWidth, borderHeight;          // 边缘图片的宽和高
    private WindowManager wm;
    private float screenWidth;                      //屏幕宽度，相机预览画面的宽度与屏幕的宽度相等
    private float xTrans, yTrans, sTop, scale;      // x位移、y位移、surfaceView距顶部的宽度、放缩倍数
    private int left, top, right, bottom;           // borderView的left、top、right、bottom值
    private boolean borderShowed = false;           //是否显示边缘
    private boolean canDraw = false;
    private int mark[][];                           //用于标记(i,j)是否被涂抹过
    private int type = 0;               //0 表示横向图片，1表示竖向图片
    private float addX = 0;             //x方向的补充值
    private float addY = 0;             //y方向的补充值
    private Bitmap maskBitmap;          //遮罩图片
    private int radius = 80;            //半径
    private int[] picPixels;            //用于存储copyPicFromFile像素值得矩阵
    private int[] maskPixels;           //用于存储maskBitmap像素值得矩阵
    private int width;                  //mask的宽和高

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_erase);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        // 获取新拍摄的照片
        id = bundle.getString("id");//获取id
        matrixValues = bundle.getFloatArray("matrix");
        matrix.setValues(matrixValues);

        wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        screenWidth = wm.getDefaultDisplay().getWidth();
        addX = ImgToolKits.addHeight * matrixValues[Matrix.MSCALE_X];
        addY = ImgToolKits.addHeight * matrixValues[Matrix.MSCALE_Y];

        initPhoto();
        initBorder();

        photoView.setOnTouchListener(myTouchListener);
        setupPhotoFilters();
    }



    /**
     * 初始化拍摄照片
     * @author 10405
     */
    private void setupPhotoFilters() {
        PhotoFiltersAdapter photoFiltersAdapter = new PhotoFiltersAdapter(this, photoView, photoBitmap);
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
        if(!judgePicStyle(picFromFile)) {
            type = 1; //如果是竖向图片，type=1
        }

        borderBitmap = ImgToolKits.initBorderPic(picFromFile, screenWidth, screenWidth, true);

        borderBitmap = Bitmap.createBitmap(borderBitmap,
                0,0,borderBitmap.getWidth(),borderBitmap.getHeight(),
                matrix, true);
        borderView.setImageBitmap(borderBitmap);

        borderWidth = borderBitmap.getWidth();
        borderHeight = borderBitmap.getHeight();

        copyPicFromFile = ImgToolKits.changeBitmapSize(picFromFile,
                borderWidth - 2 * addX * type, borderHeight - 2 * addY * (1 - type));

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

        // 准备mask
        maskBitmap = BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_mask);
        width = 2 * radius;
        maskBitmap = Bitmap.createScaledBitmap(maskBitmap,
                width, width, false);

        //前置相片添加蒙板效果
//        picPixels = new int[width * width];
        maskPixels = new int[width * width];

//        copyPicFromFile.getPixels(picPixels, 0, width, 0, 0, width, width);
        maskBitmap.getPixels(maskPixels, 0, width, 0, 0, width, width);

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
            if(PhotoFiltersAdapter.dstBitmap != null) {
                fileCacheUtil.savePicture(PhotoFiltersAdapter.dstBitmap, "Edit");
            }else {
                fileCacheUtil.savePicture(photoBitmap, "Edit");
            }
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
//        int m = 80, x = 0, y = 0;

        int x = (int)(event.getX() - left - type * addX);
        int y = (int)(event.getY() - top - (1 - type) * addY);

        if(inOldPicRegion(x, y)) {
//            double r = (float) m / 4 * 3;
            int setX = 0, setY = 0;

            for(int i = x - radius; i < x + radius; i++) {
                for (int j = y - radius; j < y + radius; j++) {

                    if(i <= 0 || i >= copyPicFromFile.getWidth()
                            || j <= 0 || j >= copyPicFromFile.getHeight()) break;

                    setX = (int)(i + left + type * addX);
                    setY = (int)(j + top + (1-type) * addY);

                    if(setX <= 0 || setY <=0 || setX >= photoBitmap.getWidth()
                            || setY >= photoBitmap.getHeight()) break;

                    double distance = Math.sqrt(Math.pow((x - i), 2) + Math.pow((y - j), 2));


                    if(mark[i][j] < 1 ) {
                        mark[i][j] += 1;

                        int pixel = copyPicFromFile.getPixel(i, j);
                        System.out.println("asdf pixel " + pixel);
//                        int p = Math.abs(x-i) * width + Math.abs(y-j);
                        int p = (i - x + radius) * width + (j - y + radius);
                        if(maskPixels[p] == 0xff000000) { //黑色
//                            photoBitmap.setPixel(setX, setY, 0);
                            pixel = 0;
//                            System.out.println("asdf black");
                        }else if(maskPixels[p] == 0) { //透明色
//                            photoBitmap.setPixel(setX, setY, pixel);
                        }else {
                            System.out.println("asdf alpha");
                            //把mask的a通道与picBitmap与
                            maskPixels[p] &= 0xff000000; //高两位位表示透明度，ff表示完全不透明
                            //mask是中间透明，四周不透明；copyPicFromFile是中间不透明，四周透明
                            //所以需要做一个减法
                            maskPixels[p] = 0xff000000 - maskPixels[p];
                            pixel &= 0x00ffffff; //提取出copyPicFromFile某点的alpha值
                            pixel |= maskPixels[p]; //做 “或”运算，合成alpha
                        }
                        photoBitmap.setPixel(setX, setY, pixel);

                    }

//                    if (distance <= r) {
//                        if (mark[i][j] < 1) {
//                            mark[i][j] += 1;
//                            photoBitmap.setPixel(setX, setY, copyPicFromFile.getPixel(i, j));
//                        }
//                    }else if(distance > r && distance <= m){
//                        if(setX > screenWidth || setY > screenWidth){
//                            break;
//                        }
//                        if(mark[i][j] < 1){
//                            photoBitmap.setPixel(setX, setY, Color.parseColor("#C4C4C4"));
//                        }
//                    }
                }
            }
            copyPhotoBitmap = photoBitmap; //保存一份副本
            photoView.setBackground(new BitmapDrawable(photoBitmap));
        }
    }

    /**
     * 老照片与mask的融合
     */
//    public void composite() {
//        int x, y, px, py;
//        for(int i = 0; i < maskPixels.length; i++) {
//            y = i / width;
////            x = i - (y - 1) * w;
//            py =  y + photoView.getTop();
////            px = xOffset + x;
//            if(py <= photoView.getTop() || py >= photoView.getTop() + screenWidth) {
//                picPixels[i] = 0;
//            }else {
//                if(maskPixels[i] == 0xff000000){ //黑色
//                    picPixels[i] = 0;
//                }else if(maskPixels[i] == 0){ //透明色
//                    //pass
//                }else{
//                    //把mask的a通道与picBitmap与
//                    maskPixels[i] &= 0xff000000; //高两位位表示透明度，ff表示完全不透明
//                    //mask是中间透明，四周不透明；copyPicFromFile是中间不透明，四周透明
//                    //所以需要做一个减法
//                    maskPixels[i] = 0xff000000 - maskPixels[i];
//                    picPixels[i] &= 0x00ffffff; //提取出copyPicFromFile某点的alpha值
//                    picPixels[i] |= maskPixels[i]; //做 “或”运算，合成alpha
//                }
//            }
//        }
//
//        //生成前置图片添加蒙板后的bitmap:resultBitmap
//        resultBitmap.setPixels(picPixels, 0, w, 0, 0, w, h);
//        oldPictureView.setBackground(new BitmapDrawable(resultBitmap));
//    }



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

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @OnClick(R.id.btnRedo)
    public void redo() {
        PhotoFiltersAdapter.dstBitmap = null;
        photoView.setBackground(new BitmapDrawable(copyPhotoBitmap));
    }

}
