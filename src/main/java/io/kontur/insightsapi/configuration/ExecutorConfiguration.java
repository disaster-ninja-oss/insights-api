package io.kontur.insightsapi.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class ExecutorConfiguration {
    @Bean
    public ThreadPoolExecutor uploadExecutor() {
        int corePoolSize = 100;
        int maxPoolSize = 150;
        int maxQueueSize = 500;
        return new ThreadPoolExecutor(corePoolSize, maxPoolSize, 60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(maxQueueSize));
    }

    @Bean
    public ThreadPoolExecutor deleteExecutor() {
        int corePoolSize = 10;
        int maxPoolSize = 20;
        int maxQueueSize = 500;
        return new ThreadPoolExecutor(corePoolSize, maxPoolSize, 60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(maxQueueSize));
    }

    @Bean
    public ThreadPoolExecutor calculationExecutor() {
        int corePoolSize = 10;
        int maxPoolSize = 20;
        int maxQueueSize = 500;
        return new ThreadPoolExecutor(corePoolSize, maxPoolSize, 60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(maxQueueSize));
    }
}
