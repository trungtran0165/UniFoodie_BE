package com.unifoodie.service;

import com.unifoodie.model.Notification;
import com.unifoodie.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class NotificationService {
    @Autowired
    private NotificationRepository notificationRepository;

    private String getCurrentDateTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
    }

    public List<Notification> getNotificationsByUserId(String userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Notification> getUnreadNotificationsByUserId(String userId) {
        return notificationRepository.findByUserIdAndIsReadOrderByCreatedAtDesc(userId, false);
    }

    public Notification createNotification(String userId, String type, String title, 
                                        String message, String relatedId) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setRelatedId(relatedId);
        notification.setRead(false);
        notification.setCreatedAt(getCurrentDateTime());
        
        return notificationRepository.save(notification);
    }

    public Notification markAsRead(String id) {
        Notification notification = notificationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Notification not found with id: " + id));
        
        notification.setRead(true);
        return notificationRepository.save(notification);
    }

    public void markAllAsRead(String userId) {
        List<Notification> unreadNotifications = getUnreadNotificationsByUserId(userId);
        unreadNotifications.forEach(notification -> {
            notification.setRead(true);
            notificationRepository.save(notification);
        });
    }

    public void deleteNotification(String id) {
        notificationRepository.deleteById(id);
    }

    // Helper method to create order status notifications
    public void createOrderStatusNotification(String userId, String orderId, String status) {
        String title = "Order Status Update";
        String message = "Your order #" + orderId + " has been " + status.toLowerCase();
        createNotification(userId, "ORDER_STATUS", title, message, orderId);
    }

    // Helper method to create promotion notifications
    public void createPromotionNotification(String userId, String promotionId, 
                                          String title, String message) {
        createNotification(userId, "PROMOTION", title, message, promotionId);
    }
} 