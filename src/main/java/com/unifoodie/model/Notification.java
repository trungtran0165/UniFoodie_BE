package com.unifoodie.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "notifications")
public class Notification {
    @Id
    private String id;
    private String userId;
    private String type; // ORDER_STATUS, PROMOTION, SYSTEM
    private String title;
    private String message;
    private boolean isRead;
    private String createdAt;
    private String relatedId; // ID of related order/promotion if applicable

    // Getters
    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getType() { return type; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public boolean isRead() { return isRead; }
    public String getCreatedAt() { return createdAt; }
    public String getRelatedId() { return relatedId; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setType(String type) { this.type = type; }
    public void setTitle(String title) { this.title = title; }
    public void setMessage(String message) { this.message = message; }
    public void setRead(boolean isRead) { this.isRead = isRead; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public void setRelatedId(String relatedId) { this.relatedId = relatedId; }
} 