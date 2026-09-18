package org.example.geoscienceragassistant.service;

import org.example.geoscienceragassistant.model.Document;
import org.example.geoscienceragassistant.model.DocumentChunk;
import org.example.geoscienceragassistant.repository.DocumentChunkRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChunkService {

    private final DocumentChunkRepository chunkRepository;
    private final QdrantService qdrantService;

    public ChunkService(
            DocumentChunkRepository chunkRepository,
            QdrantService qdrantService) {

        this.chunkRepository = chunkRepository;
        this.qdrantService = qdrantService;
    }

    public List<DocumentChunk> createChunks(Document document) {

        List<DocumentChunk> chunks = new ArrayList<>();

        if (document == null ||
                document.getContent() == null ||
                document.getContent().isBlank()) {

            return chunks;
        }

        String content = document.getContent();

        int chunkSize = 1000;
        int overlap = 100;

        int index = 0;
        int chunkIndex = 0;

        while (index < content.length()) {

            int end = Math.min(
                    index + chunkSize,
                    content.length()
            );

            String chunkText =
                    content.substring(index, end);

            DocumentChunk chunk =
                    new DocumentChunk();

            chunk.setContent(chunkText);
            chunk.setChunkIndex(chunkIndex);
            chunk.setDocument(document);

            chunks.add(chunk);

            chunkIndex++;

            if (end == content.length()) {
                break;
            }

            index = end - overlap;
        }

        // -------------------------------------------------
        // SAVE CHUNKS TO POSTGRESQL
        // -------------------------------------------------

        List<DocumentChunk> savedChunks =
                chunkRepository.saveAll(chunks);

        System.out.println(
                "Chunks saved to PostgreSQL: "
                        + savedChunks.size()
        );

        // -------------------------------------------------
        // SAVE EMBEDDINGS TO QDRANT
        // -------------------------------------------------

        qdrantService.saveChunks(savedChunks);

        return savedChunks;
    }

    public List<DocumentChunk> getChunksByDocument(
            Long documentId) {

        return chunkRepository.findByDocumentId(
                documentId
        );
    }

    public List<DocumentChunk> searchChunks(
            String keyword) {

        if (keyword == null ||
                keyword.isBlank()) {

            return List.of();
        }

        return chunkRepository
                .findByContentContainingIgnoreCase(
                        keyword
                );
    }

    public void deleteChunksByDocument(
            Long documentId) {

        List<DocumentChunk> chunks =
                chunkRepository.findByDocumentId(
                        documentId
                );

        if (!chunks.isEmpty()) {

            chunkRepository.deleteAll(chunks);
        }
    }
}