package com.swatch.smartwatch;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;

public class TemperatureActivity extends AppCompatActivity {

    private final String TEMPERATURE_CHARACTERISTIC_UUID = "94538aee-dd71-11e9-90a4-2a2ae2dbcce4";
    private TextView mTextView;
    private ImageView mImageView;
    private BaseActivity basVar;
    private Runnable mTimer1;
    private final Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temperature);
        mTextView = findViewById(R.id.textViewTemperature);
        mImageView = findViewById(R.id.imageViewTemperature);
        basVar = (BaseActivity) getApplicationContext();
        basVar.setNotification(TEMPERATURE_CHARACTERISTIC_UUID,true);
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
    @Override
    protected void onPause() {
        super.onPause();
        mHandler.removeCallbacks(mTimer1);
        basVar.setNotification(TEMPERATURE_CHARACTERISTIC_UUID,false);
    }
}
