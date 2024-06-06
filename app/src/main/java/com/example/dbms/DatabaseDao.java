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

    // Add method to retrieve tables specific to a database
    @Query("SELECT * FROM tables WHERE databaseName = :databaseName")
    LiveData<List<TableEntity>> getTablesForDatabase(String databaseName);

    @Query("SELECT * FROM databases WHERE name = :name LIMIT 1")
    DatabaseEntity getDatabaseByName(String name);

    @Insert
    void insertTable(TableEntity table);
}
