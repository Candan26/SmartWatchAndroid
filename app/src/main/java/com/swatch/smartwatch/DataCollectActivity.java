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
import com.swatch.smartwatch.ws.SmartWatchServer;

import java.nio.ByteBuffer;

public class DataCollectActivity extends AppCompatActivity {

    private static final int MAX_BYTE_LENGTH = 128;//256
    private static final String TAG = "DATA COLLECT";
    private final String DATA_COLLECT_CHARACTERISTIC_UUID = "54538aee-dd71-11e9-98a9-2a2ae2dbcce4";
    private TextView mTextView;
    private ImageView mImageView;
    private   BaseActivity basVar;
    private Runnable mTimer1;
    private final Handler mHandler = new Handler();

    public static final  int LUX = 0;
    public static final  int HUMIDITY = 1;
    public static final  int TEMPERATURE = 2;
    public static final  int HR = 3;
    public static final  int SKIN =4;

    int gsr;
    String dataToSendDBGSR = "";

    int ekg;
    String dataToSendDBEKG = "";

    int lux;
    String dataToSendDBLUX = "";

    private float humidity;
    String dataToSendDBHumidity = "";

    private float temperature;
    String dataToSendDBTemperature = "";

    private SmartWatchServer sw;

    int dbCounter=0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_collect);

        mTextView = findViewById(R.id.textViewDataCollect);
        mImageView = findViewById(R.id.imageViewDataCollect);


        final int[] counter = {0};

        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                counter[0]++;
                final EmailFragment blankFragment = new EmailFragment();
                final FragmentManager fm = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fm.beginTransaction ();
                fragmentTransaction.add(R.id.DataCollectLayoutId,blankFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });
        basVar = (BaseActivity) getApplicationContext();
        basVar.setNotification(DATA_COLLECT_CHARACTERISTIC_UUID,true);
        sw = new SmartWatchServer(this);
        plotData();
    }

    private void getCharArrayOfData(Byte array[],String hex){
        int j=0;
        for (int i = 0; i < hex.length(); i = i + 2) {
            // Step-1 Split the hex string into two character group
            String s = hex.substring(i, i + 2);
            // Step-2 Convert the each character group into integer using valueOf method
            int ik = Integer.valueOf(s,16);
            // Step-3 Cast the integer value to char
            array[j]=(byte)ik;
            j++;
        }
    }
    public  void plotData(){
        mTimer1 = new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void run() {
                String str = basVar.dataFromNotification;
                str= str.replace(" ","");
                mTextView.setText("byteArray : "+basVar.dataFromNotification );
                String s =basVar.dataFromNotification.substring(0,2);
                byte b=  Byte.valueOf(s,16);
                Byte []array =new Byte[20];
                getCharArrayOfData(array,str);
                fillDataBaseData(array);
                mTextView.setText("byteArray : "+basVar.dataFromNotification +" humidity: "+humidity +
                                  " temperature: "+ temperature + " heart rate: "+ ekg +" LUX: "+lux + " SKIN: "+gsr);
                try {
                    dataBaseSendWSProcess();
                }catch (Exception ex){
                    Log.d(TAG,ex.toString());
                }
                dbCounter++;
                mHandler.postDelayed(this,125);

            }
        };
        mHandler.postDelayed(mTimer1,500);
    }

    private void fillDataBaseData(Byte []array){
        byte []tempArray = new byte[4];

        for(int i= 0 ;i<5 ;i++){
            for(int j = 0; j<4; j++){
                tempArray[j]=array[(i*4)+j];
            }
            if(i==0){
                ByteBuffer wrapped = ByteBuffer.wrap(tempArray); // big-endian by default
                humidity = wrapped.getFloat();
                dataAppendProcess(HUMIDITY,tempArray);
            }

            if(i==1){
                ByteBuffer wrapped = ByteBuffer.wrap(tempArray); // big-endian by default
                temperature = wrapped.getFloat();
                dataAppendProcess(TEMPERATURE,tempArray);
            }

            if(i==2){
                ByteBuffer wrapped = ByteBuffer.wrap(tempArray); // big-endian by default
                ekg = wrapped.getInt();
                dataAppendProcess(HR,tempArray);
            }

            if(i==3){
                ByteBuffer wrapped = ByteBuffer.wrap(tempArray); // big-endian by default
                lux = wrapped.getInt();
                dataAppendProcess(LUX,tempArray);
            }

            if(i==4){
                ByteBuffer wrapped = ByteBuffer.wrap(tempArray); // big-endian by default
                gsr = wrapped.getInt();
                dataAppendProcess(SKIN,tempArray);
            }

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private  void dataBaseSendWSProcess(){
        if(dbCounter==MAX_BYTE_LENGTH){
            //send data process  to server
            mTextView.setText("counter finished");
            dbCounter=0;

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    sw.setSensorInfo(SmartWatchServer.LUX,dataToSendDBLUX);
                    dataToSendDBLUX ="";
                }
            },1);


            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    sw.setSensorInfo(SmartWatchServer.HUMIDITY,dataToSendDBHumidity);
                    dataToSendDBHumidity ="";
                }
            },25);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    sw.setSensorInfo(SmartWatchServer.TEMPERATURE,dataToSendDBTemperature);
                    dataToSendDBTemperature ="";
                }
            },50);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    sw.setSensorInfo(SmartWatchServer.HR,dataToSendDBEKG);
                    dataToSendDBEKG ="";
                }
            },75);


            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    sw.setSensorInfo(SmartWatchServer.SKIN,dataToSendDBGSR);
                    dataToSendDBGSR ="";
                }
            },100);
        }
    }

    private void dataAppendProcess(int data_type, byte[] array){

            if(data_type==LUX){
                dataToSendDBLUX = dataToSendDBLUX +bytesToHex (array);
            }else if(data_type==HUMIDITY){
                dataToSendDBHumidity = dataToSendDBHumidity +bytesToHex (array);
            }
            else if(data_type==TEMPERATURE){
                dataToSendDBTemperature = dataToSendDBTemperature + bytesToHex (array);
            }
            else if(data_type==HR){
                dataToSendDBEKG = dataToSendDBEKG + bytesToHex (array);
            }
            else if(data_type==SKIN){
                dataToSendDBGSR = dataToSendDBGSR + bytesToHex (array);
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
        basVar.setNotification(DATA_COLLECT_CHARACTERISTIC_UUID,false);
    }
}
