package neu.dreamerajni.utils;

/**
 * Created by 10405 on 2016/8/9.
 * This class is used to get device's orientation.
 */


import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class MyOrientationListener implements SensorEventListener{
    private Context context;
    private SensorManager sensorManager;
    private Sensor sensor;
    private float lastX ;
    private OnOrientationListener onOrientationListener ;

    MyOrientationListener(Context context){
        this.context = context;
    }

    /**
     * Start to get orientation.
     */
    public void start(){
        // 获得传感器管理器
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null){
            // 获得方向传感器TYPE_ORIENTATION
            sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        }
        // 注册
        if (sensor != null){//SensorManager.SENSOR_DELAY_UI
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI);
        }

    }

    /**
     * Stop to get orientation.
     */
    public void stop(){
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy){
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onSensorChanged(SensorEvent event){
        // 接受方向感应器的类型
        if (event.sensor.getType() == Sensor.TYPE_ORIENTATION){
            // 这里我们可以得到数据，然后根据需要来处理
            float x = event.values[SensorManager.DATA_X];

            if( Math.abs(x- lastX) > 1.0 ){
                onOrientationListener.onOrientationChanged(x);
            }
            lastX = x ;

        }
    }

    void setOnOrientationListener(OnOrientationListener onOrientationListener){
        this.onOrientationListener = onOrientationListener ;
    }

    interface OnOrientationListener{
        void onOrientationChanged(float x);
    }

}
