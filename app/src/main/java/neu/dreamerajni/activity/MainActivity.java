package neu.dreamerajni.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.baidu.mapapi.SDKInitializer;

import butterknife.Bind;
import butterknife.ButterKnife;
import neu.dreamerajni.R;
import neu.dreamerajni.utils.APPUtils;
import neu.dreamerajni.utils.BMapControlUtil;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    @Nullable
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Nullable
    @Bind(R.id.ivLogo)
    ImageView ivLogo;
    @Bind(R.id.old_map_layout)
    LinearLayout oldMapLayout;

    private BMapControlUtil bMapControlUtil = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        startIntroAnimation(); // 开始toolbar的动画

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        if (drawer != null) {
            drawer.setDrawerListener(toggle);
        }
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(this);
        }

        //调用BMap控件
        bMapControlUtil = new BMapControlUtil(this);

    }

    private void startIntroAnimation() {
        int actionbarSize = APPUtils.dpToPx(56);
        if (toolbar != null) {
            toolbar.setTranslationY(-actionbarSize);
            toolbar.animate()
                    .translationY(0)
                    .setDuration(300)
                    .setStartDelay(300);
        }
        if (ivLogo != null) {
            ivLogo.setTranslationY(-actionbarSize);
            ivLogo.animate()
                    .translationY(0)
                    .setDuration(300)
                    .setStartDelay(400);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null) {
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else {
                super.onBackPressed();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        setIconsVisible(menu, true);
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private void setIconsVisible(Menu menu, boolean flag) {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null) {
            drawer.closeDrawer(GravityCompat.START);
        }
        return true;
    }

    @Override
    protected void onStart(){
        super.onStart();

        // 开启图层定位
        if(bMapControlUtil != null) {
            bMapControlUtil.baiduMap.setMyLocationEnabled(true);
            if (!bMapControlUtil.mLocationClient.isStarted()) {
                bMapControlUtil.mLocationClient.start();
            }
            // 开启方向传感器
        bMapControlUtil.myOrientationListener.start();
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        // 在activity执行onDestroy时执行mMapView.onDestroy()，
        // 实现地图生命周期管理
        if(bMapControlUtil != null){
            bMapControlUtil.mapView.onDestroy();
            bMapControlUtil.mapView = null;
        }
    }


    @Override
    protected void onResume(){
        super.onResume();
        if(bMapControlUtil != null){
            bMapControlUtil.mapView.onResume();
        }
    }


    @Override
    protected void onPause(){
        super.onPause();
        if(bMapControlUtil != null){
            bMapControlUtil.mapView.onPause();
        }
    }

    @Override
    protected void onStop(){
        super.onStop();

        // 关闭图层定位
        if(bMapControlUtil != null) {
            bMapControlUtil.baiduMap.setMyLocationEnabled(false);
            bMapControlUtil.mLocationClient.stop();
            // 关闭方向传感器
            bMapControlUtil.myOrientationListener.stop();
        }
    }


}
