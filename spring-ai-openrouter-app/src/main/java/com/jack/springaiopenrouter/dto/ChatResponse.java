package com.jack.springaiopenrouter.dto;

import java.time.Instant;

public record ChatResponse(
        String conversationId,
        String answer,
        Instant timestamp
) {}
