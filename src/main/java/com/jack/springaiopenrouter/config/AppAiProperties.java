package com.jack.springaiopenrouter.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.ai")
public record AppAiProperties(int maxHistoryTurns) {
    public AppAiProperties {
        if (maxHistoryTurns <= 0){
            maxHistoryTurns = 8;
        }
    }
}
