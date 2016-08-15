package neu.dreamerajni.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;

import java.util.Timer;
import java.util.TimerTask;

import neu.dreamerajni.R;

public class WelcomePageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_welcome_page);

        TimerTask task = new TimerTask(){
            @Override
            public void run(){
                Intent intent = new Intent();
                intent.setClass(WelcomePageActivity.this, MainActivity.class);
//                intent.setClass(WelcomePageActivity.this, CityActivity.class);
                WelcomePageActivity.this.startActivity(intent);
                finish();
            }
        };
        Timer timer = new Timer();//计时1s
        timer.schedule(task, 1000);
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }
}
