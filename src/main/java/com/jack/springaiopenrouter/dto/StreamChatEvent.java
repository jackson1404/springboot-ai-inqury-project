package com.jack.springaiopenrouter.dto;

import java.time.Instant;

public record StreamChatEvent(
        String type,
        String conversationId,
        String content,
        Instant timestamp
) {
    public static StreamChatEvent conversation(String conversationId) {
        return new StreamChatEvent("conversation", conversationId, "", Instant.now());
    }

    public static StreamChatEvent token(String conversationId, String content) {
        return new StreamChatEvent("token", conversationId, content, Instant.now());
    }

    public static StreamChatEvent done(String conversationId) {
        return new StreamChatEvent("done", conversationId, "", Instant.now());
    }

    public static StreamChatEvent error(String conversationId, String message) {
        return new StreamChatEvent("error", conversationId, message, Instant.now());
    }
}
