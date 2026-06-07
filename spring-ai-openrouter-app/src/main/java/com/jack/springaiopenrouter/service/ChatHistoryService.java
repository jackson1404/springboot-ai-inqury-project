package com.jack.springaiopenrouter.service;

import com.jack.springaiopenrouter.config.AppAiProperties;
import com.jack.springaiopenrouter.dto.ChatConversationSummaryResponse;
import com.jack.springaiopenrouter.dto.ChatMessageResponse;
import com.jack.springaiopenrouter.dto.ConversationMessagesResponse;
import com.jack.springaiopenrouter.entity.ChatConversationEntity;
import com.jack.springaiopenrouter.entity.ChatMessageEntity;
import com.jack.springaiopenrouter.entity.ChatMessageRole;
import com.jack.springaiopenrouter.entity.UserEntity;
import com.jack.springaiopenrouter.repository.ChatConversationRepository;
import com.jack.springaiopenrouter.repository.ChatMessageRepository;
import com.jack.springaiopenrouter.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ChatHistoryService {

    private final ChatConversationRepository conversationRepository;
    private final ChatMessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ChatMemory chatMemory;
    private final int maxTitleLength;

    public ChatHistoryService(
            ChatConversationRepository conversationRepository,
            ChatMessageRepository messageRepository,
            UserRepository userRepository,
            ChatMemory chatMemory,
            AppAiProperties properties
    ) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.chatMemory = chatMemory;
        this.maxTitleLength = properties.maxTitleLength();
    }

    @Transactional
    public ChatConversationEntity resolveConversation(String conversationId, String userEmail, String firstUserMessage) {
        if (conversationId != null && !conversationId.isBlank()) {
            return conversationRepository.findByIdAndUserEmailIgnoreCase(conversationId.trim(), userEmail)
                    .orElseThrow(() -> new EntityNotFoundException("Conversation not found"));
        }

        UserEntity user = userRepository.findByEmailIgnoreCase(userEmail)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        String title = buildTitle(firstUserMessage);
        return conversationRepository.save(new ChatConversationEntity(user, title));
    }

    @Transactional
    public void appendExchange(ChatConversationEntity conversation, String userMessage, String assistantAnswer) {
        messageRepository.save(new ChatMessageEntity(conversation, ChatMessageRole.USER, userMessage));
        messageRepository.save(new ChatMessageEntity(conversation, ChatMessageRole.ASSISTANT, assistantAnswer));
        conversation.touch();
        conversationRepository.save(conversation);
    }

    @Transactional(readOnly = true)
    public List<ChatConversationSummaryResponse> listConversations(String userEmail) {
        return conversationRepository.findByUserEmailIgnoreCaseOrderByUpdatedAtDesc(userEmail).stream()
                .map(this::toSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public ConversationMessagesResponse getConversation(String conversationId, String userEmail) {
        ChatConversationEntity conversation = conversationRepository.findByIdAndUserEmailIgnoreCase(conversationId, userEmail)
                .orElseThrow(() -> new EntityNotFoundException("Conversation not found"));

        List<ChatMessageResponse> messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversation.getId()).stream()
                .map(this::toMessageResponse)
                .toList();

        return new ConversationMessagesResponse(toSummary(conversation), messages);
    }

    @Transactional
    public ChatConversationSummaryResponse renameConversation(String conversationId, String userEmail, String title) {
        ChatConversationEntity conversation = conversationRepository.findByIdAndUserEmailIgnoreCase(conversationId, userEmail)
                .orElseThrow(() -> new EntityNotFoundException("Conversation not found"));

        conversation.rename(buildTitle(title));
        return toSummary(conversationRepository.save(conversation));
    }

    @Transactional
    public void deleteConversation(String conversationId, String userEmail) {
        ChatConversationEntity conversation = conversationRepository.findByIdAndUserEmailIgnoreCase(conversationId, userEmail)
                .orElseThrow(() -> new EntityNotFoundException("Conversation not found"));
        messageRepository.deleteByConversationId(conversation.getId());
        chatMemory.clear(conversation.getId());
        conversationRepository.delete(conversation);
    }

    private ChatConversationSummaryResponse toSummary(ChatConversationEntity conversation) {
        return new ChatConversationSummaryResponse(
                conversation.getId(),
                conversation.getTitle(),
                conversation.getCreatedAt(),
                conversation.getUpdatedAt(),
                messageRepository.countByConversationId(conversation.getId())
        );
    }

    private ChatMessageResponse toMessageResponse(ChatMessageEntity message) {
        return new ChatMessageResponse(
                message.getId(),
                message.getRole().name(),
                message.getContent(),
                message.getCreatedAt()
        );
    }

    private String buildTitle(String value) {
        String clean = value == null ? "New conversation" : value.replaceAll("\\s+", " ").trim();
        if (clean.isBlank()) {
            return "New conversation";
        }
        if (clean.length() <= maxTitleLength) {
            return clean;
        }
        return clean.substring(0, Math.max(0, maxTitleLength - 3)) + "...";
    }
}
