package io.kontur.insightsapi.exception;

public class CsvFileUploadException extends Exception {

    public CsvFileUploadException(String errorMessage) {
        super(errorMessage);
    }

    public CsvFileUploadException(String errorMessage, Throwable err) {
        super(errorMessage, err);
    }
}
