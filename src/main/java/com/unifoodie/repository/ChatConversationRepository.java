package com.unifoodie.repository;

import com.unifoodie.model.ChatConversation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatConversationRepository extends MongoRepository<ChatConversation, String> {

    // Find conversation by user ID and chat type
    Optional<ChatConversation> findByUserIdAndChatType(String userId, String chatType);

    // Find all conversations ordered by last message time (newest first)
    @Query(value = "{}", sort = "{ 'lastMessageTime': -1 }")
    List<ChatConversation> findAllOrderByLastMessageTimeDesc();

    // Find conversations by chat type
    List<ChatConversation> findByChatTypeOrderByLastMessageTimeDesc(String chatType);

    // Find conversations by status
    List<ChatConversation> findByStatusOrderByLastMessageTimeDesc(String status);

    // Find conversations with unread messages
    @Query(value = "{ 'unreadCount': { $gt: 0 } }", sort = "{ 'lastMessageTime': -1 }")
    List<ChatConversation> findConversationsWithUnreadMessages();

    // Count conversations by chat type
    long countByChatType(String chatType);

    // Find conversations by user ID
    List<ChatConversation> findByUserIdOrderByLastMessageTimeDesc(String userId);
}