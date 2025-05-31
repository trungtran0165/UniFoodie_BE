package com.unifoodie.repository;

import com.unifoodie.model.ChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.time.LocalDateTime;

@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {

    // Find messages by conversation ID ordered by timestamp (oldest first)
    List<ChatMessage> findByConversationIdOrderByTimestampAsc(String conversationId);

    // Find messages by conversation ID ordered by timestamp (newest first)
    List<ChatMessage> findByConversationIdOrderByTimestampDesc(String conversationId);

    // Find latest message for a conversation
    @Query(value = "{ 'conversationId': ?0 }", sort = "{ 'timestamp': -1 }")
    ChatMessage findLatestMessageByConversationId(String conversationId);

    // Count messages in a conversation
    long countByConversationId(String conversationId);

    // Find messages after a specific timestamp
    List<ChatMessage> findByConversationIdAndTimestampAfterOrderByTimestampAsc(String conversationId,
            LocalDateTime timestamp);

    // Count unread messages (sent by user, not read by admin)
    @Query(value = "{ 'conversationId': ?0, 'isUser': true, 'status': { $ne: 'read' } }")
    long countUnreadUserMessagesByConversationId(String conversationId);

    // Delete all messages for a conversation
    void deleteByConversationId(String conversationId);
}