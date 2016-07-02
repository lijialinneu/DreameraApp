package neu.dreamerajni.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.HorizontalScrollView;
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
import neu.dreamerajni.utils.AsyncGetDataUtil;
import neu.dreamerajni.utils.BMapControlUtil;
import neu.dreamerajni.utils.FileCacheUtil;

/**
 * Created by 10405 on 2016/6/10.
 */

public class MarkerPopupWindowView extends View{

    @Bind(R.id.id_close_popup) ImageButton closeButton; //关闭按钮
    @Bind(R.id.id_marker_info) LinearLayout markerInfoLy; // 地图弹出窗口
    @Bind(R.id.id_marker_name) TextView nameView; //弹出窗口中的名字
    @Bind(R.id.cameraButton) FloatingActionButton cameraButton; //照相机对view
    @Bind(R.id.id_gallery) LinearLayout gallery;
    @Bind(R.id.id_scroll) HorizontalScrollView horizontalScrollView;

    public Activity activity;
    private LayoutInflater mInflater;
    private AsyncGetPicTask asyncGetPicTask;
    private int len = 0; //图片list的长度
    private int lastSelectedTag = -1; //上一次选中的图片的索引
    private String intentPID; // 图片的ID作为参数传递到CameraActivity


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
    }


    /**
     * closeButton 点击监听事件
     * @author 10405
     */
    @OnClick(R.id.id_close_popup)
    public void onCloseButtonClick(){
        markerInfoLy.setVisibility(View.GONE);
        RelativeLayout.LayoutParams mapParams =
                (RelativeLayout.LayoutParams) BMapControlUtil.map.getLayoutParams();
        mapParams.addRule(RelativeLayout.ABOVE, 0);
        BMapControlUtil.map.setLayoutParams(mapParams);
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
     * 复用控件
     * @author 10405
     */
    public class ViewHolder {

        @Bind(R.id.id_marker_img) ImageView imgView;
        @Bind(R.id.id_selected) ImageView selectImgView;
        @Bind(R.id.id_date) TextView dateTextView;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }



    /**
     * 地图标注点 点击监听事件
     * @author 10405
     */
    public void popupWindow(String name, String imgList) throws JSONException {

//        cameraButton.setVisibility(View.GONE);
        markerInfoLy.setVisibility(View.VISIBLE);
        gallery.removeAllViews();//清除之前加载的view
        horizontalScrollView.scrollTo(0, 0);//回滚到初始状态
        nameView.setText(name);

        final ArrayList<HashMap<String, Object>>  picList =
                AsyncGetDataUtil.decodeCrossPicturesJsonToPoint(imgList);//解析JSON数据
        len = picList.size(); //长度

        for(int i = 0; i < len; i++) { //循环添加

            final String pictureUrl = picList.get(i).get("url").toString();
            final String pictureId = picList.get(i).get("id").toString();

            View view = mInflater.inflate(R.layout.gallery_layout, gallery, false);
            ViewHolder v = new ViewHolder(view); // view 和 v 应当是一个单身变量
            v.dateTextView.setText(picList.get(i).get("date").toString());
            v.imgView.setImageResource(R.mipmap.ic_nothing);//先都设置为默认图片，
            v.imgView.setTag(i);  //设置tag
            v.selectImgView.setImageResource(R.mipmap.ic_selected); //设置选中的对号图片
            v.selectImgView.setTag(i + "a");
            v.imgView.setOnClickListener(new MyImgClickListener(pictureId));
            v.imgView.clearAnimation();
            gallery.addView(view); //添加view

            if(pictureUrl != "null"){
                asyncGetPicTask = new AsyncGetPicTask(v.imgView, pictureUrl, pictureId);
                asyncGetPicTask.execute();
            }
        }
    }


    /**
     * 图片点击事件
     * @author 10405
     */
    public class MyImgClickListener implements View.OnClickListener {

        private String pictureId;

        public MyImgClickListener(String pictureId){
            this.pictureId = pictureId;
        }

        @Override
        public void onClick(View v) {

            deleteOldSelected(); // 清除以前选中图片的对号小图标
            int i =  (int) v.getTag(); // 显示对号小图标
            ImageView flag = (ImageView) gallery.findViewWithTag(i+"a");
            flag.setVisibility(View.VISIBLE);
            lastSelectedTag = i;
            cameraButton.setVisibility(View.VISIBLE);

            intentPID = pictureId;
        }
    }

    /**
     * 删除已有的图片右上角标注
     * @author 10405
     */
    private void deleteOldSelected(){
        if(lastSelectedTag != -1) {
            if(len > lastSelectedTag){
                ImageView lastSelectedView = (ImageView) gallery.findViewWithTag(lastSelectedTag + "a");
                lastSelectedView.setVisibility(View.INVISIBLE);
            }
        }
    }


    /**
     * 异步加载图片的内部类
     * @author 10405
     */
    class AsyncGetPicTask extends AsyncTask <Void, Void, Void>{

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