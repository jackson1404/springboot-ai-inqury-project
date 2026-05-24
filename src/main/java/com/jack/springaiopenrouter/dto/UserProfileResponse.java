package com.jack.springaiopenrouter.dto;

import java.time.Instant;

public record UserProfileResponse(
        String id,
        String email,
        String displayName,
        String role,
        Instant createdAt
) {}
