package com.example.dbms;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.util.Date;

@Entity(tableName = "databases")
public class DatabaseEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;

    public Date createdDate;
}
