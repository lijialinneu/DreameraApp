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
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

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

//    private boolean borderShowed = false; //是否显示边缘
//
//
    private int a, b, c;
    private int p1x, p1y, p2x, p2y;
    private int midx, midy;
    private int powa, powb;



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

        setupPhotoFilters();
        initPhoto();
        initBorder();

        showOldPixel();

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

//        borderView = new ImageView(this);
        picFromFile = AsyncGetDataUtil.getPicFromFile(id); //从缓存中取出图片

        //先把图片变成初始大小，然后再通过matrix变换
        borderBitmap = ImgToolKits.initBorderPic(picFromFile, screenWidth, screenWidth);

        borderBitmap = Bitmap.createBitmap(borderBitmap,
                0,0,borderBitmap.getWidth(),borderBitmap.getHeight(),
                matrix, true);
//        borderView.setImageBitmap(borderBitmap);

        borderWidth = borderBitmap.getWidth();
        borderHeight = borderBitmap.getHeight();

        copyPicFromFile = ImgToolKits.changeBitmapSize(
                picFromFile, borderWidth, borderHeight - 2 * ImgToolKits.addHeight * matrixValues[Matrix.MSCALE_Y]);

        float[] matrixValues = new float[9];
        matrix.getValues(matrixValues);
        xTrans = matrixValues[Matrix.MTRANS_X];
        yTrans = matrixValues[Matrix.MTRANS_Y];

        left = (int) xTrans;
        top = (int) (yTrans + photoView.getTop());
        right = left + borderWidth;
        bottom = top + borderHeight;

//        SquaredFrameLayout.LayoutParams lp = new SquaredFrameLayout.LayoutParams(
//                borderBitmap.getWidth(), borderBitmap.getHeight());
//        lp.setMargins(left,top,right,bottom);
//        squaredFrameLayout.addView(borderView, lp);

//        borderView.setOnKeyListener(new View.OnKeyListener() {
//            @Override
//            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                switch (event.getKeyCode()) {
//                    case KeyEvent.KEYCODE_BACK:
//                        onBackPressed();
//                }
//                return false;
//            }
//        });
//        borderShowed = true;
    }


    @OnClick(R.id.btnBack)
    public void clickBack() {
        onBackPressed();
    }


//    @OnClick(R.id.btnShowBorder)
//    public void showBorder() {
//        if(borderShowed) { //如果已经显示出来了，则隐藏
//            borderView.setVisibility(View.GONE);
//            borderShowed = false;
//        } else { //如果没有显示，则显示
//            borderView.setImageBitmap(borderBitmap);
//            borderView.setVisibility(View.VISIBLE);
//            borderShowed = true;
//        }
//    }


    /**
     * 显示老照片对应的像素
     * @author 10405
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void showOldPixel(){

        //计算椭圆参数
        a = (int)(copyPicFromFile.getWidth() * 0.5f);
        b = (int)(copyPicFromFile.getHeight() * 0.5f);
        c = (int)Math.sqrt(Math.pow(a,2) - Math.pow(b,2));

        midx = (int)(copyPicFromFile.getWidth() * 0.5f);
        midy = (int)(copyPicFromFile.getHeight() * 0.5f);

        powa = a * a;
        powb = b * b;


        int addtemp = (int)(top + ImgToolKits.addHeight * matrixValues[Matrix.MSCALE_Y]);

        // TODO 这个循环开销太大了

        for(int i = 0; i <= b; i++) { // i表示列
            for(int j = 0; j <= a; j++) { // j 表示行
//                if(outScreen(i, j)) break;

                int setX = j + left;
                int setY = i + addtemp;
//                if(outScreen(setX, setY)) break;

                int xt = (a - j) * 2;
                int yt = (b - i) * 2;

                float t = calculateEllipse(i, j);
                if(t <= 1) {

                    try{
                        if(!outScreen(setX, setY) && !outScreen(j, i)) {
                            photoBitmap.setPixel(setX, setY, copyPicFromFile.getPixel(j, i));
                        }


                        if(!outScreen(setX + xt, setY) && !outScreen(j + xt, i)) {
                            photoBitmap.setPixel(setX + xt, setY, copyPicFromFile.getPixel(j + xt, i));
                        }

                        if(!outScreen(setX + xt, setY + yt) && !outScreen(j + xt, i + yt)) {
                            photoBitmap.setPixel(setX + xt, setY + yt, copyPicFromFile.getPixel(j + xt, i + yt));
                        }

                        if(!outScreen(setX, setY + yt) && !outScreen(j, i + yt)) {
                            photoBitmap.setPixel(setX, setY + yt, copyPicFromFile.getPixel(j, i + yt));
                        }

                    } catch (IllegalArgumentException e) {
                        System.out.println("asdf try try");
                    }
                }
            }
        }



//        for(int i = 0; i <= a; i++) {
//            for(int j = 0; j <= b; j++) {
//                int setX = i + left;
//                int setY = j + addtemp;
//
//                float t = ((float)(i-midx)*(i-midx))/powa + ((float)(j-midy)*(j-midy))/powb;
//                int xt = (a - i) * 2;
//                int yt = (b - j) * 2;
//                if(t <= 1) {
//                    try {
//                        photoBitmap.setPixel(setX, setY, copyPicFromFile.getPixel(i, j));
//                        photoBitmap.setPixel(setX + xt, setY, copyPicFromFile.getPixel(i + xt, j));
//                        photoBitmap.setPixel(setX + xt, setY + yt, copyPicFromFile.getPixel(i + xt, j + yt));
//                        photoBitmap.setPixel(setX, setY + yt, copyPicFromFile.getPixel(i, j + yt));
//                    }catch (IllegalArgumentException e) {
//                        // pass
//                    }
//                }
//            }
//        }
        photoView.setBackground(new BitmapDrawable(photoBitmap));
    }



    /**
     *  判断是否超出屏幕
     *  @param i
     *  @param j
     */
    public boolean outScreen(int i, int j) {
        if(i < 0 || i > screenWidth || j < 0 || j > screenWidth) {
            return true;
        }
        return false;
    }

    /**
     *  计算椭圆表达式
     */
    public float calculateEllipse(int i, int j) {



        return ((float)(j - midx) * (j - midx)) / powa + ((float)(i - midy) * (i - midy)) / powb;
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

}
