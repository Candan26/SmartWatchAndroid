package com.swatch.smartwatch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.swatch.smartwatch.fragment.SqlTableListFragment;
import com.swatch.smartwatch.ws.SmartWatchServer;

import java.util.List;
import java.util.Map;

public class EkgActivity extends AppCompatActivity {

    private final String EKG_CHARACTERISTIC_UUID = "84538aee-dd71-11e9-a4b4-2a2ae2dbcce4";
    private static  int MAX_VALUE =1024 ;
    private static  int MAX_VALUE_OF_X_AXIS =1024 ;
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
    private SmartWatchServer sw;
    List<SmartWatchServer.SensorInfo> sensorInfoListForHeartRate;


    private static final String TAG = "EKG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ekg);
        mTextView = findViewById(R.id.textViewEkg);
        mImageView = findViewById(R.id.imageViewEkg);
        basVar = (BaseActivity) getApplicationContext();
        basVar.setNotification(EKG_CHARACTERISTIC_UUID,true);
        ekgGraph = findViewById(R.id.ekgGraphViewId);

        SharedPreferences preferences =  PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Map<String, String> keyValues= (Map<String, String>) preferences.getAll();
        MAX_VALUE=Integer.parseInt(keyValues.get("ekgSampleRate"));
        MAX_VALUE_OF_X_AXIS=Integer.parseInt(keyValues.get("ekgSampleRateXAxis"));

        sw = new SmartWatchServer(this);
        //sw.getSensorInfoList(SmartWatchServer.HR);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                sensorInfoListForHeartRate =sw.sensorInfoList;
                Log.d(TAG,"Checking data on LUX");
                setFragment();
            }
        },2000);
        adjustGraphicsProperties(ekgGraph);
        plotData();
    }

    public void setFragment(){
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mImageView.setVisibility(View.GONE);
                ekgGraph.setVisibility(View.GONE);
                mTextView.setVisibility(View.GONE);
                final SqlTableListFragment sqlTableListFragment = new SqlTableListFragment(sensorInfoListForHeartRate,R.layout.activity_ekg);
                final FragmentManager fm = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fm.beginTransaction ();
                fragmentTransaction.add(R.id.EkgLayoutId,sqlTableListFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();

            }
        });
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
    public void onBackPressed() {
        super.onBackPressed();
        mImageView.setVisibility(View.VISIBLE);
        ekgGraph.setVisibility(View.VISIBLE);
        mTextView.setVisibility(View.VISIBLE);

    }

    @Override
    protected void onPause() {
        super.onPause();
        mHandler.removeCallbacks(mTimer1);
        basVar.setNotification(EKG_CHARACTERISTIC_UUID,false);
    }
}
