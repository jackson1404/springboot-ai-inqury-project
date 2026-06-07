package com.jack.springaiopenrouter.ai.intent;

import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class RuleBasedIntentDetector {

    public IntentResult detect(String userMessage) {
        String original = userMessage == null ? "" : userMessage.trim();
        String text = original.toLowerCase(Locale.ROOT);

        if (text.isBlank()) {
            return IntentResult.unknown(original, "Blank message");
        }

        if (containsAny(text, "uploaded", "document", "documents", "pdf", "file", "knowledge base", "notes")) {
            return IntentResult.documentQa(original, 0.95, "Matched document or knowledge-base keyword");
        }

        if (containsAny(text, "total spend", "spend", "spent", "bought", "purchase total", "how much has", "how much did")) {
            return IntentResult.business(ChatIntent.CUSTOMER_SPEND, extractBusinessQuery(original), 0.9, "Matched customer spend wording");
        }

        if (containsAny(text, "order", "orders", "ord-", "purchase", "purchases", "sale", "sales", "status")) {
            return IntentResult.business(ChatIntent.ORDER_SEARCH, extractBusinessQuery(original), 0.9, "Matched order or sales keyword");
        }

        if (containsAny(text, "product", "products", "stock", "price", "prices", "category", "categories", "inventory")) {
            return IntentResult.business(ChatIntent.PRODUCT_SEARCH, extractBusinessQuery(original), 0.9, "Matched product, stock, price, or inventory keyword");
        }

        if (containsAny(text, "customer", "customers", "cust-")) {
            return IntentResult.business(ChatIntent.CUSTOMER_SEARCH, extractBusinessQuery(original), 0.88, "Matched customer keyword");
        }

        if (isClearlyNormalChat(text)) {
            return IntentResult.normalChat(original, 0.82, "Matched normal software/chat wording");
        }

        return IntentResult.unknown(original, "No confident rule matched; AI structured intent can classify if enabled");
    }

    private boolean isClearlyNormalChat(String text) {
        if (containsAny(text, "hello", "hi", "thanks", "thank you")) {
            return true;
        }

        boolean hasQuestionOrExplainWord = containsAny(
                text,
                "explain", "what is", "what are", "how to", "how does", "why", "guide", "teach", "compare", "difference"
        );

        boolean hasTechnicalWord = containsAny(
                text,
                "spring", "spring boot", "java", "backend", "frontend", "react", "python", "code", "api",
                "jwt", "security", "database", "jpa", "hibernate", "controller", "service", "repository",
                "class", "interface", "annotation", "applicationlistener", "application listener", "ai", "llm", "agent"
        );

        return hasQuestionOrExplainWord && hasTechnicalWord;
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private String extractBusinessQuery(String message) {
        return message == null ? "" : message.trim();
    }
}
