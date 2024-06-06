package com.example.dbms;

import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class TableViewHolder extends RecyclerView.ViewHolder {
    TextView textViewTableName;
    TextView textViewColumnCount;


    public TableViewHolder(@NonNull View itemView) {
        super(itemView);
        textViewTableName = itemView.findViewById(R.id.textViewTableName);
        textViewColumnCount = itemView.findViewById(R.id.textViewColumnCount);
    }
}
