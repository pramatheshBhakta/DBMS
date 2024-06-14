package com.example.dbms;

public class InsertResult {
    private final boolean success;
    private final String errorMessage;
    private final boolean rowInserted;

    public InsertResult(boolean success, String errorMessage, boolean rowInserted) {
        this.success = success;
        this.errorMessage = errorMessage;
        this.rowInserted = rowInserted;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public boolean isRowInserted() {
        return rowInserted;
    }
}
