package com.example.dbms;

public class DatabaseCard {

    private String name;
    private String date;

    public DatabaseCard(String name, String date) {
        this.name = name;
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public String getDate() {
        return date;
    }
}
