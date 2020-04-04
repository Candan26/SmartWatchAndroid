package com.swatch.smartwatch;


import androidx.appcompat.app.AppCompatActivity;

import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.TextView;


import com.swatch.smartwatch.ws.SmartWatchServer;

import java.util.ArrayList;
import java.util.List;

public class HumidityActivity extends AppCompatActivity {

    private final String HUMIDITY_CHARACTERISTIC_UUID = "74538aee-dd71-11e9-98a9-2a2ae2dbcce4";
    private static final String TAG ="BLE" ;
    private TextView mTextView;
    private ImageView mImageView;
    private   BaseActivity basVar;
    private Runnable mTimer1;
    private final Handler mHandler = new Handler();
    private SmartWatchServer sw;
    List<SmartWatchServer.SensorInfo> sensorInfoListForHumidity;

    int i=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_humidity);
        mTextView= findViewById(R.id.textViewDropAnimation);
        mImageView = findViewById(R.id.imageViewDetail);
        mTextView.setText("Hello world");
        basVar = (BaseActivity) getApplicationContext();
        basVar.setNotification(HUMIDITY_CHARACTERISTIC_UUID,true);

        sw = new SmartWatchServer(this);
        sw.getSensorInfoList(SmartWatchServer.HUMIDITY);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                sensorInfoListForHumidity =sw.sensorInfoList;
                Log.d(TAG,"Checking data on Humidity");
            }
        },2000);

        plotData();
    }

    public  void plotData(){
        mTimer1 = new Runnable() {
            @Override
            public void run() {
                String str = basVar.dataFromNotification;
                str= str.replace(" ","");
                Long tempVal = Long.parseLong(str,16);
                Float f = Float.intBitsToFloat(tempVal.intValue());
                mTextView.setText("byteArray : "+basVar.dataFromNotification +"float val: "+f);
                setImageViewFromBle(f);
                mHandler.postDelayed(this,50);
            }

        };
        mHandler.postDelayed(mTimer1,500);
    }
    private void setImageViewFromBle(float humidityValue) {
        if(humidityValue<10){
            mImageView.setImageResource(R.drawable.drop_image_10_percent);
        }else if (humidityValue<20){
            mImageView.setImageResource(R.drawable.drop_image_20_percent);
        }else if (humidityValue<30){
            mImageView.setImageResource(R.drawable.drop_image_30_percent);
        }else if (humidityValue<40){
            mImageView.setImageResource(R.drawable.drop_image_40_percent);
        }else if (humidityValue<50){
            mImageView.setImageResource(R.drawable.drop_image_50_percent);
        }else if (humidityValue<60){
            mImageView.setImageResource(R.drawable.drop_image_60_percent);
        }else if (humidityValue<70){
            mImageView.setImageResource(R.drawable.drop_image_70_percent);
        }else if (humidityValue<80){
            mImageView.setImageResource(R.drawable.drop_image_80_percent);
        }else if (humidityValue<90){
            mImageView.setImageResource(R.drawable.drop_image_90_percent);
        }else {
            mImageView.setImageResource(R.drawable.drop_image_100_percent);
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        mHandler.removeCallbacks(mTimer1);
        basVar.setNotification(HUMIDITY_CHARACTERISTIC_UUID,false);
    }
}
