package com.example.dbms;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;

import androidx.sqlite.db.SimpleSQLiteQuery;
import androidx.sqlite.db.SupportSQLiteQuery;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.dbms.DatabaseDao;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TableActivity extends AppCompatActivity implements TableAdapter.OnTableClickListener {

    private RecyclerView recyclerViewTables;
    private TextView dbname;
    private TableAdapter tableAdapter;
    private List<TableItem> tableList = new ArrayList<>();
    private AppDatabase appDatabase;
    private String databaseName;
    private DatabaseDao databaseDao;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table);

        recyclerViewTables = findViewById(R.id.recyclerViewTables);
        recyclerViewTables.setLayoutManager(new LinearLayoutManager(this));

        // Pass `this` to the TableAdapter constructor
        tableAdapter = new TableAdapter(tableList, this);
        recyclerViewTables.setAdapter(tableAdapter);
        dbname = findViewById(R.id.textViewDatabaseName);

        // Get the selected database name from the intent
        databaseName = getIntent().getStringExtra("databaseName");
        dbname.setText(databaseName.toUpperCase(Locale.ROOT) + "'S DATABASE");

        // Initialize Room database
        appDatabase = AppDatabase.getDatabase(this);

        // Observe changes in the database and update UI accordingly
        appDatabase.databaseDao().getTablesForDatabase(databaseName).observe(this, new Observer<List<TableEntity>>() {
            @Override
            public void onChanged(List<TableEntity> tableEntities) {
                tableList.clear();
                for (TableEntity tableEntity : tableEntities) {
                    tableList.add(new TableItem(tableEntity.getName(), tableEntity.getColumnCount()));
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

    // Implementing the interface method to handle table item deletion
    @Override
    public void onTableDelete(int position) {
        // Retrieve the clicked table item
        TableItem tableItem = tableList.get(position);

        // Show a confirmation dialog
        new AlertDialog.Builder(this)
                .setTitle("Delete Table")
                .setMessage("Are you sure you want to delete the table " + tableItem.getName() + "?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Execute AsyncTask to delete the table
                        new DeleteTableTask(appDatabase, TableActivity.this, databaseName, tableItem.getName()).execute();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    private static class DeleteTableTask extends AsyncTask<Void, Void, Boolean> {
        private final AppDatabase appDatabase;
        private final TableActivity activity;
        private final String databaseName;
        private final String tableName;
        private String errorMessage;

        DeleteTableTask(AppDatabase appDatabase, TableActivity activity, String databaseName, String tableName) {
            this.appDatabase = appDatabase;
            this.activity = activity;
            this.databaseName = databaseName;
            this.tableName = tableName;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                // Drop the table from the database
                appDatabase.getOpenHelper().getWritableDatabase().execSQL("DROP TABLE IF EXISTS " + tableName);

                // Delete the table entity metadata from the Room database
                appDatabase.databaseDao().deleteTable(tableName, databaseName);

                return true;
            } catch (Exception e) {
                errorMessage = e.getMessage();
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(activity, "Table deleted successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(activity, "Error deleting table: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        }
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
                new CreateTableTask(appDatabase, TableActivity.this, databaseName, tableName, columnNames, dataTypes).execute();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // This method will be called when the "Cancel" button is clicked
                // Here, we simply dismiss the dialog
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    // Implementing the interface method to handle table item clicks
    @Override
    public void onTableClick(int position) {
        // Retrieve the clicked table item
        TableItem clickedTable = tableList.get(position);

        // Create an intent to navigate to the TableDetailActivity
        Intent intent = new Intent(TableActivity.this, TableDetailActivity.class);
        // Pass relevant data to the TableDetailActivity
        intent.putExtra("databaseName", databaseName);
        intent.putExtra("tableName", clickedTable.getName());
        startActivity(intent);
        startActivity(intent);
    }

    private static class CreateTableTask extends AsyncTask<Void, Void, Boolean> {
        private final AppDatabase appDatabase;
        private final TableActivity activity;
        private final String databaseName;
        private final String tableName;
        private final List<String> columnNames;
        private final List<String> dataTypes;
        private String errorMessage;

        CreateTableTask(AppDatabase appDatabase, TableActivity activity, String databaseName, String tableName, List<String> columnNames, List<String> dataTypes) {
            this.appDatabase = appDatabase;
            this.activity = activity;
            this.databaseName = databaseName;
            this.tableName = tableName;
            this.columnNames = columnNames;
            this.dataTypes = dataTypes;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            if (columnNames.size() == dataTypes.size() && columnNames.size() > 0) {
                // Generate the SQL query to create the table
                StringBuilder queryBuilder = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
                queryBuilder.append(tableName).append(" (");

                // Add column names and data types to the query
                for (int i = 0; i < columnNames.size(); i++) {
                    queryBuilder.append(columnNames.get(i)).append(" ").append(dataTypes.get(i));
                    if (i < columnNames.size() - 1) {
                        queryBuilder.append(", ");
                    }
                }

                // Close the query
                queryBuilder.append(");");

                // Execute the SQL query to create the table
                String createTableQuery = queryBuilder.toString();
                try {
                    appDatabase.getOpenHelper().getWritableDatabase().execSQL(createTableQuery);

                    // Insert metadata into the Room database
                    TableEntity tableEntity = new TableEntity(tableName, columnNames.size());
                    tableEntity.setDatabaseName(databaseName); // Set the database name
                    appDatabase.databaseDao().insertTable(tableEntity);
                    return true;
                } catch (Exception e) {
                    errorMessage = e.getMessage();
                    e.printStackTrace();
                    return false;
                }
            } else {
                errorMessage = "Please enter valid column names and data types";
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(activity, "Table created successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(activity, "Error creating table: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
