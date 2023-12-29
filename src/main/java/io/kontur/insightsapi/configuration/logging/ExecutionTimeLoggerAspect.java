package io.kontur.insightsapi.configuration.logging;


import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ExecutionTimeLoggerAspect {
    static final Logger logger = LoggerFactory.getLogger(ExecutionTimeLoggerAspect.class);

    @Around("@annotation(ExecutionTimeLogger)")
    public Object executionTimeLogger(ProceedingJoinPoint joinPoint) {
        try {
            long startTime = System.currentTimeMillis();
            Object proceed = joinPoint.proceed();
            long executionTime = (System.currentTimeMillis() - startTime);
            logger.info("{} {} method was executed in {} milliseconds", joinPoint.getSignature(), joinPoint.getArgs(), executionTime);
            return proceed;
        } catch (Throwable e) {
            logger.error("There was an error while calculating method execution time for {}", joinPoint.getSignature(), e);
            return null;
        }
    }

    @Around("@annotation(DebugExecutionTimeLogger)")
    public Object executionTimeDebugLogger(ProceedingJoinPoint joinPoint) {
        try {
            long startTime = System.currentTimeMillis();
            Object proceed = joinPoint.proceed();
            long executionTime = (System.currentTimeMillis() - startTime);
            logger.debug("{} {} method was executed in {} milliseconds", joinPoint.getSignature(), joinPoint.getArgs(), executionTime);
            return proceed;
        } catch (Throwable e) {
            logger.debug("There was an error while calculating method execution time for {}", joinPoint.getSignature(), e);
            return null;
        }
    }
}
