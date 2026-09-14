package com.solydshop.ecommerce.service;

import com.solydshop.ecommerce.payload.response.NotificationDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Reads/updates still go over HTTP (see the class-level comment on
 * NotificationServiceImpl); those are tested here by pointing at an address
 * nothing is listening on to force every call to fail, verifying failures
 * degrade instead of throwing. Creation now publishes to RabbitMQ instead -
 * tested with a mocked RabbitTemplate so we can verify both the happy path
 * (correct exchange/routing key/payload) and that a broker failure is
 * swallowed the same way an HTTP failure was.
 */
@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    private static final String EXCHANGE = "notifications.exchange";
    private static final String ROUTING_KEY = "notification.created";

    @Mock private RabbitTemplate rabbitTemplate;

    private NotificationServiceImpl service;

    @BeforeEach
    void setUp() {
        // Port 1 is a reserved, never-listening port - every HTTP call fails fast.
        service = new NotificationServiceImpl(
                "http://localhost:1", "test-key", rabbitTemplate, EXCHANGE, ROUTING_KEY);
    }

    @Test
    void getNotifications_onFailure_returnsEmptyListInsteadOfThrowing() {
        List<NotificationDTO> result = service.getNotifications(1L);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getUnreadCount_onFailure_returnsZeroInsteadOfThrowing() {
        assertEquals(0, service.getUnreadCount(1L));
    }

    @Test
    void markRead_onFailure_doesNotThrow() {
        assertDoesNotThrow(() -> service.markRead(10L, 1L));
    }

    @Test
    void markAllRead_onFailure_doesNotThrow() {
        assertDoesNotThrow(() -> service.markAllRead(1L));
    }

    @Test
    void deleteOne_onFailure_doesNotThrow() {
        assertDoesNotThrow(() -> service.deleteOne(10L, 1L));
    }

    @Test
    void deleteAll_onFailure_doesNotThrow() {
        assertDoesNotThrow(() -> service.deleteAll(1L));
    }

    @Test
    @SuppressWarnings("unchecked")
    void createForUser_publishesToCorrectExchangeAndRoutingKey() {
        service.createForUser(1L, "Title", "Message", "TYPE", 5L);

        ArgumentCaptor<Map<String, Object>> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(rabbitTemplate).convertAndSend(eq(EXCHANGE), eq(ROUTING_KEY), bodyCaptor.capture());

        Map<String, Object> body = bodyCaptor.getValue();
        assertEquals(1L, body.get("userId"));
        assertEquals("Title", body.get("title"));
        assertEquals("Message", body.get("message"));
        assertEquals("TYPE", body.get("type"));
        assertEquals(5L, body.get("resourceId"));
    }

    @Test
    void createForUser_withoutResourceId_publishesNullResourceId() {
        service.createForUser(1L, "Title", "Message", "TYPE");

        verify(rabbitTemplate).convertAndSend(eq(EXCHANGE), eq(ROUTING_KEY), argThat((Map<?, ?> body) ->
                body.get("resourceId") == null));
    }

    @Test
    void createForUser_onBrokerFailure_doesNotThrow() {
        doThrow(new AmqpException("broker unreachable"))
                .when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class));

        assertDoesNotThrow(() -> service.createForUser(1L, "Title", "Message", "TYPE", 5L));
    }
}
