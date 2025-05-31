package com.unifoodie.service;

import com.unifoodie.model.ChatConversation;
import com.unifoodie.model.ChatMessage;
import com.unifoodie.repository.ChatConversationRepository;
import com.unifoodie.repository.ChatMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ChatService {

    @Autowired
    private ChatConversationRepository conversationRepository;

    @Autowired
    private ChatMessageRepository messageRepository;

    // Create or get existing conversation
    @Transactional
    public ChatConversation createOrGetConversation(String userId, String userName, String chatType) {
        // Try to find existing conversation for this user and chat type
        Optional<ChatConversation> existingConversation = conversationRepository.findByUserIdAndChatType(userId,
                chatType);

        if (existingConversation.isPresent()) {
            return existingConversation.get();
        }

        // Create new conversation
        ChatConversation conversation = new ChatConversation(userId, userName, chatType);
        return conversationRepository.save(conversation);
    }

    // Send message from user
    @Transactional
    public ChatMessage sendUserMessage(String conversationId, String content) {
        // Save message
        ChatMessage message = ChatMessage.createUserMessage(conversationId, content);
        message = messageRepository.save(message);

        // Update conversation
        updateConversationWithNewMessage(conversationId, content, true);

        return message;
    }

    // Send message from admin
    @Transactional
    public ChatMessage sendAdminMessage(String conversationId, String content) {
        // Save message
        ChatMessage message = ChatMessage.createAdminMessage(conversationId, content);
        message = messageRepository.save(message);

        // Update conversation (don't increment unread count for admin messages)
        updateConversationWithNewMessage(conversationId, content, false);

        return message;
    }

    // Get all conversations for admin
    public List<ChatConversation> getAllConversations() {
        return conversationRepository.findAllOrderByLastMessageTimeDesc();
    }

    // Get conversations by type
    public List<ChatConversation> getConversationsByType(String chatType) {
        return conversationRepository.findByChatTypeOrderByLastMessageTimeDesc(chatType);
    }

    // Get messages for a conversation
    public List<ChatMessage> getConversationMessages(String conversationId) {
        return messageRepository.findByConversationIdOrderByTimestampAsc(conversationId);
    }

    // Mark conversation as read (reset unread count)
    @Transactional
    public void markConversationAsRead(String conversationId) {
        Optional<ChatConversation> conversationOpt = conversationRepository.findById(conversationId);
        if (conversationOpt.isPresent()) {
            ChatConversation conversation = conversationOpt.get();
            conversation.resetUnreadCount();
            conversationRepository.save(conversation);
        }
    }

    // Get conversation by ID
    public Optional<ChatConversation> getConversationById(String conversationId) {
        return conversationRepository.findById(conversationId);
    }

    // Update conversation with new message
    private void updateConversationWithNewMessage(String conversationId, String content, boolean incrementUnread) {
        Optional<ChatConversation> conversationOpt = conversationRepository.findById(conversationId);
        if (conversationOpt.isPresent()) {
            ChatConversation conversation = conversationOpt.get();
            conversation.setLastMessage(content);

            if (incrementUnread) {
                conversation.incrementUnreadCount();
            }

            conversationRepository.save(conversation);
        }
    }

    // Get conversations with unread messages
    public List<ChatConversation> getConversationsWithUnreadMessages() {
        return conversationRepository.findConversationsWithUnreadMessages();
    }

    // Delete conversation and all its messages
    @Transactional
    public void deleteConversation(String conversationId) {
        messageRepository.deleteByConversationId(conversationId);
        conversationRepository.deleteById(conversationId);
    }

    // Get conversation statistics
    public long getTotalConversationsCount() {
        return conversationRepository.count();
    }

    public long getAdminConversationsCount() {
        return conversationRepository.countByChatType("admin");
    }

    public long getChatbotConversationsCount() {
        return conversationRepository.countByChatType("chatbot");
    }
}