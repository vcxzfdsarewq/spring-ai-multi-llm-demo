package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.ai.openai.api-key=test-key",
        "app.llm.model=test-model"
})
class SpringAiDemoApplicationTests {

    @Test
    void contextLoads() {
    }
}
