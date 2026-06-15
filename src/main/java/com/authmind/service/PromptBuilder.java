package com.authmind.service;

import com.authmind.model.IdentityProviderType;
import com.authmind.model.SsoAnalysisRequest;
import org.springframework.stereotype.Component;

@Component
public class PromptBuilder {

    public String buildPrompt(SsoAnalysisRequest request, IdentityProviderType identityProvider) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Detected Identity Provider:\n").append(identityProvider).append("\n\n");
        prompt.append("Error message:\n").append(request.errorMessage()).append("\n\n");

        if (request.samlTrace() != null && !request.samlTrace().isBlank()) {
            prompt.append("SAML trace:\n").append(request.samlTrace()).append("\n\n");
        }

        if (request.identityProviderLog() != null && !request.identityProviderLog().isBlank()) {
            prompt.append("Identity Provider Log:\n").append(request.identityProviderLog()).append("\n\n");
        }

        if (request.oidcError() != null && !request.oidcError().isBlank()) {
            prompt.append("OIDC error:\n").append(request.oidcError()).append("\n\n");
        }

        return prompt.toString();
    }
}
