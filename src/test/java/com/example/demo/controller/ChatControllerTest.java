package com.example.demo.controller;

import com.example.demo.dto.ChatResponse;
import com.example.demo.service.ChatService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatController.class)
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChatService chatService;

    @Test
    void chat_正常な質問でOKが返る() throws Exception {
        when(chatService.chat("Spring AIとは？"))
                .thenReturn(new ChatResponse("Spring AIはSpringのAIフレームワークです", "gpt-4o"));

        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"question": "Spring AIとは？"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").value("Spring AIはSpringのAIフレームワークです"))
                .andExpect(jsonPath("$.model").value("gpt-4o"));
    }

    @Test
    void chat_空の質問で400が返る() throws Exception {
        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"question": ""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void chat_questionフィールドがnullで400が返る() throws Exception {
        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
