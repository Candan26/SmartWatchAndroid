package com.swatch.smartwatch;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

public class EkgActivity extends AppCompatActivity {

    private final String EKG_CHARACTERISTIC_UUID = "84538aee-dd71-11e9-a4b4-2a2ae2dbcce4";
    private static final int MAX_VALUE =1024 ;
    private static final int MAX_VALUE_OF_X_AXIS =1024 ;
    private TextView mTextView;
    private ImageView mImageView;
    private BaseActivity basVar;
    private Runnable mTimer1;
    private final Handler mHandler = new Handler();
    GraphView ekgGraph;
    int ekgGraphicCounter=0;
    private LineGraphSeries<DataPoint> mSeries;

    private int MAX_VALUE_EKG = 0xFFFF;
    private int MAX_VALUE_ADC= 4000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ekg);
        mTextView = findViewById(R.id.textViewEkg);
        mImageView = findViewById(R.id.imageButtonEKG);
        basVar = (BaseActivity) getApplicationContext();
        basVar.setNotification(EKG_CHARACTERISTIC_UUID,true);
        ekgGraph = findViewById(R.id.ekgGraphViewId);
        adjustGraphicsProperties(ekgGraph);
        plotData();

    }
    public  void plotData(){
        mTimer1 = new Runnable() {
            @Override
            public void run() {
                String str = basVar.dataFromNotification;
                str= str.replace(" ","");
                Long tempVal = Long.parseLong(str,16);
                Float f = (float)(MAX_VALUE_EKG/MAX_VALUE_ADC)*tempVal;

                mTextView.setText("byteArray : "+basVar.dataFromNotification +"float val: "+f);
                plotGraphic(f);
                mHandler.postDelayed(this,25);
            }

        };
        mHandler.postDelayed(mTimer1,500);
    }

    private void plotGraphic(Float f) {
        ekgGraphicCounter++;
        if(ekgGraphicCounter ==MAX_VALUE-1){
            ekgGraphicCounter=0;
            mSeries.resetData(new DataPoint[] {
                    new DataPoint(ekgGraphicCounter, f.floatValue())
            });
        }
        mSeries.appendData(new DataPoint(ekgGraphicCounter,f.floatValue()),true,MAX_VALUE_OF_X_AXIS);

    }

    private  void  adjustGraphicsProperties(GraphView v){
        v.getViewport().setScrollable(true);
        v.getViewport().setScalable(true);
        v.getViewport().setMinX(0);
        v.getViewport().setMaxX(MAX_VALUE_OF_X_AXIS);
        v.getViewport().setXAxisBoundsManual(true);

        mSeries = new LineGraphSeries<>();
        mSeries.setDrawDataPoints(true);
        mSeries.setDrawBackground(true);
        mSeries.setColor(Color.RED);
        v.addSeries(mSeries);

    }

    @Override
    protected void onPause() {
        super.onPause();
        mHandler.removeCallbacks(mTimer1);
        basVar.setNotification(EKG_CHARACTERISTIC_UUID,false);
    }
}
