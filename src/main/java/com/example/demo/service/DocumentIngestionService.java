package com.example.demo.service;

import com.example.demo.dto.IngestResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Profile("rag")
public class DocumentIngestionService {

    private static final Logger log = LoggerFactory.getLogger(DocumentIngestionService.class);

    private final VectorStore vectorStore;
    private final TokenTextSplitter splitter;
    private final List<Resource> documentResources;

    public DocumentIngestionService(VectorStore vectorStore,
                                    TokenTextSplitter splitter,
                                    List<Resource> documentResources) {
        this.vectorStore = vectorStore;
        this.splitter = splitter;
        this.documentResources = documentResources;
    }

    public IngestResponse ingest() {
        List<Document> allChunks = new ArrayList<>();
        List<String> processedFiles = new ArrayList<>();

        for (Resource resource : documentResources) {
            String fileName = resource.getFilename();
            try {
                List<Document> chunks = readAndSplit(resource, fileName);
                allChunks.addAll(chunks);
                processedFiles.add(fileName);
                log.info("ファイル読み込み完了: {} ({} チャンク)", fileName, chunks.size());
            } catch (Exception e) {
                log.error("ファイル読み込み失敗: {}", fileName, e);
            }
        }

        if (!allChunks.isEmpty()) {
            vectorStore.add(allChunks);
            log.info("VectorStore へ登録完了: {} チャンク / {} ファイル", allChunks.size(), processedFiles.size());
        } else {
            log.warn("登録対象の文書がありませんでした");
        }

        return new IngestResponse(allChunks.size(), processedFiles.size(), processedFiles);
    }

    private List<Document> readAndSplit(Resource resource, String fileName) {
        TextReader reader = new TextReader(resource);
        reader.getCustomMetadata().put("fileName", fileName);
        List<Document> rawDocs = reader.get();

        List<Document> chunks = splitter.apply(rawDocs);

        for (int i = 0; i < chunks.size(); i++) {
            chunks.get(i).getMetadata().put("fileName", fileName);
            chunks.get(i).getMetadata().put("chunkIndex", i);
        }

        return chunks;
    }
}
