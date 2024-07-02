package io.kontur.insightsapi.service.cacheable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * Annotation indicating that the method should acquire a lock
 * in Redis to prevent concurrent execution of long SQL queries when parallel
 * graphql queries request the same operation -- see task #18808.
 * This annotation serves as a replacement for the {@code sync=true} behavior
 * in Spring @Cacheable, ensuring synchronized access across multiple insights-api pods. 
 * @RedisLock only works in pair with @Cacheable, because it takes keyGenerator from it
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RedisLock {

    /**
     * Specifies the expiration time for the Redis lock key.
     * Defaults to 20 minutes, chosen as a trade-off between:
     *   - pessimistically, how long the longest SQL queries are expected to run?
     *   - if java crashes, how long we can afford to hold the lock that no one can release?
     */
    long timeout() default 20;
    TimeUnit timeUnit() default TimeUnit.MINUTES;
}
