package com.cursojava.libraryapi.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class DatabaseConfiguration {

    @Bean
    public DataSource dataSource(
            @Value("${DB_URL}") String url,
            @Value("${DB_USERNAME}") String username,
            @Value("${DB_PASSWORD}") String password
    ) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName("org.postgresql.Driver");

        config.setMaximumPoolSize(10);
        config.setMinimumIdle(1);
        config.setPoolName("library-db-pool");
        config.setMaxLifetime(600_000);
        config.setIdleTimeout(300_000);
        config.setConnectionTimeout(100_000);
        config.setConnectionTestQuery("select 1");

        return new HikariDataSource(config);
    }
}
