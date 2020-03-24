package com.swatch.smartwatch;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;

public class GsrActivity extends AppCompatActivity {

    private final String GSR_CHARACTERISTIC_UUID = "60538aee-dd71-11e9-98a9-2a2ae2dbcce4";
    private TextView mTextView;
    private ImageView mImageView;
    private   BaseActivity basVar;
    private Runnable mTimer1;
    private final Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gsr);

        mTextView = findViewById(R.id.textViewGsr);
        mImageView = findViewById(R.id.imageViewGsr);
        basVar = (BaseActivity) getApplicationContext();
        basVar.setNotification(GSR_CHARACTERISTIC_UUID,true);
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
                mTextView.setText("byteArray : "+basVar.dataFromNotification +"int val: "+f);
                plotGraphic(f);
                mHandler.postDelayed(this,50);
            }

        };
        mHandler.postDelayed(mTimer1,500);
    }

    private void plotGraphic(Float f) {
    }

    @Override
    protected void onPause() {
        super.onPause();
        mHandler.removeCallbacks(mTimer1);
        basVar.setNotification(GSR_CHARACTERISTIC_UUID,false);
    }
}
