package com.example.demo.service;

import com.example.demo.dto.RagResponse;
import com.example.demo.dto.SourceDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Profile("rag")
public class RagService {

    private static final Logger log = LoggerFactory.getLogger(RagService.class);

    private static final int TOP_K = 5;
    private static final int EXCERPT_MAX_CHARS = 200;

    private static final String SYSTEM_PROMPT_TEMPLATE = """
            あなたは提供されたコンテキスト情報のみを使って質問に答えるアシスタントです。
            コンテキストに答えの根拠が含まれていない場合は、推測せずに「分かりません」と返してください。
            根拠がある場合は、その内容に基づいて簡潔に回答してください。

            # コンテキスト
            %s
            """;

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    public RagService(ChatClient chatClient, VectorStore vectorStore) {
        this.chatClient = chatClient;
        this.vectorStore = vectorStore;
    }

    public RagResponse ask(String question) {
        List<Document> docs = searchSimilarDocuments(question);
        log.info("類似検索結果: {} 件", docs.size());

        String systemPrompt = buildSystemPrompt(docs);
        String answer = invokeLlm(systemPrompt, question);

        List<SourceDocument> sources = docs.stream()
                .map(this::toSourceDocument)
                .toList();

        return new RagResponse(answer, sources);
    }

    @SuppressWarnings("null")
    private List<Document> searchSimilarDocuments(String question) {
        try {
            List<Document> result = vectorStore.similaritySearch(
                    SearchRequest.builder()
                            .query(question)
                            .topK(TOP_K)
                            .build()
            );
            return result == null ? List.of() : result;
        } catch (Exception e) {
            log.error("類似検索に失敗しました", e);
            throw new IllegalStateException("ベクトル検索に失敗しました: " + e.getMessage(), e);
        }
    }

    private String buildSystemPrompt(List<Document> docs) {
        String context = docs.stream()
                .map(Document::getText)
                .filter(t -> t != null && !t.isBlank())
                .collect(Collectors.joining("\n\n---\n\n"));
        return SYSTEM_PROMPT_TEMPLATE.formatted(
                context.isBlank() ? "(関連情報なし)" : context
        );
    }

    @SuppressWarnings("null")
    private String invokeLlm(String systemPrompt, String question) {
        try {
            return chatClient.prompt()
                    .system(systemPrompt)
                    .user(question)
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("LLM 呼び出しに失敗しました", e);
            throw e;
        }
    }

    private SourceDocument toSourceDocument(Document doc) {
        String fileName = (String) doc.getMetadata().getOrDefault("fileName", "unknown");
        Object chunkIndexObj = doc.getMetadata().get("chunkIndex");
        int chunkIndex = chunkIndexObj instanceof Number n ? n.intValue() : -1;
        return new SourceDocument(fileName, chunkIndex, excerpt(doc.getText()));
    }

    private String excerpt(String text) {
        if (text == null) {
            return "";
        }
        String normalized = text.replaceAll("\\s+", " ").trim();
        if (normalized.length() <= EXCERPT_MAX_CHARS) {
            return normalized;
        }
        return normalized.substring(0, EXCERPT_MAX_CHARS) + "…";
    }
}
