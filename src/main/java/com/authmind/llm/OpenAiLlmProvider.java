package com.authmind.llm;

import com.authmind.model.SsoAnalysisResponse;
import org.springframework.ai.chat.client.ChatClient;

public class OpenAiLlmProvider implements LlmProvider {

    private final ChatClient chatClient;

    public OpenAiLlmProvider(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @Override
    public SsoAnalysisResponse analyze(String prompt) {
        return chatClient.prompt()
                .system("""
                        You are an expert enterprise SSO troubleshooting assistant specializing in Okta, SAML, OIDC, OAuth, identity federation, certificates, RelayState, JIT provisioning, and authentication policies.

                        Analyze the provided authentication issue and return practical remediation guidance.

                        Do not invent facts. If evidence is missing, say what should be checked next.

                        Return the response using the exact SsoAnalysisResponse structure.
                        """)
                .user(prompt)
                .call()
                .entity(SsoAnalysisResponse.class);
    }
}
