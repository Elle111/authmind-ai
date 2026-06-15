package com.authmind.service;

import com.authmind.llm.LlmProvider;
import com.authmind.metrics.AnalysisMetrics;
import com.authmind.model.IdentityProviderType;
import com.authmind.model.SsoAnalysisRequest;
import com.authmind.model.SsoAnalysisResponse;
import com.authmind.service.identity.IdentityProviderDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class SsoAnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(SsoAnalysisService.class);
    private static final String REQUEST_ID_KEY = "requestId";

    private final SensitiveDataSanitizer sanitizer;
    private final PromptBuilder promptBuilder;
    private final LlmProvider llmProvider;
    private final RuleBasedAnalyzer ruleAnalyzer;
    private final AnalysisMetrics metrics;
    private final IdentityProviderDetector identityProviderDetector;

    public SsoAnalysisService(SensitiveDataSanitizer sanitizer,
                               PromptBuilder promptBuilder,
                               LlmProvider llmProvider,
                               RuleBasedAnalyzer ruleAnalyzer,
                               AnalysisMetrics metrics,
                               IdentityProviderDetector identityProviderDetector) {
        this.sanitizer = sanitizer;
        this.promptBuilder = promptBuilder;
        this.llmProvider = llmProvider;
        this.ruleAnalyzer = ruleAnalyzer;
        this.metrics = metrics;
        this.identityProviderDetector = identityProviderDetector;
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
                    request.identityProviderLog(),
                    request.oidcError()
            ).orElse(null);

            if (ruleMatch != null) {
                metrics.incrementRuleMatch();
                logger.info("Rule detected: {}, confidence: {}", ruleMatch.rule(), ruleMatch.confidence());
            }

            String sanitizedErrorMessage = sanitizer.sanitize(request.errorMessage());
            String sanitizedSamlTrace = sanitizer.sanitize(request.samlTrace());
            String sanitizedIdentityProviderLog = sanitizer.sanitize(request.identityProviderLog());
            String sanitizedOidcError = sanitizer.sanitize(request.oidcError());

            SsoAnalysisRequest sanitizedRequest = new SsoAnalysisRequest(
                    sanitizedErrorMessage,
                    sanitizedSamlTrace,
                    sanitizedIdentityProviderLog,
                    sanitizedOidcError
            );

            IdentityProviderType identityProvider = identityProviderDetector.detect(sanitizedRequest);
            logger.info("Detected identity provider: {}", identityProvider);

            String prompt = promptBuilder.buildPrompt(sanitizedRequest, identityProvider);
            logger.info("User prompt sent to AI: {}", prompt);

            SsoAnalysisResponse aiResponse = llmProvider.analyze(prompt);

            SsoAnalysisResponse response = mergeWithRuleMatch(aiResponse, ruleMatch, identityProvider);

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

    private SsoAnalysisResponse mergeWithRuleMatch(SsoAnalysisResponse aiResponse, RuleBasedAnalyzer.RuleMatch ruleMatch, IdentityProviderType identityProvider) {
        String ruleDetected = ruleMatch != null ? ruleMatch.rule() : null;
        String confidenceExplanation = ruleMatch != null
                ? String.format("Confidence is high because the known error '%s' was detected in the submitted log data.", ruleMatch.rule())
                : null;

        return new SsoAnalysisResponse(
                aiResponse.likelyCause(),
                aiResponse.confidenceScore(),
                aiResponse.explanation(),
                aiResponse.remediationSteps(),
                aiResponse.identityProviderChecks(),
                aiResponse.securityNotes(),
                ruleDetected,
                confidenceExplanation,
                identityProvider
        );
    }
}
