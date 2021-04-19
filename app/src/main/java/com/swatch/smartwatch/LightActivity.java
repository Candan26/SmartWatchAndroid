package com.swatch.smartwatch;


import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.swatch.smartwatch.fragment.SqlTableListFragment;
import com.swatch.smartwatch.ws.SmartWatchServer;
import java.util.List;


public class LightActivity extends AppCompatActivity {

    private final String LUX_CHARACTERISTIC_UUID = "64538aee-dd71-11e9-98a9-2a2ae2dbcce4";
    private static final String TAG ="BLE" ;
    private TextView mTextView;
    private ImageView mImageView;
    private   BaseActivity basVar;
    private Runnable mTimer1;
    private final Handler mHandler = new Handler();
    private SmartWatchServer sw;
    List<SmartWatchServer.SensorInfo> sensorInfoListForLUX;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_light);

        mTextView= findViewById(R.id.textViewLight);
        mImageView = findViewById(R.id.imageViewLight);
        mTextView.setText("Hello world");
        basVar = (BaseActivity) getApplicationContext();
        basVar.setNotification(LUX_CHARACTERISTIC_UUID,true);

        sw = new SmartWatchServer(this);
        //sw.getSensorInfoList(SmartWatchServer.LUX);

        new Handler().postDelayed(new Runnable() {


            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void run() {
                sensorInfoListForLUX =sw.sensorInfoList;
                for (SmartWatchServer.SensorInfo sensorInfo :sw.sensorInfoList ){
                    Log.d(TAG,"Data From Lux"+ sensorInfo.getData());
                }
                Log.d(TAG,"Checking data on LUX");
                setFragment();
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
                int luxVal = Integer.parseInt(str,16);
                mTextView.setText("byteArray : "+basVar.dataFromNotification +"int val: "+luxVal);
                setImageViewFromBle(luxVal);
                mHandler.postDelayed(this,50);
            }

        };
        mHandler.postDelayed(mTimer1,500);
    }

    private void setImageViewFromBle(int luxVal) {
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

    public void setFragment(){
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mImageView.setVisibility(View.GONE);
                mTextView.setVisibility(View.GONE);
                final SqlTableListFragment sqlTableListFragment = new SqlTableListFragment(sensorInfoListForLUX,R.layout.activity_light);
                final FragmentManager fm = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fm.beginTransaction ();
                fragmentTransaction.add(R.id.LightLayoutId,sqlTableListFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        mHandler.removeCallbacks(mTimer1);
        basVar.setNotification(LUX_CHARACTERISTIC_UUID,false);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mImageView.setVisibility(View.VISIBLE);
        mTextView.setVisibility(View.VISIBLE);
    }
}



