package com.example.dbms;


public class TableItem {

    public void setName(String name) {
        this.name = name;
    }



    private String name;
    private int columnCount;

    public TableItem(String name, int columnCount) {
        this.name = name;
        this.columnCount = columnCount;
    }

    public String getName() {
        return name;
    }

    public int getColumnCount() {
        return columnCount;
    }
}
