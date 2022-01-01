package com.swatch.smartwatch;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.swatch.smartwatch.fragment.EmailFragment;
import com.swatch.smartwatch.sensors.Max3003;
import com.swatch.smartwatch.sensors.Max30102;
import com.swatch.smartwatch.sensors.Si7021;
import com.swatch.smartwatch.sensors.SkinResistance;
import com.swatch.smartwatch.ws.SmartWatchServer;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public class DataCollectActivity extends AppCompatActivity {

    private static final int MAX_BYTE_LENGTH = 128;//256
    private static final String TAG = "DATA_COLLECT";
    private static final String RB_MAX30003 = "MAX30003";
    private static final String RB_MAX30102 = "MAX30102";
    private static final String RB_SI7021 = "Si7021";
    private static final String RB_SR = "SkinResistance";
    private static final String RB_CLOSE = "ShotDownOled";
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    private static final int GSR_DEF_VAL =1870 ;
    private static final short MAX30003_BIT_POSITION = 4;
    private static final short MAX30102_BIT_POSITION = 5;
    private static final short SI7021_BIT_POSITION = 6;
    private static final short SR_BIT_POSITION = 7;

    private final String DATA_COLLECT_CHARACTERISTIC_UUID = "24538aee-dd71-11e9-98a9-2a2ae2dbcce4";
    public final String TYPE_DATABASE = "database";
    public final String TYPE_ONLINE = "online";
    private String activeRadioButton = RB_CLOSE;
    private String lastActiveButton = RB_CLOSE;

    private TextView mTextView;

    private ImageView mImageView;

    private BaseActivity basVar;
    private Switch mSwitchOnlineData;
    private Switch mSwitchSaveData;
    private CheckBox mCheckBoxEcgRr;
    private CheckBox mCheckBoxHrSpo2;
    private CheckBox mCheckBoxSkin;
    private CheckBox mCheckBoxTemperatureHumidity;

    private EditText mEdtOnlineDataCounter;
    private EditText mEdtMongoDbDataCounter;
    private GraphView swbGraph;
    private Runnable mTimer1;
    private final Handler mHandler = new Handler();

    public static final int HUMIDITY = 0;
    public static final int TEMPERATURE = 1;
    public static final int GSR = 2;
    public static final int HR = 3;
    public static final int SPO2 = 4;
    public static final int IRED = 5;
    public static final int RED = 6;
    public static final int RR_COUNTER = 7;
    public static final int RR = 8;
    public static final int BPM_COUNTER = 9;
    public static final int BPM = 10;
    public static final int ECG_COUNTER = 11;
    public static final int ECG = 12;
    private int MAX_VALUE_EKG = 0xFFFF;
    private int MAX_COUNTER_VAL_SR= 1024;
    private int MAX_COUNTER_VAL_SI7021= 512;
    private int MAX_COUNTER_VAL_MAX3003= 4096;
    private int MAX_COUNTER_VAL_MAX30102= 1024;

    private static  int MAX_VALUE_OF_X_AXIS =4096 ;
    private int max30003GraphicCounter =0;
    private int max30102GraphicCounter =0;
    private int Si7021GraphicCounter =0;
    private int skinResistanceGraphicCounter =0;
    private int onlineDataCounter = 1;
    private int mongoDataCounter = 100;

    private static byte activeSensorByte = (byte) 0xF0;

    public static  final byte[]  OLED_STATUS_SHUT_DOWN = new byte[] {6};
    public static  final byte[]  OLED_STATUS_SI7021 = new byte[] {1};
    public static  final byte[]  OLED_STATUS_GSR = new byte[] {2};
    public static  final byte[]  OLED_STATUS_MAX30102 = new byte[] {3};
    public static  final byte[]  OLED_STATUS_MAX30003 = new byte[] {4};
    public static  final byte[]  OLED_STATUS_BLE = new byte[] {5};
    public static byte OLED_STATUS_BYTE = 0x00;


    public static Number[] nba = {HUMIDITY, TEMPERATURE, GSR, HR, SPO2, IRED, RED,  RR_COUNTER, RR, BPM_COUNTER, BPM, ECG_COUNTER, ECG};
    public static Number[] nbs = {4,        4,           2,   1,  1,    4,    4,    1,          20, 1,           20,  2,           320};//size of each data
    public static Number[] nbo = {0,        4,           8,   10, 11,   12,   16,   20,         21, 41,          42,  62,          64};//location of obj

    public LinkedBlockingQueue<Max3003> lbqOnlineMax3003 = new LinkedBlockingQueue<>();
    public LinkedBlockingQueue<Max3003> lbqMax3003 = new LinkedBlockingQueue<>();
    public LinkedBlockingQueue<Max30102> lbqOnlineMax30102 = new LinkedBlockingQueue<>();
    public LinkedBlockingQueue<Max30102> lbqMax30102 = new LinkedBlockingQueue<>();
    public LinkedBlockingQueue<Si7021> lbqOnlineSi7021 = new LinkedBlockingQueue<>();
    public LinkedBlockingQueue<Si7021> lbqSi7021 = new LinkedBlockingQueue<>();
    public LinkedBlockingQueue<SkinResistance> lbqOnlineSkinResistance = new LinkedBlockingQueue<>();
    public LinkedBlockingQueue<SkinResistance> lbqSkinResistance = new LinkedBlockingQueue<>();
    private SmartWatchServer sw;

    private LineGraphSeries<DataPoint> mSeriesMax30003ECG;
    private LineGraphSeries<DataPoint> mSeriesMax30003RR;
    private LineGraphSeries<DataPoint> mSeriesMax30102IR;
    private LineGraphSeries<DataPoint> mSeriesMax30102R;
    private LineGraphSeries<DataPoint> mSeriesSkin;
    private LineGraphSeries<DataPoint> mSeriesTemperature;
    private LineGraphSeries<DataPoint> mSeriesHumidity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_collect);
        initViewWidgets();
        setCheckBoxDefaultPosition();
        initCheckBoxListeners();
        setSwitchDefaultPosition();
        //setImageViewForEmailSending();
        setBleDefaultParams();
        setOnlineDataMongoDbDataSendCounterAndObj();
        initWebServer();
        adjustGraphicsProperties(swbGraph);
        plotData();
    }

    private void initCheckBoxListeners() {
        mCheckBoxEcgRr.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    setBitOfOledBleStatus(MAX30003_BIT_POSITION);
                }else {
                    clearBitOfOledBleStatus(MAX30003_BIT_POSITION);
                }
                basVar = (BaseActivity) getApplicationContext();
                basVar.sendCharacteristic(DATA_COLLECT_CHARACTERISTIC_UUID, new byte[]{activeSensorByte});
            }
        });

        mCheckBoxHrSpo2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    setBitOfOledBleStatus(MAX30102_BIT_POSITION);
                }else {
                    clearBitOfOledBleStatus(MAX30102_BIT_POSITION);
                }
                basVar = (BaseActivity) getApplicationContext();
                basVar.sendCharacteristic(DATA_COLLECT_CHARACTERISTIC_UUID, new byte[]{activeSensorByte});
            }
        });

        mCheckBoxSkin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    setBitOfOledBleStatus(SR_BIT_POSITION);
                }else {
                    clearBitOfOledBleStatus(SR_BIT_POSITION);
                }
                basVar = (BaseActivity) getApplicationContext();
                basVar.sendCharacteristic(DATA_COLLECT_CHARACTERISTIC_UUID, new byte[]{activeSensorByte});
            }
        });

        mCheckBoxTemperatureHumidity.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    setBitOfOledBleStatus(SI7021_BIT_POSITION);
                }else {
                    clearBitOfOledBleStatus(SI7021_BIT_POSITION);
                }
                basVar = (BaseActivity) getApplicationContext();
                basVar.sendCharacteristic(DATA_COLLECT_CHARACTERISTIC_UUID, new byte[]{activeSensorByte});
            }
        });

    }

    private void initWebServer() {
        sw = new SmartWatchServer(this);
    }


    private void setOnlineDataMongoDbDataSendCounterAndObj() {
        mEdtOnlineDataCounter.setText(Integer.toString(onlineDataCounter) );
        mEdtOnlineDataCounter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onlineDataCounter = Math.abs(Integer.parseInt(mEdtOnlineDataCounter.getText().toString()));
            }
        });
        mEdtMongoDbDataCounter.setText(Integer.toString(mongoDataCounter));
        mEdtMongoDbDataCounter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mongoDataCounter = Math.abs(Integer.parseInt(mEdtMongoDbDataCounter.getText().toString()));

            }
        });
    }

    private void setBleDefaultParams() {
        basVar = (BaseActivity) getApplicationContext();
        basVar.setNotification(DATA_COLLECT_CHARACTERISTIC_UUID, true);
        basVar.sendCharacteristic(DATA_COLLECT_CHARACTERISTIC_UUID, OLED_STATUS_BLE);
    }

    private void setImageViewForEmailSending() {
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EmailFragment blankFragment = new EmailFragment();
                final FragmentManager fm = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fm.beginTransaction();
                fragmentTransaction.add(R.id.DataCollectLayoutId, blankFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });
    }

    private void setSwitchDefaultPosition() {
        mSwitchOnlineData.setChecked(false);
        mSwitchSaveData.setChecked(false);
    }

    private void setCheckBoxDefaultPosition() {
        mCheckBoxEcgRr.setChecked(true);
        mCheckBoxHrSpo2.setChecked(true);
        mCheckBoxSkin.setChecked(true);
        mCheckBoxTemperatureHumidity.setChecked(true);
    }

    private void initViewWidgets() {
        mTextView = findViewById(R.id.textViewDataCollect);
        mImageView = findViewById(R.id.imageViewDataCollect);

        mSwitchOnlineData = findViewById(R.id.swOnlineData);
        mSwitchSaveData = findViewById(R.id.swSaveDataOnMongo);

        mCheckBoxEcgRr = findViewById(R.id.cbEcgRR);
        mCheckBoxHrSpo2 = findViewById(R.id.cbHrSpo2);
        mCheckBoxSkin = findViewById(R.id.cbSkin);
        mCheckBoxTemperatureHumidity = findViewById(R.id.cbTempHumid);

        mEdtOnlineDataCounter =findViewById(R.id.edtOnlineDataCounter);
        mEdtMongoDbDataCounter = findViewById(R.id.edtMongoDbDataCounter);

        swbGraph = findViewById(R.id.swbGraphViewId);
    }

    private void adjustGraphicsProperties(GraphView swbGraph) {
        setDefaultCanvasForGraph(swbGraph);
        initDataGraphSeries();
        //swbGraph.addSeries(mSeries);
    }

    private void setDefaultCanvasForGraph(GraphView swbGraph) {
        swbGraph.getViewport().setScrollable(true);
        swbGraph.getViewport().setScalable(true);
        swbGraph.getViewport().setMinX(0);
        swbGraph.getViewport().setMaxX(MAX_VALUE_OF_X_AXIS);
        swbGraph.getViewport().setXAxisBoundsManual(true);
        swbGraph.getViewport().setDrawBorder(true);
    }

    private void initDataGraphSeries() {
        // Define Max30003 graph
        mSeriesMax30003ECG = new LineGraphSeries<>();
        mSeriesMax30003ECG.setAnimated(true);
        mSeriesMax30003ECG.setThickness(2);
        mSeriesMax30003ECG.setDrawDataPoints(false);
        mSeriesMax30003ECG.setDrawBackground(false);
        mSeriesMax30003ECG.setTitle("ECG Data");
        mSeriesMax30003ECG.setColor(Color.RED);

        mSeriesMax30003RR = new LineGraphSeries<>();
        mSeriesMax30003RR.setAnimated(true);
        mSeriesMax30003RR.setThickness(2);
        mSeriesMax30003RR.setDrawDataPoints(false);
        mSeriesMax30003RR.setDrawBackground(false);
        mSeriesMax30003RR.setColor(Color.GREEN);

        // Define Max30102 graph
        mSeriesMax30102IR = new LineGraphSeries<>();
        mSeriesMax30102IR.setAnimated(true);
        mSeriesMax30102IR.setThickness(2);
        mSeriesMax30102IR.setDrawDataPoints(false);
        mSeriesMax30102IR.setDrawBackground(false);
        mSeriesMax30102IR.setColor(Color.BLUE);

        mSeriesMax30102R = new LineGraphSeries<>();
        mSeriesMax30102R.setAnimated(true);
        mSeriesMax30102R.setThickness(2);
        mSeriesMax30102R.setDrawDataPoints(false);
        mSeriesMax30102R.setDrawBackground(false);
        mSeriesMax30102R.setColor(Color.MAGENTA);

        //Define Si7021
        mSeriesHumidity = new LineGraphSeries<>();
        mSeriesHumidity.setAnimated(true);
        mSeriesHumidity.setThickness(2);
        mSeriesHumidity.setDrawDataPoints(false);
        mSeriesHumidity.setDrawBackground(false);
        mSeriesHumidity.setColor(Color.BLUE);

        mSeriesTemperature = new LineGraphSeries<>();
        mSeriesTemperature.setAnimated(true);
        mSeriesTemperature.setThickness(2);
        mSeriesTemperature.setDrawDataPoints(false);
        mSeriesTemperature.setDrawBackground(false);
        mSeriesTemperature.setColor(Color.RED);

        //Define Skin Resistance
        mSeriesSkin = new LineGraphSeries<>();
        mSeriesSkin.setAnimated(true);
        mSeriesSkin.setThickness(2);
        mSeriesSkin.setDrawDataPoints(false);
        mSeriesSkin.setDrawBackground(false);
        mSeriesSkin.setColor(Color.BLACK);
    }

    private void getCharArrayOfData(Byte array[], String hex) {
        int j = 0;
        for (int i = 0; i < hex.length(); i = i + 2) {
            // Step-1 Split the hex string into two character group
            String s = hex.substring(i, i + 2);
            // Step-2 Convert the each character group into integer using valueOf method
            int ik = Integer.valueOf(s, 16);
            // Step-3 Cast the integer value to char
            array[j] = (byte) ik;
            j++;
        }
    }

    public void plotData() {
        mTimer1 = new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void run() {
                String str = (String) BaseActivity.queueBle.poll();
                //String str = basVar.dataFromNotification;
                if(str !=null){
                    str = str.replace(" ", "");
                    mTextView.setText("byteArray : " + str);
                    Log.d("TEMP", "length "+str.toCharArray().length);
                    //String s = basVar.dataFromNotification.substring(0, 2);
                    //byte b = Byte.valueOf(s, 16);
                    Byte[] array = new Byte[450];
                    Arrays.fill(array, (byte) 0);
                    getCharArrayOfData(array, str);
                    // fillDataBaseData(array);
                    feedQueryFromBle(array);
                    printGraphOfData();
                    StringBuilder sb = new StringBuilder();
                    sb.append("HUM: " + nba[HUMIDITY].floatValue() + " ");
                    sb.append("TEMP: " + nba[TEMPERATURE].floatValue() + "\n");
                    sb.append("GSR: " + nba[GSR].shortValue() + "\n");
                    sb.append("HR: " + nba[HR] + " ");
                    sb.append("SPO2: " + nba[SPO2] + " ");
                    sb.append("IRED: " + nba[IRED] + " ");
                    sb.append("RED: " + nba[RED] + "\n");
                    sb.append("RR_COUNTER: " + nba[RR_COUNTER] + " ");
                    sb.append("RR: " + nba[RR] + " ");
                    sb.append("BPM_COUNTER: " + nba[BPM_COUNTER] + " ");
                    sb.append("BPM: " + nba[BPM].intValue() + " ");
                    sb.append("ECG_COUNTER: " + nba[ECG_COUNTER] + "\n");

                    try {
                        if(mSwitchSaveData.isChecked()){
                            pushDataOnDatabase(sb);
                        }
                        if(mSwitchOnlineData.isChecked()){
                            pushDataOnOnline(sb);
                        }
                        if (!mSwitchOnlineData.isChecked() && !mSwitchOnlineData.isChecked()){
                            clearQueues();
                        }
                    } catch (Exception ex) {
                        Log.d(TAG, ex.toString());
                    }
                    mTextView.setText(sb.toString() + "\n");
                }else{
                    //mTextView.setText("Queue is empty \n");
                }

                mHandler.postDelayed(this, 90);
            }
        };
        mHandler.postDelayed(mTimer1, 300);
    }

    private void printGraphOfData() {
        clearQueues(activeRadioButton);// clears non active button queues
        if(activeRadioButton.equals(RB_SR)){
            double skinResistance = 0;
            List<SkinResistance> tmp = new ArrayList<>();
            lbqSkinResistance.drainTo(tmp);
           for(SkinResistance s : tmp){
               skinResistance = s.getSr();
               printSkinResistanceData(skinResistance);
           }
        }
        if(activeRadioButton.equals(RB_MAX30003)){
            double ecgData = 0;
            double rrData = 0;
            List<Max3003> tmp = new ArrayList<>();
            lbqMax3003.drainTo(tmp);
          /*
            if(tmp.size()==0){
                resetDataOnSeries(mSeriesMax30003ECG, ecgData, max30003GraphicCounter);
            }

           */
            for( Max3003 m : tmp){
                List<Short> ecgList = m.getEcgList();
                for(Short s: ecgList){
                    rrData = 0;
                    ecgData = s;
                    printMax30003Data(ecgData, rrData);
                }
                m.clearEcgList();
            }
        }
        if(activeRadioButton.equals(RB_MAX30102)){
            double r = 0;
            double ir = 0;
            List<Max30102> tmp = new ArrayList<>();
            lbqMax30102.drainTo(tmp);
            for (Max30102 m : tmp){
                r = m.getRedVal();
                ir = m.getIredVal();
                printMax30102Data(r, ir);
            }
        }
        if(activeRadioButton.equals(RB_SI7021)){
            double temperature = 0;
            double humidity = 0;
            List<Si7021> tmp = new ArrayList<>();
            lbqSi7021.drainTo(tmp);
            for(Si7021 s : tmp){
                temperature = s.getTemperature();
                humidity = s.getHumidity();
                printSi7021Data(temperature,humidity);
            }

        }
    }

    private void printSkinResistanceData(double skinResistance) {
        skinResistanceGraphicCounter++;
        if(skinResistanceGraphicCounter >= MAX_COUNTER_VAL_SR-1){
            skinResistanceGraphicCounter =0;
            resetDataOnSeries(mSeriesSkin, skinResistance, skinResistanceGraphicCounter);
        }
        appendDataOnSeries(mSeriesSkin, skinResistance, skinResistanceGraphicCounter);
    }

    private void printSi7021Data(double temperature, double humidity) {
        Si7021GraphicCounter++;
        if(Si7021GraphicCounter >= MAX_COUNTER_VAL_SI7021-1){
            Si7021GraphicCounter =0;
            resetDataOnSeries(mSeriesTemperature, mSeriesHumidity, temperature, humidity, Si7021GraphicCounter);
        }
        appendDataOnSeries(mSeriesTemperature, mSeriesHumidity, temperature, humidity, Si7021GraphicCounter);
    }

    private void printMax30102Data(double r, double ir) {
        max30102GraphicCounter++;
        if(max30102GraphicCounter >= MAX_COUNTER_VAL_MAX30102-1){
            max30102GraphicCounter =0;
            resetDataOnSeries(mSeriesMax30102R, r, max30102GraphicCounter);
        }
        appendDataOnSeries(mSeriesMax30102R, r, max30102GraphicCounter);
    }

    private void printMax30003Data(double ecgData, double rrData) {
        max30003GraphicCounter++;
        if(max30003GraphicCounter >= MAX_COUNTER_VAL_MAX3003-1){
            max30003GraphicCounter =0;
            resetDataOnSeries(mSeriesMax30003ECG, ecgData, max30003GraphicCounter);
        }
        appendDataOnSeries(mSeriesMax30003ECG,  ecgData,  max30003GraphicCounter);
    }

    private void resetDataOnSeries( LineGraphSeries<DataPoint> dp1,  LineGraphSeries<DataPoint> dp2 , double d1, double d2, int counter){
        dp1.resetData(new DataPoint[] { new DataPoint(counter, d1) });
        dp2.resetData(new DataPoint[] { new DataPoint(counter, d2) });
    }

    private void resetDataOnSeries( LineGraphSeries<DataPoint> dp1,  double d1, int counter){
        dp1.resetData(new DataPoint[] { new DataPoint(counter, d1) });
    }

    private void appendDataOnSeries( LineGraphSeries<DataPoint> dp1,  LineGraphSeries<DataPoint> dp2 , double d1, double d2, int counter){
        dp1.appendData(new DataPoint(counter, d1),true,MAX_VALUE_OF_X_AXIS);
        dp2.appendData(new DataPoint(counter, d2),true,MAX_VALUE_OF_X_AXIS);
    }

    private void appendDataOnSeries( LineGraphSeries<DataPoint> dp1, double d1, int counter){
        dp1.appendData(new DataPoint(counter, d1),true,MAX_VALUE_OF_X_AXIS);
    }

    private void clearQueues(String rbStatus){
        if(!rbStatus.equals(RB_SI7021) ||  rbStatus.equals(RB_CLOSE)){
             lbqSi7021.clear();
        }
        if(!rbStatus.equals(RB_SR) ||  rbStatus.equals(RB_CLOSE)){
            lbqSkinResistance.clear();
        }
        if(!rbStatus.equals(RB_MAX30003) ||  rbStatus.equals(RB_CLOSE)){
            lbqMax3003.clear();
        }
        if(!rbStatus.equals(RB_MAX30102) ||  rbStatus.equals(RB_CLOSE)){
            lbqMax30102.clear();
        }
    }

    private void clearQueues() {
        lbqOnlineMax30102.clear();
        lbqOnlineMax3003.clear();
        lbqOnlineSkinResistance.clear();
        lbqOnlineSi7021.clear();
    }

    private void feedQueryFromBle(Byte[] array) {
        try {
            byte[] tempArray = new byte[0];
            ByteBuffer wrapped;
            if(mCheckBoxTemperatureHumidity.isChecked() == true){

                setBitOfOledBleStatus(SI7021_BIT_POSITION);
                //Get humidity
                tempArray = new byte[nbs[HUMIDITY].intValue()];
                Si7021 si7021 = new Si7021();
                for (int j = 0; j < nbs[HUMIDITY].intValue(); j++) {
                    tempArray[j] = array[nbo[HUMIDITY].intValue() + j];
                }
                wrapped = ByteBuffer.wrap(tempArray); // big-endian by default
                si7021.setHumidityByte(si7021.getHumidityByte().append(bytesToHex(tempArray)));
                si7021.setType("environment");
                nba[HUMIDITY] = wrapped.getFloat();
                si7021.setHumidity( nba[HUMIDITY] .floatValue());
                //Get Temperature data
                tempArray = new byte[ nbs[TEMPERATURE].intValue()];
                for (int j = 0; j < nbs[TEMPERATURE].intValue(); j++) {
                    tempArray[j] = array[nbo[TEMPERATURE].intValue() + j];
                }
                wrapped = ByteBuffer.wrap(tempArray); // big-endian by default
                si7021.setTemperatureByte(si7021.getTemperatureByte().append(bytesToHex(tempArray)));
                nba[TEMPERATURE] = wrapped.getFloat();
                si7021.setTemperature( nba[TEMPERATURE] .floatValue());
                si7021.setCounter(si7021.getCounter()+1);
                si7021.setDate(new Date());
                lbqOnlineSi7021.put(si7021);
                lbqSi7021.put(si7021);
                Log.d(TAG, "feedQueryFromBle: " + si7021.toString() + " value= " + nba[TEMPERATURE] + " value= " + nba[HUMIDITY]);
            }else {
                clearBitOfOledBleStatus(SI7021_BIT_POSITION);
            }
            if(mCheckBoxSkin.isChecked() == true){
                setBitOfOledBleStatus(SR_BIT_POSITION);
                //Get GSR data
                SkinResistance skinResistance = new SkinResistance();
                skinResistance.setType("skin");
                tempArray = new byte[nbs[GSR].intValue()];
                for (int j = 0; j <nbs[GSR].intValue(); j++) {
                    tempArray[j] = array[nbo[GSR].intValue() + j];
                }
                wrapped = ByteBuffer.wrap(tempArray); // big-endian by default
                skinResistance.setSkinResistance(skinResistance.getSkinResistance().append(bytesToHex(tempArray)));
                short tempShort = wrapped.getShort();
                //(float)((1024+2*tempShort)*10000)/(512-tempShort);
                nba[GSR] = tempShort;
                skinResistance.setSr(nba[GSR].floatValue());
                skinResistance.setCounter(skinResistance.getCounter()+1);
                skinResistance.setDate(new Date());
                lbqOnlineSkinResistance.put(skinResistance);
                lbqSkinResistance.put(skinResistance);
                Log.d(TAG, "feedQueryFromBle: "+ skinResistance.toString() + " value= " + nba[GSR]);
            }else {
                clearBitOfOledBleStatus(SR_BIT_POSITION);
            }

            if(mCheckBoxHrSpo2.isChecked() == true){
                setBitOfOledBleStatus(MAX30102_BIT_POSITION);
                //Get Max30102 data
                Max30102 max30102 = new Max30102();
                max30102.setType("heart");
                //HR -> max30102
                tempArray = new byte[nbs[HR].intValue()];
                for (int j = 0; j <nbs[HR].intValue(); j++) {
                    tempArray[j] = array[nbo[HR].intValue() + j];
                }
                wrapped = ByteBuffer.wrap(tempArray); // big-endian by default
                max30102.setHr(max30102.getHr().append(bytesToHex(tempArray)));
                nba[HR] =  (int) wrapped.get() % 0xFF;
                //SPO2 -> max30102
                tempArray = new byte[nbs[SPO2].intValue()];
                for (int j = 0; j <nbs[SPO2].intValue(); j++) {
                    tempArray[j] = array[nbo[SPO2].intValue() + j];
                }
                wrapped = ByteBuffer.wrap(tempArray); // big-endian by default
                max30102.setSpo2(max30102.getSpo2().append(bytesToHex(tempArray)));
                nba[SPO2] =  (byte) wrapped.get() % 0xFF;

                //IRED -> max30102
                tempArray = new byte[nbs[IRED].intValue()];
                for (int j = 0; j <nbs[IRED].intValue(); j++) {
                    tempArray[j] = array[nbo[IRED].intValue() + j];
                }
                wrapped = ByteBuffer.wrap(tempArray); // big-endian by default
                max30102.setIred(max30102.getIred().append(bytesToHex(tempArray)));
                nba[IRED] =  wrapped.getInt();
                max30102.setIredVal(nba[IRED].intValue());
                //IRED -> max30102
                tempArray = new byte[nbs[RED].intValue()];
                for (int j = 0; j <nbs[RED].intValue(); j++) {
                    tempArray[j] = array[nbo[RED].intValue() + j];
                }
                wrapped = ByteBuffer.wrap(tempArray); // big-endian by default
                max30102.setRed(max30102.getRed().append(bytesToHex(tempArray)));
                nba[RED] =  wrapped.getInt();
                max30102.setRedVal(nba[RED].intValue());
                max30102.setCounter(max30102.getCounter()+1);
                Log.d(TAG, "feedQueryFromBle: " + max30102.toString() + " value= " + nba[RED]);
                max30102.setDate(new Date());
                lbqOnlineMax30102.put(max30102);
                lbqMax30102.put(max30102);
            }else {
                clearBitOfOledBleStatus(MAX30102_BIT_POSITION);
            }

            if(mCheckBoxEcgRr.isChecked() == true){
                setBitOfOledBleStatus(MAX30003_BIT_POSITION);
                //Get max30003 data
                //RR -> Max3003
                Max3003 max3003 = new Max3003();
                max3003.setType("heart");
                if(array[nbo[RR_COUNTER].intValue()].intValue()>0){
                    nba[RR_COUNTER] =  (array[nbo[RR_COUNTER].intValue()]&0xff);//wrapped.getInt();
                    if(nba[RR_COUNTER].intValue() != 0){
                        tempArray = new byte[nba[RR_COUNTER].intValue()*4];
                        for(int j = 0; j< nba[RR_COUNTER].intValue()*4; j++){
                            tempArray[j] = array[nbo[RR].intValue() + j];
                        }
                        max3003.setRr(max3003.getRr().append(bytesToHex(tempArray)));
                        nba[RR] = array[nbo[RR].intValue()+0]<<24 | array[nbo[RR].intValue()+1]<<16 |
                                array[nbo[RR].intValue()+2]<<8 | array[nbo[RR].intValue()+3]&0xff;
                    }
                }else{
                    nba[RR_COUNTER] = 0;
                }
                //BPM -> Max3003
                if(array[nbo[BPM_COUNTER].intValue()].intValue()>0){
                    nba[RR_COUNTER] =  (array[nbo[BPM_COUNTER].intValue()]&0xff);//wrapped.getInt();
                    if(nba[BPM_COUNTER].intValue() != 0){
                        tempArray = new byte[nba[BPM_COUNTER].intValue()*4];
                        for(int j = 0; j< nba[BPM_COUNTER].intValue()*4; j++){
                            tempArray[j] = array[nbo[BPM].intValue() + j];
                        }
                        max3003.setBpm(max3003.getBpm().append(bytesToHex(tempArray)));
                    }
                    nba[BPM] = Float.intBitsToFloat(array[nbo[BPM].intValue()+0]<<24 ^ array[nbo[BPM].intValue()+1]<<16 ^
                            array[nbo[BPM].intValue()+2]<<8 ^ array[nbo[BPM].intValue()+3]&0xff);
                }else{
                    nba[BPM_COUNTER] = 0;
                }
                
                if(nbs[ECG_COUNTER].intValue()>0){
                    //ECG -> Max3003
                    nba[ECG_COUNTER] =  (array[nbo[ECG_COUNTER].intValue()+1]) << 8 | (array[nbo[ECG_COUNTER].intValue()]&0xff);//wrapped.getInt();
                    int shortCounter = 0;
                    if(nba[ECG_COUNTER].intValue() != 0){
                        tempArray = new byte[nba[ECG_COUNTER].intValue()*2];
                        byte[] tempEcgArray = new byte[]{0,0};
                        for(int j = 0; j< nba[ECG_COUNTER].intValue()*2; j++){
                            shortCounter++;
                            tempArray[j] = array[nbo[ECG_COUNTER].intValue() + j];
                            tempEcgArray[j%2] = array[nbo[ECG_COUNTER].intValue() + j];
                            if(shortCounter % 2 ==0){
                                if(j == 1){
                                    // the first byte must be swiped
                                    byte tb = tempEcgArray[0];
                                    tempEcgArray[0]=tempEcgArray[1];
                                    tempEcgArray[1]= tb;
                                    //Log.d("ECG", "array  : "  + Arrays.toString(array));
                                }
                                shortCounter = 0;
                                Short s = ByteBuffer.wrap(tempEcgArray).getShort();
                                max3003.appendValueOnEcgList(s);
                            }
                        }
                    }
                }else{
                    nba[ECG_COUNTER] = 0;
                }
                Log.d(TAG, "feedQueryFromBle: " + max3003.toString() + " cRR = " +
                        nba[RR_COUNTER] + " cBPM = " + nba[BPM_COUNTER] +" cECG " + nba[ECG_COUNTER]);
                if(nba[ECG_COUNTER].intValue() != 0 || nba[RR_COUNTER].intValue() != 0 || nba[BPM_COUNTER].intValue() != 0){
                    max3003.setCounter(max3003.getCounter()+1);
                    max3003.setEcg(max3003.getEcg().append(bytesToHex(tempArray)));
                    max3003.setDate(new Date());
                    lbqOnlineMax3003.put(max3003);
                    lbqMax3003.put(max3003);
                }
            }else {
                clearBitOfOledBleStatus(MAX30003_BIT_POSITION);
            }

        }catch (Exception ex){
            ex.printStackTrace();
            Log.d(TAG, "feedQueryFromBle: " +ex.getCause());
        }
    }

    private void setBitOfOledBleStatus(short position) {
        activeSensorByte |= 1 << position ;
    }

    private void clearBitOfOledBleStatus(short position) {
        activeSensorByte &= ~(1 << position);
    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void pushDataOnOnline( StringBuilder sb) {
        webServiceProcess(sb, TYPE_ONLINE);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void pushDataOnDatabase( StringBuilder sb) {
        webServiceProcess(sb, TYPE_DATABASE);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void webServiceProcess( StringBuilder sb,String type) {
        if (!isQueueReadyToSend(type))
            return;
        if(type.equals(TYPE_DATABASE))
            sb.append(" \n \n \n Data send to db \n \n \n \n");
        else
            sb.append(" \n \n \n Data send to Online \n \n \n \n");

        if(mCheckBoxEcgRr.isChecked() == true){
            List<Max3003> tmp = new ArrayList<>();
            lbqOnlineMax3003.drainTo(tmp);
            tmp.stream().forEach(s -> {sw.setSensorInfo(s,type);});
            //sw.setSensorInfo(max3003, TYPE_DATABASE);
        }

        if(mCheckBoxHrSpo2.isChecked() == true){
            List<Max30102> tmp = new ArrayList<>();
            lbqOnlineMax30102.drainTo(tmp);
            tmp.stream().forEach(s -> {sw.setSensorInfo(s,type);});
            //sw.setSensorInfo(max30102, TYPE_DATABASE);
        }

        if(mCheckBoxTemperatureHumidity.isChecked() == true){
            List<Si7021> tmp = new ArrayList<>();
            lbqOnlineSi7021.drainTo(tmp);
            tmp.stream().forEach(s -> {sw.setSensorInfo(s,type);});
            //sw.setSensorInfo(si7021, TYPE_DATABASE);
        }

        if(mCheckBoxSkin.isChecked() == true){
            List<SkinResistance> tmp = new ArrayList<>();
            lbqOnlineSkinResistance.drainTo(tmp);
            tmp.stream().forEach(s -> {sw.setSensorInfo(s,type);});
            //sw.setSensorInfo(skinResistance, TYPE_DATABASE);
        }
    }

    private boolean isQueueReadyToSend(String type) {
        int counter = 0 ;
        if(type.equals(TYPE_DATABASE))
            counter = mongoDataCounter;
        else
            counter = onlineDataCounter;
        if(lbqOnlineMax3003.size()< counter && lbqOnlineMax30102.size()< counter &&
                lbqOnlineSkinResistance.size()< counter && lbqOnlineSi7021.size()< counter )
            return false;
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        mHandler.removeCallbacks(mTimer1);
        basVar.setNotification(DATA_COLLECT_CHARACTERISTIC_UUID, false);
    }
    byte setUpperHalfByte(byte orig, byte nibble) {
        byte res = orig;
        res &= 0x0F; // Clear out the upper nibble
        res |= ((nibble ) & 0xF0); // OR in the desired mask
        return res;
    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();
        // Check which radio button was clicked
        if(view.getId()!=R.id.rbtOledShutDown)
        swbGraph.removeAllSeries();
        basVar =  (BaseActivity)getApplicationContext();
        basVar.setNotification(DATA_COLLECT_CHARACTERISTIC_UUID,true);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                switch(view.getId()) {
                    case R.id.rbtOledShutDown:
                        if (checked){
                            //activeRadioButton = RB_CLOSE;
                            basVar = (BaseActivity) getApplicationContext();
                            activeSensorByte =  setUpperHalfByte(OLED_STATUS_SHUT_DOWN[0] , activeSensorByte);
                            basVar.sendCharacteristic(DATA_COLLECT_CHARACTERISTIC_UUID, new byte[]{activeSensorByte});
                        }
                        break;
                    case R.id.rbtOledTempAndHumidity:
                        if (checked){
                            activeRadioButton = RB_SI7021;
                            swbGraph.addSeries(mSeriesHumidity);
                            swbGraph.addSeries(mSeriesTemperature);
                            basVar = (BaseActivity) getApplicationContext();
                            activeSensorByte = setUpperHalfByte(OLED_STATUS_SI7021[0] , activeSensorByte);
                            basVar.sendCharacteristic(DATA_COLLECT_CHARACTERISTIC_UUID, new byte[]{activeSensorByte});
                        }
                        break;
                    case R.id.rbtOledGsr:
                        if (checked){
                            activeRadioButton = RB_SR;
                            swbGraph.addSeries(mSeriesSkin);
                            basVar = (BaseActivity) getApplicationContext();
                            activeSensorByte = setUpperHalfByte(OLED_STATUS_GSR[0] , activeSensorByte);
                            basVar.sendCharacteristic(DATA_COLLECT_CHARACTERISTIC_UUID, new byte[]{activeSensorByte});
                        }
                        break;
                    case R.id.rbHrSpo2:
                        if (checked){
                            activeRadioButton = RB_MAX30102;
                            //swbGraph.addSeries(mSeriesMax30102IR);
                            swbGraph.addSeries(mSeriesMax30102R);
                            basVar = (BaseActivity) getApplicationContext();
                            activeSensorByte = setUpperHalfByte(OLED_STATUS_MAX30102[0] , activeSensorByte);
                            basVar.sendCharacteristic(DATA_COLLECT_CHARACTERISTIC_UUID, new byte[]{activeSensorByte});
                        }
                        break;
                    case R.id.rbtOledEcgRr:
                        if (checked){
                            activeRadioButton = RB_MAX30003;
                            swbGraph.addSeries(mSeriesMax30003ECG);
                            //swbGraph.addSeries(mSeriesMax30003RR);
                            basVar = (BaseActivity) getApplicationContext();
                            activeSensorByte = setUpperHalfByte(OLED_STATUS_MAX30003[0] , activeSensorByte);
                            basVar.sendCharacteristic(DATA_COLLECT_CHARACTERISTIC_UUID, new byte[]{activeSensorByte});
                        }
                        break;
                    case R.id.rbtOledBle:
                        if (checked){
                            activeRadioButton = RB_CLOSE;
                            basVar = (BaseActivity) getApplicationContext();
                            activeSensorByte = setUpperHalfByte(OLED_STATUS_BLE[0] , activeSensorByte);
                            basVar.sendCharacteristic(DATA_COLLECT_CHARACTERISTIC_UUID, new byte[]{activeSensorByte});
                        }
                        break;
                }
            }
        },500);


    }
}
