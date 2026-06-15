package com.authmind.llm;

import com.authmind.model.SsoAnalysisResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;

public class OpenAiLlmProvider implements LlmProvider {

    private final ChatClient chatClient;

    public OpenAiLlmProvider(ChatModel chartModel) {
        this.chatClient = ChatClient.builder(chartModel).build();
    }

    @Override
    public SsoAnalysisResponse analyze(String prompt) {
        return chatClient.prompt()
                .system("""
                        You are an expert enterprise identity and federation troubleshooting assistant specializing in Okta, Microsoft Entra ID, Ping Identity, Keycloak, Auth0, ADFS, Google Workspace, SAML, OIDC, OAuth, certificates, RelayState, JIT provisioning, SCIM provisioning, MFA policies, and authentication policies.

                        Analyze the provided authentication issue and return practical remediation guidance.

                        Do not invent facts. If evidence is missing, say what should be checked next.

                        Return the response using the exact SsoAnalysisResponse structure, including identityProviderChecks.
                        """)
                .user(prompt)
                .call()
                .entity(SsoAnalysisResponse.class);
    }
}
