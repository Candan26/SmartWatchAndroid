package com.swatch.smartwatch.ws;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.preference.PreferenceManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class SmartWatchServer extends Service {


    private static final String TAG = "WS";
    private static String EnvironmentSensorSubPageURL= "/api/environment";
    private static String SkinSensorSubPageURL= "/api/skin";
    private static String HeartSensorSubPageURL= "/api/heart";
    private static String UserInfoSubPageURL= "/api/userInfo";
    private static String EmailSubPageURL= "/api/email";

    public static final  String LUX= "Luminance";
    public static final  String HUMIDITY= "humidity";
    public static final  String TEMPERATURE= "temperature";
    public static final  String HR= "heart";
    public static final  String SKIN= "skin";
    public static final  String EMAIL= "email";

    private String mIp = "";
    private String mPort = "";

    public List<SensorInfo> sensorInfoList;
    private List<UserInfo> userInfoList;

    private Context mContext;
    private String personName="";

    String pattern = "yyyy-MM-dd";
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

    public SmartWatchServer(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        Map<String, String> keyValues = (Map<String, String>) preferences.getAll();
        mIp = keyValues.get("serverIpAddress");
        mPort = keyValues.get("serverPortNumber");
        personName= keyValues.get("usrName")+keyValues.get("usrSurname")+keyValues.get("usrAge");
        mContext = context;
        sensorInfoList= new ArrayList<>();
        userInfoList= new ArrayList<>();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void setSensorInfo(final String sensorType, final String data){
        StringBuffer response = null;
        RequestQueue queue = Volley.newRequestQueue(mContext);

        String url = "http://" + mIp + ":" + mPort + getSubPageUrl(sensorType);
        Log.d(TAG, "url name " + url);

        Date today =  Calendar.getInstance().getTime();
        final String date = simpleDateFormat.format(today);
        Log.d(TAG,"Date: "+date);
        HashMap<String, String> params = new HashMap<String, String>();
        if(sensorType.equals(EMAIL)){
            try {
                JSONObject jsonObject = new JSONObject(data);
                //JSONObject jsonObject = jsonArray.getJSONObject(i+1);
                params.put("address",jsonObject.getString("address"));
                params.put("skinPageNumber",jsonObject.getString("skinPageNumber"));
                params.put("skinRowPerPage",jsonObject.getString("skinRowPerPage"));
                params.put("heartPageNumber",jsonObject.getString("heartPageNumber"));
                params.put("heartRowPerPage",jsonObject.getString("heartRowPerPage"));
                params.put("envPageNumber",jsonObject.getString("envPageNumber"));
                params.put("envRowPerPage",jsonObject.getString("envRowPerPage"));

            }catch (Exception e){
                Log.d(TAG,e.toString());
            }
        }else {
            params.put("id", "1");
            params.put("type", sensorType);
            params.put("data", data);
            params.put("date", date);
            params.put("person", personName);
        }
        JsonObjectRequest req = new JsonObjectRequest(url, new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.d(TAG, response.toString(4));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, error.toString());
            }
        });
        queue.add(req);
    }

    public List<SensorInfo> getSensorInfoList(final String sensorType){
        StringBuffer response = null;
        RequestQueue queue = Volley.newRequestQueue(mContext);

        String url = "http://" + mIp + ":" + mPort + getSubPageUrl(sensorType);
        Log.d(TAG, "url name " + url);
        try {
            StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.d(TAG, "Response is: " + response);
                            sensorInfoList= parseJSONData4SensorInfo(response,sensorType);
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d(TAG, "That didn't work!");
                }
            });
            queue.add(stringRequest);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Please check ip and port", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        return sensorInfoList;
    }

    private List<SensorInfo> parseJSONData4SensorInfo(String response, String SensorType){
        try {
            JSONArray jsonArray = new JSONArray(response);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if(jsonObject.get("type").equals(SensorType)){
                    Log.d(TAG, "From JSON OBJECT got for typ"+ jsonObject.get("type"));
                    SensorInfo sensorInfo = new SensorInfo();
                    sensorInfo.SensorInfoDataBaseType= SensorType;
                    sensorInfo.id = jsonObject.getString("id");
                    sensorInfo.type = jsonObject.getString("type");
                    sensorInfo.data = jsonObject.getString("data");
                    sensorInfo.date = jsonObject.getString("date");
                    sensorInfo.person = jsonObject.getString("person");
                    sensorInfoList.add(sensorInfo);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return sensorInfoList;
    }

    private String getSubPageUrl(String type){
        String subUrl="";
        if(type.equals("Luminance") || type.equals("humidity") || type.equals("temperature"))
            subUrl = EnvironmentSensorSubPageURL;
        else if(type.equals("skin"))
            subUrl = SkinSensorSubPageURL;
        else if(type.equals("heart"))
            subUrl=HeartSensorSubPageURL;
        else if(type.equals("email"))
            subUrl=EmailSubPageURL;
        else
            subUrl= UserInfoSubPageURL;
        return subUrl;
    }


/*
    public void getWebServiceResponseData() {
        StringBuffer response = null;
        RequestQueue queue = Volley.newRequestQueue(mContext);
        String subPageUrl = "/api/environment";
        String url = "http://" + mIp + ":" + mPort + subPageUrl;
        Log.d(TAG, "url name " + url);
        try {
            StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            // Display the first 500 characters of the response string.
                            Log.d(TAG, "Response is: " + response);
                            try {
                                JSONArray jsonArray = new JSONArray(response);
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    Log.d(TAG, "From JSON OBJECT " + i + " " + jsonObject.get("id"));
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d(TAG, "That didn't work!");
                }
            });
            queue.add(stringRequest);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Please check ip and port", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
*/
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    public class SensorInfo {

        private String SensorInfoDataBaseType;
        private String id;
        private String type;
        private String data;
        private String date;
        private String person;

        public String getId() {
            return id;
        }

        public String getType() {
            return type;
        }

        public String getData() {
            return data;
        }

        public String getDate() {
            return date;
        }

        public String getPerson() {
            return person;
        }

        public String getSensorInfoDataBaseType() {
            return SensorInfoDataBaseType;
        }
    }

    public class UserInfo {
        //for configuration
        private String configId;
        private String name;
        private String surName;
        private String age;
        private String weight;
        private String height;
    }
}