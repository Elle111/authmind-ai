package com.authmind.service;

import com.authmind.llm.LlmProvider;
import com.authmind.metrics.AnalysisMetrics;
import com.authmind.model.SsoAnalysisRequest;
import com.authmind.model.SsoAnalysisResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SsoAnalysisServiceTest {

    @Mock
    private SensitiveDataSanitizer sanitizer;

    @Mock
    private PromptBuilder promptBuilder;

    @Mock
    private LlmProvider llmProvider;

    @Mock
    private RuleBasedAnalyzer ruleAnalyzer;

    @Mock
    private AnalysisMetrics metrics;

    private SsoAnalysisService service;

    @BeforeEach
    void setUp() {
        service = new SsoAnalysisService(sanitizer, promptBuilder, llmProvider, ruleAnalyzer, metrics);
    }

    @Test
    void analyze_withRuleMatch_sanitizesInputAndMergesResults() {
        SsoAnalysisRequest request = new SsoAnalysisRequest(
                "Error: app_not_configured_for_user",
                "john@test.com",
                null,
                null
        );

        RuleBasedAnalyzer.RuleMatch ruleMatch = new RuleBasedAnalyzer.RuleMatch(
                "app_not_configured_for_user",
                "User is not assigned to application",
                95
        );

        when(ruleAnalyzer.analyze(any(), any(), any(), any())).thenReturn(java.util.Optional.of(ruleMatch));
        when(sanitizer.sanitize("Error: app_not_configured_for_user")).thenReturn("Error: app_not_configured_for_user");
        when(sanitizer.sanitize("john@test.com")).thenReturn("[email-redacted]");
        when(promptBuilder.buildPrompt(any())).thenReturn("Built prompt");

        SsoAnalysisResponse aiResponse = new SsoAnalysisResponse(
                "AI detected cause",
                80,
                "AI explanation",
                List.of("AI step 1"),
                List.of("AI check 1"),
                List.of("AI note 1"),
                null,
                null
        );

        when(llmProvider.analyze(any())).thenReturn(aiResponse);

        SsoAnalysisResponse result = service.analyze(request);

        assertNotNull(result);
        assertEquals("AI detected cause", result.likelyCause());
        assertEquals("app_not_configured_for_user", result.ruleDetected());
        assertTrue(result.confidenceExplanation().contains("app_not_configured_for_user"));

        verify(sanitizer).sanitize("Error: app_not_configured_for_user");
        verify(sanitizer).sanitize("john@test.com");
        verify(promptBuilder).buildPrompt(any());
        verify(llmProvider).analyze(any());
        verify(ruleAnalyzer).analyze(any(), any(), any(), any());
        verify(metrics).incrementRequest();
        verify(metrics).incrementSuccess();
        verify(metrics).incrementRuleMatch();
    }

    @Test
    void analyze_withoutRuleMatch_sanitizesInputAndReturnsAiResponse() {
        SsoAnalysisRequest request = new SsoAnalysisRequest(
                "Generic error message",
                null,
                null,
                null
        );

        when(ruleAnalyzer.analyze(any(), any(), any(), any())).thenReturn(java.util.Optional.empty());
        when(sanitizer.sanitize("Generic error message")).thenReturn("Generic error message");
        when(promptBuilder.buildPrompt(any())).thenReturn("Built prompt");

        SsoAnalysisResponse aiResponse = new SsoAnalysisResponse(
                "AI detected cause",
                75,
                "AI explanation",
                List.of("AI step 1"),
                List.of("AI check 1"),
                List.of("AI note 1"),
                null,
                null
        );

        when(llmProvider.analyze(any())).thenReturn(aiResponse);

        SsoAnalysisResponse result = service.analyze(request);

        assertNotNull(result);
        assertEquals("AI detected cause", result.likelyCause());
        assertNull(result.ruleDetected());
        assertNull(result.confidenceExplanation());

        verify(sanitizer).sanitize("Generic error message");
        verify(promptBuilder).buildPrompt(any());
        verify(llmProvider).analyze(any());
        verify(metrics).incrementRequest();
        verify(metrics).incrementSuccess();
        verify(metrics, never()).incrementRuleMatch();
    }

    @Test
    void analyze_withException_sanitizesInputAndIncrementsFailure() {
        SsoAnalysisRequest request = new SsoAnalysisRequest(
                "Error message",
                null,
                null,
                null
        );

        when(ruleAnalyzer.analyze(any(), any(), any(), any())).thenReturn(java.util.Optional.empty());
        when(sanitizer.sanitize("Error message")).thenReturn("Error message");
        when(promptBuilder.buildPrompt(any())).thenReturn("Built prompt");
        when(llmProvider.analyze(any())).thenThrow(new RuntimeException("AI service error"));

        assertThrows(RuntimeException.class, () -> service.analyze(request));

        verify(sanitizer).sanitize("Error message");
        verify(promptBuilder).buildPrompt(any());
        verify(llmProvider).analyze(any());
        verify(metrics).incrementRequest();
        verify(metrics).incrementFailure();
        verify(metrics, never()).incrementSuccess();
    }

    @Test
    void analyze_withSensitiveData_masksBeforeSendingToAI() {
        SsoAnalysisRequest request = new SsoAnalysisRequest(
                "Contact john.smith@company.com",
                "Bearer abc123xyz",
                null,
                null
        );

        when(ruleAnalyzer.analyze(any(), any(), any(), any())).thenReturn(java.util.Optional.empty());
        when(sanitizer.sanitize("Contact john.smith@company.com")).thenReturn("Contact [email-redacted]");
        when(sanitizer.sanitize("Bearer abc123xyz")).thenReturn("Bearer [token-redacted]");
        when(promptBuilder.buildPrompt(any())).thenReturn("Built prompt with [email-redacted] and [token-redacted]");

        SsoAnalysisResponse aiResponse = new SsoAnalysisResponse(
                "Cause",
                70,
                "Explanation",
                List.of(),
                List.of(),
                List.of(),
                null,
                null
        );

        when(llmProvider.analyze(any())).thenReturn(aiResponse);

        service.analyze(request);

        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
        verify(llmProvider).analyze(promptCaptor.capture());

        String capturedPrompt = promptCaptor.getValue();
        assertTrue(capturedPrompt.contains("[email-redacted]"));
        assertTrue(capturedPrompt.contains("[token-redacted]"));
        assertFalse(capturedPrompt.contains("john.smith@company.com"));
        assertFalse(capturedPrompt.contains("abc123xyz"));
    }

    @Test
    void analyze_withAllInputFields_sanitizesAllFields() {
        SsoAnalysisRequest request = new SsoAnalysisRequest(
                "Error message",
                "SAML trace with email@test.com",
                "Okta log with Bearer token123",
                "OIDC error"
        );

        when(ruleAnalyzer.analyze(any(), any(), any(), any())).thenReturn(java.util.Optional.empty());
        when(sanitizer.sanitize(any())).thenAnswer(invocation -> {
            String input = invocation.getArgument(0);
            if (input.contains("email")) return "[email-redacted]";
            if (input.contains("Bearer")) return "Bearer [token-redacted]";
            return input;
        });
        when(promptBuilder.buildPrompt(any())).thenReturn("Built prompt");

        SsoAnalysisResponse aiResponse = new SsoAnalysisResponse(
                "Cause",
                70,
                "Explanation",
                List.of(),
                List.of(),
                List.of(),
                null,
                null
        );

        when(llmProvider.analyze(any())).thenReturn(aiResponse);

        service.analyze(request);

        verify(sanitizer).sanitize("Error message");
        verify(sanitizer).sanitize("SAML trace with email@test.com");
        verify(sanitizer).sanitize("Okta log with Bearer token123");
        verify(sanitizer).sanitize("OIDC error");
        verify(promptBuilder).buildPrompt(any());
        verify(llmProvider).analyze(any());
    }
}
