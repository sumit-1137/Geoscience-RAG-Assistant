package org.example.geoscienceragassistant.controller;

import org.example.geoscienceragassistant.embedding.EmbeddingService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/embeddings")
public class EmbeddingController {

    private final EmbeddingService embeddingService;

    public EmbeddingController(EmbeddingService embeddingService) {
        this.embeddingService = embeddingService;
    }

    @PostMapping(
            value = "/test",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Map<String, Object> testEmbedding(
            @RequestBody Map<String, String> request) throws Exception {

        String text = request.get("text");

        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException(
                    "text cannot be empty"
            );
        }

        float[] embedding =
                embeddingService.generateEmbedding(text);

        Map<String, Object> response = new HashMap<>();

        response.put("text", text);
        response.put("dimensions", embedding.length);
        response.put(
                "first_10_values",
                Arrays.copyOf(embedding, 10)
        );

        return response;
    }
}