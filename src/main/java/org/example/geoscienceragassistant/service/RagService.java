package org.example.geoscienceragassistant.service;

import org.example.geoscienceragassistant.model.DocumentChunk;
import org.example.geoscienceragassistant.model.RagResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RagService {

    private final QdrantService qdrantService;
    private final GeminiService geminiService;

    public RagService(
            QdrantService qdrantService,
            GeminiService geminiService) {

        this.qdrantService = qdrantService;
        this.geminiService = geminiService;
    }

    public RagResponse generateAnswer(String question) {

        // =====================================================
        // 1. VALIDATE QUESTION
        // =====================================================

        if (question == null || question.isBlank()) {

            return new RagResponse(
                    "Please enter a question.",
                    new ArrayList<>()
            );
        }

        System.out.println(
                "RAG question: " + question
        );

        // =====================================================
        // 2. SEARCH QDRANT
        // =====================================================

        List<DocumentChunk> chunks;

        try {

            chunks =
                    qdrantService.searchSimilarChunks(question);

        } catch (Exception e) {

            e.printStackTrace();

            return new RagResponse(
                    "Unable to search the geoscience knowledge base.",
                    new ArrayList<>()
            );
        }

        if (chunks == null) {

            chunks = new ArrayList<>();
        }

        System.out.println(
                "Retrieved chunks: " + chunks.size()
        );

        // =====================================================
        // 3. NO RELEVANT INFORMATION
        // =====================================================

        if (chunks.isEmpty()) {

            return new RagResponse(
                    "I could not find relevant information in the uploaded geoscience documents.",
                    new ArrayList<>()
            );
        }

        // =====================================================
        // 4. BUILD CONTEXT
        // =====================================================

        StringBuilder context =
                new StringBuilder();

        for (DocumentChunk chunk : chunks) {

            if (chunk == null) {
                continue;
            }

            if (chunk.getContent() == null ||
                    chunk.getContent().isBlank()) {

                continue;
            }

            context.append(
                    "\n--- Document Chunk ---\n"
            );

            context.append(
                    chunk.getContent()
            );

            context.append(
                    "\n"
            );
        }

        // =====================================================
        // 5. CREATE RAG PROMPT
        // =====================================================

        String prompt =
                """
                You are GeoMind AI, a Geoscience Knowledge Assistant.

                Answer the user's question using ONLY the information
                provided in the context below.

                Rules:

                1. Do not invent facts.
                2. Do not use outside knowledge.
                3. If the answer is not present in the context,
                   clearly say:
                   "The information is not available in the uploaded documents."
                4. Keep the answer clear and easy to understand.
                5. For educational questions, explain the answer simply.
                6. Do not mention Qdrant, embeddings, vector databases,
                   retrieval, or internal system details.
                7. Do not create fake sources.

                ================= CONTEXT =================

                %s

                ================= QUESTION =================

                %s

                ================= ANSWER =================
                """.formatted(
                        context,
                        question
                );

        // =====================================================
        // 6. SEND TO GEMINI
        // =====================================================

        System.out.println(
                "Sending retrieved context to Gemini..."
        );

        String answer;

        try {

            answer =
                    geminiService.generateAnswer(prompt);

        } catch (Exception e) {

            e.printStackTrace();

            return new RagResponse(
                    "Failed to generate answer from Gemini.",
                    createSources(chunks)
            );
        }

        // =====================================================
        // 7. CHECK GEMINI RESPONSE
        // =====================================================

        if (answer == null || answer.isBlank()) {

            answer =
                    "I could not generate an answer from the provided documents.";
        }

        System.out.println(
                "Gemini answer generated."
        );

        // =====================================================
        // 8. CREATE SOURCES
        // =====================================================

        List<RagResponse.Source> sources =
                createSources(chunks);

        // =====================================================
        // 9. RETURN FINAL RESPONSE
        // =====================================================

        return new RagResponse(
                answer,
                sources
        );
    }

    // =========================================================
    // CREATE SOURCE LIST
    // =========================================================

    private List<RagResponse.Source> createSources(
            List<DocumentChunk> chunks) {

        List<RagResponse.Source> sources =
                new ArrayList<>();

        if (chunks == null) {
            return sources;
        }

        for (DocumentChunk chunk : chunks) {

            if (chunk == null) {
                continue;
            }

            String fileName =
                    "Unknown document";

            if (chunk.getDocument() != null) {

                if (chunk.getDocument().getFileName() != null &&
                        !chunk.getDocument().getFileName().isBlank()) {

                    fileName =
                            chunk.getDocument().getFileName();
                }
            }

            Integer chunkIndex =
                    chunk.getChunkIndex();

            sources.add(
                    new RagResponse.Source(
                            fileName,
                            chunkIndex
                    )
            );
        }

        return sources;
    }
}