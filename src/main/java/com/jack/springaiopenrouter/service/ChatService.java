package com.jack.springaiopenrouter.service;

import com.jack.springaiopenrouter.dto.ChatRequest;
import com.jack.springaiopenrouter.dto.ChatResponse;
import com.jack.springaiopenrouter.tool.DatabaseBusinessTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class ChatService {

    private final ChatClient chatClient;
    private final ConversationMemoryService memoryService;
    private final DatabaseBusinessTools databaseBusinessTools;

    public ChatService(
            ChatClient.Builder chatClientBuilder,
            ConversationMemoryService memoryService,
            DatabaseBusinessTools databaseBusinessTools
    ) {
        this.chatClient = chatClientBuilder.build();
        this.memoryService = memoryService;
        this.databaseBusinessTools = databaseBusinessTools;
    }

    public ChatResponse chat(ChatRequest request) {
        String conversationId = memoryService.resolveConversationId(request.conversationId());
        String history = memoryService.renderHistory(conversationId);

        String answer = chatClient
                .prompt()
                .system(buildSystemPrompt(history))
                .user(request.message())
                .tools(databaseBusinessTools)
                .call()
                .content();

        memoryService.append(conversationId, request.message(), answer);
        return new ChatResponse(conversationId, answer, Instant.now());
    }

    private String buildSystemPrompt(String history) {
        return """
                You are a helpful AI assistant inside a Spring Boot demo backend.

                Main behavior:
                - Answer normal software/backend questions clearly.
                - When the user asks about business data, use the available PostgreSQL-backed tools.
                - Business data includes customers, orders, products, prices, stock, order status, and customer spend.
                - Never invent business records. If tool results are empty, say no matching database records were found.
                - Keep answers practical and concise unless the user asks for more detail.

                Conversation history:
                %s
                """.formatted(history);
    }
}
