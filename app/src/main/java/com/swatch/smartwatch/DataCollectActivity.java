package com.swatch.smartwatch;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;

import java.nio.ByteBuffer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataCollectActivity extends AppCompatActivity {

    private static final int MAX_BYTE_LENGTH = 256;
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
    byte [] dataArrayGSR = new byte[1024];

    int ekg;
    byte []dataArrayEKG = new byte[1024];

    int lux;
    byte []dataArrayLUX = new byte[1024];

    private float humidity;
    byte []dataArrayHumidity = new byte[1024];

    private float temperature;
    byte []dataArrayTemperature = new byte[1024];

    int dbCounter=0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_collect);

        mTextView = findViewById(R.id.textViewDataCollect);
        mImageView = findViewById(R.id.imageViewDataCollect);
        basVar = (BaseActivity) getApplicationContext();
        basVar.setNotification(DATA_COLLECT_CHARACTERISTIC_UUID,true);
        plotData();
    }

    public static String ASCIItoHEX(String ascii)
    {
        // Initialize final String
        String hex = "";

        // Make a loop to iterate through
        // every character of ascii string
        for (int i = 0; i < ascii.length(); i++) {

            // take a char from
            // position i of string
            char ch = ascii.charAt(i);

            // cast char to integer and
            // find its ascii value
            int in = (int)ch;

            // change this ascii value
            // integer to hexadecimal value
            String part = Integer.toHexString(in);

            // add this hexadecimal value
            // to final string.
            hex += part;
        }
        // return the final string hex
        return hex;
    }

    private void getCharArrayOfData(Byte array[],String hex){
        int j=0;
        for (int i = 0; i < hex.length(); i = i + 2) {
            // Step-1 Split the hex string into two character group
            String s = hex.substring(i, i + 2);
            // Step-2 Convert the each character group into integer using valueOf method
            int ik = Integer.valueOf(s,16);

            //byte b=  Byte.valueOf(s,16);
            // Step-3 Cast the integer value to char
            array[j]=(byte)ik;
            j++;
        }
    }
    public  void plotData(){
        mTimer1 = new Runnable() {
            @Override
            public void run() {
                String str = basVar.dataFromNotification;
                str= str.replace(" ","");
                mTextView.setText("byteArray : "+basVar.dataFromNotification );
                String s =basVar.dataFromNotification.substring(0,2);
                byte b=  Byte.valueOf(s,16);
                Byte []array =new Byte[20];
                getCharArrayOfData(array,str);

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


                dbCounter++;
                mTextView.setText("byteArray : "+basVar.dataFromNotification +" humidity: "+humidity +
                                  " temperature: "+ temperature + " heart rate: "+ ekg +" LUX: "+lux + " SKIN: "+gsr);
                mHandler.postDelayed(this,25);
                if(dbCounter==MAX_BYTE_LENGTH){
                    //send data process  to server
                    mTextView.setText("counter finished");
                    dbCounter=0;
                }

            }

        };
        mHandler.postDelayed(mTimer1,500);
    }


    private void dataAppendProcess(int data_type, byte[] array){
        for(int i=0; i<4; i++){
            if(data_type==LUX){
                dataArrayLUX[(4*dbCounter)+i]=array[i];
            }else if(data_type==HUMIDITY){
                dataArrayHumidity[(4*dbCounter)+i]=array[i];
            }
            else if(data_type==TEMPERATURE){
                dataArrayTemperature[(4*dbCounter)+i]=array[i];
            }
            else if(data_type==HR){
                dataArrayEKG[(4*dbCounter)+i]=array[i];
            }
            else if(data_type==SKIN){
                dataArrayGSR[(4*dbCounter)+i]=array[i];
            }
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        mHandler.removeCallbacks(mTimer1);
        basVar.setNotification(DATA_COLLECT_CHARACTERISTIC_UUID,false);
    }
}
