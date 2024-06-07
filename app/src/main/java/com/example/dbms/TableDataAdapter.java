package com.example.dbms;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
public class TableDataAdapter extends RecyclerView.Adapter<TableDataAdapter.ViewHolder> {

    private List<List<String>> tableData;
    private List<String> columnNames;

    public TableDataAdapter(List<List<String>> tableData) {
        this.tableData = tableData;
        this.columnNames = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_table_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        List<String> rowData = tableData.get(position);
        holder.bindData(rowData);
    }

    @Override
    public int getItemCount() {
        return tableData.size();
    }

    public void setColumnNames(List<String> columnNames) {
        this.columnNames = columnNames;
    }

    public List<List<String>> getTableData() {
        return tableData;
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {

        private LinearLayout linearLayoutRow;
        private List<TextView> textViewList;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            linearLayoutRow = itemView.findViewById(R.id.tableDataRow);
            textViewList = new ArrayList<>();
        }

        public void bindData(List<String> rowData) {
            linearLayoutRow.removeAllViews();
            textViewList.clear();
            for (String cellData : rowData) {
                TextView textView = new TextView(itemView.getContext());
                textView.setText(cellData);

                textView.setPadding(16, 8, 16, 8); // Adjust padding for spacing
                textView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1)); // Adjust layout parameters
                textView.setBackgroundResource(R.drawable.border_bottom); // Set background drawable with right border
                textView.setGravity(Gravity.CENTER); // Center text horizontally and vertically
                linearLayoutRow.addView(textView);
                textViewList.add(textView);
            }
        }
    }
}
