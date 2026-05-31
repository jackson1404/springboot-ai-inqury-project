package com.jack.springaiopenrouter.controller;

import com.jack.springaiopenrouter.ai.intent.ChatRouteDecision;
import com.jack.springaiopenrouter.ai.intent.ChatRoutePolicy;
import com.jack.springaiopenrouter.dto.IntentDetectionRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/intent")
public class IntentController {

    private final ChatRoutePolicy chatRoutePolicy;

    public IntentController(ChatRoutePolicy chatRoutePolicy) {
        this.chatRoutePolicy = chatRoutePolicy;
    }

    @PostMapping("/detect")
    public ChatRouteDecision detect(@Valid @RequestBody IntentDetectionRequest request) {
        return chatRoutePolicy.decide(request.message());
    }
}
