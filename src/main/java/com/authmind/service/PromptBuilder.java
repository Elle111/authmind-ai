package com.authmind.service;

import com.authmind.model.SsoAnalysisRequest;
import org.springframework.stereotype.Component;

@Component
public class PromptBuilder {

    public String buildPrompt(SsoAnalysisRequest request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Error message:\n").append(request.errorMessage()).append("\n\n");

        if (request.samlTrace() != null && !request.samlTrace().isBlank()) {
            prompt.append("SAML trace:\n").append(request.samlTrace()).append("\n\n");
        }

        if (request.oktaLog() != null && !request.oktaLog().isBlank()) {
            prompt.append("Okta System Log:\n").append(request.oktaLog()).append("\n\n");
        }

        if (request.oidcError() != null && !request.oidcError().isBlank()) {
            prompt.append("OIDC error:\n").append(request.oidcError()).append("\n\n");
        }

        return prompt.toString();
    }
}
