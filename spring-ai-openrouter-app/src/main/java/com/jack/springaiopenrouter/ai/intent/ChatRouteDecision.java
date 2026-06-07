package com.jack.springaiopenrouter.ai.intent;

public record ChatRouteDecision(
        ChatIntent intent,
        IntentSource source,
        String query,
        double confidence,
        boolean attachBusinessTools,
        boolean useDocumentRetrieval,
        boolean requiresClarification,
        String clarificationQuestion,
        String reason
) {
    public ChatRouteDecision {
        if (intent == null) {
            intent = ChatIntent.UNKNOWN;
        }
        if (source == null) {
            source = IntentSource.FALLBACK;
        }
        query = query == null ? "" : query.trim();
        confidence = Math.max(0.0, Math.min(1.0, confidence));
        clarificationQuestion = clarificationQuestion == null ? "" : clarificationQuestion.trim();
        reason = reason == null ? "" : reason.trim();
    }

    public static ChatRouteDecision from(IntentResult result, IntentSource source, boolean attachBusinessTools, boolean useDocumentRetrieval) {
        return new ChatRouteDecision(
                result.intent(),
                source,
                result.query(),
                result.confidence(),
                attachBusinessTools,
                useDocumentRetrieval,
                result.requiresClarification(),
                result.clarificationQuestion(),
                result.reason()
        );
    }

    public static ChatRouteDecision normalFallback(String message, String reason) {
        return new ChatRouteDecision(
                ChatIntent.NORMAL_CHAT,
                IntentSource.FALLBACK,
                message,
                0.5,
                false,
                false,
                false,
                "",
                reason
        );
    }
}
