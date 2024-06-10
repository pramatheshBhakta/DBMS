package com.example.dbms;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
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

        // Create a LinearLayout to hold input fields
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        // Fetch column data types from the database
        List<String> columnDataTypes = appDatabase.getTableColumnTypes(tableName);

        // Create EditText fields for each column
        final List<EditText> editTexts = new ArrayList<>();
        for (int i = 0; i < columnNames.size(); i++) {
            EditText editText = new EditText(this);
            editText.setHint(columnNames.get(i));
            layout.addView(editText);
            editTexts.add(editText);

            // Validate data type based on column data types fetched from the database
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

                // Validate data types before updating
                boolean isValid = true;
                for (int i = 0; i < updatedRowData.size(); i++) {
                    if (!appDatabase.validateDataType(updatedRowData.get(i), columnDataTypes.get(i))) {
                        editTexts.get(i).setError("Invalid data type");
                        isValid = false;
                    }
                }

                if (isValid) {
                    // Call the method to handle update operation
                    new InsertRowTask().execute(updatedRowData);

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

    // Method to handle the update operation


    private void showInsertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Insert Row");

        // Create a LinearLayout to hold input fields
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        // Fetch column data types from the database
        List<String> columnDataTypes = appDatabase.getTableColumnTypes(tableName);

        // Create EditText fields for each column
        final List<EditText> editTexts = new ArrayList<>();
        for (int i = 0; i < columnNames.size(); i++) {
            EditText editText = new EditText(this);
            editText.setHint(columnNames.get(i));
            layout.addView(editText);
            editTexts.add(editText);

            // Validate data type based on column data types fetched from the database
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

                // Validate data types before insertion
                boolean isValid = true;
                for (int i = 0; i < rowData.size(); i++) {
                    if (!validateDataType(rowData.get(i), columnDataTypes.get(i))) {
                        editTexts.get(i).setError("Invalid data type");
                        isValid = false;
                    }
                }

                if (isValid) {
                    new InsertRowTask().execute(rowData);
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

    private boolean validateDataType(String value, String dataType) {
        // Validate data type based on the expected type
        // For simplicity, we'll perform basic validation for demonstration purposes

        // Validate INTEGER data type
        if (dataType.equalsIgnoreCase("INTEGER")) {
            try {
                Integer.parseInt(value);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        if (dataType.equalsIgnoreCase("REAL")) {
            try {
                Float.parseFloat(value);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }

        if (dataType.equalsIgnoreCase("BLOB")) {
            // BLOB data type can be validated by checking if it represents binary data
            // For simplicity, we'll assume any non-empty value is valid
            return !value.isEmpty();
        }
        // Validate TEXT data type
        if (dataType.equalsIgnoreCase("TEXT")) {
            // No validation needed for TEXT type
            return true;
        }


        // Add more validations for other data types as needed

        // Unknown data type, return false
        return false;
    }



    private void showDeleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Row");

        // Create an EditText to input row ID to delete
        final EditText editTextRowId = new EditText(this);
        editTextRowId.setHint("Type Name to be Deleted");
        builder.setView(editTextRowId);

        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int rowId = Integer.parseInt(editTextRowId.getText().toString());
                new DeleteRowTask().execute(rowId);
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

    private class InsertRowTask extends AsyncTask<List<String>, Void, Boolean> {
        @Override
        protected Boolean doInBackground(List<String>... lists) {
            try {
                // Perform the upsert operation
                appDatabase.upsertRow(tableName, columnNames, appDatabase.getTableColumnTypes(tableName), lists[0]);
                return true; // Operation successful
            } catch (Exception e) {
                e.printStackTrace();
                return false; // Operation failed
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                // Check if data was inserted or updated
                if (appDatabase.isDataInserted()) {
                    // Data was inserted
                    Toast.makeText(TableDetailActivity.this, "Row inserted successfully", Toast.LENGTH_SHORT).show();
                } else {
                    // Data was updated
                    Toast.makeText(TableDetailActivity.this, "Row updated successfully", Toast.LENGTH_SHORT).show();
                }
                // Reload table data to reflect changes
                new LoadTableDataTask().execute();
            } else {
                Toast.makeText(TableDetailActivity.this, "Error inserting/updating row", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private class DeleteRowTask extends AsyncTask<Integer, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Integer... integers) {
            try {
                appDatabase.deleteRow(tableName, integers[0]);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                new LoadTableDataTask().execute(); // Reload table data to reflect changes
                Toast.makeText(TableDetailActivity.this, "Row deleted successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(TableDetailActivity.this, "Error deleting row", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
