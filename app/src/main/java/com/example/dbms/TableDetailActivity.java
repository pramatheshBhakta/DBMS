package com.example.dbms;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class TableDetailActivity extends AppCompatActivity {

    private RecyclerView recyclerViewTableData;
    private TableDataAdapter tableDataAdapter;
    private List<List<String>> tableData = new ArrayList<>();
    private List<String> columnNames = new ArrayList<>();
    private AppDatabase appDatabase;
    private String databaseName;
    private String tableName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table_detail);

        recyclerViewTableData = findViewById(R.id.recyclerViewTableData);
        recyclerViewTableData.setLayoutManager(new LinearLayoutManager(this));
        tableDataAdapter = new TableDataAdapter(tableData);
        recyclerViewTableData.setAdapter(tableDataAdapter);

        // Get the database and table names from the intent
        databaseName = getIntent().getStringExtra("databaseName");
        tableName = getIntent().getStringExtra("tableName");

        // Initialize Room database
        appDatabase = AppDatabase.getDatabase(this);

        // Load the table data and columns
        new LoadTableDataTask().execute();
    }

    private class LoadTableDataTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                tableData.clear();
                columnNames.clear();
                columnNames.addAll(appDatabase.getTableColumns(tableName));
                tableData.addAll(appDatabase.getTableData(tableName));
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                displayTable();
            } else {
                Toast.makeText(TableDetailActivity.this, "Error loading table data", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void displayTable() {
        // Display column names dynamically in the first row
        LinearLayout linearLayoutColumnNames = findViewById(R.id.linearLayoutColumnNames);
        linearLayoutColumnNames.removeAllViews();
        for (int i = 0; i < columnNames.size(); i++) {
            TextView textView = new TextView(this);
            textView.setText(columnNames.get(i));
            textView.setPadding(16, 8, 16, 8);
            textView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1)); // Set layout weight
            textView.setGravity(Gravity.CENTER); // Center text horizontally and vertically
            textView.setTextColor(Color.WHITE); // Set text color to white

            // Set background with border for all columns
            textView.setBackgroundResource(R.drawable.header_border);

            linearLayoutColumnNames.addView(textView);
        }

        // Notify RecyclerView adapter about data changes
        recyclerViewTableData.setLayoutManager(new LinearLayoutManager(this));
        tableDataAdapter = new TableDataAdapter(tableData);
        recyclerViewTableData.setAdapter(tableDataAdapter);
        tableDataAdapter.setColumnNames(columnNames);
        tableDataAdapter.notifyDataSetChanged();
    }







}
