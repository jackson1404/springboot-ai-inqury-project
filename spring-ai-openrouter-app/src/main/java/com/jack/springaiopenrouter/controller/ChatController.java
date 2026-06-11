package com.jack.springaiopenrouter.controller;

import com.jack.springaiopenrouter.dto.ChatRequest;
import com.jack.springaiopenrouter.dto.ChatResponse;
import com.jack.springaiopenrouter.dto.StreamChatEvent;
import com.jack.springaiopenrouter.service.ChatService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public ChatResponse chat(@Valid @RequestBody ChatRequest request, Authentication authentication) {
        log.info("Chat request received: user={}, conversationId={}, messageLength={}",
                authentication.getName(),
                request.conversationId(),
                request.message() == null ? 0 : request.message().length());

        ChatResponse response = chatService.chat(request, authentication.getName());

        log.info("Chat request completed: user={}, conversationId={}, responseLength={}",
                authentication.getName(),
                response.conversationId(),
                response.answer() == null ? 0 : response.answer().length());

        return response;
    }

    @PostMapping(value = "/stream", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<StreamChatEvent> streamChat(@Valid @RequestBody ChatRequest request, Authentication authentication) {
        log.info("Streaming chat request received: user={}, conversationId={}, messageLength={}",
                authentication.getName(),
                request.conversationId(),
                request.message() == null ? 0 : request.message().length());

        return chatService.streamChat(request, authentication.getName())
                .doOnComplete(() -> log.info("Streaming chat request completed: user={}, conversationId={}",
                        authentication.getName(), request.conversationId()))
                .doOnError(error -> log.error("Streaming chat request failed: user={}, conversationId={}, error={}",
                        authentication.getName(), request.conversationId(), error.getMessage(), error));
    }
}
