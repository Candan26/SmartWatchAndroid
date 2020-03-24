package com.swatch.smartwatch;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class DataCollectActivity extends AppCompatActivity {

    private final String DATA_COLLECT_CHARACTERISTIC_UUID = "54538aee-dd71-11e9-98a9-2a2ae2dbcce4";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_collect);
    }
}
