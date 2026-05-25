package com.jack.springaiopenrouter.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RenameConversationRequest(
        @NotBlank(message = "title is required")
        @Size(max = 120, message = "title must be shorter than 120 characters")
        String title
) {}
