package com.example.dbms;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class TableActivity extends AppCompatActivity {

    private RecyclerView recyclerViewTables;
    private TableAdapter tableAdapter;
    private List<TableItem> tableList = new ArrayList<>();
    private AppDatabase appDatabase;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table);

        recyclerViewTables = findViewById(R.id.recyclerViewTables);
        recyclerViewTables.setLayoutManager(new LinearLayoutManager(this));
        tableAdapter = new TableAdapter(tableList);
        recyclerViewTables.setAdapter(tableAdapter);

        // Initialize Room database
        appDatabase = AppDatabase.getDatabase(this);

        // Observe changes in the database and update UI accordingly
        appDatabase.databaseDao().getAllTables().observe(this, new Observer<List<TableEntity>>() {
            @Override
            public void onChanged(List<TableEntity> tableEntities) {
                tableList.clear();
                for (TableEntity tableEntity : tableEntities) {
                    tableList.add(new TableItem(tableEntity.name, tableEntity.columnCount));
                }
                tableAdapter.notifyDataSetChanged();
            }
        });

        // Set up FloatingActionButton click listener
        FloatingActionButton fabAddTable = findViewById(R.id.fabAddTable);
        fabAddTable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddTableDialog();
            }
        });
    }

    private void showAddTableDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.add_table_dialog, null);
        builder.setView(dialogView);

        EditText editTextTableName = dialogView.findViewById(R.id.editTextTableName);
        EditText editTextColumnCount = dialogView.findViewById(R.id.editTextColumnCount);

        builder.setTitle("Add Table");
        builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String tableName = editTextTableName.getText().toString().trim();
                String columnCountString = editTextColumnCount.getText().toString().trim();
                if (!tableName.isEmpty() && !columnCountString.isEmpty()) {
                    int columnCount = Integer.parseInt(columnCountString);
                    // Show another dialog to add column names and data types based on the column count
                    showAddColumnDialog(tableName, columnCount);
                } else {
                    Toast.makeText(TableActivity.this, "Please enter valid table name and column count", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();
    }

    private void showAddColumnDialog(String tableName, int columnCount) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.add_column_dialog, null);
        builder.setView(dialogView);

        LinearLayout linearLayoutColumnContainer = dialogView.findViewById(R.id.linearLayoutColumnContainer);

        for (int i = 0; i < columnCount; i++) {
            View columnView = inflater.inflate(R.layout.item_column_field, null);
            EditText editTextColumnName = columnView.findViewById(R.id.editTextColumnName);
            Spinner spinnerDataType = columnView.findViewById(R.id.spinnerDataType);

            // Populate spinner with data types (e.g., text, integer, real)
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.data_types, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerDataType.setAdapter(adapter);

            // Add the inflated view to the linear layout container
            linearLayoutColumnContainer.addView(columnView);
        }

        builder.setTitle("Add Columns");
        builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Get column names and data types from the views dynamically
                List<String> columnNames = new ArrayList<>();
                List<String> dataTypes = new ArrayList<>();

                for (int i = 0; i < columnCount; i++) {
                    View columnView = linearLayoutColumnContainer.getChildAt(i);
                    EditText editTextColumnName = columnView.findViewById(R.id.editTextColumnName);
                    Spinner spinnerDataType = columnView.findViewById(R.id.spinnerDataType);

                    String columnName = editTextColumnName.getText().toString().trim();
                    String dataType = spinnerDataType.getSelectedItem().toString();

                    if (!columnName.isEmpty()) {
                        columnNames.add(columnName);
                        dataTypes.add(dataType);
                    }
                }

                // Create a new table with the specified attributes
                createTable(tableName, columnNames, dataTypes);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();
    }

    private void createTable(String tableName, String columnDefinition) {
        // Check if the database is initialized
        if (appDatabase != null) {
            // Ensure there are column names and data types
            if (!tableName.isEmpty() && !columnDefinition.isEmpty()) {
                // Generate the SQL query to create the table
                String query = "CREATE TABLE IF NOT EXISTS "+ tableName + " (" + columnDefinition + ")";

                // Execute the SQL query to create the table
                try {
                    appDatabase.databaseWriteExecutor.execute(() -> {
                        appDatabase.databaseDao().createTable(query);
                    });
                    Toast.makeText(this, "Table created successfully", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(this, "Error creating table: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(this, "Please enter valid table name and column definition", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Database is not initialized", Toast.LENGTH_SHORT).show();
        }
    }

}
