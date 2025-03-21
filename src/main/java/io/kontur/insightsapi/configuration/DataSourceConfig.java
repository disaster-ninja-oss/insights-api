package io.kontur.insightsapi.configuration;

import javax.sql.DataSource;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class DataSourceConfig {

    @Value("${spring.datasource.primary-url}")
    private String primaryDatasourceUrl;

    @Value("${spring.datasource.secondary-url}")
    private String secondaryDatasourceUrl;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Bean(name = "writeDataSource")
    @LiquibaseDataSource
    public DataSource writeDataSource() {
        return DataSourceBuilder.create()
                .url(primaryDatasourceUrl)
                .username(dbUsername)
                .password(dbPassword)
                .build();
    }

    @Primary
    @Bean(name = "readDataSource")
    public DataSource readDataSource() {
        return DataSourceBuilder.create()
                .url(secondaryDatasourceUrl)
                .username(dbUsername)
                .password(dbPassword)
                .build();
    }
}
