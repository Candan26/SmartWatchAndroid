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
import android.widget.ImageView;
import android.widget.TextView;

import com.swatch.smartwatch.fragment.EmailFragment;
import com.swatch.smartwatch.sensors.Max3003;
import com.swatch.smartwatch.sensors.Max30102;
import com.swatch.smartwatch.sensors.Si7021;
import com.swatch.smartwatch.sensors.SkinResistance;
import com.swatch.smartwatch.ws.SmartWatchServer;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Date;

public class DataCollectActivity extends AppCompatActivity {

    private static final int MAX_BYTE_LENGTH = 128;//256
    private static final String TAG = "DATA COLLECT";
    private final String DATA_COLLECT_CHARACTERISTIC_UUID = "24538aee-dd71-11e9-98a9-2a2ae2dbcce4";
    private TextView mTextView;
    private ImageView mImageView;
    private BaseActivity basVar;
    private Runnable mTimer1;
    private final Handler mHandler = new Handler();

    public static final int HUMIDITY = 0;
    public static final int TEMPERATURE = 1;
    public static final int GSR = 2;
    public static final int HR = 3;
    public static final int SPO2 = 4;
    public static final int ECG = 5;
    public static final int RR = 6;

    public static Number[] nba = {HUMIDITY, TEMPERATURE, GSR, HR, SPO2, ECG, RR};
    public static Number[] nbs = {4, 4, 2, 1, 1, 4, 4};//size of each data
    public static Number[] nbo = {0, 4, 8, 10, 11, 12, 16};//size of each data

    Max3003 max3003;
    Max30102 max30102;
    Si7021 si7021;
    SkinResistance skinResistance;


    private SmartWatchServer sw;

    int dbCounter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_collect);

        mTextView = findViewById(R.id.textViewDataCollect);
        mImageView = findViewById(R.id.imageViewDataCollect);

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
        max3003  = new Max3003();
        max30102 = new Max30102();
        si7021 = new Si7021();
        skinResistance = new SkinResistance();
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
                mTextView.setText("byteArray : " + basVar.dataFromNotification);
                String s = basVar.dataFromNotification.substring(0, 2);
                byte b = Byte.valueOf(s, 16);
                Byte[] array = new Byte[40];
                Arrays.fill(array, (byte) 0);
                getCharArrayOfData(array, str);
                Log.d("TEMP", str);
                fillDataBaseData(array);
                StringBuilder sb = new StringBuilder();
                sb.append("HUM: " + nba[HUMIDITY].floatValue() + "\n");
                sb.append("TEMP: " + nba[TEMPERATURE].floatValue() + "\n");
                sb.append("GSR: " + nba[GSR].shortValue() + "\n");
                sb.append("HR: " + nba[HR] + "\n");
                sb.append("SPO2: " + nba[SPO2] + "\n");
                sb.append("ECG: " + nba[ECG].intValue() + "\n");
                sb.append("RR: " + nba[RR].intValue() + "\n");
                mTextView.setText(sb.toString() + "\n");
                try {
                    dataBaseSendWSProcess();
                } catch (Exception ex) {
                    Log.d(TAG, ex.toString());
                }
                dbCounter++;
                mHandler.postDelayed(this, 125);

            }
        };
        mHandler.postDelayed(mTimer1, 501);
    }

    private void fillDataBaseData(Byte[] array) {
        for (int i = 0; i < nbs.length; i++) {
            byte[] tempArray = new byte[nbs[i].intValue()];
            for (int j = 0; j < nbs[i].intValue(); j++) {
                tempArray[j] = array[(nbo[i].intValue()) + j];
            }
            ByteBuffer wrapped = ByteBuffer.wrap(tempArray); // big-endian by default
            dataAppendProcess(i, tempArray);
            if (i == HUMIDITY || i == TEMPERATURE) {
                nba[i] = wrapped.getFloat();
            } else if(nbs[i].intValue() == 2) {
                nba[i] =  wrapped.getShort();
            }else if(nbs[i].intValue() == 1) {
                nba[i] =  (int)wrapped.get()%0xFF;
            }else{
                nba[i] = wrapped.getInt();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void dataBaseSendWSProcess() {
        if(si7021.getCounter() < 300)
            return;
        mTextView.setText("Data send to db");
        sw.setSensorInfo(max3003);
        sw.setSensorInfo(max30102);
        sw.setSensorInfo(si7021);
        sw.setSensorInfo(skinResistance);
        resetCounterAndData();
        /*
        if(dbCounter==MAX_BYTE_LENGTH){
            //send data process  to server
            mTextView.setText("counter finished");
            dbCounter=0;

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    sw.setSensorInfo(SmartWatchServer.LUX, dataToSendHr);
                    dataToSendHr ="";
                }
            },1);


            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    sw.setSensorInfo(SmartWatchServer.HUMIDITY, dataToSendHumidity);
                    dataToSendHumidity ="";
                }
            },25);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    sw.setSensorInfo(SmartWatchServer.TEMPERATURE, dataToSendTemperature);
                    dataToSendTemperature ="";
                }
            },50);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    sw.setSensorInfo(SmartWatchServer.HR, dataToSendEcg);
                    dataToSendEcg ="";
                }
            },75);


            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    sw.setSensorInfo(SmartWatchServer.SKIN, dataToSendSpo2);
                    dataToSendSpo2 ="";
                }
            },100);
        }

         */
    }

    private void resetCounterAndData() {
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
        //skin resistance
        skinResistance.setCounter(0);
        skinResistance.getSkinResistance().setLength(0);
    }

    private void dataAppendProcess(int data_type, byte[] array) {
        if(si7021.getCounter()>=300 && data_type == HUMIDITY )
            return;
        if (data_type == HUMIDITY) {//Humidty and Temperature are gathered from same  sensor
            si7021.setType("environment");
            si7021.setCounter(si7021.getCounter()+1);
            si7021.setHumidityByte(si7021.getHumidityByte().append(bytesToHex(array)));
            si7021.setDate(new Date());
        } else if (data_type == TEMPERATURE) {
           si7021.setTemperatureByte(si7021.getTemperatureByte().append(bytesToHex(array)));
            si7021.setDate(new Date());
        } else if (data_type == GSR) {
            skinResistance.setType("skin");
            skinResistance.setCounter(skinResistance.getCounter()+1);
            skinResistance.setSkinResistance(skinResistance.getSkinResistance().append(bytesToHex(array)));
            skinResistance.setDate(new Date());
        } else if (data_type == HR) { // HR and SPO2 are gathered from same  sensor
            max30102.setType("heart");
            max30102.setCounter(max30102.getCounter()+1);
            max30102.setHr(max30102.getHr().append(bytesToHex(array)));
            max30102.setDate(new Date());
        } else if (data_type == SPO2) {
            max30102.setSpo2(max30102.getSpo2().append(bytesToHex(array)));
            max30102.setDate(new Date());;
        } else if (data_type == ECG) {// ECG and RR are gathered from same  sensor
            max3003.setType("heart");
            max3003.setCounter(max3003.getCounter()+1);
            max3003.setEcg(max3003.getEcg().append(bytesToHex(array)));
            max3003.setDate(new Date());
        } else if (data_type == RR) {
            max3003.setRr(max3003.getRr().append(bytesToHex(array)));
            max3003.setDate(new Date());
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

    @Override
    protected void onPause() {
        super.onPause();
        mHandler.removeCallbacks(mTimer1);
        basVar.setNotification(DATA_COLLECT_CHARACTERISTIC_UUID, false);
    }
}
