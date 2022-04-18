package io.kontur.insightsapi.exception;

public class EmptySqlQueryAnswer extends RuntimeException {

    public EmptySqlQueryAnswer() {
        super("Sql query answer is empty");
    }
}
