package io.kontur.insightsapi.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("Insights API")
                .description("Technical API for Insights service. Deprecated HTTP API for population provided by Kontur"))
                .info(apiInfo());
    }

    private Info apiInfo() {
        return new Info()
                .title("Insights API")
                .description("Technical API for Insights service. Deprecated HTTP API for population provided by Kontur")
                .version("0.1")
                .termsOfService("https://www.kontur.io/about/#contact")
                .contact(new Contact().name("Kontur").url("http://kontur.io").email("hello@kontur.io"))
                .license(new License().name("License of API").url("https://www.kontur.io/about/#contact"));
    }
}
