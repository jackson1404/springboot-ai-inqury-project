package com.jack.springaiopenrouter.config;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiChatConfig {

    @Bean
    public ChatMemory chatMemory(ChatMemoryRepository chatMemoryRepository, AppAiProperties properties) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(properties.maxMemoryMessages())
                .build();
    }

    @Bean
    @ConditionalOnMissingBean(ToolCallbackProvider.class)
    @ConditionalOnProperty(name = "spring.ai.mcp.client.enabled", havingValue = "false")
    public ToolCallbackProvider noOpToolCallbackProvider() {
        return () -> new ToolCallback[0];
    }
}
