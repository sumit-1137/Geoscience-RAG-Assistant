package org.example.geoscienceragassistant.controller;

import org.example.geoscienceragassistant.model.RagResponse;
import org.example.geoscienceragassistant.service.RagService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rag")
public class RagController {

    private final RagService ragService;

    public RagController(RagService ragService) {
        this.ragService = ragService;
    }

    @GetMapping
    public RagResponse askQuestion(
            @RequestParam String question) {

        System.out.println(
                "RAG question: " + question
        );

        return ragService.generateAnswer(question);
    }
}