package com.jack.springaiopenrouter.ai.intent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;

@Service
public class AiIntentDetectionService {

    private static final Logger log = LoggerFactory.getLogger(AiIntentDetectionService.class);

    private final ChatClient chatClient;
    private final BeanOutputConverter<IntentResult> converter;

    public AiIntentDetectionService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
        this.converter = new BeanOutputConverter<>(IntentResult.class);
    }

    public IntentResult detect(String userMessage) {
        String message = userMessage == null ? "" : userMessage.trim();

        if (message.isBlank()) {
            return IntentResult.unknown("", "Blank message");
        }

        try {
            String raw = chatClient.prompt()
                    .system(buildClassifierSystemPrompt())
                    .user(buildClassifierUserPrompt(message))
                    .call()
                    .content();

            IntentResult result = converter.convert(raw);
            if (result == null) {
                return IntentResult.unknown(message, "AI classifier returned null structured output");
            }
            return normalize(result, message);
        } catch (Exception ex) {
            log.warn("AI intent detection failed. Falling back to NORMAL_CHAT. Reason: {}", ex.getMessage());
            return IntentResult.normalChat(message, 0.5, "AI classifier failed; fallback to normal chat");
        }
    }

    private String buildClassifierSystemPrompt() {
        return """
                You are a strict routing classifier for a Spring Boot AI assistant.

                Your job:
                - Classify the user's message into exactly one backend route.
                - Do not answer the user.
                - Return only the structured output format requested by the application.
                - Use UNKNOWN only when the request cannot be classified.
                - Set requiresClarification=true if the user request is too vague to route safely.

                Allowed intents:
                - NORMAL_CHAT: General Java, Spring Boot, backend, AI, programming, or explanation questions.
                - CUSTOMER_SEARCH: Find, search, list, or explain customer records.
                - ORDER_SEARCH: Find, search, list, or explain orders, purchases, sales, order status, or order history.
                - PRODUCT_SEARCH: Find, search, list, or explain products, prices, categories, inventory, or stock.
                - CUSTOMER_SPEND: Calculate or summarize how much a customer spent or bought.
                - DOCUMENT_QA: Answer from uploaded documents, files, PDFs, notes, or a knowledge base.
                - UNKNOWN: The message is unclear and cannot be safely routed.

                Examples:
                User: Explain ApplicationListener in Spring Boot.
                Intent: NORMAL_CHAT

                User: Find orders for CUST-1001.
                Intent: ORDER_SEARCH

                User: How much has CUST-1001 bought from us?
                Intent: CUSTOMER_SPEND

                User: Show products with stock available.
                Intent: PRODUCT_SEARCH

                User: According to my uploaded PDF, explain target groups.
                Intent: DOCUMENT_QA

                User: Show me that thing from yesterday.
                Intent: UNKNOWN with requiresClarification=true
                """;
    }

    private String buildClassifierUserPrompt(String message) {
        return """
                User message:
                %s

                Required structured output format:
                %s
                """.formatted(message, converter.getFormat());
    }

    private IntentResult normalize(IntentResult result, String originalMessage) {
        ChatIntent intent = result.intent() == null ? ChatIntent.UNKNOWN : result.intent();
        String query = result.query() == null || result.query().isBlank() ? originalMessage : result.query();

        boolean requiresTools = switch (intent) {
            case CUSTOMER_SEARCH, ORDER_SEARCH, PRODUCT_SEARCH, CUSTOMER_SPEND -> true;
            default -> result.requiresTools();
        };

        boolean requiresDocuments = intent == ChatIntent.DOCUMENT_QA || result.requiresDocuments();
        boolean requiresClarification = intent == ChatIntent.UNKNOWN || result.requiresClarification();

        return new IntentResult(
                intent,
                query,
                result.confidence(),
                requiresTools,
                requiresDocuments,
                requiresClarification,
                result.clarificationQuestion(),
                result.reason()
        );
    }
}
