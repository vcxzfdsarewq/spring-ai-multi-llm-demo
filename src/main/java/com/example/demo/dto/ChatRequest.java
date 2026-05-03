package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;

public record ChatRequest(
        @NotBlank(message = "質問は必須です") String question
) {}
