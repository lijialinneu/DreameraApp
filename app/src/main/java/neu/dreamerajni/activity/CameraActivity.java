package neu.dreamerajni.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.commonsware.cwac.camera.CameraHost;
import com.commonsware.cwac.camera.CameraHostProvider;
import com.commonsware.cwac.camera.CameraView;
import com.commonsware.cwac.camera.PictureTransaction;
import com.commonsware.cwac.camera.SimpleCameraHost;
import com.commonsware.cwac.camera.ZoomTransaction;

import java.io.File;
import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import neu.dreamerajni.R;
import neu.dreamerajni.utils.AsyncGetDataUtil;
import neu.dreamerajni.utils.FileCacheUtil;
import neu.dreamerajni.utils.ImgToolKits;
import neu.dreamerajni.utils.ZoomListener;
import neu.dreamerajni.view.RevealBackgroundView;

import static android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
import static android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;


@SuppressWarnings("deprecation")
public class CameraActivity extends AppCompatActivity implements
        RevealBackgroundView.OnStateChangeListener, CameraHostProvider {

    public static final String ARG_REVEAL_START_LOCATION = "reveal_start_location";

    @Bind(R.id.vRevealBackground)
    RevealBackgroundView vRevealBackground;
    @Bind(R.id.vPhotoRoot)
    View vTakePhotoRoot;
    @Bind(R.id.cameraView)
    CameraView cameraView;
    @Bind(R.id.btnTakePhoto)
    Button btnTakePhoto;

    private String pictureID; //从上一个Activity传递过来的图片的ID
    private SurfaceView surfaceView; //绘制边缘图
    private SurfaceHolder surfaceHolder;
    private int zoom = 0; //相机焦距

    private Bitmap picFromFile;
    private Bitmap borderBitmap;
    private WindowManager wm;
    private WindowManager.LayoutParams wmParams;
    private Matrix lastMatrix = new Matrix();

    private  Canvas canvas;

    /**
     * 一个静态函数，用于处理activity之间的参数传递
     * @param startingLocation
     * @param startingActivity
     * @param id
     */
    public static void startCameraFromLocation(int[] startingLocation,
                                               Activity startingActivity, String id) {
        Intent intent = new Intent(startingActivity, CameraActivity.class);
        intent.putExtra(ARG_REVEAL_START_LOCATION, startingLocation);
        intent.putExtra("id", id);
        startingActivity.startActivity(intent);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_camera);
        ButterKnife.bind(this);
        setupRevealBackground(savedInstanceState);
    }

    private void setupRevealBackground(Bundle savedInstanceState) {
        vRevealBackground.setFillPaintColor(0xFF16181a);
        vRevealBackground.setOnStateChangeListener(this);
        if (savedInstanceState == null) {
            final int[] startingLocation = getIntent().getIntArrayExtra(ARG_REVEAL_START_LOCATION);
            vRevealBackground.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    vRevealBackground.getViewTreeObserver().removeOnPreDrawListener(this);
                    vRevealBackground.startFromLocation(startingLocation);
                    return true;
                }
            });
        } else {
            vRevealBackground.setToFinishedFrame();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        cameraView.onResume();
    }


    @Override
    protected void onPause() {
        super.onPause();
        cameraView.onPause();
        // 为了避免onPause后转偏旋转90度
        this.finish(); //强制退出
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //当activity 被destory时需要立即清除之前加载的view，否则会出现窗体泄露异常
        wm.removeViewImmediate(surfaceView);
    }

    @OnClick(R.id.btnTakePhoto)
    public void onTakePhotoClick() {
        btnTakePhoto.setEnabled(false);
        cameraView.takePicture(true, true);

    }

    @OnClick(R.id.btnCloseCamera)
    public void onCloseCamera() {
        onBackPressed();
    }

    @Override
    public void onStateChange(int state) {
        if (RevealBackgroundView.STATE_FINISHED == state) {
            vTakePhotoRoot.setVisibility(View.VISIBLE);
            addBorderPicture(); //添加边缘图
        } else {
            vTakePhotoRoot.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * 相机预览界面缩小（也就是焦距缩小？）
     * @author 10405
     */
    @OnClick(R.id.smaller)
    public void onZoomSmaller(){
        try{
            zoom -= 6;
            zoom = zoom < 0 ? 0 : zoom;
            ZoomTransaction z = cameraView.zoomTo(zoom);
            z.go();
        }catch (IllegalArgumentException e){
            e.printStackTrace();
        }
    }


    /**
     * 相机预览界面放大（也就是焦距变大？）
     * @author 10405
     */
    @OnClick(R.id.bigger)
    public void onZoomBigger(){
        try{
            zoom += 6;
            ZoomTransaction z = cameraView.zoomTo(zoom);
            z.go();
        }catch (IllegalArgumentException e){
            e.printStackTrace();
        }
    }


    /**
     * 添加轮廓图
     * @author 10405
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void addBorderPicture() {

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();//从上一个Activity获取参数图片
        pictureID = bundle.getString("id");
        picFromFile = AsyncGetDataUtil.getPicFromFile(pictureID); //从缓存中取出图片

        wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);

        borderBitmap = ImgToolKits.initBorderPic(
                picFromFile,
                wm.getDefaultDisplay().getWidth(),
                wm.getDefaultDisplay().getWidth(),
                true
        );

        surfaceView = new SurfaceView(this);
        surfaceHolder = surfaceView.getHolder();
        surfaceView.setBackground(new BitmapDrawable(borderBitmap));

        surfaceView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                switch (event.getKeyCode()) {
                    case KeyEvent.KEYCODE_BACK:
                        onCloseCamera();
                }
                return false;
            }
        });

        wmParams = new WindowManager.LayoutParams();
        wmParams.width = borderBitmap.getWidth();
        wmParams.height = borderBitmap.getHeight();
        wmParams.flags = FLAG_NOT_TOUCHABLE;
        ViewGroup parent = (ViewGroup) surfaceView.getParent();
        if (parent != null) {
            parent.removeAllViews();
        }
        wm.addView(surfaceView, wmParams);

        cameraView.setOnTouchListener(new ZoomListener(this, borderBitmap) { //触摸监听
            @Override
            public void zoom(Matrix matrix) {
                surfaceView.setBackgroundResource(0); //删除背景
                canvas = surfaceHolder.lockCanvas();
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                canvas.drawBitmap(borderBitmap, matrix, null);

                surfaceHolder.unlockCanvasAndPost(canvas);
                surfaceHolder.setFormat(PixelFormat.TRANSPARENT); //设置背景透明
                surfaceHolder.lockCanvas(new Rect(0, 0, 0, 0));
                surfaceHolder.unlockCanvasAndPost(canvas);

                lastMatrix.set(matrix);
            }
        });

    }


    @Override
    public CameraHost getCameraHost() {
        return new MyCameraHost(this);
    }

    class MyCameraHost extends SimpleCameraHost {

        private Camera.Size previewSize;

        public MyCameraHost(Context ctxt) {
            super(ctxt);
        }

        @Override
        public boolean useFullBleedPreview() {
            return true;
        }

        @Override
        public Camera.Size getPictureSize(PictureTransaction xact, Camera.Parameters parameters) {
            return previewSize;
        }

        @Override
        public Camera.Parameters adjustPreviewParameters(Camera.Parameters parameters) {
            Camera.Parameters parameters1 = super.adjustPreviewParameters(parameters);
            previewSize = parameters1.getPreviewSize();
            return parameters1;
        }

        @Override
        public void saveImage(PictureTransaction xact, byte[] image) {

            String path = FileCacheUtil.CAMERAPATH; //存储JSON的路径
            FileCacheUtil fileCacheUtil = new FileCacheUtil(path);
            // 目录下只存一个临时文件，所以在保存之前，删除其余文件
            FileCacheUtil.deleteFile( new File(path));
            Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);

            Rect frame = new Rect();
            getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
            int statusBarHeight = frame.top;

            bitmap = Bitmap.createBitmap(bitmap,
                    vTakePhotoRoot.getLeft(),
                    vTakePhotoRoot.getTop() + statusBarHeight,
                    vTakePhotoRoot.getWidth(),
                    vTakePhotoRoot.getHeight()
            );
            try {
                fileCacheUtil.savePicture(bitmap, "Photo");
            } catch (IOException e) {
                e.printStackTrace();
            }

            //跳转到下一个Activity
            Intent intent = new Intent();
            intent.setClass(CameraActivity.this, HandleActivity.class);
            intent.putExtra("id", pictureID);
            float[] matrixValues = new float[9];
            lastMatrix.getValues(matrixValues);
            intent.putExtra("matrix", matrixValues); //传递矩阵
            CameraActivity.this.startActivity(intent);

        }
    }

}