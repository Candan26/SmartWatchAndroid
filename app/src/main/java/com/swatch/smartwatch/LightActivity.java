package com.swatch.smartwatch;


import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.swatch.smartwatch.ws.SmartWatchServer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


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
        sw.getSensorInfoList(SmartWatchServer.LUX);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                sensorInfoListForLUX =sw.sensorInfoList;
                Log.d(TAG,"Checking data on LUX");
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
    @Override
    protected void onPause() {
        super.onPause();
        mHandler.removeCallbacks(mTimer1);
        basVar.setNotification(LUX_CHARACTERISTIC_UUID,false);
    }

/*
    public  void getWebServiceResponseData() {
        StringBuffer response = null;
        RequestQueue queue = Volley.newRequestQueue(this);
        String subPageUrl="/api/environment";
        SharedPreferences preferences =  PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Map<String, String> keyValues= (Map<String, String>) preferences.getAll();
        String ip=keyValues.get("serverIpAddress");
        String port=keyValues.get("serverPortNumber");
        String url ="http://"+ip+":"+port+subPageUrl;
        Log.d(TAG,"url name "+ url);
        try {

            StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            // Display the first 500 characters of the response string.
                            Log.d(TAG,"Response is: "+ response);
                            try {
                                JSONArray jsonArray = new JSONArray(response);
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    Log.d(TAG,"From JSON OBJECT "+i+" "+jsonObject.get("id"));
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d(TAG,"That didn't work!");
                }
            });


            queue.add(stringRequest);
        }catch (Exception e){
            Toast.makeText(getApplicationContext(),"Please check ip and port", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

 */
}



