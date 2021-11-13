package com.swatch.smartwatch;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.swatch.smartwatch.fragment.EmailFragment;
import com.swatch.smartwatch.sensors.Max3003;
import com.swatch.smartwatch.sensors.Max30102;
import com.swatch.smartwatch.sensors.Si7021;
import com.swatch.smartwatch.sensors.SkinResistance;
import com.swatch.smartwatch.ws.SmartWatchServer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public class DataCollectActivity extends AppCompatActivity {

    private static final int MAX_BYTE_LENGTH = 128;//256
    private static final String TAG = "DATA COLLECT";
    private final String DATA_COLLECT_CHARACTERISTIC_UUID = "24538aee-dd71-11e9-98a9-2a2ae2dbcce4";
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

    public static Number[] nba = {HUMIDITY, TEMPERATURE, GSR, HR, SPO2, IRED, RED,  RR_COUNTER, RR, BPM_COUNTER, BPM, ECG_COUNTER, ECG};
    public static Number[] nbs = {4,        4,           2,   1,  1,    4,    4,    1,          20, 1,           20,  2,           320};//size of each data
    public static Number[] nbo = {0,        4,           8,   10, 11,   12,   16,   20,         21, 41,          42,  62,          64};//location of obj

    public final String TYPE_DATABASE = "database";
    public final String TYPE_ONLINE = "online";
    Max3003 max3003, oMax3003;
    Max30102 max30102, oMax30102;
    Si7021 si7021, oSi7021;
    SkinResistance skinResistance, oSkinResistance;

    public LinkedBlockingQueue<Max3003> lbgOnlineMax3003 = new LinkedBlockingQueue<>();
    public LinkedBlockingQueue<Max30102> lbgOnlineMax30102 = new LinkedBlockingQueue<>();
    public LinkedBlockingQueue<Si7021> lbgOnlineSi7021 = new LinkedBlockingQueue<>();
    public LinkedBlockingQueue<SkinResistance> lbgOnlineSkinResistance = new LinkedBlockingQueue<>();



    private SmartWatchServer sw;

    int dbCounter = 0;
    int onlineDataCounter = 1;
    int mongoDataCounter = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_collect);

        mTextView = findViewById(R.id.textViewDataCollect);
        mImageView = findViewById(R.id.imageViewDataCollect);

        mSwitchOnlineData = (Switch) findViewById(R.id.swOnlineData);
        mSwitchSaveData = (Switch) findViewById(R.id.swSaveDataOnMongo);

        mCheckBoxEcgRr = (CheckBox) findViewById(R.id.cbEcgRR);
        mCheckBoxHrSpo2 = (CheckBox)findViewById(R.id.cbHrSpo2);
        mCheckBoxSkin = (CheckBox)findViewById(R.id.cbSkin);
        mCheckBoxTemperatureHumidity = (CheckBox)findViewById(R.id.cbTempHumid);
        mEdtOnlineDataCounter =findViewById(R.id.edtOnlineDataCounter);
        mEdtMongoDbDataCounter = findViewById(R.id.edtMongoDbDataCounter);
        mCheckBoxEcgRr.setChecked(true);
        mCheckBoxHrSpo2.setChecked(true);
        mCheckBoxSkin.setChecked(true);
        mCheckBoxTemperatureHumidity.setChecked(true);

        mSwitchOnlineData.setChecked(false);
        mSwitchSaveData.setChecked(false);


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
        basVar = (BaseActivity) getApplicationContext();
        basVar.setNotification(DATA_COLLECT_CHARACTERISTIC_UUID, true);

        max3003 = new Max3003();
        oMax3003 = new Max3003();

        max30102 = new Max30102();
        oMax30102 = new Max30102();

        si7021 = new Si7021();
        oSi7021 = new Si7021();

        skinResistance = new SkinResistance();
        oSkinResistance = new SkinResistance();

        mEdtOnlineDataCounter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onlineDataCounter = Math.abs(Integer.parseInt(mEdtOnlineDataCounter.getText().toString()));
            }
        });
        mEdtMongoDbDataCounter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mongoDataCounter = Math.abs(Integer.parseInt(mEdtMongoDbDataCounter.getText().toString()));

            }
        });

        sw = new SmartWatchServer(this);
        plotData();
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
                String str = basVar.dataFromNotification;
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
                StringBuilder sb = new StringBuilder();
                sb.append("HUM: " + nba[HUMIDITY].floatValue() + "\n");
                sb.append("TEMP: " + nba[TEMPERATURE].floatValue() + "\n");
                sb.append("GSR: " + nba[GSR].shortValue() + "\n");
                sb.append("HR: " + nba[HR] + "\n");
                sb.append("SPO2: " + nba[SPO2] + "\n");
                sb.append("IRED: " + nba[IRED] + "\n");
                sb.append("RED: " + nba[RED] + "\n");
                sb.append("RR_COUNTER: " + nba[RR_COUNTER] + "\n");
                sb.append("RR: " + nba[RR] + "\n");
                sb.append("BPM_COUNTER: " + nba[BPM_COUNTER] + "\n");
                sb.append("BPM: " + nba[BPM].intValue() + "\n");
                sb.append("ECG_COUNTER: " + nba[ECG_COUNTER] + "\n");

                mTextView.setText(sb.toString() + "\n");
                try {
                    if(mSwitchSaveData.isChecked()){
                        dataBaseSendWSProcess();
                    }else {
                        clearQueues();
                    }
                    if(mSwitchOnlineData.isChecked()){
                        dataBaseSendWSProcessOnline();
                    }else{
                        clearQueues();
                    }

                } catch (Exception ex) {
                    Log.d(TAG, ex.toString());
                }
                dbCounter++;
                mHandler.postDelayed(this, 125);

            }
        };
        mHandler.postDelayed(mTimer1, 501);
    }

    private void clearQueues() {
        lbgOnlineMax30102.clear();
        lbgOnlineMax3003.clear();
        lbgOnlineSkinResistance.clear();
        lbgOnlineSi7021.clear();

    }

    private void feedQueryFromBle(Byte[] array) {
        try {
            byte[] tempArray = new byte[0];
            ByteBuffer wrapped;
            if(mCheckBoxTemperatureHumidity.isChecked() == true){
                //Get humidity
                tempArray = new byte[nbs[HUMIDITY].intValue()];
                Si7021 si7021 = new Si7021();
                for (int j = 0; j < nbs[HUMIDITY].intValue(); j++) {
                    tempArray[j] = array[nbo[HUMIDITY].intValue() + j];
                }
                wrapped = ByteBuffer.wrap(tempArray); // big-endian by default
                si7021.setHumidityByte(si7021.getHumidityByte().append(bytesToHex(tempArray)));
                nba[HUMIDITY] = wrapped.getFloat();
                //Get Temperature data
                tempArray = new byte[ nbs[TEMPERATURE].intValue()];
                for (int j = 0; j < nbs[TEMPERATURE].intValue(); j++) {
                    tempArray[j] = array[nbo[TEMPERATURE].intValue() + j];
                }
                wrapped = ByteBuffer.wrap(tempArray); // big-endian by default
                si7021.setTemperatureByte(si7021.getTemperatureByte().append(bytesToHex(tempArray)));
                nba[TEMPERATURE] = wrapped.getFloat();
                si7021.setCounter(si7021.getCounter()+1);
                lbgOnlineSi7021.put(si7021);
                Log.d(TAG, "feedQueryFromBle: " + si7021.toString() + " value= " + nba[TEMPERATURE] + " value= " + nba[HUMIDITY]);
            }
            if(mCheckBoxSkin.isChecked() == true){
                //Get GSR data
                SkinResistance skinResistance = new SkinResistance();
                tempArray = new byte[nbs[GSR].intValue()];
                for (int j = 0; j <nbs[GSR].intValue(); j++) {
                    tempArray[j] = array[nbo[GSR].intValue() + j];
                }
                wrapped = ByteBuffer.wrap(tempArray); // big-endian by default
                skinResistance.setSkinResistance(skinResistance.getSkinResistance().append(bytesToHex(tempArray)));
                nba[GSR] = wrapped.getShort();
                skinResistance.setCounter(skinResistance.getCounter()+1);
                lbgOnlineSkinResistance.put(skinResistance);
                Log.d(TAG, "feedQueryFromBle: "+ skinResistance.toString() + " value= " + nba[GSR]);
            }

            if(mCheckBoxHrSpo2.isChecked() == true){
                //Get Max30102 data
                Max30102 max30102 = new Max30102();
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

                //IRED -> max30102
                tempArray = new byte[nbs[RED].intValue()];
                for (int j = 0; j <nbs[RED].intValue(); j++) {
                    tempArray[j] = array[nbo[RED].intValue() + j];
                }
                wrapped = ByteBuffer.wrap(tempArray); // big-endian by default
                max30102.setRed(max30102.getRed().append(bytesToHex(tempArray)));
                nba[RED] =  wrapped.getInt();
                max30102.setCounter(max30102.getCounter()+1);
                Log.d(TAG, "feedQueryFromBle: " + max30102.toString() + " value= " + nba[RED]);
                lbgOnlineMax30102.put(max30102);
            }

            if(mCheckBoxEcgRr.isChecked() == true){
                //Get max30003 data
                //RR -> Max3003
                Max3003 max3003 = new Max3003();
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
                    if(nba[ECG_COUNTER].intValue() != 0){
                        tempArray = new byte[nba[ECG_COUNTER].intValue()*2];
                        for(int j = 0; j< nba[ECG_COUNTER].intValue()*2; j++){
                            tempArray[j] = array[nbo[ECG_COUNTER].intValue() + j];
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
                    lbgOnlineMax3003.put(max3003);
                }
            }

        }catch (Exception ex){
            ex.printStackTrace();
            Log.d(TAG, "feedQueryFromBle: " +ex.getCause());
        }
    }

    private void fillDataBaseData(Byte[] array) {
        for (int i = 0; i < nbs.length; i++) {
            byte[] tempArray = new byte[nbs[i].intValue()];
            for (int j = 0; j < nbs[i].intValue(); j++) {
                tempArray[j] = array[(nbo[i].intValue()) + j];
            }
            ByteBuffer wrapped = ByteBuffer.wrap(tempArray); // big-endian by default
            if (mSwitchSaveData.isChecked()) {
                dataAppendProcess(i, tempArray);
            }
            if (mSwitchOnlineData.isChecked()) {
                dataAppendReal(i, tempArray);
            }
            if (i == HUMIDITY || i == TEMPERATURE) {
                nba[i] = wrapped.getFloat();
            } else if (nbs[i].intValue() == 2) {
                nba[i] = wrapped.getShort();
            } else if (nbs[i].intValue() == 1) {
                nba[i] = (int) wrapped.get() % 0xFF;
            } else if(nbs[i].intValue() == 4)  {
                nba[i] = wrapped.getInt();
            }else{
                nba[i] = wrapped.get(0);
            }
        }
    }

    private void dataAppendReal(int data_type, byte[] array) {
        if (oSi7021.getCounter() >= onlineDataCounter && data_type == HUMIDITY)
            return;
        if (data_type == HUMIDITY) {//Humidty and Temperature are gathered from same  sensor
            oSi7021.setType("environment");
            oSi7021.setCounter(oSi7021.getCounter() + 1);
            oSi7021.setHumidityByte(oSi7021.getHumidityByte().append(bytesToHex(array)));
            oSi7021.setDate(new Date());
        } else if (data_type == TEMPERATURE) {
            oSi7021.setTemperatureByte(oSi7021.getTemperatureByte().append(bytesToHex(array)));
            oSi7021.setDate(new Date());
        } else if (data_type == GSR) {
            oSkinResistance.setType("skin");
            oSkinResistance.setCounter(oSkinResistance.getCounter() + 1);
            oSkinResistance.setSkinResistance(oSkinResistance.getSkinResistance().append(bytesToHex(array)));
            oSkinResistance.setDate(new Date());
        } else if (data_type == HR) { // HR and SPO2 are gathered from same  sensor
            oMax30102.setType("heart");
            oMax30102.setCounter(oMax30102.getCounter() + 1);
            oMax30102.setHr(oMax30102.getHr().append(bytesToHex(array)));
            oMax30102.setDate(new Date());
        } else if (data_type == SPO2) {
            oMax30102.setSpo2(oMax30102.getSpo2().append(bytesToHex(array)));
            oMax30102.setDate(new Date());
        }
        else if(data_type == IRED){
            oMax30102.setIred(oMax30102.getIred().append(bytesToHex(array)));
        }
        else if(data_type == RED){
            oMax30102.setRed(oMax30102.getRed().append(bytesToHex(array)));
        } else if (data_type == RR) {
            oMax3003.setRr(oMax3003.getRr().append(bytesToHex(array)));
            oMax3003.setDate(new Date());
        } else if (data_type == ECG) {// ECG and RR are gathered from same  sensor
            oMax3003.setType("heart");
            oMax3003.setCounter(oMax3003.getCounter() + 1);
            oMax3003.setEcg(oMax3003.getEcg().append(bytesToHex(array)));
            oMax3003.setDate(new Date());
        }
    }


    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();


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
    private void dataBaseSendWSProcessOnline() {
        if (oSi7021.getCounter() < onlineDataCounter)
            return;
        StringBuilder sb = new StringBuilder();
        sb.append(" \n \n \n Data send to Online \n \n \n \n");
        mTextView.setText(sb.toString() + "\n");
        if(mCheckBoxEcgRr.isChecked() == true)
            sw.setSensorInfo(oMax3003, TYPE_ONLINE);
        if(mCheckBoxHrSpo2.isChecked() == true)
            sw.setSensorInfo(oMax30102, TYPE_ONLINE);
        if(mCheckBoxTemperatureHumidity.isChecked() == true)
            sw.setSensorInfo(oSi7021, TYPE_ONLINE);
        if(mCheckBoxSkin.isChecked() == true)
            sw.setSensorInfo(oSkinResistance, TYPE_ONLINE);
        resetCounterAndData(TYPE_ONLINE);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void dataBaseSendWSProcess() {
        if (getQueueSize())
            return;
        StringBuilder sb = new StringBuilder();
        sb.append(" \n \n \n Data send to db \n \n \n \n");
        mTextView.setText(sb.toString() + "\n");
        if(mCheckBoxEcgRr.isChecked() == true){
            List<Max3003> tmp = new ArrayList<>();
            lbgOnlineMax3003.drainTo(tmp);
            tmp.stream().forEach(s -> {sw.setSensorInfo(s,TYPE_DATABASE);});
            //sw.setSensorInfo(max3003, TYPE_DATABASE);
        }

        if(mCheckBoxHrSpo2.isChecked() == true){
            List<Max30102> tmp = new ArrayList<>();
            lbgOnlineMax30102.drainTo(tmp);
            tmp.stream().forEach(s -> {sw.setSensorInfo(s,TYPE_DATABASE);});
            //sw.setSensorInfo(max30102, TYPE_DATABASE);
        }

        if(mCheckBoxTemperatureHumidity.isChecked() == true){
            List<Si7021> tmp = new ArrayList<>();
            lbgOnlineSi7021.drainTo(tmp);
            tmp.stream().forEach(s -> {sw.setSensorInfo(s,TYPE_DATABASE);});
            //sw.setSensorInfo(si7021, TYPE_DATABASE);
        }

        if(mCheckBoxSkin.isChecked() == true){
            List<SkinResistance> tmp = new ArrayList<>();
            lbgOnlineSkinResistance.drainTo(tmp);
            tmp.stream().forEach(s -> {sw.setSensorInfo(s,TYPE_DATABASE);});
            //sw.setSensorInfo(skinResistance, TYPE_DATABASE);
        }
        resetCounterAndData(TYPE_DATABASE);
    }

    private boolean getQueueSize() {
        if(lbgOnlineMax3003.size()< mongoDataCounter || lbgOnlineMax30102.size()< mongoDataCounter ||
                lbgOnlineSkinResistance.size()< mongoDataCounter || lbgOnlineSi7021.size()< mongoDataCounter )
            return true;
        return false;
    }

    private void resetCounterAndData(String type) {
        if (type.equals(TYPE_DATABASE)) {
            //reset si7021
            si7021.setCounter(0);
            si7021.getHumidityByte().setLength(0);
            si7021.getTemperatureByte().setLength(0);
            //reset Max3003
            max3003.setCounter(0);
            max3003.getEcg().setLength(0);
            max3003.getRr().setLength(0);
            //reset max30102
            max30102.setCounter(0);
            max30102.getHr().setLength(0);
            max30102.getSpo2().setLength(0);
            max30102.getIred().setLength(0);
            max30102.getRed().setLength(0);
            //skin resistance
            skinResistance.setCounter(0);
            skinResistance.getSkinResistance().setLength(0);
        } else {
            oSi7021.setCounter(0);
            oSi7021.getHumidityByte().setLength(0);
            oSi7021.getTemperatureByte().setLength(0);
            //reset Max3003
            oMax3003.setCounter(0);
            oMax3003.getEcg().setLength(0);
            oMax3003.getRr().setLength(0);
            //reset max30102
            oMax30102.setCounter(0);
            oMax30102.getHr().setLength(0);
            oMax30102.getSpo2().setLength(0);
            oMax30102.getIred().setLength(0);
            oMax30102.getRed().setLength(0);
            //skin resistance
            oSkinResistance.setCounter(0);
            oSkinResistance.getSkinResistance().setLength(0);
        }

    }

    private void dataAppendProcess(int data_type, byte[] array) {
        if (si7021.getCounter() >= mongoDataCounter && data_type == HUMIDITY)
            return;
        if (data_type == HUMIDITY) {//Humidty and Temperature are gathered from same  sensor
            si7021.setType("environment");
            si7021.setCounter(si7021.getCounter() + 1);
            si7021.setHumidityByte(si7021.getHumidityByte().append(bytesToHex(array)));
            si7021.setDate(new Date());
        } else if (data_type == TEMPERATURE) {
            si7021.setTemperatureByte(si7021.getTemperatureByte().append(bytesToHex(array)));
            si7021.setDate(new Date());
        } else if (data_type == GSR) {
            skinResistance.setType("skin");
            skinResistance.setCounter(skinResistance.getCounter() + 1);
            skinResistance.setSkinResistance(skinResistance.getSkinResistance().append(bytesToHex(array)));
            skinResistance.setDate(new Date());
        } else if (data_type == HR) { // HR and SPO2 are gathered from same  sensor
            max30102.setType("heart");
            max30102.setCounter(max30102.getCounter() + 1);
            max30102.setHr(max30102.getHr().append(bytesToHex(array)));
            max30102.setDate(new Date());
        } else if (data_type == SPO2) {
            max30102.setSpo2(max30102.getSpo2().append(bytesToHex(array)));
            max30102.setDate(new Date());
        } else if(data_type == IRED){
            max30102.setIred(max30102.getIred().append(bytesToHex(array)));
        } else if(data_type == RED){
            max30102.setRed(max30102.getRed().append(bytesToHex(array)));
        } else if (data_type == RR) {
            max3003.setRr(max3003.getRr().append(bytesToHex(array)));
            max3003.setDate(new Date());
        } else if (data_type == ECG) {// ECG and RR are gathered from same  sensor
            max3003.setType("heart");
            max3003.setCounter(max3003.getCounter() + 1);
            max3003.setEcg(max3003.getEcg().append(bytesToHex(array)));
            max3003.setDate(new Date());
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        mHandler.removeCallbacks(mTimer1);
        basVar.setNotification(DATA_COLLECT_CHARACTERISTIC_UUID, false);
    }
}
