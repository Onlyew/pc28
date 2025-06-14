package org.pc28;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.autoconfigure.exclude=org.springframework.ai.model.openai.autoconfigure.OpenAiAudioTranscriptionAutoConfiguration," +
    "org.springframework.ai.model.openai.autoconfigure.OpenAiAudioSpeechAutoConfiguration," +
    "org.springframework.ai.model.openai.autoconfigure.OpenAiChatAutoConfiguration," +
    "org.springframework.ai.model.openai.autoconfigure.OpenAiEmbeddingAutoConfiguration," +
    "org.springframework.ai.model.openai.autoconfigure.OpenAiImageAutoConfiguration," +
    "org.springframework.ai.model.openai.autoconfigure.OpenAiModerationAutoConfiguration",
    "spring.ai.openai.enabled=false"
})
class Pc28ApplicationTests {

    @Test
    void contextLoads() {
    }

}
