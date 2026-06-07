package com.jack.springaiopenrouter.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatRequest(
        @Size(max = 80, message = "conversationId must be shorter than 80 characters")
        String conversationId,

        @NotBlank(message = "message is required")
        @Size(max = 4000, message = "message must be shorter than 4000 characters")
        String message
) {}
