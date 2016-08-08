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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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
    @Bind(R.id.id_alpha)
    SeekBar alphaSeekBar;
    @Bind(R.id.btnNextActivity)
    ImageButton nextButton;

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

    // 遮罩mask处理
    private Bitmap maskBitmap;
    private final int WITHOUT = -1;
    private static final int MASK = 1;
    private Bitmap resultBitmap;
    private int xOffset, yOffset;
    private int alpha;
    private int[] resIds = new int[]{   //渐变
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

    }



    @Override
    protected void onResume() {
        super.onResume();
        adjustAlpha(); // 初始化滑块,调整老照片的alpha值
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

        System.out.println("asdf left " + left);
        System.out.println("asdf top "+ top);

        oldPictureView = new SurfaceView(this);
        surfaceHolder = oldPictureView.getHolder();
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
        if(judgePicStyle(picFromFile)) {
            wmParams.x = left - leftw;
            wmParams.y = (int)(top - topw + ImgToolKits.addHeight * matrixValues[Matrix.MSCALE_Y]);

        } else {
            wmParams.x = (int)(left - leftw + ImgToolKits.addHeight * matrixValues[Matrix.MSCALE_X]);
            wmParams.y = top - topw;
        }
        xOffset = wmParams.x + leftw;
        yOffset = wmParams.y + topw;
        wmParams.width = copyPicFromFile.getWidth();
        wmParams.height = copyPicFromFile.getHeight();
        wmParams.flags = FLAG_NOT_TOUCHABLE;
//        wmParams.alpha = 0.5f;
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
        maskBitmap = BitmapFactory.decodeResource(this.getResources(), resIds[MASK]);
        maskBitmap = Bitmap.createScaledBitmap(maskBitmap,
                copyPicFromFile.getWidth(), copyPicFromFile.getHeight(), false);
        int w = copyPicFromFile.getWidth();
        int h = copyPicFromFile.getHeight();
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
                //pass
            }else{
                //把mask的a通道与picBitmap与
                maskPixels[i] &= 0xff000000;
                maskPixels[i] = 0xff000000 - maskPixels[i];
                picPixels[i] &= 0x00ffffff;
                picPixels[i] |= maskPixels[i];
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
     * 点击分享按钮
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
     * 调整图片的alpha值
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

}
