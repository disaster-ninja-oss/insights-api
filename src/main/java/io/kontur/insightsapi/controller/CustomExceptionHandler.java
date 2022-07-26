package io.kontur.insightsapi.controller;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class CustomExceptionHandler extends ResponseEntityExceptionHandler {

    private final Counter clientAbortExceptionCounter;

    CustomExceptionHandler(MeterRegistry meterRegistry){
        clientAbortExceptionCounter = Counter
                .builder("client_abort_exceptions")
                .register(meterRegistry);
    }

    @ExceptionHandler(org.apache.catalina.connector.ClientAbortException.class)
    public void clientAbortExceptionHandler(org.apache.catalina.connector.ClientAbortException ex, WebRequest request){
        clientAbortExceptionCounter.increment();
    }
}
