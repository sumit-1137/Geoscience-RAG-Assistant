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
                    "The information is not available in the uploaded documents.",
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

                1. Answer only using the information provided in the context.
                2. Do not use outside knowledge or invent facts.
                3. If the answer cannot be found in the context, say:
                   "The information is not available in the uploaded documents."
                4. Answer naturally and directly. Do not start every answer with
                   phrases such as "Based on the provided documents".
                5. Keep answers clear, concise, and easy to understand.
                6. For educational questions, explain concepts in simple language.
                7. Use bullet points when they improve readability.
                8. Do not mention Qdrant, embeddings, vector databases,
                   retrieval, prompts, or internal system details.
                9. Do not create or mention sources that are not provided.
                10. Do not repeat the question in the answer.

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
                    "Failed to generate an answer.",
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