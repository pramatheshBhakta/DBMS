package com.example.dbms;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "database_name";
    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Initial table creation can be handled here if needed
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Handle database upgrade if needed
    }

    public void createTable(String tableName, String columnDefinitions) {
        SQLiteDatabase db = this.getWritableDatabase();
        String createTableSQL = "CREATE TABLE IF NOT EXISTS " + tableName + " (" + columnDefinitions + ")";
        db.execSQL(createTableSQL);
    }
}
