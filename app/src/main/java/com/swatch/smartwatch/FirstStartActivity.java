package com.swatch.smartwatch;


import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class FirstStartActivity extends AppCompatActivity {

    private ProgressBar mProgressBar;
    private TextView mTextView;
    private ImageButton mImageButton;
    private Handler mHandler;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final long SCAN_PERIOD = 10000;
    private static final long CANCEL_SCAN_PERIOD = 100;

    private LeDeviceListAdapter mLeDeviceListAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;

    @Override
    protected void onResume() {
        super.onResume();
        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
        // Initializes list view adapter.
        mLeDeviceListAdapter = new LeDeviceListAdapter();
        //setListAdapter(mLeDeviceListAdapter);
        scanLeDevice(true);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_start);
        mProgressBar =findViewById(R.id.progressBarStart);
        mTextView = findViewById(R.id.textViewSearch);
        mImageButton = findViewById(R.id.imageButtonSearch);

        mImageButton.setVisibility(View.GONE);
        mTextView.setText("Starting for searching SMART WATCH device");
        mProgressBar.setVisibility(View.VISIBLE);

        mHandler = new Handler();
        checkBluetoothLowEnergyIsSupported();

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        checkBluetoothISSupported();
        checkAccessCoarseLocation();
        scanLeDevice(true);
        //
        mImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTextView.setText("Starting for searching SMART WATCH device");
                mProgressBar.setVisibility(View.VISIBLE);
                mImageButton.setVisibility(View.GONE);
                scanLeDevice(true);
            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mProgressBar.setVisibility(View.GONE);
                mImageButton.setVisibility(View.VISIBLE);
                mTextView.setText("Device could not found please  open device and search again with button");
                if (mScanning) {
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    mScanning = false;
                }
            }
        },6000);
    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void scanLeDevice(final boolean enable) {
        if(enable){
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    invalidateOptionsMenu();
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        invalidateOptionsMenu();
    }
    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String bleName= getString(R.string.ble_name);
                            //Log.d("BLE", bleName);
                            if(device.getName()!=null && device.getName().equals(bleName)){
                                Log.d("BLE",device.getAddress() +" "+ device.getName());
                                Log.d("BLE","Device has found");
                                final Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                intent.putExtra(MainActivity.EXTRAS_DEVICE_NAME, device.getName());
                                intent.putExtra(MainActivity.EXTRAS_DEVICE_ADDRESS, device.getAddress());
                                BaseActivity baseVal = (BaseActivity)getApplicationContext();
                                baseVal.deviceName=device.getName();
                                baseVal.deviceAddress=device.getAddress();
                                //BaseActivity.getInstance().deviceAddress=device.getAddress();
                                //BaseActivity.getInstance().deviceName=device.getName();
                                if (mScanning) {
                                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                                    mScanning = false;
                                }
                                startActivity(intent);
                            }
                            Log.d("BLE",device.getAddress() +" "+ device.getName());
                        }
                    });
                }
            };


    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkAccessCoarseLocation() {
        if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("This app needs location access");
            builder.setMessage("Please grant location access so this app can detect peripherals.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                }
            });
            builder.show();
        }
    }


    private void checkBluetoothISSupported() {
        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
    }

    private void checkBluetoothLowEnergyIsSupported() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mLeDevices;
        private LayoutInflater mInflator;

        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<BluetoothDevice>();
        }

        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int position) {
            return mLeDevices.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return null;
        }
    }
}
