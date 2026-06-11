package com.authmind.service;

import com.authmind.model.SsoAnalysisRequest;
import com.authmind.model.SsoAnalysisResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

@Service
public class SsoAnalysisService {

    private static final String SYSTEM_PROMPT = """
            You are an expert enterprise SSO troubleshooting assistant specializing in Okta, SAML, OIDC, OAuth, identity federation, certificates, RelayState, JIT provisioning, and authentication policies.

            Analyze the provided authentication issue and return practical remediation guidance.

            Do not invent facts. If evidence is missing, say what should be checked next.

            Return the response using the exact SsoAnalysisResponse structure.
            """;

    private final ChatClient chatClient;

    public SsoAnalysisService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public SsoAnalysisResponse analyze(SsoAnalysisRequest request) {
        String userPrompt = buildUserPrompt(request);

        SsoAnalysisResponse response = chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(userPrompt)
                .call()
                .entity(SsoAnalysisResponse.class);

        return response;
    }

    private String buildUserPrompt(SsoAnalysisRequest request) {
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
