package com.swatch.smartwatch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.swatch.smartwatch.fragment.SqlTableListFragment;
import com.swatch.smartwatch.ws.SmartWatchServer;

import java.util.List;

public class TemperatureActivity extends AppCompatActivity {

    private final String TEMPERATURE_CHARACTERISTIC_UUID = "94538aee-dd71-11e9-90a4-2a2ae2dbcce4";
    private TextView mTextView;
    private ImageView mImageView;
    private BaseActivity basVar;
    private Runnable mTimer1;
    private final Handler mHandler = new Handler();
    private SmartWatchServer sw;
    List<SmartWatchServer.SensorInfo> sensorInfoListForTemperature;
    private static final String TAG = "TEMP";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temperature);
        mTextView = findViewById(R.id.textViewTemperature);
        mImageView = findViewById(R.id.imageViewTemperature);
        basVar = (BaseActivity) getApplicationContext();
        basVar.setNotification(TEMPERATURE_CHARACTERISTIC_UUID,true);

        sw = new SmartWatchServer(this);
        //sw.getSensorInfoList(SmartWatchServer.TEMPERATURE);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                sensorInfoListForTemperature =sw.sensorInfoList;
                Log.d(TAG,"Checking data on Temperature");
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
                Long tempVal = Long.parseLong(str,16);
                Float f = Float.intBitsToFloat(tempVal.intValue());
                mTextView.setText("byteArray : "+basVar.dataFromNotification +"float val: "+f);
                setImageViewFromBle(f);
                mHandler.postDelayed(this,50);
            }

        };
        mHandler.postDelayed(mTimer1,500);
    }

    private void setImageViewFromBle(float tempVal) {
        if(tempVal<10){
            mImageView.setImageResource(R.drawable.termo_10);
        }else if (tempVal<12.5){
            mImageView.setImageResource(R.drawable.termo_12_5);
        }else if (tempVal<15){
            mImageView.setImageResource(R.drawable.termo_15);
        }else if (tempVal<17.5){
            mImageView.setImageResource(R.drawable.termo_17_5);
        }else if (tempVal<20){
            mImageView.setImageResource(R.drawable.termo_20);
        }else if (tempVal<22.5){
            mImageView.setImageResource(R.drawable.termo_22_5);
        }else if (tempVal<25){
            mImageView.setImageResource(R.drawable.termo_25);
        }else if (tempVal<27.5){
            mImageView.setImageResource(R.drawable.termo_27_5);
        }else if (tempVal<30){
            mImageView.setImageResource(R.drawable.termo_30);
        }else if (tempVal<32.5){
            mImageView.setImageResource(R.drawable.termo_32_5);
        }else if (tempVal<35){
            mImageView.setImageResource(R.drawable.termo_35);
        }else if (tempVal<37.5){
            mImageView.setImageResource(R.drawable.termo_37_5);
        }else if (tempVal<40){
            mImageView.setImageResource(R.drawable.termo_40);
        }else{
            mImageView.setImageResource(R.drawable.termo_40);
        }
    }

    public void setFragment(){
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mImageView.setVisibility(View.GONE);
                mTextView.setVisibility(View.GONE);
                final SqlTableListFragment sqlTableListFragment = new SqlTableListFragment(sensorInfoListForTemperature,R.layout.activity_temperature);
                final FragmentManager fm = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fm.beginTransaction ();
                fragmentTransaction.add(R.id.TemperatureLayoutId,sqlTableListFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        mHandler.removeCallbacks(mTimer1);
        basVar.setNotification(TEMPERATURE_CHARACTERISTIC_UUID,false);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mImageView.setVisibility(View.VISIBLE);
        mTextView.setVisibility(View.VISIBLE);
    }
}
