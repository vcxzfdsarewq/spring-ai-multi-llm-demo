package com.example.demo.service;

import com.example.demo.dto.IngestResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class DocumentIngestionServiceTest {

    @Mock
    private VectorStore vectorStore;

    @Test
    void ingest_チャンクにfileNameとchunkIndexが設定される() {
        Resource resource = new ClassPathResource("test-docs/sample.md");
        var service = new DocumentIngestionService(
                vectorStore, new TokenTextSplitter(), List.of(resource));

        IngestResponse response = service.ingest();

        ArgumentCaptor<List<Document>> captor = ArgumentCaptor.forClass(List.class);
        verify(vectorStore).add(captor.capture());

        List<Document> chunks = captor.getValue();
        assertThat(chunks).isNotEmpty();
        assertThat(chunks).allSatisfy(chunk -> {
            assertThat(chunk.getMetadata()).containsKey("fileName");
            assertThat(chunk.getMetadata()).containsKey("chunkIndex");
            assertThat(chunk.getMetadata().get("fileName")).isEqualTo("sample.md");
        });

        // chunkIndex は 0 始まりで連番になっている
        for (int i = 0; i < chunks.size(); i++) {
            assertThat(chunks.get(i).getMetadata().get("chunkIndex")).isEqualTo(i);
        }

        assertThat(response.totalChunks()).isEqualTo(chunks.size());
        assertThat(response.fileCount()).isEqualTo(1);
        assertThat(response.files()).containsExactly("sample.md");
    }

    @Test
    void ingest_ファイルリストが空のときVectorStoreに追加しない() {
        var service = new DocumentIngestionService(
                vectorStore, new TokenTextSplitter(), List.of());

        IngestResponse response = service.ingest();

        assertThat(response.totalChunks()).isZero();
        assertThat(response.fileCount()).isZero();
        assertThat(response.files()).isEmpty();
        verify(vectorStore, never()).add(anyList());
    }

    @Test
    void ingest_存在しないファイルをスキップして他のファイルは処理する() {
        Resource bad = new ClassPathResource("test-docs/not-exist.md");
        Resource good = new ClassPathResource("test-docs/sample.md");
        var service = new DocumentIngestionService(
                vectorStore, new TokenTextSplitter(), List.of(bad, good));

        IngestResponse response = service.ingest();

        // 壊れたファイルはスキップ、正常ファイルは登録
        assertThat(response.fileCount()).isEqualTo(1);
        assertThat(response.files()).containsExactly("sample.md");
        assertThat(response.totalChunks()).isPositive();
    }
}
