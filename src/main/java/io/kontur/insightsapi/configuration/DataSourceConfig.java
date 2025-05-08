package io.kontur.insightsapi.configuration;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class DataSourceConfig {

    @Value("${spring.datasource.leader-url}")
    private String leaderUrl;

    @Value("${spring.datasource.replica-url}")
    private String replicaUrl;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.hikari.connection-timeout}")
    private long connectionTimeout;

    @Value("${spring.datasource.hikari.validation-timeout}")
    private long validationTimeout;

    @Value("${spring.datasource.hikari.maximum-pool-size}")
    private int maximumPoolSize;

    @Value("${spring.datasource.hikari.register-mbeans}")
    private boolean registerMbeans;

    private HikariConfig createHikariConfig(String url, int poolSize) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(dbUsername);
        config.setPassword(dbPassword);
        config.setConnectionTimeout(connectionTimeout);
        config.setValidationTimeout(validationTimeout);
        config.setMaximumPoolSize(poolSize);
        config.setRegisterMbeans(registerMbeans);
        return config;
    }

    @Bean(name = "writeDataSource")
    @LiquibaseDataSource
    public DataSource writeDataSource() {
        HikariConfig config = createHikariConfig(leaderUrl, maximumPoolSize / 3);  // don't need many write connections
        return new HikariDataSource(config);
    }

    // most insights-api queries are only reading the data, so RO connection is default
    @Primary
    @Bean(name = "readDataSource")
    public DataSource readDataSource() {
        HikariConfig config = createHikariConfig(replicaUrl, maximumPoolSize);
        return new HikariDataSource(config);
    }
}
