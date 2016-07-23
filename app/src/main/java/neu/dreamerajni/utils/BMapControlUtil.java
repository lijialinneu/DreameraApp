package neu.dreamerajni.utils;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapLoadedCallback;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.TextOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;
import neu.dreamerajni.R;
import neu.dreamerajni.view.MarkerPopupWindowView;


/**
 * Created by 10405 on 2016/6/6.
 */

public class BMapControlUtil {

    public Activity activity; //调用BmMap控件的Activity

    @Bind(R.id.id_bmapView)
    public MapView mapView; //绑定地图控件

    public BaiduMap baiduMap; //地图实例
    private MarkerPopupWindowView markerPopupWindowView;//底部弹窗
    public static RelativeLayout map; //地图的容器

    /**
     * 构造函数
     * @param activity
     */
    public BMapControlUtil(final Activity activity) {
        this.activity = activity;
        ButterKnife.bind(this, activity);
        initBMap();//初始化百度地图
    }

    /**
     * 初始化地图
     */
    public void initBMap(){

        markerPopupWindowView = new MarkerPopupWindowView(activity);

        map = (RelativeLayout) activity.findViewById(R.id.id_map);
        baiduMap = mapView.getMap();

        LatLng latLng = new LatLng(41.802273,123.417315);//将地图移至沈阳市
        MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(latLng);
        baiduMap.animateMapStatus(u);

        baiduMap.setOnMapLoadedCallback(callBackHandler);
        baiduMap.setOnMarkerClickListener(markerClickHandler);

        initMarker();//初始化地图标注点
    }


    /**
     * 地图回调
     * @author 10405
     */
    OnMapLoadedCallback callBackHandler  = new OnMapLoadedCallback() {//回调函数
            @Override
            public void onMapLoaded() {
                LatLng northeast = new LatLng(41.94892,123.725236); //东北角
                LatLng southwest = new LatLng(41.618652,123.161532); //西南角
                //设置地图显示范围
                baiduMap.setMapStatusLimits(
                        new LatLngBounds.Builder().include(northeast).include(southwest).build()
                );
            }
    };

    /**
     * 标注点点击
     * @author 10405
     */
    OnMarkerClickListener markerClickHandler = new OnMarkerClickListener() {
        @Override
        public boolean onMarkerClick(final Marker marker){
            try {
                markerPopupWindowView.popupWindow(
                        (String) marker.getExtraInfo().get("name"),
                        (String) marker.getExtraInfo().get("cross_pictures")
                );// 弹出InfoWindow


                RelativeLayout.LayoutParams mapParams =
                        (RelativeLayout.LayoutParams) map.getLayoutParams();
                mapParams.addRule(RelativeLayout.ABOVE, R.id.id_marker_info);
                map.setLayoutParams(mapParams);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return true;
        }
    };


    /**
     * 初始化地图标注点
     * @author 10405
     */
    public void initMarker() {
        AsyncGetDataUtil.getJSONData();//准备好加载的数据
        String jsonFromFile = null;
        while(jsonFromFile == null){
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            jsonFromFile = AsyncGetDataUtil.getJSONFromFile();//JSON数据从文件缓存中读到内存中
        }
        addMarkerOnMap(jsonFromFile);

    }


    /**
     * 在地图上加载标注点
     * @author 10405
     * @param jsonString
     */
    public void addMarkerOnMap(String jsonString) {
        ArrayList<HashMap<String, Object>> pointList = null;
        try {
            pointList = AsyncGetDataUtil.decodeJsonToPoint(jsonString);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        for(int i = 0; i < pointList.size(); i++){

            //在地图上添加标注点marker
            HashMap<String, Object> item = pointList.get(i);
            LatLng latLngMarker = new LatLng(
                    Double.parseDouble(item.get("latitude").toString()),
                    Double.parseDouble(item.get("longitude").toString())
            );// 标注点的经纬度

            BitmapDescriptor mIconMaker = BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker);
            OverlayOptions overlayOptions = new MarkerOptions()//添加图片标注
                    .position(latLngMarker)
                    .icon(mIconMaker)
                    .zIndex(5);// 图标
            Marker marker = (Marker) (baiduMap.addOverlay(overlayOptions));
            OverlayOptions textOption = new TextOptions()//添加文字标注
                    .position(latLngMarker)
                    .text(item.get("name").toString())
                    .fontSize(42)
                    .fontColor(Color.rgb(0, 150, 64));
            baiduMap.addOverlay(textOption);

            //绑定marker的数据
            Bundle bundle = new Bundle();
            bundle.putSerializable("name", item.get("name").toString());
            bundle.putSerializable("cross_pictures", item.get("cross_pictures").toString());
            marker.setExtraInfo(bundle);
        }
    }
}