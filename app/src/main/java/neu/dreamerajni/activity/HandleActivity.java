package neu.dreamerajni.activity;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.SurfaceView;
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
import neu.dreamerajni.utils.OpenCVCanny;
import neu.dreamerajni.utils.OpenCVSmooth;
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

    private Bitmap photoBitmap, copyPhotoBitmap; //新拍摄的照片和副本
    private Bitmap picFromFile, copyPicFromFile; //从文件中获取的源照片和副本
    private Bitmap borderBitmap; //边缘检测后的图片
    private int borderWidth, borderHeight; // 边缘图片的宽和高

    private WindowManager wm;
    private float screenWidth; //屏幕宽度，相机预览画面的宽度与屏幕的宽度相等
    private float xTrans, yTrans, sTop, scale; // x位移、y位移、surfaceView距顶部的宽度、放缩倍数
    private int left, top;// borderView的left、top

    private int a, b, c; //椭圆的参数
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

        picFromFile = AsyncGetDataUtil.getPicFromFile(id); //从缓存中取出图片

        //先把图片变成初始大小，然后再通过matrix变换
        borderBitmap = ImgToolKits.initBorderPic(picFromFile, screenWidth, screenWidth);
        borderBitmap = Bitmap.createBitmap(borderBitmap,
                0,0,borderBitmap.getWidth(),borderBitmap.getHeight(),
                matrix, true);
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

    }


    @OnClick(R.id.btnBack)
    public void clickBack() {
        onBackPressed();
    }

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

        for(int i = 0; i <= b; i++) { // i表示列
            for(int j = 0; j <= a; j++) { // j 表示行
                int setX = j + left;
                int setY = i + addtemp;
                int xt = (a - j) * 2;
                int yt = (b - i) * 2;
                float t = calculateEllipse(i, j);

                if(t <= 1) {
                    try{
                        if(!outScreen(setX, setY)) {
                            photoBitmap.setPixel(setX, setY, copyPicFromFile.getPixel(j, i));
                        }
                        if(!outScreen(setX + xt, setY)) {
                            photoBitmap.setPixel(setX + xt, setY, copyPicFromFile.getPixel(j + xt, i));
                        }
                        if(!outScreen(setX + xt, setY + yt)) {
                            photoBitmap.setPixel(setX + xt, setY + yt, copyPicFromFile.getPixel(j + xt, i + yt));
                        }
                        if(!outScreen(setX, setY + yt)) {
                            photoBitmap.setPixel(setX, setY + yt, copyPicFromFile.getPixel(j, i + yt));
                        }

                        if(t >= 0.98 && t < 0.99) {
                            j ++;
                        }else if(t >= 0.99 && t < 1){
                            j += 2;
                        }
                    } catch (IllegalArgumentException e) {
//                        e.printStackTrace(); //pass
                    }
                }
            }
        }


//        int width = photoBitmap.getWidth();
//        int height = photoBitmap.getHeight();
//        int[] pix = new int [width * height];
//        photoBitmap.getPixels(pix, 0, width, 0, 0, width, height);
//        int[] resultPixes = OpenCVSmooth.smooth(pix, width, height);
//        Bitmap b = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444);
//        b.setPixels(resultPixes, 0, width, 0, 0, width, height);

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
