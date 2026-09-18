package org.example.geoscienceragassistant.controller;

import org.example.geoscienceragassistant.model.DocumentChunk;
import org.example.geoscienceragassistant.service.QdrantService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final QdrantService qdrantService;

    public SearchController(QdrantService qdrantService) {
        this.qdrantService = qdrantService;
    }

    @GetMapping
    public List<DocumentChunk> search(
            @RequestParam String query) {

        return qdrantService.searchSimilarChunks(query);
    }
}