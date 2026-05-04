package com.example.demo.service;

import com.example.demo.dto.ChatResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class ChatService {

    private final ChatClient chatClient;
    private final String model;

    public ChatService(ChatClient chatClient,
                       @Value("${app.llm.model}") String model) {
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

    public Flux<String> stream(String question) {
        return chatClient.prompt()
                .user(question)
                .stream()
                .content();
    }
}
