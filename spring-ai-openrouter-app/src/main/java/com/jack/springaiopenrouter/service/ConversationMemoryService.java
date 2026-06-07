package com.jack.springaiopenrouter.service;

import com.jack.springaiopenrouter.config.AppAiProperties;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class ConversationMemoryService {

    private final Map<String, Deque<Turn>> memory = new ConcurrentHashMap<>();
    private final int maxHistoryTurns;

    public ConversationMemoryService(AppAiProperties properties) {
        this.maxHistoryTurns = properties.maxHistoryTurns();
    }

    public String resolveConversationId(String conversationId) {
        if (conversationId == null || conversationId.isBlank()) {
            return UUID.randomUUID().toString();
        }
        return conversationId.trim();
    }

    public String renderHistory(String conversationId) {
        Deque<Turn> turns = memory.get(conversationId);
        if (turns == null || turns.isEmpty()) {
            return "No previous conversation.";
        }

        return turns.stream()
                .map(turn -> "User: " + turn.userMessage() + "\nAssistant: " + turn.assistantAnswer())
                .collect(Collectors.joining("\n\n"));
    }

    public void append(String conversationId, String userMessage, String assistantAnswer) {
        Deque<Turn> turns = memory.computeIfAbsent(conversationId, ignored -> new ArrayDeque<>());
        turns.addLast(new Turn(userMessage, assistantAnswer));

        while (turns.size() > maxHistoryTurns) {
            turns.removeFirst();
        }
    }

    private record Turn(String userMessage, String assistantAnswer) {}
}
