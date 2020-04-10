package com.swatch.smartwatch.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.swatch.smartwatch.R;
import com.swatch.smartwatch.ws.SmartWatchServer;

import java.util.List;

public class SqlTableAdapter  extends RecyclerView.Adapter{


    List<SmartWatchServer.SensorInfo> mSensorInfoList;
    int mLayoutResourceId;

    public SqlTableAdapter(List<SmartWatchServer.SensorInfo> sensorInfoList, int layoutResourceId){
        this.mSensorInfoList=sensorInfoList;
        this.mLayoutResourceId= layoutResourceId;
    }



    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.sql_table, parent, false);
        return new RowViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        RowViewHolder rowViewHolder = (RowViewHolder) holder;
        int rowPos = rowViewHolder.getAdapterPosition();
        if (rowPos == 0) {
            // Header Cells. Main Headings appear here
            int id = R.drawable.table_header_cell_bg;
            rowViewHolder.id.setBackgroundResource(R.drawable.table_header_cell_bg);
            rowViewHolder.type.setBackgroundResource(R.drawable.table_header_cell_bg);
            rowViewHolder.data.setBackgroundResource(R.drawable.table_header_cell_bg);
            rowViewHolder.date.setBackgroundResource(R.drawable.table_header_cell_bg);
            rowViewHolder.person.setBackgroundResource(R.drawable.table_header_cell_bg);


            rowViewHolder.id.setText("ID");
            rowViewHolder.type.setText("TYPE");
            rowViewHolder.data.setText("DATA");
            rowViewHolder.date.setText("DATE");
            rowViewHolder.person.setText("PERSON");
        } else {
            SmartWatchServer.SensorInfo modal = mSensorInfoList.get(rowPos-1);

            // Content Cells. Content appear here
            rowViewHolder.id.setBackgroundResource(R.drawable.table_content_cell_bg);
            rowViewHolder.type.setBackgroundResource(R.drawable.table_content_cell_bg);
            rowViewHolder.data.setBackgroundResource(R.drawable.table_content_cell_bg);
            rowViewHolder.date.setBackgroundResource(R.drawable.table_content_cell_bg);
            rowViewHolder.person.setBackgroundResource(R.drawable.table_content_cell_bg);

            rowViewHolder.id.setText(modal.getId()+"");
            rowViewHolder.type.setText(modal.getType());
            rowViewHolder.data.setText(modal.getData()+"");
            rowViewHolder.date.setText(modal.getDate()+"");
            rowViewHolder.person.setText(modal.getPerson()+"");
        }
    }

    @Override
    public int getItemCount() {
        return mSensorInfoList.size();
    }



    public class RowViewHolder extends RecyclerView.ViewHolder {
        protected TextView id;
        protected TextView type;
        protected TextView data;
        protected TextView date;
        protected TextView person;

        public RowViewHolder(View itemView) {
            super(itemView);


            TextView id1 = itemView.findViewById(R.id.textViewLight);
            id = itemView.findViewById(R.id.txtViewSqlId);
            type = itemView.findViewById(R.id.txtViewSqlType);
            data = itemView.findViewById(R.id.txtViewSqlData);
            date = itemView.findViewById(R.id.txtViewSqlDate);
            person = itemView.findViewById(R.id.txtViewSqlPerson);
        }
    }
}
