package com.example.dbms;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tables")
public class TableEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private int columnCount;
    private String databaseName; // Add databaseName field

    public TableEntity(String name, int columnCount) {
        this.name = name;
        this.columnCount = columnCount;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getColumnCount() {
        return columnCount;
    }

    public void setColumnCount(int columnCount) {
        this.columnCount = columnCount;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }
}
