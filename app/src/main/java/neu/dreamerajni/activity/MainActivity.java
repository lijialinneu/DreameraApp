package neu.dreamerajni.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;

import neu.dreamerajni.R;
import neu.dreamerajni.utils.BMapControlUtil;
import neu.dreamerajni.utils.HttpConnectionUtil;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private BMapControlUtil bMapControlUtil = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        // 注意该方法要再setContentView方法之前实现
        SDKInitializer.initialize(getApplicationContext());

        if (!HttpConnectionUtil.isNetworkAvailable(MainActivity.this)) {//检查是否有网络
            Toast.makeText(getApplicationContext(),
                    "当前没有可用网络！", Toast.LENGTH_LONG).show();//没网提醒
            Intent intent = new Intent(); //跳转到NoNetActivity
            intent.setClass(MainActivity.this, NoNetActivity.class);
            MainActivity.this.startActivity(intent);
        }

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //调用BMap控件
        bMapControlUtil = new BMapControlUtil(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

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
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


//    @Override
//    protected void onStart(){
//        //TODO:开启图层定位
//        super.onStart();
//    }
//
//
//    @Override
//    protected void onStop(){
//        //TODO:停止图层定位
//        super.onStop();
//    }


    @Override
    protected void onDestroy(){
        super.onDestroy();
        // 在activity执行onDestroy时执行mMapView.onDestroy()，
        // 实现地图生命周期管理
        if(bMapControlUtil !=null){
            bMapControlUtil.mapView.onDestroy();
            bMapControlUtil.mapView = null;
        }
    }


    @Override
    protected void onResume(){
        super.onResume();
        // 在activity执行onResume时执行mMapView. onResume ()，
        // 实现地图生命周期管理
        if(bMapControlUtil !=null){
            bMapControlUtil.mapView.onResume();
        }
    }


    @Override
    protected void onPause(){
        super.onPause();
        // 在activity执行onPause时执行mMapView. onPause ()，
        // 实现地图生命周期管理
        if(bMapControlUtil !=null){
            bMapControlUtil.mapView.onPause();
        }
    }
}
