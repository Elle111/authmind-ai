package com.authmind.llm;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class LlmProviderConfig {

    @Bean
    @ConditionalOnProperty(name = "authmind.llm.provider", havingValue = "openai", matchIfMissing = true)
    public LlmProvider openAiLlmProvider(@Qualifier("openAiChatModel") ChatModel chatModel) {
        return new OpenAiLlmProvider(chatModel);
    }

    @Bean
    @ConditionalOnProperty(name = "authmind.llm.provider", havingValue = "ollama")
    public LlmProvider ollamaLlmProvider(@Qualifier("ollamaChatModel") ChatModel chatModel) {
        return new OllamaLlmProvider(chatModel);
    }

}
