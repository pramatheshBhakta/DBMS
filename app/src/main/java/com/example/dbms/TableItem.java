package com.example.dbms;


public class TableItem {
    private String tableName;
    private int columnCount;

    public TableItem(String tableName, int columnCount) {
        this.tableName = tableName;
        this.columnCount = columnCount;
    }

    public String getTableName() {
        return tableName;
    }

    public int getColumnCount() {
        return columnCount;
    }


}
