package io.kontur.insightsapi.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.scalars.ExtendedScalars;
import graphql.schema.GraphQLScalarType;
import io.kontur.insightsapi.model.JacksonJsonCoercing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GraphQLConfig {

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
