package com.example.dbms;

import android.content.Context;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteConstraintException;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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

        // Set up the Insert button click listener
        findViewById(R.id.btnInsert).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInsertDialog();
            }
        });
        findViewById(R.id.btnUpdate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUpdateDialog();
            }
        });

        // Set up the Delete button click listener
        findViewById(R.id.btnDelete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show a dialog to input the position to delete
                showDeletePositionDialog();
            }
        });
    }

    private void showDeletePositionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Row");

        // Create an EditText to input position to delete
        final EditText editTextPosition = new EditText(this);
        editTextPosition.setHint("Enter position to delete");
        builder.setView(editTextPosition);

        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Get the position entered by the user
                int position = Integer.parseInt(editTextPosition.getText().toString());

                // Confirm the deletion by displaying the first field of the row
                List<List<String>> tableData = tableDataAdapter.getTableData();
                if (position >= 1 && position <= tableData.size()) {
                    List<String> rowToDelete = tableData.get(position - 1); // Adjust position to match list index
                    String firstField = rowToDelete.get(0); // Get the first field of the row
                    showDeleteConfirmationDialog(position, firstField);
                } else {
                    Toast.makeText(TableDetailActivity.this, "Invalid position", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void showDeleteConfirmationDialog(final int position, String firstField) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Deletion");
        builder.setMessage("Do you want to delete the row Starting With Data : " + firstField + "?");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Call deleteRow method with the specified position
                appDatabase.deleteRow(tableName, position);

                // Reload table data after deletion
                new LoadTableDataTask().execute();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
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

    private void showUpdateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update Row");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        List<String> columnDataTypes = appDatabase.getTableColumnTypes(tableName);

        final List<EditText> editTexts = new ArrayList<>();
        for (int i = 0; i < columnNames.size(); i++) {
            EditText editText = new EditText(this);
            editText.setHint(columnNames.get(i));
            layout.addView(editText);
            editTexts.add(editText);

            final int index = i;
            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    if (!appDatabase.validateDataType(s.toString(), columnDataTypes.get(index))) {
                        editText.setError("Invalid data type");
                    }
                }
            });
        }

        builder.setView(layout);

        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                List<String> updatedRowData = new ArrayList<>();
                for (EditText editText : editTexts) {
                    updatedRowData.add(editText.getText().toString());
                }

                boolean isValid = true;
                for (int i = 0; i < updatedRowData.size(); i++) {
                    if (!appDatabase.validateDataType(updatedRowData.get(i), columnDataTypes.get(i))) {
                        editTexts.get(i).setError("Invalid data type");
                        isValid = false;
                    }
                }

                if (isValid) {
                    new UpdateRowTask().execute(updatedRowData);
                } else {
                    Toast.makeText(TableDetailActivity.this, "Please correct the data types", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private class UpdateRowTask extends AsyncTask<List<String>, Void, InsertResult> {
        @Override
        protected InsertResult doInBackground(List<String>... params) {
            try {
                if (params != null && params.length == 1) {
                    List<String> updatedRowData = params[0];
                    List<String> foreignKeyInfo = appDatabase.getForeignKeyInfo(tableName); // Assuming you retrieve foreign key info here

                    return appDatabase.upsertRow(tableName, columnNames, appDatabase.getTableColumnTypes(tableName), updatedRowData, foreignKeyInfo);
                } else {
                    return new InsertResult(false, "Invalid parameters", false);
                }
            } catch (Exception e) {
                e.printStackTrace();
                return new InsertResult(false, "Error updating row", false);
            }
        }

        @Override
        protected void onPostExecute(InsertResult result) {
            if (result.isSuccess()) {
                if (result.isRowInserted()) {
                    Toast.makeText(TableDetailActivity.this, "Row not found,Data inserted successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(TableDetailActivity.this, "Row updated successfully", Toast.LENGTH_SHORT).show();
                }
                new LoadTableDataTask().execute();
            } else {
                Toast.makeText(TableDetailActivity.this, result.getErrorMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showInsertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Insert Row");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        List<String> columnDataTypes = appDatabase.getTableColumnTypes(tableName);
        List<String> foreignKeyInfo = appDatabase.getForeignKeyInfo(tableName); // Retrieve foreign key info

        final List<EditText> editTexts = new ArrayList<>();
        for (int i = 0; i < columnNames.size(); i++) {
            EditText editText = new EditText(this);
            editText.setHint(columnNames.get(i));
            layout.addView(editText);
            editTexts.add(editText);

            final int index = i;
            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    if (!appDatabase.validateDataType(s.toString(), columnDataTypes.get(index))) {
                        editText.setError("Invalid data type");
                    }
                }
            });
        }

        builder.setView(layout);

        builder.setPositiveButton("Insert", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                List<String> rowData = new ArrayList<>();
                for (EditText editText : editTexts) {
                    rowData.add(editText.getText().toString());
                }

                boolean isValid = true;
                for (int i = 0; i < rowData.size(); i++) {
                    if (!appDatabase.validateDataType(rowData.get(i), columnDataTypes.get(i))) {
                        editTexts.get(i).setError("Invalid data type");
                        isValid = false;
                    }
                }

                if (isValid) {
                    new InsertRowTask().execute(rowData, foreignKeyInfo);
                } else {
                    Toast.makeText(TableDetailActivity.this, "Please correct the data types", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private class InsertRowTask extends AsyncTask<Object, Void, InsertResult> {
        @Override
        protected InsertResult doInBackground(Object... params) {
            try {
                List<String> rowData = (List<String>) params[0];
                List<String> foreignKeyInfo = (List<String>) params[1];
                return appDatabase.upsertRow(tableName, columnNames, appDatabase.getTableColumnTypes(tableName), rowData, foreignKeyInfo);
            } catch (Exception e) {
                e.printStackTrace();
                return new InsertResult(false, "Error inserting/updating row", false);
            }
        }

        @Override
        protected void onPostExecute(InsertResult result) {
            if (result.isSuccess()) {
                if (result.isRowInserted()) {
                    Toast.makeText(TableDetailActivity.this, "Row inserted successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(TableDetailActivity.this, "Row already Present, updated successfully", Toast.LENGTH_SHORT).show();
                }
                new LoadTableDataTask().execute();
            } else {
                Toast.makeText(TableDetailActivity.this, result.getErrorMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }





}
