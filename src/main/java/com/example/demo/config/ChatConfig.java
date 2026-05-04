package com.example.demo.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class ChatConfig {

    @Bean
    @Profile("ollama")
    @SuppressWarnings("null")
    public ChatClient ollamaChatClient(@Qualifier("ollamaChatModel") ChatModel chatModel) {
        return ChatClient.builder(chatModel)
                .defaultOptions(new OllamaChatOptions.Builder().numPredict(-1).build())
                .build();
    }

    @Bean
    @Profile("openai")
    @SuppressWarnings("null")
    public ChatClient openAiChatClient(@Qualifier("openAiChatModel") ChatModel chatModel) {
        return ChatClient.builder(chatModel).build();
    }
}
