package io.kontur.insightsapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableSwagger2
@EnableAsync
@EnableRetry
public class InsightsApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(InsightsApiApplication.class, args);
    }

}
