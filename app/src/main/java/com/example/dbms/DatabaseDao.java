package com.example.dbms;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.room.Update;
import androidx.sqlite.db.SupportSQLiteQuery;

import java.util.List;
@Dao
public interface DatabaseDao {

    @Insert
    void insert(DatabaseEntity database);



    @Query("DELETE FROM databases WHERE name = :name")
    void deleteDatabaseByName(String name);

    @Query("SELECT * FROM databases")
    LiveData<List<DatabaseEntity>> getAllDatabases();


    @Query("SELECT * FROM tables WHERE databaseName = :databaseName")
    LiveData<List<TableEntity>> getTablesForDatabase(String databaseName);

    @Query("SELECT * FROM databases WHERE name = :name LIMIT 1")
    DatabaseEntity getDatabaseByName(String name);

    @Insert
    void insertTable(TableEntity table);

    @Query("DELETE FROM tables WHERE name = :tableName AND databaseName = :databaseName")
    void deleteTable(String tableName, String databaseName);


    @Query("SELECT name FROM sqlite_master WHERE type='table' AND name != 'android_metadata'")
    List<String> getAllTables();



}
