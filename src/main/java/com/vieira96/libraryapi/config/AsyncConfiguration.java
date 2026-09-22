package com.vieira96.libraryapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
@EnableAsync
public class AsyncConfiguration {

    @Bean("notificationExecutor")
    public Executor notificationExecutor() {
        // Uma virtual thread por tarefa: ideal para I/O bloqueante (JPA + AMQP).
        // Sem pool/fila — virtual threads são descartáveis, sem CallerRuns.
        return Executors.newThreadPerTaskExecutor(
                Thread.ofVirtual().name("notif-", 0).factory());
    }
}
