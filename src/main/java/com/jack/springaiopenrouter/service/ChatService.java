package com.jack.springaiopenrouter.service;

import com.jack.springaiopenrouter.config.AppAiProperties;
import com.jack.springaiopenrouter.dto.ChatRequest;
import com.jack.springaiopenrouter.dto.ChatResponse;
import com.jack.springaiopenrouter.dto.StreamChatEvent;
import com.jack.springaiopenrouter.entity.ChatConversationEntity;
import com.jack.springaiopenrouter.tool.DatabaseBusinessTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class ChatService {

    private final ChatClient chatClient;
    private final ChatHistoryService chatHistoryService;
    private final DatabaseBusinessTools databaseBusinessTools;
    private final int streamMinBufferChars;
    private final int streamMaxWaitMillis;

    public ChatService(
            ChatClient.Builder chatClientBuilder,
            ChatMemory chatMemory,
            ChatHistoryService chatHistoryService,
            DatabaseBusinessTools databaseBusinessTools,
            AppAiProperties properties
    ) {
        this.chatClient = chatClientBuilder
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
        this.chatHistoryService = chatHistoryService;
        this.databaseBusinessTools = databaseBusinessTools;
        this.streamMinBufferChars = properties.streamMinBufferChars();
        this.streamMaxWaitMillis = properties.streamMaxWaitMillis();
    }

    public ChatResponse chat(ChatRequest request, String userEmail) {
        ChatConversationEntity conversation = chatHistoryService.resolveConversation(
                request.conversationId(),
                userEmail,
                request.message()
        );

        String answer = chatClient
                .prompt()
                .system(buildSystemPrompt())
                .user(request.message())
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, conversation.getId()))
                .tools(databaseBusinessTools)
                .call()
                .content();

        chatHistoryService.appendExchange(conversation, request.message(), answer);
        return new ChatResponse(conversation.getId(), answer, Instant.now());
    }

    private boolean shouldUseBusinessTools(String message) {
        String text = message == null ? "" : message.toLowerCase();

        return text.contains("customer")
                || text.contains("order")
                || text.contains("product")
                || text.contains("stock")
                || text.contains("price")
                || text.contains("spend")
                || text.contains("cust-")
                || text.contains("ord-");
    }

    public Flux<StreamChatEvent> streamChat(ChatRequest request, String userEmail) {
        ChatConversationEntity conversation = chatHistoryService.resolveConversation(
                request.conversationId(),
                userEmail,
                request.message()
        );

        AtomicReference<StringBuilder> assistantAnswerRef = new AtomicReference<>(new StringBuilder());

        Flux<StreamChatEvent> startEvent = Flux.just(StreamChatEvent.conversation(conversation.getId()));

        var prompt = chatClient
                .prompt()
                .system(buildSystemPrompt())
                .user(request.message())
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, conversation.getId()));

        Flux<String> rawChunks;

        if (shouldUseBusinessTools(request.message())) {
            rawChunks = prompt
                    .tools(databaseBusinessTools)
                    .stream()
                    .content();
        } else {
            rawChunks = prompt
                    .stream()
                    .content();
        }

        Flux<StreamChatEvent> tokenEvents = bufferTextChunks(rawChunks)
                .map(chunk -> {
                    assistantAnswerRef.get().append(chunk);
                    return StreamChatEvent.token(conversation.getId(), chunk);
                })
                .onErrorResume(error -> Flux.just(
                        StreamChatEvent.error(conversation.getId(), cleanErrorMessage(error))
                ));

        Flux<StreamChatEvent> doneEvent = Flux.defer(() -> {
            String finalAnswer = assistantAnswerRef.get().toString();

            if (!finalAnswer.isBlank()) {
                chatHistoryService.appendExchange(conversation, request.message(), finalAnswer);
            }

            return Flux.just(StreamChatEvent.done(conversation.getId()));
        });

        return startEvent
                .concatWith(tokenEvents)
                .concatWith(doneEvent);
    }

    private Flux<String> bufferTextChunks(Flux<String> source) {
        return Flux.create(sink -> {
            StringBuilder buffer = new StringBuilder();
            Object lock = new Object();

            // flush = send buffered text to frontend and empty the buffer
            Runnable flushIfNeeded = () -> {
                synchronized (lock) {
                    if (!buffer.isEmpty() && !sink.isCancelled()) {
                        sink.next(buffer.toString());
                        buffer.setLength(0);
                    }
                }
            };

            Disposable timer = Flux.interval(Duration.ofMillis(streamMaxWaitMillis))
                    .subscribe(tick -> flushIfNeeded.run());

            Disposable subscription = source.subscribe(
                    chunk -> {
                        if (chunk == null || chunk.isEmpty()) {
                            return;
                        }

                        synchronized (lock) {
                            buffer.append(chunk);

                            if (shouldFlushBuffer(buffer) && !sink.isCancelled()) {
                                sink.next(buffer.toString());
                                buffer.setLength(0);
                            }
                        }
                    },
                    error -> {
                        timer.dispose();
                        sink.error(error);
                    },
                    () -> {
                        timer.dispose();
                        flushIfNeeded.run();
                        sink.complete();
                    }
            );

            sink.onDispose(() -> {
                timer.dispose();
                subscription.dispose();
            });
        });
    }

    private boolean shouldFlushBuffer(StringBuilder buffer) {
        if (buffer.length() >= streamMinBufferChars) {
            return true;
        }

        String text = buffer.toString();

        return text.endsWith(". ")
                || text.endsWith("! ")
                || text.endsWith("? ")
                || text.endsWith(".\n")
                || text.endsWith("!\n")
                || text.endsWith("?\n")
                || text.endsWith("\n\n");
    }

    private String buildSystemPrompt() {
        return """
                You are a helpful AI assistant inside a secure Spring Boot backend.

                Behavior rules:
                - Answer normal software/backend questions clearly.
                - When the user asks about business data, use the available PostgreSQL-backed tools.
                - Business data includes customers, orders, products, prices, stock, order status, and customer spend.
                - Never invent business records. If tool results are empty, say no matching database records were found.
                - Use the Spring AI chat memory advisor context naturally for follow-up questions.
                - Keep answers practical and concise unless the user asks for more detail.
                """;
    }

    private String cleanErrorMessage(Throwable error) {
        if (error == null || error.getMessage() == null || error.getMessage().isBlank()) {
            return "Streaming failed due to an unknown AI provider error.";
        }

        String message = error.getMessage();
        if (message.length() > 300) {
            return message.substring(0, 300) + "...";
        }

        return message;
    }
}
