package com.jack.springaiopenrouter.ai.intent;

public record IntentResult(
        ChatIntent intent,
        String query,
        double confidence,
        boolean requiresTools,
        boolean requiresDocuments,
        boolean requiresClarification,
        String clarificationQuestion,
        String reason
) {
    public IntentResult {
        if (intent == null) {
            intent = ChatIntent.UNKNOWN;
        }
        query = query == null ? "" : query.trim();
        confidence = Math.max(0.0, Math.min(1.0, confidence));
        clarificationQuestion = clarificationQuestion == null ? "" : clarificationQuestion.trim();
        reason = reason == null ? "" : reason.trim();
    }

    public static IntentResult normalChat(String query, double confidence, String reason) {
        return new IntentResult(
                ChatIntent.NORMAL_CHAT,
                query,
                confidence,
                false,
                false,
                false,
                "",
                reason
        );
    }

    public static IntentResult business(ChatIntent intent, String query, double confidence, String reason) {
        return new IntentResult(
                intent,
                query,
                confidence,
                true,
                false,
                false,
                "",
                reason
        );
    }

    public static IntentResult documentQa(String query, double confidence, String reason) {
        return new IntentResult(
                ChatIntent.DOCUMENT_QA,
                query,
                confidence,
                false,
                true,
                false,
                "",
                reason
        );
    }

    public static IntentResult unknown(String query, String reason) {
        return new IntentResult(
                ChatIntent.UNKNOWN,
                query,
                0.0,
                false,
                false,
                false,
                "",
                reason
        );
    }
}
