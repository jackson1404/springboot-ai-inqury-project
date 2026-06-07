package com.jack.springaiopenrouter.dto;

import java.time.Instant;

public record ApiMessageResponse(
        String message,
        Instant timestamp
) {}
