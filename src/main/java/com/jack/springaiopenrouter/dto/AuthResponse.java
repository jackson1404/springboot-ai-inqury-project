package com.jack.springaiopenrouter.dto;

import java.time.Instant;

public record AuthResponse(
        String tokenType,
        String accessToken,
        Instant expiresAt,
        UserProfileResponse user
) {}
