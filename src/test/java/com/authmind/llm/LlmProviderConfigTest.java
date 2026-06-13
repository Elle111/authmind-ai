package com.authmind.llm;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class LlmProviderConfigTest {

    @Mock
    private ChatClient.Builder chatClientBuilder;

    @Mock
    private OpenAiLlmProvider openAiLlmProvider;

    @Test
    void llmProvider_openai_returnsOpenAiLlmProvider() {
        LlmProviderConfig config = new LlmProviderConfig();

        LlmProvider provider = config.llmProvider("openai", openAiLlmProvider);

        assertNotNull(provider);
        assertInstanceOf(OpenAiLlmProvider.class, provider);
        assertEquals(openAiLlmProvider, provider);
    }

    @Test
    void llmProvider_openaiCaseInsensitive_returnsOpenAiLlmProvider() {
        LlmProviderConfig config = new LlmProviderConfig();

        LlmProvider provider = config.llmProvider("OPENAI", openAiLlmProvider);

        assertNotNull(provider);
        assertInstanceOf(OpenAiLlmProvider.class, provider);
        assertEquals(openAiLlmProvider, provider);
    }

    @Test
    void llmProvider_unsupportedProvider_throwsException() {
        LlmProviderConfig config = new LlmProviderConfig();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> config.llmProvider("unsupported", openAiLlmProvider)
        );

        assertEquals("Unsupported LLM provider: unsupported", exception.getMessage());
    }

    @Test
    void openAiLlmProvider_createsInstance() {
        LlmProviderConfig config = new LlmProviderConfig();

        OpenAiLlmProvider provider = config.openAiLlmProvider(chatClientBuilder);

        assertNotNull(provider);
        assertInstanceOf(OpenAiLlmProvider.class, provider);
    }
}
