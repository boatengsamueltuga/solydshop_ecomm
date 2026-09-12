package com.solydshop.ecommerce.service;

import com.solydshop.ecommerce.payload.response.NotificationDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * Notifications were extracted into their own service (see
 * https://github.com/boatengsamueltuga/solydshop-notifications). This talks
 * to it over HTTP instead of a local repository, but keeps the same
 * interface so every caller (OrderServiceImpl, ProductServiceImpl, etc.) and
 * NotificationController are unaffected by the extraction.
 *
 * A notification failure must never break the real action it's attached to
 * (placing an order, approving a seller, ...), so writes are logged and
 * swallowed rather than propagated - matching the isolation the old
 * REQUIRES_NEW local-transaction implementation provided. Reads degrade to
 * an empty result on failure so the frontend's notification bell doesn't
 * 500 just because this service is briefly unavailable.
 */
@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final RestClient restClient;

    public NotificationServiceImpl(
            @Value("${notification.service.url}") String baseUrl,
            @Value("${internal.api-key}") String internalApiKey) {

        var requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(2000);
        requestFactory.setReadTimeout(3000);

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .defaultHeader("X-Internal-Api-Key", internalApiKey)
                .build();
    }

    @Override
    public List<NotificationDTO> getNotifications(Long userId) {
        try {
            return restClient.get()
                    .uri("/internal/notifications?userId={userId}", userId)
                    .retrieve()
                    .body(new org.springframework.core.ParameterizedTypeReference<List<NotificationDTO>>() {});
        } catch (Exception e) {
            log.error("Failed to fetch notifications for user {}: {}", userId, e.getMessage());
            return List.of();
        }
    }

    @Override
    public long getUnreadCount(Long userId) {
        try {
            Map<String, Long> response = restClient.get()
                    .uri("/internal/notifications/unread-count?userId={userId}", userId)
                    .retrieve()
                    .body(new org.springframework.core.ParameterizedTypeReference<Map<String, Long>>() {});
            return response == null ? 0 : response.getOrDefault("count", 0L);
        } catch (Exception e) {
            log.error("Failed to fetch unread count for user {}: {}", userId, e.getMessage());
            return 0;
        }
    }

    @Override
    public void markRead(Long notificationId, Long userId) {
        try {
            restClient.put()
                    .uri("/internal/notifications/{id}/read?userId={userId}", notificationId, userId)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.error("Failed to mark notification {} read for user {}: {}", notificationId, userId, e.getMessage());
        }
    }

    @Override
    public void markAllRead(Long userId) {
        try {
            restClient.put()
                    .uri("/internal/notifications/read-all?userId={userId}", userId)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.error("Failed to mark all notifications read for user {}: {}", userId, e.getMessage());
        }
    }

    @Override
    public void deleteOne(Long notificationId, Long userId) {
        try {
            restClient.delete()
                    .uri("/internal/notifications/{id}?userId={userId}", notificationId, userId)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.error("Failed to delete notification {} for user {}: {}", notificationId, userId, e.getMessage());
        }
    }

    @Override
    public void deleteAll(Long userId) {
        try {
            restClient.delete()
                    .uri("/internal/notifications/all?userId={userId}", userId)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.error("Failed to delete all notifications for user {}: {}", userId, e.getMessage());
        }
    }

    @Override
    public void createForUser(Long userId, String title, String message, String type) {
        createForUser(userId, title, message, type, null);
    }

    @Override
    public void createForUser(Long userId, String title, String message, String type, Long resourceId) {
        try {
            Map<String, Object> body = new java.util.HashMap<>();
            body.put("userId", userId);
            body.put("title", title);
            body.put("message", message);
            body.put("type", type);
            body.put("resourceId", resourceId);

            restClient.post()
                    .uri("/internal/notifications")
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.error("Failed to create notification for user {}: {}", userId, e.getMessage());
        }
    }
}
