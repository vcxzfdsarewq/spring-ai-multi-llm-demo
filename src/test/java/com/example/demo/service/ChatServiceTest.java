package com.example.demo.service;

import com.example.demo.dto.ChatResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ChatClient chatClient;

    @Mock
    private ChatClient.ChatClientRequestSpec requestSpec;

    @Mock
    private ChatClient.CallResponseSpec callResponseSpec;

    private ChatService chatService;

    @BeforeEach
    void setUp() {
        chatService = new ChatService(chatClient, "gpt-4o");
    }

    @Test
    void chat_LLMから回答を取得してレスポンスを返す() {
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user("Spring AIとは？")).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn("Spring AIはSpringのAIフレームワークです");

        ChatResponse response = chatService.chat("Spring AIとは？");

        assertThat(response.answer()).isEqualTo("Spring AIはSpringのAIフレームワークです");
        assertThat(response.model()).isEqualTo("gpt-4o");
    }
}
