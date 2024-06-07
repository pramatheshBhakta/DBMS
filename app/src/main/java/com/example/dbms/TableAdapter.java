package com.example.dbms;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TableAdapter extends RecyclerView.Adapter<TableAdapter.TableViewHolder> {

    private List<TableItem> tableList;
    private OnTableClickListener listener;

    public TableAdapter(List<TableItem> tableList, OnTableClickListener listener) {
        this.tableList = tableList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TableViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_table, parent, false);
        return new TableViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TableViewHolder holder, int position) {
        TableItem tableItem = tableList.get(position);
        holder.textViewTableName.setText(tableItem.getName());
        holder.textViewColumnCount.setText(String.valueOf(tableItem.getColumnCount()));
    }

    @Override
    public int getItemCount() {
        return tableList.size();
    }

    public class TableViewHolder extends RecyclerView.ViewHolder {
        public TextView textViewTableName;
        public TextView textViewColumnCount;
        public ImageButton buttonDeleteTable;

        public TableViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTableName = itemView.findViewById(R.id.textViewTableName);
            textViewColumnCount = itemView.findViewById(R.id.textViewColumnCount);
            buttonDeleteTable = itemView.findViewById(R.id.TableButtonDelete);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onTableClick(position);
                        }
                    }
                }
            });

            buttonDeleteTable.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onTableDelete(position);
                        }
                    }
                }
            });
        }
    }

    public interface OnTableClickListener {
        void onTableClick(int position);
        void onTableDelete(int position);
    }
}
