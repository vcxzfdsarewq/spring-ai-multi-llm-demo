package com.example.demo.controller;

import com.example.demo.dto.IngestResponse;
import com.example.demo.dto.RagRequest;
import com.example.demo.dto.RagResponse;
import com.example.demo.service.DocumentIngestionService;
import com.example.demo.service.RagService;
import jakarta.validation.Valid;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rag")
@Profile("rag")
public class RagController {

    private final DocumentIngestionService ingestionService;
    private final RagService ragService;

    public RagController(DocumentIngestionService ingestionService, RagService ragService) {
        this.ingestionService = ingestionService;
        this.ragService = ragService;
    }

    @PostMapping("/ingest")
    public ResponseEntity<IngestResponse> ingest() {
        return ResponseEntity.ok(ingestionService.ingest());
    }

    @PostMapping("/ask")
    public ResponseEntity<RagResponse> ask(@Valid @RequestBody RagRequest request) {
        return ResponseEntity.ok(ragService.ask(request.question()));
    }
}
