package com.jack.springaiopenrouter.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.ai")
public record AppAiProperties(
        int maxHistoryTurns,
        int maxMemoryMessages,
        int maxTitleLength,
        int streamMinBufferChars,
        int streamMaxWaitMillis,
        boolean intentRoutingEnabled,
        double intentMinConfidence
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
        if (streamMinBufferChars <= 0) {
            streamMinBufferChars = 120;
        }
        if (streamMaxWaitMillis <= 0) {
            streamMaxWaitMillis = 400;
        }
        if (intentMinConfidence <= 0.0 || intentMinConfidence > 1.0) {
            intentMinConfidence = 0.65;
        }
    }
}
