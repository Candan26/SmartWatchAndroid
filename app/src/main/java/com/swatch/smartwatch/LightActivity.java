package com.swatch.smartwatch;


import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;



public class LightActivity extends AppCompatActivity {

    private final String LUX_CHARACTERISTIC_UUID = "64538aee-dd71-11e9-98a9-2a2ae2dbcce4";
    private static final String TAG ="BLE" ;
    private TextView mTextView;
    private ImageView mImageView;
    private boolean buttonLightFlag = true;
    private   BaseActivity basVar;
    private Runnable mTimer1;
    private final Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_light);

        mTextView= findViewById(R.id.textViewLight);
        mImageView = findViewById(R.id.imageViewLight);
        mTextView.setText("Hello world");
        basVar = (BaseActivity) getApplicationContext();

        basVar.setNotification(LUX_CHARACTERISTIC_UUID,true);
        plotData();
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(buttonLightFlag){
                    //basVar.setNotification(LUX_CHARACTERISTIC_UUID,true);
                    buttonLightFlag=false;
                }
                else{
                    //basVar.setNotification(LUX_CHARACTERISTIC_UUID,false);
                    buttonLightFlag=true;
                }
            }
        });
    }

    public  void plotData(){
        mTimer1 = new Runnable() {
            @Override
            public void run() {
                String str = basVar.dataFromNotification;
                str= str.replace(" ","");
                int luxVal = Integer.parseInt(str,16);
                mTextView.setText("byteArray : "+basVar.dataFromNotification +"int val: "+luxVal);
                setImageVıewFromBle(luxVal);
                mHandler.postDelayed(this,50);
            }

        };
        mHandler.postDelayed(mTimer1,500);
    }

    private void setImageVıewFromBle(int luxVal) {
        if(luxVal<50){
            mImageView.setImageResource(R.drawable.light_bump_below_50);
        }else if (luxVal<500){
            mImageView.setImageResource(R.drawable.light_bump_between_50_500);
        }else if (luxVal<1000){
            mImageView.setImageResource(R.drawable.light_bump_between_500_1000);
        }else if (luxVal<1500){
            mImageView.setImageResource(R.drawable.light_bump_between_1000_1500);
        }else if (luxVal<2000){
            mImageView.setImageResource(R.drawable.light_bump_between_1500_2000);
        }else if (luxVal<2500){
            mImageView.setImageResource(R.drawable.light_bump_between_2000_2500);
        }else if (luxVal<3000){
            mImageView.setImageResource(R.drawable.light_bump_between_3000_3500);
        }else if (luxVal<3500){
            mImageView.setImageResource(R.drawable.light_bump_between_3500_4000);
        }else{
            mImageView.setImageResource(R.drawable.light_bump_between_3500_4000);
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        mHandler.removeCallbacks(mTimer1);
        basVar.setNotification(LUX_CHARACTERISTIC_UUID,false);
    }

}
