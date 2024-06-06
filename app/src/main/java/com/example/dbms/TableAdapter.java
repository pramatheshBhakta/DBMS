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
    private OnTableClickListener mListener;

    public interface OnTableClickListener {
        void onTableClick(int position);
        void onTableDelete(int position); // Added method for delete action
    }

    public void setOnTableClickListener(OnTableClickListener listener) {
        mListener = listener;
    }

    public TableAdapter(List<TableItem> tableList) {
        this.tableList = tableList;
    }

    @NonNull
    @Override
    public TableViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_table, parent, false);
        return new TableViewHolder(itemView, mListener);
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
        private ImageButton buttonDelete; // Added delete button

        public TableViewHolder(@NonNull View itemView, final OnTableClickListener listener) {
            super(itemView);
            textViewTableName = itemView.findViewById(R.id.textViewTableName);
            textViewColumnCount = itemView.findViewById(R.id.textViewColumnCount);
            buttonDelete = itemView.findViewById(R.id.TableButtonDelete); // Initialize delete button

            // Set onClickListener for delete button
            buttonDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onTableDelete(position); // Call onTableDelete method
                        }
                    }
                }
            });

            // Set onClickListener for item view
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
        }
    }
}
