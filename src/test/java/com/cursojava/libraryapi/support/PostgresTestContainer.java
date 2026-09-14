package com.cursojava.libraryapi.support;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Compartilha um PostgreSQL efêmero entre os testes da mesma JVM.
 * O Ryuk, do Testcontainers, remove o container quando a suíte termina.
 */
public abstract class PostgresTestContainer {

    private static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer(DockerImageName.parse("postgres:16.3"))
            .withDatabaseName("libraryapi_test")
            .withUsername("libraryapi")
            .withPassword("libraryapi");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry registry) {
        registry.add("DB_URL", POSTGRES::getJdbcUrl);
        registry.add("DB_USERNAME", POSTGRES::getUsername);
        registry.add("DB_PASSWORD", POSTGRES::getPassword);
    }
}
