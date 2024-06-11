package com.example.dbms;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {DatabaseEntity.class, TableEntity.class}, version = 2, exportSchema = false)
@TypeConverters(DateConverter.class)
public abstract class AppDatabase extends RoomDatabase {
    private boolean isDataInserted = false;
    public abstract DatabaseDao databaseDao();

    private static volatile AppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "app_database.db")
                            .fallbackToDestructiveMigration() // Enable destructive migrations
                            .build();
                }
            }
        }
        return INSTANCE;
    }
    public List<String> getTableColumnTypes(String tableName) {
        SupportSQLiteDatabase db = getOpenHelper().getReadableDatabase();
        List<String> columnTypes = new ArrayList<>();

        Cursor cursor = db.query("PRAGMA table_info(" + tableName + ")");
        if (cursor.moveToFirst()) {
            do {
                columnTypes.add(cursor.getString(cursor.getColumnIndexOrThrow("type")));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return columnTypes;
    }

    public boolean validateDataType(String value, String dataType) {
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

        // Validate TEXT data type
        if (dataType.equalsIgnoreCase("TEXT")) {
            // No validation needed for TEXT type
            return true;
        }
        if (dataType.equalsIgnoreCase("REAL")) {
            try {
                Float.parseFloat(value);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        // Add more validations for other data types as needed

        // Unknown data type, return false
        return false;
    }


    public List<List<String>> getTableData(String tableName) {
        SupportSQLiteDatabase db = getOpenHelper().getReadableDatabase();
        List<List<String>> data = new ArrayList<>();

        Cursor cursor = db.query("SELECT * FROM " + tableName);
        if (cursor.moveToFirst()) {
            int columnCount = cursor.getColumnCount();
            do {
                List<String> row = new ArrayList<>();
                for (int i = 0; i < columnCount; i++) {
                    row.add(cursor.getString(i));
                }
                data.add(row);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return data;
    }

    public List<String> getTableColumns(String tableName) {
        SupportSQLiteDatabase db = getOpenHelper().getReadableDatabase();
        List<String> columnNames = new ArrayList<>();

        Cursor cursor = db.query("PRAGMA table_info(" + tableName + ")");
        if (cursor.moveToFirst()) {
            do {
                columnNames.add(cursor.getString(cursor.getColumnIndexOrThrow("name")));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return columnNames;
    }

    public void deleteRow(String tableName, int position) {
        position=position-1;
        SupportSQLiteDatabase db = getOpenHelper().getWritableDatabase();
        String primaryKeyColumn = "ROWID"; // Assuming the primary key column is ROWID, replace it with the actual primary key column name

        // Construct the DELETE SQL query using the primary key column
        String sql = "DELETE FROM " + tableName + " WHERE " + primaryKeyColumn + " IN (SELECT " + primaryKeyColumn + " FROM " + tableName + " LIMIT 1 OFFSET " + position + ")";

        // Execute the SQL query to delete the row at the specified position
        db.execSQL(sql);
    }

    public void upsertRow(String tableName, List<String> columnNames, List<String> columnDataTypes, List<String> rowData) {
        SupportSQLiteDatabase db = getOpenHelper().getWritableDatabase();
        ContentValues values = new ContentValues();

        // Validate data types before insertion
        for (int i = 0; i < columnNames.size(); i++) {
            String columnName = columnNames.get(i);
            String dataType = columnDataTypes.get(i);
            String value = rowData.get(i);

            // Validate value against data type
            if (validateDataType(value, dataType)) {
                // Value matches data type, insert it into ContentValues
                values.put(columnName, value);
            } else {
                // Value does not match data type, handle error (e.g., display a message)
                Log.e("AppDatabase", "Invalid value for column " + columnName + ": " + value);
                return; // Skip insertion for this row
            }
        }

        // Check if the row with the given primary key already exists
        String primaryKeyColumnName = columnNames.get(0); // Assuming the first column is the primary key
        String primaryKeyValue = rowData.get(0); // Assuming the primary key value is in the first column
        Cursor cursor = db.query("SELECT * FROM " + tableName + " WHERE " + primaryKeyColumnName + " = ?", new String[]{primaryKeyValue});
        if (cursor != null && cursor.moveToFirst()) {
            // Row with the given primary key exists, update it
            db.update(tableName, 0, values, primaryKeyColumnName + " = ?", new String[]{primaryKeyValue});
            isDataInserted = false; // Data was updated, not inserted
        } else {
            // Row with the given primary key does not exist, insert it
            db.insert(tableName, 0, values);
            isDataInserted = true; // Data was inserted, not updated
        }
        if (cursor != null) {
            cursor.close();
        }
    }

    public boolean isDataInserted() {
        return isDataInserted;
    }
}




