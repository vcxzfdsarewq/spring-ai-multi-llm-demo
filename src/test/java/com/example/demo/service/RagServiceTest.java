package com.example.demo.service;

import com.example.demo.dto.RagResponse;
import com.example.demo.dto.SourceDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class RagServiceTest {

    @Mock
    private ChatClient chatClient;

    @Mock
    private VectorStore vectorStore;

    @Mock
    private ChatClient.ChatClientRequestSpec requestSpec;

    @Mock
    private ChatClient.CallResponseSpec callResponseSpec;

    private RagService ragService;

    @BeforeEach
    void setUp() {
        ragService = new RagService(chatClient, vectorStore);
    }

    @Test
    void ask_類似検索結果を元に回答とsourcesを返す() {
        Document doc1 = new Document(
                "Spring AI は Java 向けの AI フレームワークです。",
                Map.of("fileName", "spring-ai.md", "chunkIndex", 0));
        Document doc2 = new Document(
                "pgvector は PostgreSQL のベクトル検索拡張です。",
                Map.of("fileName", "pgvector.md", "chunkIndex", 3));
        when(vectorStore.similaritySearch(any(SearchRequest.class)))
                .thenReturn(List.of(doc1, doc2));

        ArgumentCaptor<String> systemPromptCaptor = ArgumentCaptor.forClass(String.class);
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(systemPromptCaptor.capture())).thenReturn(requestSpec);
        when(requestSpec.user("Spring AIとは？")).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn("Spring AI は Java 向けの AI フレームワークです。");

        RagResponse response = ragService.ask("Spring AIとは？");

        assertThat(response.answer()).isEqualTo("Spring AI は Java 向けの AI フレームワークです。");
        assertThat(response.sources()).hasSize(2);

        SourceDocument first = response.sources().get(0);
        assertThat(first.fileName()).isEqualTo("spring-ai.md");
        assertThat(first.chunkIndex()).isEqualTo(0);
        assertThat(first.excerpt()).contains("Spring AI");

        SourceDocument second = response.sources().get(1);
        assertThat(second.fileName()).isEqualTo("pgvector.md");
        assertThat(second.chunkIndex()).isEqualTo(3);
        assertThat(second.excerpt()).contains("pgvector");

        // システムプロンプトに検索結果がコンテキストとして含まれている
        String systemPrompt = systemPromptCaptor.getValue();
        assertThat(systemPrompt).contains("Spring AI は Java 向けの AI フレームワークです。");
        assertThat(systemPrompt).contains("pgvector");
        assertThat(systemPrompt).contains("分かりません");
    }

    @Test
    void ask_検索結果が空のときsourcesは空でLLMには関連情報なしで問い合わせる() {
        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of());

        ArgumentCaptor<String> systemPromptCaptor = ArgumentCaptor.forClass(String.class);
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(systemPromptCaptor.capture())).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn("分かりません");

        RagResponse response = ragService.ask("未知の質問");

        assertThat(response.answer()).isEqualTo("分かりません");
        assertThat(response.sources()).isEmpty();
        assertThat(systemPromptCaptor.getValue()).contains("(関連情報なし)");
    }

    @Test
    void ask_長いチャンクはexcerptが200文字に切り詰められる() {
        String longText = "あ".repeat(500);
        Document doc = new Document(longText, Map.of("fileName", "long.md", "chunkIndex", 0));
        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of(doc));
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn("回答");

        RagResponse response = ragService.ask("質問");

        String excerpt = response.sources().get(0).excerpt();
        assertThat(excerpt).hasSize(201); // 200 + 末尾の「…」
        assertThat(excerpt).endsWith("…");
    }

    @Test
    void ask_VectorStore例外時はIllegalStateExceptionにラップしてLLMを呼ばない() {
        when(vectorStore.similaritySearch(any(SearchRequest.class)))
                .thenThrow(new RuntimeException("DB接続エラー"));

        assertThatThrownBy(() -> ragService.ask("質問"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ベクトル検索に失敗しました");

        verify(chatClient, never()).prompt();
    }
}
