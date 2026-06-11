package com.jack.springaiopenrouter.service;

import com.jack.springaiopenrouter.ai.intent.ChatRouteDecision;
import com.jack.springaiopenrouter.ai.intent.ChatRoutePolicy;
import com.jack.springaiopenrouter.config.AppAiProperties;
import com.jack.springaiopenrouter.dto.ChatRequest;
import com.jack.springaiopenrouter.dto.ChatResponse;
import com.jack.springaiopenrouter.dto.StreamChatEvent;
import com.jack.springaiopenrouter.entity.ChatConversationEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClient.ChatClientRequestSpec;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.stereotype.Service;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    private final ChatClient chatClient;
    private final ChatHistoryService chatHistoryService;
    private final ToolCallbackProvider mcpToolProvider;
    private final ChatRoutePolicy chatRoutePolicy;
    private final int streamMinBufferChars;
    private final int streamMaxWaitMillis;

    public ChatService(
            ChatClient.Builder chatClientBuilder,
            ChatMemory chatMemory,
            ChatHistoryService chatHistoryService,
            ToolCallbackProvider mcpToolProvider,
            ChatRoutePolicy chatRoutePolicy,
            AppAiProperties properties
    ) {
        this.chatClient = chatClientBuilder
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
        this.chatHistoryService = chatHistoryService;
        this.mcpToolProvider = mcpToolProvider;
        this.chatRoutePolicy = chatRoutePolicy;
        this.streamMinBufferChars = properties.streamMinBufferChars();
        this.streamMaxWaitMillis = properties.streamMaxWaitMillis();
    }

    public ChatResponse chat(ChatRequest request, String userEmail) {
        ChatConversationEntity conversation = chatHistoryService.resolveConversation(
                request.conversationId(),
                userEmail,
                request.message()
        );

        ChatRouteDecision routeDecision = chatRoutePolicy.decide(request.message());

        log.info("Chat route decided: conversationId={}, intent={}, source={}, confidence={}, attachBusinessTools={}, useDocumentRetrieval={}, reason={}",
                conversation.getId(),
                routeDecision.intent(),
                routeDecision.source(),
                routeDecision.confidence(),
                routeDecision.attachBusinessTools(),
                routeDecision.useDocumentRetrieval(),
                routeDecision.reason());

        if (routeDecision.requiresClarification() && !routeDecision.clarificationQuestion().isBlank()) {
            String clarification = routeDecision.clarificationQuestion();
            chatHistoryService.appendExchange(conversation, request.message(), clarification);
            return new ChatResponse(conversation.getId(), clarification, Instant.now());
        }

        ChatClientRequestSpec prompt = basePrompt(request, conversation, routeDecision);

        String answer;

        if (routeDecision.attachBusinessTools()) {
            log.info("MCP client tools attached to AI request: conversationId={}, provider={}",
                    conversation.getId(), mcpToolProvider.getClass().getName());
            answer = prompt.toolCallbacks(mcpToolProvider).call().content();
            log.info("AI response completed after MCP-enabled request: conversationId={}, answerLength={}",
                    conversation.getId(), answer == null ? 0 : answer.length());
        } else {
            log.info("AI request sent without MCP tools: conversationId={}", conversation.getId());
            answer = prompt.call().content();
            log.info("AI response completed without MCP tools: conversationId={}, answerLength={}",
                    conversation.getId(), answer == null ? 0 : answer.length());
        }

        chatHistoryService.appendExchange(conversation, request.message(), answer);
        return new ChatResponse(conversation.getId(), answer, Instant.now());
    }

    public Flux<StreamChatEvent> streamChat(ChatRequest request, String userEmail) {
        ChatConversationEntity conversation = chatHistoryService.resolveConversation(
                request.conversationId(),
                userEmail,
                request.message()
        );

        ChatRouteDecision routeDecision = chatRoutePolicy.decide(request.message());
        AtomicReference<StringBuilder> assistantAnswerRef = new AtomicReference<>(new StringBuilder());

        log.info("Streaming chat route decided: conversationId={}, intent={}, source={}, confidence={}, attachBusinessTools={}, useDocumentRetrieval={}, reason={}",
                conversation.getId(),
                routeDecision.intent(),
                routeDecision.source(),
                routeDecision.confidence(),
                routeDecision.attachBusinessTools(),
                routeDecision.useDocumentRetrieval(),
                routeDecision.reason());

        Flux<StreamChatEvent> startEvent = Flux.just(StreamChatEvent.conversation(conversation.getId()));

        if (routeDecision.requiresClarification() && !routeDecision.clarificationQuestion().isBlank()) {
            String clarification = routeDecision.clarificationQuestion();
            Flux<StreamChatEvent> clarificationEvents = bufferTextChunks(Flux.just(clarification))
                    .map(chunk -> {
                        assistantAnswerRef.get().append(chunk);
                        return StreamChatEvent.token(conversation.getId(), chunk);
                    });

            Flux<StreamChatEvent> doneEvent = Flux.defer(() -> {
                chatHistoryService.appendExchange(conversation, request.message(), assistantAnswerRef.get().toString());
                return Flux.just(StreamChatEvent.done(conversation.getId()));
            });

            return startEvent.concatWith(clarificationEvents).concatWith(doneEvent);
        }

        ChatClientRequestSpec prompt = basePrompt(request, conversation, routeDecision);

        Flux<String> rawChunks;

        if (routeDecision.attachBusinessTools()) {
            log.info("MCP client tools attached to streaming AI request: conversationId={}, provider={}",
                    conversation.getId(), mcpToolProvider.getClass().getName());
            rawChunks = prompt.toolCallbacks(mcpToolProvider).stream().content();
        } else {
            log.info("Streaming AI request sent without MCP tools: conversationId={}", conversation.getId());
            rawChunks = prompt.stream().content();
        }

        Flux<StreamChatEvent> tokenEvents = bufferTextChunks(rawChunks)
                .map(chunk -> {
                    assistantAnswerRef.get().append(chunk);
                    return StreamChatEvent.token(conversation.getId(), chunk);
                })
                .onErrorResume(error -> {
                    log.error("Streaming AI request failed: conversationId={}, attachBusinessTools={}, error={}",
                            conversation.getId(), routeDecision.attachBusinessTools(), error.getMessage(), error);
                    return Flux.just(StreamChatEvent.error(conversation.getId(), cleanErrorMessage(error)));
                });

        Flux<StreamChatEvent> doneEvent = Flux.defer(() -> {
            String finalAnswer = assistantAnswerRef.get().toString();

            if (!finalAnswer.isBlank()) {
                chatHistoryService.appendExchange(conversation, request.message(), finalAnswer);
                log.info("Streaming AI response completed: conversationId={}, attachBusinessTools={}, answerLength={}",
                        conversation.getId(), routeDecision.attachBusinessTools(), finalAnswer.length());
            } else {
                log.warn("Streaming AI response completed with blank answer: conversationId={}, attachBusinessTools={}",
                        conversation.getId(), routeDecision.attachBusinessTools());
            }

            return Flux.just(StreamChatEvent.done(conversation.getId()));
        });

        return startEvent
                .concatWith(tokenEvents)
                .concatWith(doneEvent);
    }

    private ChatClientRequestSpec basePrompt(
            ChatRequest request,
            ChatConversationEntity conversation,
            ChatRouteDecision routeDecision
    ) {
        return chatClient
                .prompt()
                .system(buildSystemPrompt(routeDecision))
                .user(request.message())
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, conversation.getId()));
    }

    private Flux<String> bufferTextChunks(Flux<String> source) {
        return Flux.create(sink -> {
            StringBuilder buffer = new StringBuilder();
            Object lock = new Object();

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

    private String buildSystemPrompt(ChatRouteDecision routeDecision) {
        String routeSummary = """
                Current routing decision:
                - intent: %s
                - source: %s
                - confidence: %.2f
                - attachBusinessTools: %s
                - useDocumentRetrieval: %s
                - reason: %s
                """.formatted(
                routeDecision.intent(),
                routeDecision.source(),
                routeDecision.confidence(),
                routeDecision.attachBusinessTools(),
                routeDecision.useDocumentRetrieval(),
                routeDecision.reason()
        );

        return """
                You are a helpful AI assistant inside a secure Spring Boot backend.

                Behavior rules:
                - Answer normal software/backend questions clearly.
                - Use PostgreSQL-backed business tools only when they are attached to this request.
                - Business data includes customers, orders, products, prices, stock, order status, and customer spend.
                - Never invent business records. If tool results are empty, say no matching database records were found.
                - Use the Spring AI chat memory advisor context naturally for follow-up questions.
                - Keep answers practical and concise unless the user asks for more detail.

                %s
                """.formatted(routeSummary);
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
