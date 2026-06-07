package com.jack.springaiopenrouter.controller;

import com.jack.springaiopenrouter.dto.ChatConversationSummaryResponse;
import com.jack.springaiopenrouter.dto.ConversationMessagesResponse;
import com.jack.springaiopenrouter.dto.RenameConversationRequest;
import com.jack.springaiopenrouter.service.ChatHistoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

    private final ChatHistoryService chatHistoryService;

    public ConversationController(ChatHistoryService chatHistoryService) {
        this.chatHistoryService = chatHistoryService;
    }

    @GetMapping
    public List<ChatConversationSummaryResponse> list(Authentication authentication) {
        return chatHistoryService.listConversations(authentication.getName());
    }

    @GetMapping("/{conversationId}")
    public ConversationMessagesResponse get(
            @PathVariable String conversationId,
            Authentication authentication
    ) {
        return chatHistoryService.getConversation(conversationId, authentication.getName());
    }

    @PatchMapping("/{conversationId}")
    public ChatConversationSummaryResponse rename(
            @PathVariable String conversationId,
            @Valid @RequestBody RenameConversationRequest request,
            Authentication authentication
    ) {
        return chatHistoryService.renameConversation(conversationId, authentication.getName(), request.title());
    }

    @DeleteMapping("/{conversationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable String conversationId,
            Authentication authentication
    ) {
        chatHistoryService.deleteConversation(conversationId, authentication.getName());
    }
}
