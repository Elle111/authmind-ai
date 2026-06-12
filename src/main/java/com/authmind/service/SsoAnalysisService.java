package com.authmind.service;

import com.authmind.metrics.AnalysisMetrics;
import com.authmind.model.SsoAnalysisRequest;
import com.authmind.model.SsoAnalysisResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class SsoAnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(SsoAnalysisService.class);
    private static final String REQUEST_ID_KEY = "requestId";

    private static final String SYSTEM_PROMPT = """
            You are an expert enterprise SSO troubleshooting assistant specializing in Okta, SAML, OIDC, OAuth, identity federation, certificates, RelayState, JIT provisioning, and authentication policies.

            Analyze the provided authentication issue and return practical remediation guidance.

            Do not invent facts. If evidence is missing, say what should be checked next.

            Return the response using the exact SsoAnalysisResponse structure.
            """;

    private final ChatClient chatClient;
    private final SensitiveDataSanitizer sanitizer;
    private final RuleBasedAnalyzer ruleAnalyzer;
    private final AnalysisMetrics metrics;

    public SsoAnalysisService(ChatClient.Builder chatClientBuilder,
                               SensitiveDataSanitizer sanitizer,
                               RuleBasedAnalyzer ruleAnalyzer,
                               AnalysisMetrics metrics) {
        this.chatClient = chatClientBuilder.build();
        this.sanitizer = sanitizer;
        this.ruleAnalyzer = ruleAnalyzer;
        this.metrics = metrics;
    }

    public SsoAnalysisResponse analyze(SsoAnalysisRequest request) {
        String requestId = UUID.randomUUID().toString();
        long startTime = System.currentTimeMillis();
        MDC.put(REQUEST_ID_KEY, requestId);

        metrics.incrementRequest();

        try {
            logger.info("Starting SSO analysis");

            RuleBasedAnalyzer.RuleMatch ruleMatch = ruleAnalyzer.analyze(
                    request.errorMessage(),
                    request.samlTrace(),
                    request.oktaLog(),
                    request.oidcError()
            ).orElse(null);

            if (ruleMatch != null) {
                metrics.incrementRuleMatch();
                logger.info("Rule detected: {}, confidence: {}", ruleMatch.rule(), ruleMatch.confidence());
            }

            String sanitizedErrorMessage = sanitizer.sanitize(request.errorMessage());
            String sanitizedSamlTrace = sanitizer.sanitize(request.samlTrace());
            String sanitizedOktaLog = sanitizer.sanitize(request.oktaLog());
            String sanitizedOidcError = sanitizer.sanitize(request.oidcError());

            String userPrompt = buildUserPrompt(sanitizedErrorMessage, sanitizedSamlTrace, sanitizedOktaLog, sanitizedOidcError);
            logger.info("User prompt sent to AI: {}", userPrompt);

            SsoAnalysisResponse aiResponse = chatClient.prompt()
                    .system(SYSTEM_PROMPT)
                    .user(userPrompt)
                    .call()
                    .entity(SsoAnalysisResponse.class);

            SsoAnalysisResponse response = mergeWithRuleMatch(aiResponse, ruleMatch);

            long duration = System.currentTimeMillis() - startTime;
            logger.info("SSO analysis completed successfully in {}ms", duration);
            metrics.incrementSuccess();

            return response;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("SSO analysis failed after {}ms", duration, e);
            metrics.incrementFailure();
            throw e;
        } finally {
            MDC.remove(REQUEST_ID_KEY);
        }
    }

    private String buildUserPrompt(String errorMessage, String samlTrace, String oktaLog, String oidcError) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Error message:\n").append(errorMessage).append("\n\n");

        if (samlTrace != null && !samlTrace.isBlank()) {
            prompt.append("SAML trace:\n").append(samlTrace).append("\n\n");
        }

        if (oktaLog != null && !oktaLog.isBlank()) {
            prompt.append("Okta System Log:\n").append(oktaLog).append("\n\n");
        }

        if (oidcError != null && !oidcError.isBlank()) {
            prompt.append("OIDC error:\n").append(oidcError).append("\n\n");
        }

        return prompt.toString();
    }

    private SsoAnalysisResponse mergeWithRuleMatch(SsoAnalysisResponse aiResponse, RuleBasedAnalyzer.RuleMatch ruleMatch) {
        String ruleDetected = ruleMatch != null ? ruleMatch.rule() : null;
        String confidenceExplanation = ruleMatch != null
                ? String.format("Confidence is high because the known Okta error '%s' was detected in the submitted log data.", ruleMatch.rule())
                : null;

        return new SsoAnalysisResponse(
                aiResponse.likelyCause(),
                aiResponse.confidenceScore(),
                aiResponse.explanation(),
                aiResponse.remediationSteps(),
                aiResponse.oktaChecks(),
                aiResponse.securityNotes(),
                ruleDetected,
                confidenceExplanation
        );
    }
}
