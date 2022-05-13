package io.kontur.insightsapi.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.parser.ParserOptions;
import graphql.scalars.ExtendedScalars;
import graphql.schema.GraphQLScalarType;
import io.kontur.insightsapi.model.JacksonJsonCoercing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class GraphQLConfig {

    @Value("${graphql-request.parser-options.max-tokens}")
    Integer maxTokens;

    @PostConstruct
    public void init() {
        ParserOptions.setDefaultParserOptions(ParserOptions.newParserOptions()
                .maxTokens(maxTokens).build());
    }

    @Bean
    public GraphQLScalarType longType() {
        return ExtendedScalars.GraphQLLong;
    }

    @Bean
    public GraphQLScalarType jsonType(@Autowired ObjectMapper objectMapper) {
        return GraphQLScalarType.newScalar()
                .name("GeoJSON")
                .description("A custom scalar that handles geojson")
                .coercing(new JacksonJsonCoercing(objectMapper))
                .build();
    }
}
