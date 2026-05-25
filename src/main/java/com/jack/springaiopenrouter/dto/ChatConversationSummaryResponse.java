package com.jack.springaiopenrouter.dto;

import java.time.Instant;

public record ChatConversationSummaryResponse(
        String id,
        String title,
        Instant createdAt,
        Instant updatedAt,
        long messageCount
) {}
