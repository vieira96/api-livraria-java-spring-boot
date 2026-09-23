package com.vieira96.libraryapi.integration.notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.lang.reflect.Field;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private NotificationPublisher publisher;

    @BeforeEach
    void setUp() throws Exception {
        // Sem Spring aqui: @Value não é injetado, então preenchemos via reflection.
        // A chamada direta executa de forma síncrona (o @Async só vale via proxy),
        // o que deixa o teste determinístico.
        Field field = NotificationPublisher.class.getDeclaredField("notificationsQueue");
        field.setAccessible(true);
        field.set(publisher, "notifications.book-created");
    }

    @Test
    void shouldPublishBookCreatedEvent() {
        UUID bookId = UUID.randomUUID();

        publisher.publishBookCreated(bookId, "Dom Casmurro");

        ArgumentCaptor<BookCreatedEvent> eventCaptor = ArgumentCaptor.forClass(BookCreatedEvent.class);
        verify(rabbitTemplate).convertAndSend(eq("notifications.book-created"), eventCaptor.capture());

        BookCreatedEvent event = eventCaptor.getValue();
        assertThat(event.pattern()).isEqualTo("book.created");
        assertThat(event.data().bookId()).isEqualTo(bookId);
        assertThat(event.data().title()).isEqualTo("Dom Casmurro");
    }

    @Test
    void shouldNotPropagateBrokerFailure() {
        doThrow(new AmqpConnectException(new Exception("broker fora do ar")))
                .when(rabbitTemplate).convertAndSend(any(String.class), any(Object.class));

        assertThatNoException()
                .isThrownBy(() -> publisher.publishBookCreated(UUID.randomUUID(), "Dom Casmurro"));
    }
}
