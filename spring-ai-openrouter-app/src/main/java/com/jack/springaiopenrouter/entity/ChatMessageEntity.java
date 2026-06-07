package com.jack.springaiopenrouter.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(
        name = "chat_messages",
        indexes = {
                @Index(name = "idx_chat_messages_conversation_created", columnList = "conversation_id, created_at")
        }
)
public class ChatMessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversation_id", nullable = false)
    private ChatConversationEntity conversation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ChatMessageRole role;

    @Lob
    @Column(nullable = false, columnDefinition = "text")
    private String content;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected ChatMessageEntity() {
        // Required by JPA
    }

    public ChatMessageEntity(ChatConversationEntity conversation, ChatMessageRole role, String content) {
        this.conversation = conversation;
        this.role = role;
        this.content = content;
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public ChatConversationEntity getConversation() {
        return conversation;
    }

    public ChatMessageRole getRole() {
        return role;
    }

    public String getContent() {
        return content;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
