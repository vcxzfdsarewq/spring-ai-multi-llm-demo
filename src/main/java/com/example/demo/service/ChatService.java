package com.example.demo.service;

import com.example.demo.dto.ChatResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    private final ChatClient chatClient;
    private final String model;

    public ChatService(ChatClient chatClient,
                       @Value("${spring.ai.openai.chat.options.model:gpt-4o}") String model) {
        this.chatClient = chatClient;
        this.model = model;
    }

    public ChatResponse chat(String question) {
        String answer = chatClient.prompt()
                .user(question)
                .call()
                .content();
        return new ChatResponse(answer, model);
    }
}
