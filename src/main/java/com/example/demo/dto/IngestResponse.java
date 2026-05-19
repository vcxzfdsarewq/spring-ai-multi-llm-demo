package com.example.demo.dto;

import java.util.List;

public record IngestResponse(int totalChunks, int fileCount, List<String> files) {
}
