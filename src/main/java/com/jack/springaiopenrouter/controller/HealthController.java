package com.jack.springaiopenrouter.controller;

import com.jack.springaiopenrouter.dto.ApiMessageResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/ping")
    public ApiMessageResponse ping() {
        return new ApiMessageResponse("spring-ai-openrouter-demo is running", Instant.now());
    }
}
