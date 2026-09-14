package com.solydshop.ecommerce.service;

import com.solydshop.ecommerce.payload.response.NotificationDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The notification service now talks to an external HTTP service (see
 * https://github.com/boatengsamueltuga/solydshop-notifications). Rather than
 * mock that call, these tests point at an address nothing is listening on to
 * force every call to fail - and verify the one behavior that matters most
 * about this extraction: a failure here must never surface as an exception
 * to a caller (an order placement, a product moderation action, ...), it
 * must be swallowed/degraded instead.
 */
class NotificationServiceImplTest {

    private NotificationServiceImpl service;

    @BeforeEach
    void setUp() {
        // Port 1 is a reserved, never-listening port - every call fails fast.
        service = new NotificationServiceImpl("http://localhost:1", "test-key");
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
    void createForUser_onFailure_doesNotThrow() {
        assertDoesNotThrow(() -> service.createForUser(1L, "Title", "Message", "TYPE"));
        assertDoesNotThrow(() -> service.createForUser(1L, "Title", "Message", "TYPE", 5L));
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
}
