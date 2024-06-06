package com.example.dbms;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TableAdapter extends RecyclerView.Adapter<TableAdapter.TableViewHolder> {

    private List<TableItem> tableList;

    public TableAdapter(List<TableItem> tableList) {
        this.tableList = tableList;
    }

    @NonNull
    @Override
    public TableViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_table, parent, false);
        return new TableViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TableViewHolder holder, int position) {
        TableItem currentTable = tableList.get(position);
        holder.textViewTableName.setText(currentTable.getName());
        holder.textViewColumnCount.setText(String.valueOf(currentTable.getColumnCount()));
    }

    @Override
    public int getItemCount() {
        return tableList.size();
    }

    static class TableViewHolder extends RecyclerView.ViewHolder {

        private TextView textViewTableName;
        private TextView textViewColumnCount;

        public TableViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTableName = itemView.findViewById(R.id.textViewTableName);
            textViewColumnCount = itemView.findViewById(R.id.textViewColumnCount);
        }
    }
}
