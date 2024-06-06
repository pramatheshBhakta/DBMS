package com.example.dbms;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {DatabaseEntity.class, TableEntity.class}, version = 2)
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

    public void insertRow(String tableName, List<String> rowData, List<String> columnNames) {
        SupportSQLiteDatabase db = getOpenHelper().getWritableDatabase();
        ContentValues values = new ContentValues();

        for (int i = 0; i < columnNames.size(); i++) {
            values.put(columnNames.get(i), rowData.get(i));
        }

        db.insert(tableName, 0, values);
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

    public void deleteRow(String tableName, int id) {
        SupportSQLiteDatabase db = getOpenHelper().getWritableDatabase();
        db.delete(tableName, "id = ?", new Object[]{id});
    }

    public void updateRow(String tableName, int id, List<String> rowData, List<String> columnNames) {
        SupportSQLiteDatabase db = getOpenHelper().getWritableDatabase();
        ContentValues values = new ContentValues();

        for (int i = 0; i < columnNames.size(); i++) {
            values.put(columnNames.get(i), rowData.get(i));
        }

        db.update(tableName, 0, values, "id = ?", new Object[]{id});
    }
}
