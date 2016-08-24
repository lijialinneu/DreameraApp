package neu.dreamerajni.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import neu.dreamerajni.R;
import neu.dreamerajni.adapter.PhotoFiltersAdapter;
import neu.dreamerajni.utils.AsyncGetDataUtil;
import neu.dreamerajni.utils.FileCacheUtil;
import neu.dreamerajni.view.RevealBackgroundView;
import neu.dreamerajni.view.SquaredFrameLayout;

public class FilterActivity extends AppCompatActivity implements
         RevealBackgroundView.OnStateChangeListener {

    @Bind(R.id.vRevealBackgroundFilter)
    RevealBackgroundView vRevealBackground;
    @Bind(R.id.pictureView)
    ImageView pictureView; //拍摄的照片
    @Bind(R.id.squareFrameLayoutFilter)
    SquaredFrameLayout squaredFrameLayout;
    @Bind(R.id.rvFiltersFilter)
    RecyclerView rvFilters;

    private Bitmap pictureBitmap;

    /**
     * 一个静态函数，用于处理activity之间的参数传递
     */
    static void startFilterFromLocation(int[] startingLocation,
                                               Activity startingActivity, String imageUri) {
        Intent intent = new Intent(startingActivity, FilterActivity.class);
        intent.putExtra("startingLocation", startingLocation);
        intent.putExtra("imageUri", imageUri);
        startingActivity.startActivity(intent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);
        ButterKnife.bind(this);
        setupRevealBackground(savedInstanceState);
    }

    private void setupRevealBackground(Bundle savedInstanceState) {
        vRevealBackground.setFillPaintColor(0xFFFFFFFF);
        vRevealBackground.setOnStateChangeListener(this);
        if (savedInstanceState == null) {
            final int[] startingLocation = getIntent().getIntArrayExtra("startingLocation");
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
        vRevealBackground.setFillPaintColor(0xff16181a);
    }

    @Override
    public void onStateChange(int state) {
        if (RevealBackgroundView.STATE_FINISHED == state) {
            initPicture(); //初始化图片
            setupPhotoFilters(); //显示过滤图片列表
        }
    }

    /**
     * 初始化图片
     */
    private void initPicture() {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();//从上一个Activity获取参数图片
        Uri imageUri = Uri.parse(bundle.getString("imageUri"));
        try {
            // 读取uri所在的图片
            pictureBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
        }catch (Exception e){
            e.printStackTrace();
        }
        pictureView.setImageBitmap(pictureBitmap);
    }


    /**
     * 显示过滤图片列表
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void setupPhotoFilters() {
        PhotoFiltersAdapter photoFiltersAdapter = new PhotoFiltersAdapter(
                this, pictureView, pictureBitmap);
        rvFilters.setHasFixedSize(true);
        rvFilters.setAdapter(photoFiltersAdapter);
        rvFilters.setLayoutManager(new LinearLayoutManager(
                this, LinearLayoutManager.HORIZONTAL, false));
    }

    /**
     * 撤销之前的操作
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @OnClick(R.id.btnReset)
    void reset() { //撤销
        PhotoFiltersAdapter.dstBitmap = null;
        pictureView.setImageBitmap(pictureBitmap);
    }


    /**
     * 点击分享按钮
     */
    @OnClick(R.id.btnShare)
    void shareMyPhoto() {
        //先把编辑后的图片存到SD
        String path = FileCacheUtil.EDITPATH; //存储JSON的路径
        FileCacheUtil fileCacheUtil = new FileCacheUtil(path);

        try {
            if(PhotoFiltersAdapter.dstBitmap != null) {
                fileCacheUtil.savePicture(PhotoFiltersAdapter.dstBitmap, "Edit");
            }else {
                fileCacheUtil.savePicture(pictureBitmap, "Edit");
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

    @OnClick(R.id.btnBackFilter)
    void clickBack() {
        onBackPressed();
    }
}