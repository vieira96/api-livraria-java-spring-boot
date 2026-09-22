package com.vieira96.libraryapi.integration.notification;

import com.vieira96.libraryapi.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final UserRepository userRepository;

    @Value("${notifications.queue}")
    private String notificationsQueue;

    @Value("${notifications.chunk-size}")
    private int chunkSize;

    @Async("notificationExecutor")
    public void publishBookCreated(UUID bookId, String title) {
        try {
            int page = 0;
            int totalRecipients = 0;
            Slice<UUID> userIds;
            do {
                userIds = userRepository.findIdsBy(PageRequest.of(page, chunkSize));
                if (userIds.hasContent()) {
                    rabbitTemplate.convertAndSend(
                            notificationsQueue,
                            BookCreatedEvent.of(bookId, title, userIds.getContent()));
                    totalRecipients += userIds.getNumberOfElements();
                }
                page++;
            } while (userIds.hasNext());

            if (totalRecipients == 0) {
                log.info("Nenhum usuário para notificar: bookId={}", bookId);
                return;
            }
            log.info("Evento book.created publicado: bookId={}, destinatarios={}, lotes={}",
                    bookId, totalRecipients, page);
        } catch (Exception e) {
            log.warn("Falha ao publicar book.created: bookId={}", bookId, e);
        }
    }
}
