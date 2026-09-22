package com.vieira96.libraryapi.support;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Lifecycle dos containers é gerenciado pelo Ryuk (sidecar do Testcontainers).
 * Ele faz cleanup automático quando a JVM encerra.
 */
@SuppressWarnings("resource")
public abstract class IntegrationTestContainer {

    private static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer(DockerImageName.parse("postgres:16.3"))
            .withDatabaseName("libraryapi_test")
            .withUsername("libraryapi")
            .withPassword("libraryapi");

    private static final GenericContainer<?> REDIS = new GenericContainer<>(DockerImageName.parse("redis:7.4-alpine"))
            .withExposedPorts(6379);

    private static final RabbitMQContainer RABBITMQ =
            new RabbitMQContainer(DockerImageName.parse("rabbitmq:4-management-alpine"));

    static {
        POSTGRES.start();
        REDIS.start();
        RABBITMQ.start();
    }

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("DB_URL", POSTGRES::getJdbcUrl);
        registry.add("DB_USERNAME", POSTGRES::getUsername);
        registry.add("DB_PASSWORD", POSTGRES::getPassword);
        registry.add("JWT_SECRET", () -> "integration-test-jwt-secret-with-32-bytes");
        registry.add("LOGIN_ATTEMPTS_ENABLED", () -> "false");
        registry.add("REDIS_HOST", REDIS::getHost);
        registry.add("REDIS_PORT", () -> REDIS.getMappedPort(6379));
        registry.add("RABBITMQ_HOST", RABBITMQ::getHost);
        registry.add("RABBITMQ_PORT", RABBITMQ::getAmqpPort);
        registry.add("RABBITMQ_USER", RABBITMQ::getAdminUsername);
        registry.add("RABBITMQ_PASSWORD", RABBITMQ::getAdminPassword);
    }
}
