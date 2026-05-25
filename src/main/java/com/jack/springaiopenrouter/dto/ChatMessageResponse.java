package com.jack.springaiopenrouter.dto;

import java.time.Instant;

public record ChatMessageResponse(
        Long id,
        String role,
        String content,
        Instant createdAt
) {}
