package com.jack.springaiopenrouter.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DataInquiryRequest(
        @NotBlank(message = "query is required")
        @Size(max = 300, message = "query must be shorter than 300 characters")
        String query
) {}
