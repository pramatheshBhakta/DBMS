package com.example.dbms;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tables")
public class TableEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;

    public int columnCount;
}
