package com.jack.springaiopenrouter.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.ai")
public record AppAiProperties(
        int maxHistoryTurns,
        int maxMemoryMessages,
        int maxTitleLength
) {
    public AppAiProperties {
        if (maxHistoryTurns <= 0) {
            maxHistoryTurns = 8;
        }
        if (maxMemoryMessages <= 0) {
            maxMemoryMessages = 20;
        }
        if (maxTitleLength <= 0) {
            maxTitleLength = 80;
        }
    }
}
