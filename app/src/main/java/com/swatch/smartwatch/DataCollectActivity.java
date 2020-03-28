package com.swatch.smartwatch;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;

public class DataCollectActivity extends AppCompatActivity {

    private final String DATA_COLLECT_CHARACTERISTIC_UUID = "54538aee-dd71-11e9-98a9-2a2ae2dbcce4";
    private TextView mTextView;
    private ImageView mImageView;
    private   BaseActivity basVar;
    private Runnable mTimer1;
    private final Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_collect);

        mTextView = findViewById(R.id.textViewDataCollect);
        mImageView = findViewById(R.id.imageViewDataCollect);
        basVar = (BaseActivity) getApplicationContext();
        basVar.setNotification(DATA_COLLECT_CHARACTERISTIC_UUID,true);
        plotData();
    }
    public  void plotData(){
        mTimer1 = new Runnable() {
            @Override
            public void run() {
                String str = basVar.dataFromNotification;
                mTextView.setText("byteArray : "+basVar.dataFromNotification );
                mHandler.postDelayed(this,25);
            }

        };
        mHandler.postDelayed(mTimer1,500);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mHandler.removeCallbacks(mTimer1);
        basVar.setNotification(DATA_COLLECT_CHARACTERISTIC_UUID,false);
    }
}
