package com.jack.springaiopenrouter.service;

import com.jack.springaiopenrouter.dto.ChatRequest;
import com.jack.springaiopenrouter.dto.ChatResponse;
import com.jack.springaiopenrouter.entity.ChatConversationEntity;
import com.jack.springaiopenrouter.tool.DatabaseBusinessTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;
import java.time.Instant;

@Service
public class ChatService {

    private final ChatClient chatClient;
    private final ChatHistoryService chatHistoryService;
    private final DatabaseBusinessTools databaseBusinessTools;

    public ChatService(
            ChatClient.Builder chatClientBuilder,
            ChatMemory chatMemory,
            ChatHistoryService chatHistoryService,
            DatabaseBusinessTools databaseBusinessTools
    ) {
        this.chatClient = chatClientBuilder
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
        this.chatHistoryService = chatHistoryService;
        this.databaseBusinessTools = databaseBusinessTools;
    }

    public ChatResponse chat(ChatRequest request, String userEmail) {
        ChatConversationEntity conversation = chatHistoryService.resolveConversation(
                request.conversationId(),
                userEmail,
                request.message()
        );

        String answer = chatClient
                .prompt()
                .system(buildSystemPrompt())
                .user(request.message())
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, conversation.getId()))
                .tools(databaseBusinessTools)
                .call()
                .content();

        chatHistoryService.appendExchange(conversation, request.message(), answer);
        return new ChatResponse(conversation.getId(), answer, Instant.now());
    }

    private String buildSystemPrompt() {
        return """
                You are a helpful AI assistant inside a secure Spring Boot backend.

                Behavior rules:
                - Answer normal software/backend questions clearly.
                - When the user asks about business data, use the available PostgreSQL-backed tools.
                - Business data includes customers, orders, products, prices, stock, order status, and customer spend.
                - Never invent business records. If tool results are empty, say no matching database records were found.
                - Use the Spring AI chat memory advisor context naturally for follow-up questions.
                - Keep answers practical and concise unless the user asks for more detail.
                """;
    }
}
