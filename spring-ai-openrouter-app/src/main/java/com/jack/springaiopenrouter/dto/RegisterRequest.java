package com.jack.springaiopenrouter.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "displayName is required")
        @Size(min = 2, max = 80, message = "displayName must be between 2 and 80 characters")
        String displayName,

        @NotBlank(message = "email is required")
        @Email(message = "email must be valid")
        @Size(max = 120, message = "email must be shorter than 120 characters")
        String email,

        @NotBlank(message = "password is required")
        @Size(min = 8, max = 72, message = "password must be between 8 and 72 characters")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$",
                message = "password must contain at least one letter and one number"
        )
        String password
) {}
