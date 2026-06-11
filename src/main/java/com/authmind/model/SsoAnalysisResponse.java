package com.authmind.model;

import java.util.List;

public record SsoAnalysisResponse(
        String likelyCause,
        int confidenceScore,
        String explanation,
        List<String> remediationSteps,
        List<String> oktaChecks,
        List<String> securityNotes
) {}
