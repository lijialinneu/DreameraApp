package neu.dreamerajni.utils;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.RelativeLayout;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapLoadedCallback;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.GroundOverlay;
import com.baidu.mapapi.map.GroundOverlayOptions;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.TextOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import neu.dreamerajni.R;
import neu.dreamerajni.utils.clustering.Cluster;
import neu.dreamerajni.utils.clustering.ClusterItem;
import neu.dreamerajni.utils.clustering.ClusterManager;
import neu.dreamerajni.view.MarkerPopupWindowView;
import neu.dreamerajni.utils.MyOrientationListener.OnOrientationListener;

/**
 * Created by 10405 on 2016/6/6.
 * BMap Controller used in MainActivity
 */

public class BMapControlUtil {

    @Bind(R.id.id_bmapView)
    public MapView mapView;                                  //绑定地图控件

    public Activity activity;                                //调用BmMap控件的Activity
    public BaiduMap baiduMap;                                //地图实例
    public static MarkerPopupWindowView markerPopupWindowView;     //底部弹窗
    public static RelativeLayout map;                        //地图的容器
    private boolean isFristLocation = true;                  //第一次定位
    private LocationMode mCurrentMode = LocationMode.NORMAL;  //定位模式
    public LocationClient mLocationClient;                   //客户端
    private int mXDirection;                                 //方向传感器X方向的值
    private double mCurrentLantitude;                        //经度
    private double mCurrentLongitude;                        //纬度
    private float mCurrentAccracy;                           //精度
    public MyOrientationListener myOrientationListener;      //方向传感器的监听器

    /**
     * The variable below is related to maker cluster on bmap
     */
    public ClusterManager clusterManager;


    /**
     * Constructor
     * @param activity the activity
     */
    public BMapControlUtil(final Activity activity) {
        this.activity = activity;
        ButterKnife.bind(this, activity);
        initBMap();//初始化百度地图
    }

    /**
     * Initialize baidu map
     */
    private void initBMap(){
        initMyLocation();       // 初始化定位
        initOritationListener();// 初始化方向传感器

        markerPopupWindowView = new MarkerPopupWindowView(activity);
        map = (RelativeLayout) activity.findViewById(R.id.id_map);
        baiduMap = mapView.getMap();

        baiduMap.setOnMapLoadedCallback(callBackHandler);

        clusterManager = new ClusterManager<MyItem>(activity, baiduMap);
        initMarker();//初始化地图标注点

        // 设置地图监听，当地图状态发生改变时，进行点聚合运算
        baiduMap.setOnMapStatusChangeListener(clusterManager);
        baiduMap.setOnMarkerClickListener(clusterManager);
    }


    /**
     * Initialize location
     */
    private void initMyLocation(){
        // 定位初始化
        mLocationClient = new LocationClient(activity);
        MyLocationListener mMyLocationListener = new MyLocationListener();
        mLocationClient.registerLocationListener(mMyLocationListener);
        // 设置定位的相关配置
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);// 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(10000);
        mLocationClient.setLocOption(option);
    }

    /**
     * Get present location
     */
    private class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location){
            // map view 销毁后不在处理新接收的位置
            if (location == null || mapView == null){
                return;
            }
            // 构造定位数据
            MyLocationData locData = new MyLocationData.Builder()
                .accuracy(location.getRadius())
                // 此处设置开发者获取到的方向信息，顺时针0-360
                .direction(mXDirection).latitude(location.getLatitude())
                .longitude(location.getLongitude()).build();
            mCurrentAccracy = location.getRadius();

//            System.out.println("asdf error code"+location.getLocType());
            baiduMap.setMyLocationData(locData);               // 设置定位数据
            mCurrentLantitude = location.getLatitude();
            mCurrentLongitude = location.getLongitude();

            BitmapDescriptor mCurrentMarker = null;            // 设置自定义图标
            MyLocationConfiguration config = new MyLocationConfiguration(
                    mCurrentMode, true, mCurrentMarker);
            baiduMap.setMyLocationConfigeration(config);

        }
    }

    /**
     * Initialize map orientation listener
     */
    private void initOritationListener(){
        myOrientationListener = new MyOrientationListener(activity);
        myOrientationListener.setOnOrientationListener(new OnOrientationListener() {
            @Override
            public void onOrientationChanged(float x) {
                mXDirection = (int) x;
                // 构造定位数据
                MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(mCurrentAccracy)
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(mXDirection)
                    .latitude(mCurrentLantitude)
                    .longitude(mCurrentLongitude).build();
                baiduMap.setMyLocationData(locData);// 设置定位数据
                BitmapDescriptor mCurrentMarker = null;// 设置自定义图标
                MyLocationConfiguration config = new MyLocationConfiguration(
                        mCurrentMode, true, mCurrentMarker);
                baiduMap.setMyLocationConfigeration(config);
            }
        });
    }


    /**
     * Map call back
     */
    OnMapLoadedCallback callBackHandler  = new OnMapLoadedCallback() {//回调函数

        @Override
        public void onMapLoaded() {
//            LatLng northeast = new LatLng(41.94892,123.725236); //东北角
//            LatLng southwest = new LatLng(41.618652,123.161532); //西南角
//            //设置地图显示范围
//            baiduMap.setMapStatusLimits(
//                    new LatLngBounds.Builder().include(northeast).include(southwest).build()
//            );
            baiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(
                    new MapStatus.Builder().zoom(18).build()
            ));
            if (isFristLocation){                // 第一次定位时，将地图位置移动到当前位置
                isFristLocation = false;
                LatLng ll = new LatLng(mCurrentLantitude, mCurrentLongitude);
                MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
                baiduMap.animateMapStatus(u);
            }
        }
    };

    /**
     * Initialize marker clickerListener
     * Call addMarker function.
     */
    public void initMarker() {
        AsyncGetDataUtil.getJSONData();//准备好加载的数据
        String jsonFromFile = null;
        while(jsonFromFile == null) {
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
     * Add marker on map
     * @param jsonString json from file
     */
    public void addMarkerOnMap(String jsonString) {
        ArrayList<HashMap<String, Object>> pointList = null;
        try {
            pointList = AsyncGetDataUtil.decodeJsonToPoint(jsonString);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        List<MyItem> items = new ArrayList<MyItem>();

        for(int i = 0; i < pointList.size(); i++) {

            //在地图上添加标注点marker
            HashMap<String, Object> item = pointList.get(i);

            if(item.get("cross_pictures").equals("[]")) {
               continue;
            }

            LatLng latLngMarker = new LatLng(
                    Double.parseDouble(item.get("latitude").toString()),
                    Double.parseDouble(item.get("longitude").toString())
            );// 标注点的经纬度

            items.add(new MyItem(
                    latLngMarker,
                    item.get("name").toString(),
                    item.get("text").toString(),
                    item.get("cross_pictures").toString()
            ));
        }

        clusterManager.addItems(items);
    }


    /**
     * 每个Marker点，包含Marker点坐标以及图标
     */
    public class MyItem implements ClusterItem {

        private final LatLng mPosition;
        private String name;
        private String text;
        private String cross_pictures;

        public MyItem(LatLng latLng, String name, String text, String cross_pictures) {
            mPosition = latLng;
            this.name = name;
            this.text = text;
            this.cross_pictures = cross_pictures;
        }

        @Override
        public LatLng getPosition() {
            return mPosition;
        }//返回marker的坐标

        @Override
        public BitmapDescriptor getBitmapDescriptor() {//返回marker的图标
            return BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker);
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getText() {
            return text;
        }

        @Override
        public String getCrossPictures() {
            return cross_pictures;
        }
    }

}