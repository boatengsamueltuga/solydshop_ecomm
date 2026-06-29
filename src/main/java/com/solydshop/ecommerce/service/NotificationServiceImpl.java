package com.solydshop.ecommerce.service;

import com.solydshop.ecommerce.entity.Notification;
import com.solydshop.ecommerce.exception.ResourceNotFoundException;
import com.solydshop.ecommerce.payload.response.NotificationDTO;
import com.solydshop.ecommerce.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public List<NotificationDTO> getNotifications(Long userId) {
        return notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .limit(30)
                .map(this::toDTO)
                .toList();
    }

    @Override
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    @Override
    @Transactional
    public void markRead(Long notificationId, Long userId) {
        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        if (!n.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Notification not found");
        }
        n.setRead(true);
        notificationRepository.save(n);
    }

    @Override
    @Transactional
    public void markAllRead(Long userId) {
        notificationRepository.markAllReadByUserId(userId);
    }

    @Override
    @Transactional
    public void deleteOne(Long notificationId, Long userId) {
        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        if (!n.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Notification not found");
        }
        notificationRepository.delete(n);
    }

    @Override
    @Transactional
    public void deleteAll(Long userId) {
        notificationRepository.deleteAllByUserId(userId);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createForUser(Long userId, String title, String message, String type) {
        notificationRepository.save(new Notification(userId, title, message, type));
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createForUser(Long userId, String title, String message, String type, Long resourceId) {
        notificationRepository.save(new Notification(userId, title, message, type, resourceId));
    }

    private NotificationDTO toDTO(Notification n) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(n.getId());
        dto.setTitle(n.getTitle());
        dto.setMessage(n.getMessage());
        dto.setType(n.getType());
        dto.setRead(n.isRead());
        dto.setCreatedAt(n.getCreatedAt());
        dto.setResourceId(n.getResourceId());
        return dto;
    }
}
