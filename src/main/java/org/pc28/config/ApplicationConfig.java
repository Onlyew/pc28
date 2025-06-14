package org.pc28.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;

/**
 * 应用程序配置类
 * 用于禁用Spring AI的自动配置
 */
@Configuration
@EnableAutoConfiguration(exclude = {
    org.springframework.ai.model.openai.autoconfigure.OpenAiAudioSpeechAutoConfiguration.class,
    org.springframework.ai.model.openai.autoconfigure.OpenAiChatAutoConfiguration.class,
    org.springframework.ai.model.openai.autoconfigure.OpenAiEmbeddingAutoConfiguration.class,
    org.springframework.ai.model.openai.autoconfigure.OpenAiImageAutoConfiguration.class
})
public class ApplicationConfig {
    // 空实现
} 