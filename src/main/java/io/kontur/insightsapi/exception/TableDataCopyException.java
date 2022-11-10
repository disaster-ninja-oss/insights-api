package io.kontur.insightsapi.exception;

public class TableDataCopyException extends Exception {

    public TableDataCopyException(String errorMessage) {
        super(errorMessage);
    }

    public TableDataCopyException(Throwable err) {
        super(err);
    }

    public TableDataCopyException(String errorMessage, Throwable err) {
        super(errorMessage, err);
    }
}
