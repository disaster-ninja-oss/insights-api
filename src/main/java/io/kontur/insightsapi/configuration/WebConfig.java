package io.kontur.insightsapi.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(apiInfo())
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }

    private Info apiInfo() {
        return new Info()
                .title("Insights API")
                .description("Technical API for Insights service. Deprecated HTTP API for population provided by Kontur")
                .version("0.1")
                .termsOfService("https://www.kontur.io/about/#footer")
                .contact(new Contact().name("Kontur").url("http://kontur.io").email("hello@kontur.io"))
                .license(new License().name("License of API").url("https://www.kontur.io/about/#footer"));
    }
}
