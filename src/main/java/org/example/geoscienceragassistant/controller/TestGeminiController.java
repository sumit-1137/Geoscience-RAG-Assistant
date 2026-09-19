package org.example.geoscienceragassistant.controller;

import org.example.geoscienceragassistant.service.GeminiService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestGeminiController {

    private final GeminiService geminiService;

    public TestGeminiController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @GetMapping("/api/test-gemini")
    public String testGemini() {

        return geminiService.generateAnswer(
                "What is geoscience? Answer in one short sentence."
        );
    }
}