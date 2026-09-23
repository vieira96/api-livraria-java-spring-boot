package com.vieira96.libraryapi.integration.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${notifications.queue}")
    private String notificationsQueue;

    @Async("notificationExecutor")
    public void publishBookCreated(UUID bookId, String title) {
        try {
            rabbitTemplate.convertAndSend(notificationsQueue, BookCreatedEvent.of(bookId, title));
            log.info("Evento book.created publicado: bookId={}", bookId);
        } catch (Exception e) {
            log.warn("Falha ao publicar book.created: bookId={}", bookId, e);
        }
    }
}
