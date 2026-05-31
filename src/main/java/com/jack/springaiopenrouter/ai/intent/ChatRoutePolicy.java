package com.jack.springaiopenrouter.ai.intent;

import com.jack.springaiopenrouter.config.AppAiProperties;
import org.springframework.stereotype.Service;

@Service
public class ChatRoutePolicy {

    private final RuleBasedIntentDetector ruleBasedIntentDetector;
    private final AiIntentDetectionService aiIntentDetectionService;
    private final boolean aiIntentRoutingEnabled;
    private final double minAiIntentConfidence;

    public ChatRoutePolicy(
            RuleBasedIntentDetector ruleBasedIntentDetector,
            AiIntentDetectionService aiIntentDetectionService,
            AppAiProperties properties
    ) {
        this.ruleBasedIntentDetector = ruleBasedIntentDetector;
        this.aiIntentDetectionService = aiIntentDetectionService;
        this.aiIntentRoutingEnabled = properties.intentRoutingEnabled();
        this.minAiIntentConfidence = properties.intentMinConfidence();
    }

    public ChatRouteDecision decide(String userMessage) {
        IntentResult ruleResult = ruleBasedIntentDetector.detect(userMessage);

        if (isConfidentRuleResult(ruleResult)) {
            return toDecision(ruleResult, IntentSource.RULE);
        }

        if (!aiIntentRoutingEnabled) {
            return ChatRouteDecision.normalFallback(userMessage, "AI intent routing disabled; fallback to normal chat");
        }

        IntentResult aiResult = aiIntentDetectionService.detect(userMessage);
        if (aiResult.confidence() < minAiIntentConfidence) {
            return ChatRouteDecision.normalFallback(
                    userMessage,
                    "AI classifier confidence below threshold: " + aiResult.confidence()
            );
        }

        return toDecision(aiResult, IntentSource.AI_STRUCTURED_OUTPUT);
    }

    private boolean isConfidentRuleResult(IntentResult result) {
        if (result == null || result.intent() == null) {
            return false;
        }

        if (result.intent() == ChatIntent.UNKNOWN) {
            return false;
        }

        // NORMAL_CHAT is treated as confident only when the rule classifier found no business/document signals.
        // This avoids an extra AI classifier call for normal technical questions.
        return result.confidence() >= 0.75;
    }

    private ChatRouteDecision toDecision(IntentResult result, IntentSource source) {
        boolean attachBusinessTools = switch (result.intent()) {
            case CUSTOMER_SEARCH, ORDER_SEARCH, PRODUCT_SEARCH, CUSTOMER_SPEND -> true;
            default -> false;
        };

        boolean useDocumentRetrieval = result.intent() == ChatIntent.DOCUMENT_QA;

        return ChatRouteDecision.from(result, source, attachBusinessTools, useDocumentRetrieval);
    }
}
