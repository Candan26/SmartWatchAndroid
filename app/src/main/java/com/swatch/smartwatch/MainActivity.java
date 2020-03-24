package com.swatch.smartwatch;


import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ImageButton;

import com.swatch.smartwatch.bluetooth.BluetoothLeService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
    private ExpandableListView mGattServicesList;
    private BluetoothLeService mBluetoothLeService;
    private boolean mConnected = false;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    private final String SMART_WATCH_SERVICE_UUID = "94538aee-dd71-11e9-8a34-2a2ae2dbcce4";

    private  BluetoothGattCharacteristic temperatureCharacteristic=null;
    private final String TEMPERATURE_CHARACTERISTIC_UUID = "94538aee-dd71-11e9-90a4-2a2ae2dbcce4";

    private  BluetoothGattCharacteristic humidityCharacteristic=null;
    private final String HUMIDITY_CHARACTERISTIC_UUID = "74538aee-dd71-11e9-98a9-2a2ae2dbcce4";

    private  BluetoothGattCharacteristic esrCharacteristic=null;
    private final String ESR_CHARACTERISTIC_UUID = "84538aee-dd71-11e9-a4b4-2a2ae2dbcce4";

    private  BluetoothGattCharacteristic luxCharacteristic=null;
    private final String LUX_CHARACTERISTIC_UUID = "64538aee-dd71-11e9-98a9-2a2ae2dbcce4";

    private  BluetoothGattCharacteristic gsrCharacteristic=null;
    private final String GSR_CHARACTERISTIC_UUID = "60538aee-dd71-11e9-98a9-2a2ae2dbcce4";

    private  BluetoothGattCharacteristic dataCharacteristic=null;
    private final String DATA_COLLECT_CHARACTERISTIC_UUID = "54538aee-dd71-11e9-98a9-2a2ae2dbcce4";

    private HashMap<String, BluetoothGattCharacteristic> bleCharHashMap = new HashMap<String, BluetoothGattCharacteristic>();



    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

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

        //setButtonsVisibleSituation(View.INVISIBLE);

        mButtonDrop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent intent = new Intent(getApplicationContext(),HumidityActivity.class);
                //startActivity(intent);
                humidityCharacteristic= bleCharHashMap.get(HUMIDITY_CHARACTERISTIC_UUID);
                if(buttonDropFlag){
                    mBluetoothLeService.setCharacteristicNotification(humidityCharacteristic,true);
                    buttonDropFlag=false;
                }
                else{
                    mBluetoothLeService.setCharacteristicNotification(humidityCharacteristic,false);
                    buttonDropFlag=true;
                }

            }
        });


        mButtonDataCollect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //mBluetoothLeService.connect(mDeviceAddress);
                //Intent intent = new Intent(getApplicationContext(), DataCollectActivity.class);
                //startActivity(intent);
                dataCharacteristic= bleCharHashMap.get(DATA_COLLECT_CHARACTERISTIC_UUID);
                if(buttonDataCollectFlag ){
                    mBluetoothLeService.setCharacteristicNotification(dataCharacteristic,true);
                    buttonDataCollectFlag=false;
                }
                else{
                    mBluetoothLeService.setCharacteristicNotification(dataCharacteristic,false);
                    buttonDataCollectFlag=true;
                }

            }
        });

        mButtonTemperature.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent intent = new Intent(getApplicationContext(), TemperatureActivity.class);
                //startActivity(intent);
                temperatureCharacteristic= bleCharHashMap.get(TEMPERATURE_CHARACTERISTIC_UUID);
                if(buttonTemperatureFlag){
                    mBluetoothLeService.setCharacteristicNotification(temperatureCharacteristic,true);
                    buttonTemperatureFlag=false;
                }
                else{
                    mBluetoothLeService.setCharacteristicNotification(temperatureCharacteristic,false);
                    buttonTemperatureFlag=true;
                }
            }
        });

        mButtonESR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent intent = new Intent(getApplicationContext(), EkgActivity.class);
                //startActivity(intent);
                esrCharacteristic= bleCharHashMap.get(ESR_CHARACTERISTIC_UUID);
                if(buttonESRFlag){
                    mBluetoothLeService.setCharacteristicNotification(esrCharacteristic,true);
                    buttonESRFlag=false;
                }
                else{
                    mBluetoothLeService.setCharacteristicNotification(esrCharacteristic,false);
                    buttonESRFlag=true;
                }
            }
        });

        mButtonGSR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent intent = new Intent(getApplicationContext(), EsrActivity.class);
                //startActivity(intent);
                gsrCharacteristic= bleCharHashMap.get(GSR_CHARACTERISTIC_UUID);
                if(buttonGSRFlag){
                    mBluetoothLeService.setCharacteristicNotification(gsrCharacteristic,true);
                    buttonGSRFlag=false;
                }
                else{
                    mBluetoothLeService.setCharacteristicNotification(gsrCharacteristic,false);
                    buttonGSRFlag=true;
                }
            }
        });

        mButtonLight.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),LightActivity.class);
                luxCharacteristic= bleCharHashMap.get(LUX_CHARACTERISTIC_UUID);
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onStop() {
        super.onStop();
        // basVar.disconnectBle();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //basVar.pauseBle();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //basVar.unboundBle();
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
