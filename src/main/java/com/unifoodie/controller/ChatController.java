package com.unifoodie.controller;

import com.unifoodie.model.ChatConversation;
import com.unifoodie.model.ChatMessage;
import com.unifoodie.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    @Autowired
    private ChatService chatService;

    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @PostMapping("/start")
    public ResponseEntity<?> startConversation(@RequestBody Map<String, Object> request) {
        try {
            String chatType = (String) request.get("chatType");
            String userId = (String) request.get("userId");

            // Generate userName if not provided
            String userName = (String) request.getOrDefault("userName", "Khách hàng " + userId.substring(0, 8));

            // Create or get existing conversation
            ChatConversation conversation = chatService.createOrGetConversation(userId, userName, chatType);

            return ResponseEntity.ok(Map.of(
                    "conversationId", conversation.getId(),
                    "status", "success",
                    "message", "Conversation started successfully"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Failed to start conversation",
                    "message", e.getMessage()));
        }
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(@RequestBody Map<String, Object> request) {
        try {
            String message = (String) request.get("message");
            String conversationId = (String) request.get("conversationId");

            if (message == null || message.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Message content is required"));
            }

            if (conversationId == null || conversationId.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Conversation ID is required"));
            }

            // Save user message
            ChatMessage chatMessage = chatService.sendUserMessage(conversationId, message.trim());

            // Convert to response format
            Map<String, Object> response = new HashMap<>();
            response.put("id", chatMessage.getId());
            response.put("content", chatMessage.getContent());
            response.put("isUser", chatMessage.isUser());
            response.put("timestamp", chatMessage.getTimestamp().format(formatter));
            response.put("status", "received");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Failed to send message",
                    "message", e.getMessage()));
        }
    }

    @GetMapping("/admin/conversations")
    public ResponseEntity<?> getAdminConversations() {
        try {
            List<ChatConversation> conversations = chatService.getAllConversations();

            // Convert to response format
            List<Map<String, Object>> response = conversations.stream()
                    .map(this::convertConversationToMap)
                    .toList();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Failed to fetch conversations",
                    "message", e.getMessage()));
        }
    }

    @GetMapping("/admin/conversations/{conversationId}/messages")
    public ResponseEntity<?> getConversationMessages(@PathVariable String conversationId) {
        try {
            // Mark conversation as read when admin views it
            chatService.markConversationAsRead(conversationId);

            List<ChatMessage> messages = chatService.getConversationMessages(conversationId);

            // Convert to response format
            List<Map<String, Object>> response = messages.stream()
                    .map(this::convertMessageToMap)
                    .toList();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Failed to fetch messages",
                    "message", e.getMessage()));
        }
    }

    @PostMapping("/admin/send")
    public ResponseEntity<?> sendAdminMessage(@RequestBody Map<String, Object> request) {
        try {
            String message = (String) request.get("message");
            String conversationId = (String) request.get("conversationId");

            if (message == null || message.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Message content is required"));
            }

            if (conversationId == null || conversationId.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Conversation ID is required"));
            }

            // Save admin message
            ChatMessage chatMessage = chatService.sendAdminMessage(conversationId, message.trim());

            // Convert to response format
            Map<String, Object> response = new HashMap<>();
            response.put("id", chatMessage.getId());
            response.put("content", chatMessage.getContent());
            response.put("isUser", false);
            response.put("isAdmin", true);
            response.put("timestamp", chatMessage.getTimestamp().format(formatter));
            response.put("status", "sent");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Failed to send admin message",
                    "message", e.getMessage()));
        }
    }

    // Helper method to convert ChatConversation to Map
    private Map<String, Object> convertConversationToMap(ChatConversation conversation) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", conversation.getId());
        map.put("userId", conversation.getUserId());
        map.put("userName", conversation.getUserName());
        map.put("chatType", conversation.getChatType());
        map.put("lastMessage", conversation.getLastMessage());
        map.put("lastMessageTime",
                conversation.getLastMessageTime() != null ? conversation.getLastMessageTime().format(formatter) : null);
        map.put("unreadCount", conversation.getUnreadCount());
        map.put("status", conversation.getStatus());
        return map;
    }

    // Helper method to convert ChatMessage to Map
    private Map<String, Object> convertMessageToMap(ChatMessage message) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", message.getId());
        map.put("content", message.getContent());
        map.put("isUser", message.isUser());
        map.put("isAdmin", message.isAdmin());
        map.put("timestamp", message.getTimestamp().format(formatter));
        map.put("status", message.getStatus());
        return map;
    }
}