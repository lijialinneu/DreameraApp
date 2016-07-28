package neu.dreamerajni.view;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import neu.dreamerajni.R;
import neu.dreamerajni.activity.CameraActivity;
import neu.dreamerajni.adapter.PhotoListAdapter;
import neu.dreamerajni.utils.AsyncGetDataUtil;
import neu.dreamerajni.utils.BMapControlUtil;

/**
 * Created by 10405 on 2016/6/10.
 */

public class MarkerPopupWindowView extends View{

    @Bind(R.id.id_close_popup)
    ImageButton closeButton; //关闭按钮
    @Bind(R.id.id_marker_info)
    LinearLayout markerInfoLy; // 地图弹出窗口
    @Bind(R.id.id_marker_name)
    TextView nameView; //弹出窗口中的名字

//    @Bind(R.id.cameraButton)
//    FloatingActionButton cameraButton; //照相机对view
//    @Bind(R.id.id_gallery)
//    RecyclerView gallery;

    private static FloatingActionButton cameraButton;
    private static RecyclerView gallery;
//    private static int len = 0; //图片list的长度
//    private static int lastSelectedTag = -1; //上一次选中的图片的索引
    private static String intentPID; // 图片的ID作为参数传递到CameraActivity

    public Activity activity;
    private LayoutInflater mInflater;
//    private AsyncGetPicTask asyncGetPicTask;
    private boolean NEVERPOPUP = true;
    private String noDataTip = "该处暂无数据";
    private static ImageView flagView;

    /**
     * 构造函数
     * @param context
     * @return string
     * @author 10405
     */
    public MarkerPopupWindowView(Context context){
        super(context);
        this.activity = (Activity) context;
        ButterKnife.bind(this, activity);
        mInflater = LayoutInflater.from(activity);

        cameraButton = (FloatingActionButton) activity.findViewById(R.id.cameraButton);
        gallery = (RecyclerView) activity.findViewById(R.id.id_gallery);
    }


    /**
     * closeButton 点击监听事件
     * @author 10405
     */
    @OnClick(R.id.id_close_popup)
    public void onCloseButtonClick(){

        TranslateAnimation mShowAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                0.0f, Animation.RELATIVE_TO_SELF, 1.0f);
        mShowAction.setDuration(500);
        markerInfoLy.startAnimation(mShowAction);

        RelativeLayout.LayoutParams mapParams =
                (RelativeLayout.LayoutParams) BMapControlUtil.map.getLayoutParams();
        mapParams.addRule(RelativeLayout.ABOVE, 0);
        BMapControlUtil.map.setLayoutParams(mapParams);

        markerInfoLy.setVisibility(View.GONE);
        cameraButton.setVisibility(INVISIBLE);
        NEVERPOPUP = true;
    }


    /**
     * cameraView 点击监听事件
     * @author 10405
     */
    @OnClick(R.id.cameraButton)
    public void onCameraButtonClick(){
        int[] startingLocation = new int[2];
        cameraButton.getLocationOnScreen(startingLocation);
        startingLocation[0] += cameraButton.getWidth() / 2;
        CameraActivity.startCameraFromLocation(startingLocation, activity, intentPID);
        activity.overridePendingTransition(0, 0);
    }


    /**
     * 地图标注点 点击监听事件
     * @author 10405
     */
    @TargetApi(Build.VERSION_CODES.M)
    public void popupWindow(String name, String imgList) throws JSONException {
        cameraButton.setVisibility(INVISIBLE);
        if(NEVERPOPUP) {
            TranslateAnimation mShowAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                    Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                    1.0f, Animation.RELATIVE_TO_SELF, 0.0f);
            mShowAction.setDuration(300);
            markerInfoLy.startAnimation(mShowAction);
            NEVERPOPUP = false;
        }
        markerInfoLy.setVisibility(View.VISIBLE);

        if(imgList.equals("[]")) {
            nameView.setText(noDataTip);
            gallery.setVisibility(GONE);
        } else {
            nameView.setText(name);
            final ArrayList<HashMap<String, Object>> picList =
                    AsyncGetDataUtil.decodeCrossPicturesJsonToPoint(imgList);//解析JSON数据
            PhotoListAdapter photoListAdapter = new PhotoListAdapter(activity, picList);
            gallery.setVisibility(VISIBLE);
            gallery.setHasFixedSize(true);
            gallery.setAdapter(photoListAdapter);
            gallery.setLayoutManager(
                    new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false));
        }
    }

    /**
     * 图片点击事件
     * @author 10405
     */
    public static class MyImgClickListener implements View.OnClickListener {

        private String pictureId;
        private String url;

        public MyImgClickListener(String pictureId, String url){
            this.pictureId = pictureId;
            this.url = url;
        }

        @Override
        public void onClick(View v) {
            deleteOldSelected(); // 清除以前选中图片的对号小图标
            int i =  (int) v.getTag(); // 显示对号小图标

//            ImageView flag = (ImageView) gallery.findViewWithTag(i + "a");
//            flag.setVisibility(View.VISIBLE);

            flagView = (ImageView) gallery.findViewWithTag(i + "a");
            flagView.setVisibility(VISIBLE);

//            lastSelectedTag = i;
            cameraButton.setVisibility(View.VISIBLE);

            intentPID = pictureId; //设置Activity间传递的参数
        }
    }

    /**
     * 删除已有的图片右上角标注
     * @author 10405
     */
    private static void deleteOldSelected(){
        if(flagView != null) {
            flagView.setVisibility(GONE);
        }
//        if(lastSelectedTag != -1) {
//            if(len > lastSelectedTag){
//                ImageView lastSelectedView = (ImageView) gallery.findViewWithTag(lastSelectedTag + "a");
//                if(lastSelectedView != null) {
//                    lastSelectedView.setVisibility(View.GONE);
//                }
//            }
//        }
    }


    /**
     * 异步加载图片的内部类
     * @author 10405
     */
    public static class AsyncGetPicTask extends AsyncTask <Void, Void, Void>{

        private ImageView imgView;
        private Bitmap pictureBitmap;
        private String pictureUrl;
        private String pictureId;

        public AsyncGetPicTask(ImageView i, String pictureUrl, String picId){
            this.imgView = i;
            this.pictureUrl = pictureUrl;
            this.pictureId = picId;
        }

        @Override
        protected Void doInBackground(Void... params) {
            AsyncGetDataUtil.getPictureData(pictureId, pictureUrl);
            pictureBitmap = null;
            while (pictureBitmap == null) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                pictureBitmap = AsyncGetDataUtil.getPicFromFile(pictureId);//JSON数据从文件缓存中读到内存中
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void integer) {
            this.imgView.setImageBitmap(pictureBitmap);
        }
    }
}