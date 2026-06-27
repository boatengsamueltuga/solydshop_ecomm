package com.solydshop.ecommerce.service;

import com.solydshop.ecommerce.payload.response.NotificationDTO;

import java.util.List;

public interface NotificationService {
    List<NotificationDTO> getNotifications(Long userId);
    long getUnreadCount(Long userId);
    void markRead(Long notificationId, Long userId);
    void markAllRead(Long userId);
    void createForUser(Long userId, String title, String message, String type);
}
