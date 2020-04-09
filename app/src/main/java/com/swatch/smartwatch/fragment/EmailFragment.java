package com.swatch.smartwatch.fragment;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.swatch.smartwatch.R;
import com.swatch.smartwatch.ws.SmartWatchServer;

import org.json.JSONObject;

import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EmailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EmailFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    EditText mEditTextEmailHostName;
    EditText mEditTextSkinPageNumber;
    EditText mEditTextSkinRowPerPage;
    EditText mEditTextHeartPageNumber;
    EditText mEditTextHeartRowPerPage;
    EditText mEditTextEnvPageNumber;
    EditText mEditTextEnvRowPerPage;

    Button mButtonSendInformation;

    private SmartWatchServer sw;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public EmailFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment EmailFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static EmailFragment newInstance(String param1, String param2) {
        EmailFragment fragment = new EmailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =inflater.inflate(R.layout.fragment_email, container, false);
        setViewFromId(view);
        return view;
    }

    public  void setViewFromId(View view){
        mEditTextEmailHostName= view.findViewById(R.id.editTextEmailHostName);
        mEditTextSkinPageNumber= view.findViewById(R.id.editTextSkinPageNumber);
        mEditTextSkinRowPerPage= view.findViewById(R.id.editTextSkinRowPerPage);
        mEditTextHeartPageNumber= view.findViewById(R.id.editTextHeartPageNumber);
        mEditTextHeartRowPerPage= view.findViewById(R.id.editTextHeartRowPerPage);
        mEditTextEnvPageNumber= view.findViewById(R.id.editTextEnvPageNumber);
        mEditTextEnvRowPerPage= view.findViewById(R.id.editTextEnvRowPerPage);


        SharedPreferences preferences =  PreferenceManager.getDefaultSharedPreferences(view.getContext());
        Map<String, String> keyValues= (Map<String, String>) preferences.getAll();
        mEditTextEmailHostName.setText(keyValues.get("usrMailAddress"));
        mEditTextSkinPageNumber.setText("1");
        mEditTextSkinRowPerPage.setText("400");
        mEditTextHeartPageNumber.setText("1");
        mEditTextHeartRowPerPage.setText("400");
        mEditTextEnvPageNumber.setText("1");
        mEditTextEnvRowPerPage.setText("400");

        sw = new SmartWatchServer(view.getContext());
        mButtonSendInformation =view.findViewById(R.id.buttonSendInformation);
        mButtonSendInformation.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                Log.d("TEST","sending email to user");
                String mailData =getMailData();
                if(mailData!=null)
                    sw.setSensorInfo(SmartWatchServer.EMAIL,mailData);
                getActivity().onBackPressed();
            }
        });
    }

    String getMailData(){
        String response ="";
        try {
            JSONObject email= new JSONObject();
            email.put("address",mEditTextEmailHostName.getText());
            email.put("skinPageNumber",mEditTextSkinPageNumber.getText());
            email.put("skinRowPerPage",mEditTextSkinRowPerPage.getText());
            email.put("heartPageNumber",mEditTextHeartPageNumber.getText());
            email.put("heartRowPerPage",mEditTextHeartRowPerPage.getText());
            email.put("envPageNumber",mEditTextEnvPageNumber.getText());
            email.put("envRowPerPage",mEditTextEnvRowPerPage.getText());
            return  email.toString();
        }catch (Exception e){
            Log.d("Exception",e.toString());
            return null;
        }

    }
}
