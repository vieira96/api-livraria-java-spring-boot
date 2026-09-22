package com.vieira96.libraryapi.integration.notification;

import com.vieira96.libraryapi.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.SliceImpl;

import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NotificationPublisher publisher;

    @BeforeEach
    void setUp() throws Exception {
        // Sem Spring aqui: @Value não é injetado, então preenchemos via reflection.
        // A chamada direta executa de forma síncrona (o @Async só vale via proxy),
        // o que deixa o teste determinístico.
        setField("notificationsQueue", "notifications.book-created");
        setField("chunkSize", 2);
    }

    @Test
    void shouldPublishOneMessagePerChunk() {
        UUID bookId = UUID.randomUUID();
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        UUID third = UUID.randomUUID();

        when(userRepository.findIdsBy(PageRequest.of(0, 2)))
                .thenReturn(new SliceImpl<>(List.of(first, second), PageRequest.of(0, 2), true));
        when(userRepository.findIdsBy(PageRequest.of(1, 2)))
                .thenReturn(new SliceImpl<>(List.of(third), PageRequest.of(1, 2), false));

        publisher.publishBookCreated(bookId, "Dom Casmurro");

        ArgumentCaptor<BookCreatedEvent> eventCaptor = ArgumentCaptor.forClass(BookCreatedEvent.class);
        verify(rabbitTemplate, times(2))
                .convertAndSend(eq("notifications.book-created"), eventCaptor.capture());

        List<BookCreatedEvent> events = eventCaptor.getAllValues();
        assertThat(events).allSatisfy(event -> {
            assertThat(event.pattern()).isEqualTo("book.created");
            assertThat(event.data().bookId()).isEqualTo(bookId);
            assertThat(event.data().title()).isEqualTo("Dom Casmurro");
        });
        assertThat(events.get(0).data().userIds()).containsExactly(first, second);
        assertThat(events.get(1).data().userIds()).containsExactly(third);
    }

    @Test
    void shouldSkipPublishWhenThereAreNoUsers() {
        when(userRepository.findIdsBy(any()))
                .thenReturn(new SliceImpl<>(List.of(), PageRequest.of(0, 2), false));

        publisher.publishBookCreated(UUID.randomUUID(), "Dom Casmurro");

        verifyNoInteractions(rabbitTemplate);
    }

    @Test
    void shouldNotPropagateBrokerFailure() {
        UUID first = UUID.randomUUID();
        when(userRepository.findIdsBy(PageRequest.of(0, 2)))
                .thenReturn(new SliceImpl<>(List.of(first), PageRequest.of(0, 2), false));
        doThrow(new AmqpConnectException(new Exception("broker fora do ar")))
                .when(rabbitTemplate).convertAndSend(any(String.class), any(Object.class));

        assertThatNoException()
                .isThrownBy(() -> publisher.publishBookCreated(UUID.randomUUID(), "Dom Casmurro"));
    }

    private void setField(String name, Object value) throws Exception {
        Field field = NotificationPublisher.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(publisher, value);
    }
}
