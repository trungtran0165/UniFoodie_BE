package com.unifoodie.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "chat_messages")
public class ChatMessage {
    @Id
    private String id;
    private String conversationId;
    private String content;
    private boolean isUser;
    private boolean isAdmin;
    private LocalDateTime timestamp;
    private String status; // "sent", "delivered", "read"

    // Constructors
    public ChatMessage() {
        this.timestamp = LocalDateTime.now();
        this.status = "sent";
    }

    public ChatMessage(String conversationId, String content, boolean isUser, boolean isAdmin) {
        this();
        this.conversationId = conversationId;
        this.content = content;
        this.isUser = isUser;
        this.isAdmin = isAdmin;
    }

    // Static factory methods
    public static ChatMessage createUserMessage(String conversationId, String content) {
        return new ChatMessage(conversationId, content, true, false);
    }

    public static ChatMessage createAdminMessage(String conversationId, String content) {
        return new ChatMessage(conversationId, content, false, true);
    }

    public static ChatMessage createSystemMessage(String conversationId, String content) {
        return new ChatMessage(conversationId, content, false, false);
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isUser() {
        return isUser;
    }

    public void setUser(boolean isUser) {
        this.isUser = isUser;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}