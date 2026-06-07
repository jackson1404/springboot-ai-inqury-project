package com.jack.springaiopenrouter.repository;

import com.jack.springaiopenrouter.entity.ChatMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, Long> {

    List<ChatMessageEntity> findByConversationIdOrderByCreatedAtAsc(String conversationId);

    long countByConversationId(String conversationId);

    void deleteByConversationId(String conversationId);
}
