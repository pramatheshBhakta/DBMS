package com.example.dbms;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface DatabaseDao {

    @Insert
    void insert(DatabaseEntity database);

    @Query("DELETE FROM databases")
    void deleteAllDatabases();

    @Query("DELETE FROM databases WHERE name = :name")
    void deleteDatabaseByName(String name);

    @Query("SELECT * FROM databases")
    LiveData<List<DatabaseEntity>> getAllDatabases();

    // Add method to retrieve all tables
    @Query("SELECT * FROM tables")
    LiveData<List<TableEntity>> getAllTables();

    @Query("SELECT * FROM databases WHERE name = :name LIMIT 1")
    DatabaseEntity getDatabaseByName(String name);

    @Query("CREATE TABLE IF NOT EXISTS :tableName (:columnDefinition)")
    void createTable(String tableName, String columnDefinition);

}
