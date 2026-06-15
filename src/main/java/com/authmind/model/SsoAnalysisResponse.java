package com.authmind.model;

import java.util.List;

public record SsoAnalysisResponse(
        String likelyCause,
        int confidenceScore,
        String explanation,
        List<String> remediationSteps,
        List<String> identityProviderChecks,
        List<String> securityNotes,
        String ruleDetected,
        String confidenceExplanation,
        IdentityProviderType identityProvider
) {}
