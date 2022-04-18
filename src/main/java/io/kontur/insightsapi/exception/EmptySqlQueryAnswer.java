package io.kontur.insightsapi.exception;

public class EmptySqlQueryAnswer extends RuntimeException {

    public EmptySqlQueryAnswer(String message) {
        super(message);
    }
}
