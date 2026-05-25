package com.jack.springaiopenrouter.repository;

import com.jack.springaiopenrouter.entity.ChatConversationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatConversationRepository extends JpaRepository<ChatConversationEntity, String> {

    List<ChatConversationEntity> findByUserEmailIgnoreCaseOrderByUpdatedAtDesc(String email);

    Optional<ChatConversationEntity> findByIdAndUserEmailIgnoreCase(String id, String email);
}
