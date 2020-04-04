package com.swatch.smartwatch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.swatch.smartwatch.ws.SmartWatchServer;

import java.util.List;
import java.util.Map;

public class GsrActivity extends AppCompatActivity {

    private static int MAX_VALUE =1024 ;
    private static int MAX_VALUE_OF_X_AXIS =1024 ;


    private final String GSR_CHARACTERISTIC_UUID = "60538aee-dd71-11e9-98a9-2a2ae2dbcce4";
    private TextView mTextView;
    private ImageView mImageView;
    private   BaseActivity basVar;
    private Runnable mTimer1;
    GraphView gsrGraph;
    private final Handler mHandler = new Handler();
    int gsrGraphicCounter=0;
    private LineGraphSeries<DataPoint> mSeries;
    private SmartWatchServer sw;
    List<SmartWatchServer.SensorInfo> sensorInfoListForSKIN;
    private static final String TAG = "GSR";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gsr);

        mTextView = findViewById(R.id.textViewGsr);
        mImageView = findViewById(R.id.imageViewGsr);
        basVar = (BaseActivity) getApplicationContext();
        basVar.setNotification(GSR_CHARACTERISTIC_UUID,true);
        gsrGraph = findViewById(R.id.gsrGraphViewId);

        SharedPreferences preferences =  PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Map<String, String> keyValues= (Map<String, String>) preferences.getAll();
        MAX_VALUE=Integer.parseInt(keyValues.get("egrSampleRate"));
        MAX_VALUE_OF_X_AXIS=Integer.parseInt(keyValues.get("egrSampleRateXAxis"));

        sw = new SmartWatchServer(this);
        sw.getSensorInfoList(SmartWatchServer.SKIN);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                sensorInfoListForSKIN =sw.sensorInfoList;
                Log.d(TAG,"Checking data on GSR");
            }
        },2000);

        adjustGraphicsProperties(gsrGraph);
        plotData();
    }
    public  void plotData(){
        mTimer1 = new Runnable() {
            @Override
            public void run() {
                String str = basVar.dataFromNotification;
                str= str.replace(" ","");
                Long tempVal = Long.parseLong(str,16);
                //Human Resistance = ((1024+2*Serial_Port_Reading)*10000)/(512-Serial_Port_Reading), unit is ohm
                Float f =(float)(((1024+2*tempVal)*10000)/(512-tempVal));
                mTextView.setText("byteArray : "+basVar.dataFromNotification +"float val: "+f);
                plotGraphic(f);
                mHandler.postDelayed(this,25);
            }

        };
        mHandler.postDelayed(mTimer1,500);
    }

    private void plotGraphic(Float f) {
        gsrGraphicCounter++;
        if(gsrGraphicCounter ==MAX_VALUE-1){
            gsrGraphicCounter=0;
            mSeries.resetData(new DataPoint[] {
                    new DataPoint(gsrGraphicCounter, f.floatValue())
            });
        }
        mSeries.appendData(new DataPoint(gsrGraphicCounter,f.floatValue()),true,MAX_VALUE_OF_X_AXIS);
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
        mSeries.setColor(Color.GREEN);
        v.addSeries(mSeries);

    }

    @Override
    protected void onPause() {
        super.onPause();
        mHandler.removeCallbacks(mTimer1);
        basVar.setNotification(GSR_CHARACTERISTIC_UUID,false);
    }
}
