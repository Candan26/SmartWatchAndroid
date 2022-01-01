package com.swatch.smartwatch;


import android.app.Activity;
import android.app.Application;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.SimpleExpandableListAdapter;


import com.swatch.smartwatch.bluetooth.BluetoothLeService;
import com.swatch.smartwatch.bluetooth.SampleGattAttributes;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public class BaseActivity extends Application {


    private static Context context;


    private static final String TAG ="BLE";
    public String deviceName;
    public String deviceAddress;
    public String dataFromNotification="";
    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    public boolean isButtonsActiveFlag = false;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

    public BluetoothLeService mBluetoothLeService;
    private HashMap<String, BluetoothGattCharacteristic> bleCharHashMap = new HashMap<String, BluetoothGattCharacteristic>();

    public static LinkedBlockingQueue queueBle = new LinkedBlockingQueue();

    @Override
    public void onCreate() {
        super.onCreate();
        context=getApplicationContext();
    }

    public void connectBle(){
        Intent gattServiceIntent = new Intent(context, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        Log.d(TAG,"Device Address "+deviceAddress);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mBluetoothLeService != null) {
                    mBluetoothLeService.connect(deviceAddress);
                }
            }
        },600);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
            }
        },1000);
    }

    public  void disconnectBle(){
        mBluetoothLeService.disconnect();
    }

    public void pauseBle(){
        unregisterReceiver(mGattUpdateReceiver);
    }

    public  void unboundBle(){
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    public void destroyBleConnection(){
        mBluetoothLeService.disconnect();
        mBluetoothLeService=null;
    }

    public  void setNotification(String characteristic_uuid, boolean status){
        BluetoothGattCharacteristic dataCharacteristic= bleCharHashMap.get(characteristic_uuid);
        mBluetoothLeService.setCharacteristicNotification(dataCharacteristic,status);

    }

    public  boolean sendCharacteristic(String characteristic_uuid, byte[] character){

        Log.d("BLE_WRITE", "Char "+ new BigInteger(1, character).toString(16));
        BluetoothGattCharacteristic dataCharacteristic= bleCharHashMap.get(characteristic_uuid);
        dataCharacteristic.setValue(character);
        return mBluetoothLeService.writeCharacteristic(dataCharacteristic);
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                //mConnected = true;
                //updateConnectionState(R.string.connected);
                Log.d(TAG,"connected");
                //invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                //mConnected = false;
                //updateConnectionState(R.string.disconnected);
                Log.d(TAG,"disconnected");
                //invalidateOptionsMenu();
                //clearUI();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(mBluetoothLeService.getSupportedGattServices());

            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                //displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
                Log.v(TAG,intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
                dataFromNotification =intent.getStringExtra(BluetoothLeService.EXTRA_CHAR_DATA);
                try {
                    queueBle.put(dataFromNotification);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //Log.d(TAG,"Incoming from intent data" +dataFromNotification);
            }
        }
    };




    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                //finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(deviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };


    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
        String unknownServiceString = getResources().getString(R.string.unknown_service);
        String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
                = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            Log.d(TAG,"service uuid value "+uuid);
            currentServiceData.put(
                    LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                    new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<BluetoothGattCharacteristic>();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                bleCharHashMap.put(uuid,gattCharacteristic);
                Log.d(TAG,"characteristic uuid value "+uuid);
                currentCharaData.put(
                        LIST_NAME, SampleGattAttributes.lookup(uuid, unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);
            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }

        isButtonsActiveFlag = true;
    }
}
