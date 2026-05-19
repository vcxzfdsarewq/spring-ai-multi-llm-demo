package com.example.demo.dto;

import java.util.List;

public record RagResponse(String answer, List<SourceDocument> sources) {}
