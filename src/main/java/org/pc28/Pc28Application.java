package org.pc28;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
// 删除不再需要的导入语句

@EnableScheduling
@SpringBootApplication(exclude = {
    org.springframework.ai.model.openai.autoconfigure.OpenAiAudioTranscriptionAutoConfiguration.class,
    org.springframework.ai.model.openai.autoconfigure.OpenAiAudioSpeechAutoConfiguration.class,
    org.springframework.ai.model.openai.autoconfigure.OpenAiChatAutoConfiguration.class,
    org.springframework.ai.model.openai.autoconfigure.OpenAiEmbeddingAutoConfiguration.class,
    org.springframework.ai.model.openai.autoconfigure.OpenAiImageAutoConfiguration.class,
    org.springframework.ai.model.openai.autoconfigure.OpenAiModerationAutoConfiguration.class
})
public class Pc28Application {

    public static void main(String[] args) {
        SpringApplication.run(Pc28Application.class, args);
    }

}
