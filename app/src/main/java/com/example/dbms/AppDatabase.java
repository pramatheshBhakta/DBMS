package com.example.dbms;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.util.Log;
import android.widget.Toast;

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
        position = position - 1; // Adjust position to match list index

        // Get the primary key column name
        String primaryKeyColumn = "ROWID"; // Replace with the actual primary key column name

        // Get the primary key value of the row to delete
        String primaryKeyValue = getPrimaryKeyValue(tableName, primaryKeyColumn, position);

        // Check for foreign key constraints


        // If no foreign key constraints, proceed with deletion
        SupportSQLiteDatabase db = getOpenHelper().getWritableDatabase();

        // Construct the DELETE SQL query using the primary key column
        String sql = "DELETE FROM " + tableName + " WHERE " + primaryKeyColumn + " IN (SELECT " + primaryKeyColumn + " FROM " + tableName + " LIMIT 1 OFFSET " + position + ")";

        // Execute the SQL query to delete the row at the specified position
        db.execSQL(sql);
    }

    public String getPrimaryKeyValue(String tableName, String primaryKeyColumn, int position) {
        SupportSQLiteDatabase db = getOpenHelper().getReadableDatabase();

        // Query to retrieve the primary key value at the specified position
        String sql = "SELECT " + primaryKeyColumn + " FROM " + tableName + " LIMIT 1 OFFSET " + position;
        String primaryKeyValue = null;

        Cursor cursor = null;
        try {
            cursor = db.query(sql, null); // Use query method to execute the SQL and retrieve cursor
            if (cursor != null && cursor.moveToFirst()) {
                primaryKeyValue = cursor.getString(cursor.getColumnIndexOrThrow(primaryKeyColumn));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return primaryKeyValue;
    }






    public InsertResult upsertRow(String tableName, List<String> columnNames, List<String> columnDataTypes, List<String> rowData, List<String> foreignKeyInfo) {
        SupportSQLiteDatabase db = getOpenHelper().getWritableDatabase();
        ContentValues values = new ContentValues();

        // Validate data types before insertion or update
        for (int i = 0; i < columnNames.size(); i++) {
            String columnName = columnNames.get(i);
            String dataType = columnDataTypes.get(i);
            String value = rowData.get(i);

            // Validate value against data type
            if (validateDataType(value, dataType)) {
                // Value matches data type, insert it into ContentValues
                values.put(columnName, value);
            } else {
                Log.e("AppDatabase", "Invalid value for column " + columnName + ": " + value);
                return new InsertResult(false, "Invalid value for column " + columnName, false);
            }
        }

        // Validate foreign keys before insertion or update
        for (String foreignKey : foreignKeyInfo) {
            String[] parts = foreignKey.split("\\.");
            String refTable = parts[0];
            String refColumn = parts[1];
            String fkValue = rowData.get(columnNames.indexOf(refColumn));
            if (!validateForeignKey(refTable, refColumn, fkValue)) {
                Log.e("AppDatabase", "Foreign key constraint failed for column " + refColumn + ": " + fkValue);
                return new InsertResult(false, "Foreign key constraint failed for column " + refColumn, false);
            }
        }

        // Check if the row with the given primary key already exists
        String primaryKeyColumnName = columnNames.get(0); // Assuming the first column is the primary key
        String primaryKeyValue = rowData.get(0); // Assuming the primary key value is in the first column
        Cursor cursor = db.query("SELECT * FROM " + tableName + " WHERE " + primaryKeyColumnName + " = ?", new String[]{primaryKeyValue});
        try {
            if (cursor != null && cursor.moveToFirst()) {
                // Row with the given primary key exists, update it
                db.update(tableName, 0, values, primaryKeyColumnName + " = ?", new String[]{primaryKeyValue});
                return new InsertResult(true, "Updation Success", false);
            } else {
                // Row with the given primary key does not exist, insert it
                db.insert(tableName, 0, values);
                return new InsertResult(true, "Insertion Success", true);
            }
        } catch (SQLiteConstraintException e) {
            Log.e("AppDatabase", "SQLiteConstraintException: " + e.getMessage());
            return new InsertResult(false, "SQLiteConstraintException: " + e.getMessage(), false);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }




    public boolean validateForeignKey(String tableName, String columnName, String value) {
        SupportSQLiteDatabase db = getOpenHelper().getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + tableName + " WHERE " + columnName + " = ?";
        Cursor cursor = db.query(query, new String[]{value});
        boolean exists = false;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                exists = cursor.getInt(0) > 0;
            }
            cursor.close();
        }
        return exists;
    }

    public List<String> getForeignKeyInfo(String tableName) {
        List<String> foreignKeyInfo = new ArrayList<>();
        SupportSQLiteDatabase db = getOpenHelper().getReadableDatabase();

        // Query to get foreign key constraints from the specified table
        Cursor cursor = db.query("PRAGMA foreign_key_list(" + tableName + ")");
        if (cursor.moveToFirst()) {
            do {
                // Get the referenced table and column
                String referencedTable = cursor.getString(cursor.getColumnIndexOrThrow("table"));
                String referencedColumn = cursor.getString(cursor.getColumnIndexOrThrow("to"));
                foreignKeyInfo.add(referencedTable + "." + referencedColumn);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return foreignKeyInfo;
    }



}




