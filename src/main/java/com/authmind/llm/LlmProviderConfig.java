package com.authmind.llm;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LlmProviderConfig {

    @Bean
    public OpenAiLlmProvider openAiLlmProvider(ChatClient.Builder chatClientBuilder) {
        return new OpenAiLlmProvider(chatClientBuilder);
    }

    @Bean
    public LlmProvider llmProvider(
            @Value("${authmind.llm.provider:openai}") String provider,
            OpenAiLlmProvider openAiLlmProvider
    ) {
        if ("openai".equalsIgnoreCase(provider)) {
            return openAiLlmProvider;
        }

        throw new IllegalArgumentException("Unsupported LLM provider: " + provider);
    }
}
