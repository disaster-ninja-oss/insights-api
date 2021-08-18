package io.kontur.insightsapi.configuration;

import graphql.Scalars;
import graphql.schema.GraphQLScalarType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GraphQLConfig {

    @Bean
    public GraphQLScalarType longType() {
        return Scalars.GraphQLLong;
    }
}
