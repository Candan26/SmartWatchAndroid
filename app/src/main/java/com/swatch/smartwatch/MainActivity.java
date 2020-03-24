package com.swatch.smartwatch;


import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.swatch.smartwatch.bluetooth.BluetoothLeService;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {


    private boolean buttonDropFlag = true;
    private boolean buttonDataCollectFlag = true;
    private boolean buttonESRFlag = true;
    private boolean buttonGSRFlag = true;
    private boolean buttonTemperatureFlag = true;
    private boolean buttonLightFlag = true;

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private static final String TAG ="BLE" ;
    public String mDeviceName;
    private String mDeviceAddress;


    ImageButton mButtonDrop;
    ImageButton mButtonDataCollect;
    ImageButton mButtonGSR;
    ImageButton mButtonESR;
    ImageButton mButtonTemperature;
    ImageButton mButtonLight;
    private   BaseActivity basVar;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mButtonDrop = findViewById(R.id.imageButtonDrop);
        mButtonDataCollect = findViewById(R.id.imageButtonData);
        mButtonTemperature = findViewById(R.id.imageButtonTermo);
        mButtonESR = findViewById(R.id.imageButtonEKG);
        mButtonGSR = findViewById(R.id.imageButtonESR);
        mButtonLight = findViewById(R.id.imageButtonLight);
        basVar = (BaseActivity) getApplicationContext();
        basVar.connectBle();

        setButtonsVisibleSituation(View.INVISIBLE);
        while(basVar.isButtonsActiveFlag); // wait until ble connect
        setButtonsVisibleSituation(View.VISIBLE);
        mButtonDrop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),HumidityActivity.class);
                startActivity(intent);
            }
        });


        mButtonDataCollect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), DataCollectActivity.class);
                startActivity(intent);
            }
        });

        mButtonTemperature.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), TemperatureActivity.class);
                startActivity(intent);
            }
        });

        mButtonESR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), EkgActivity.class);
                startActivity(intent);
            }
        });

        mButtonGSR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), GsrActivity.class);
                startActivity(intent);
            }
        });

        mButtonLight.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),LightActivity.class);
                startActivity(intent);
            }
        });

    }

    void setButtonsVisibleSituation(int status){
        mButtonDrop.setVisibility(status);
        mButtonDataCollect.setVisibility(status);
        mButtonTemperature.setVisibility(status);
        mButtonESR.setVisibility(status);
        mButtonGSR.setVisibility(status);
        mButtonLight.setVisibility(status);
    }

}
