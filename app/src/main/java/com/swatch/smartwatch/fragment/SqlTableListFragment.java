package com.swatch.smartwatch.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.swatch.smartwatch.R;
import com.swatch.smartwatch.adapter.SqlTableAdapter;
import com.swatch.smartwatch.ws.SmartWatchServer;

import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SqlTableListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SqlTableListFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    RecyclerView recyclerView;
    List<SmartWatchServer.SensorInfo> mSensorInfoList;
    int mResourceId;
    public SqlTableListFragment() {
        // Required empty public constructor
    }

    public  SqlTableListFragment(List<SmartWatchServer.SensorInfo> sensorInfoList, int resourceId){
        this.mSensorInfoList=sensorInfoList;
        this.mResourceId=resourceId;
    }
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SqlTableList.
     */
    // TODO: Rename and change types and number of parameters
    public static SqlTableListFragment newInstance(String param1, String param2) {
        SqlTableListFragment fragment = new SqlTableListFragment();
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
        View view =inflater.inflate(R.layout.fragment_sql_table_list, container, false);
        recyclerView= view.findViewById(R.id.recyclerViewSensorInfoList);
        SqlTableAdapter adapter = new SqlTableAdapter(mSensorInfoList,mResourceId);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);
        return view;
    }

}
