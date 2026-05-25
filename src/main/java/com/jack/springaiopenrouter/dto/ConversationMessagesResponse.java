package com.jack.springaiopenrouter.dto;

import java.util.List;

public record ConversationMessagesResponse(
        ChatConversationSummaryResponse conversation,
        List<ChatMessageResponse> messages
) {}
