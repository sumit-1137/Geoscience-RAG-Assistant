package org.example.geoscienceragassistant.service;

import io.qdrant.client.QdrantClient;
import io.qdrant.client.grpc.Collections;
import io.qdrant.client.grpc.Points;
import org.example.geoscienceragassistant.embedding.EmbeddingService;
import org.example.geoscienceragassistant.model.DocumentChunk;
import org.example.geoscienceragassistant.repository.DocumentChunkRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class QdrantService {

    private final QdrantClient qdrantClient;
    private final EmbeddingService embeddingService;
    private final DocumentChunkRepository chunkRepository;

    private static final String COLLECTION_NAME =
            "geoscience_chunks";

    public QdrantService(
            QdrantClient qdrantClient,
            EmbeddingService embeddingService,
            DocumentChunkRepository chunkRepository) {

        this.qdrantClient = qdrantClient;
        this.embeddingService = embeddingService;
        this.chunkRepository = chunkRepository;
    }

    // =====================================================
    // CREATE COLLECTION
    // =====================================================

    public void createCollection() {

        try {

            boolean exists =
                    qdrantClient
                            .collectionExistsAsync(
                                    COLLECTION_NAME
                            )
                            .get();

            if (!exists) {

                qdrantClient
                        .createCollectionAsync(
                                COLLECTION_NAME,

                                Collections.VectorParams
                                        .newBuilder()
                                        .setSize(384)
                                        .setDistance(
                                                Collections.Distance.Cosine
                                        )
                                        .build()
                        )
                        .get();

                System.out.println(
                        "Qdrant collection created: "
                                + COLLECTION_NAME
                );

            } else {

                System.out.println(
                        "Qdrant collection already exists: "
                                + COLLECTION_NAME
                );
            }

        } catch (Exception e) {

            System.err.println(
                    "Failed to create Qdrant collection"
            );

            e.printStackTrace();
        }
    }

    // =====================================================
    // SAVE ONE CHUNK VECTOR
    // =====================================================

    public void saveChunk(DocumentChunk chunk) {

        try {

            if (chunk == null ||
                    chunk.getContent() == null ||
                    chunk.getContent().isBlank()) {

                return;
            }

            System.out.println(
                    "Generating embedding for chunk: "
                            + chunk.getId()
            );

            float[] vector =
                    embeddingService.generateEmbedding(
                            chunk.getContent()
                    );

            Points.PointStruct point =
                    Points.PointStruct.newBuilder()

                            .setId(
                                    Points.PointId.newBuilder()
                                            .setNum(
                                                    chunk.getId()
                                            )
                                            .build()
                            )

                            .setVectors(
                                    Points.Vectors.newBuilder()
                                            .setVector(
                                                    Points.Vector.newBuilder()
                                                            .addAllData(
                                                                    toFloatList(vector)
                                                            )
                                                            .build()
                                            )
                                            .build()
                            )

                            .build();

            qdrantClient
                    .upsertAsync(
                            COLLECTION_NAME,
                            List.of(point)
                    )
                    .get();

            System.out.println(
                    "Chunk vector saved to Qdrant: "
                            + chunk.getId()
            );

        } catch (Exception e) {

            System.err.println(
                    "Failed to save chunk to Qdrant: "
                            + (chunk != null
                            ? chunk.getId()
                            : "null")
            );

            e.printStackTrace();
        }
    }

    // =====================================================
    // SAVE MULTIPLE CHUNKS
    // =====================================================

    public void saveChunks(List<DocumentChunk> chunks) {

        if (chunks == null || chunks.isEmpty()) {

            System.out.println(
                    "No chunks to save to Qdrant."
            );

            return;
        }

        System.out.println(
                "Saving "
                        + chunks.size()
                        + " chunks to Qdrant..."
        );

        for (DocumentChunk chunk : chunks) {

            saveChunk(chunk);
        }

        System.out.println(
                "Finished saving chunks to Qdrant."
        );
    }

    // =====================================================
    // SEARCH SIMILAR CHUNKS
    // =====================================================

    public List<DocumentChunk> searchSimilarChunks(
            String query) {

        try {

            // -------------------------------------------------
            // 1. Validate query
            // -------------------------------------------------

            if (query == null || query.isBlank()) {

                System.out.println(
                        "Search query is empty."
                );

                return List.of();
            }

            System.out.println(
                    "Generating embedding for query: "
                            + query
            );

            // -------------------------------------------------
            // 2. Convert query into embedding
            // -------------------------------------------------

            float[] queryVector =
                    embeddingService.generateEmbedding(
                            query
                    );

            // -------------------------------------------------
            // 3. Search Qdrant
            // -------------------------------------------------

            Points.SearchPoints searchPoints =
                    Points.SearchPoints.newBuilder()

                            .setCollectionName(
                                    COLLECTION_NAME
                            )

                            .addAllVector(
                                    toFloatList(queryVector)
                            )

                            .setLimit(5)

                            .build();

            List<Points.ScoredPoint> searchResult =
                    qdrantClient
                            .searchAsync(searchPoints)
                            .get();

            System.out.println(
                    "Qdrant search completed. Results: "
                            + searchResult.size()
            );

            // -------------------------------------------------
            // 4. Load actual chunks from PostgreSQL
            // -------------------------------------------------

            List<DocumentChunk> results =
                    new ArrayList<>();

            final float MIN_SCORE = 0.35f;

            for (Points.ScoredPoint point : searchResult) {

                Long chunkId =
                        point.getId().getNum();

                float score =
                        point.getScore();

                System.out.println(
                        "Similar chunk ID: "
                                + chunkId
                                + " | Score: "
                                + score
                );

                // Only accept sufficiently relevant chunks
                if (score >= MIN_SCORE) {

                    chunkRepository
                            .findById(chunkId)
                            .ifPresent(results::add);

                } else {

                    System.out.println(
                            "Skipping low-score chunk: "
                                    + chunkId
                                    + " | Score: "
                                    + score
                    );
                }
            }

            // -------------------------------------------------
            // 5. Return chunks
            // -------------------------------------------------

            System.out.println(
                    "Returning "
                            + results.size()
                            + " chunks."
            );

            return results;

        } catch (Exception e) {

            System.err.println(
                    "Failed to search Qdrant"
            );

            e.printStackTrace();

            return List.of();
        }
    }

    // =====================================================
    // CONVERT float[] → List<Float>
    // =====================================================

    private List<Float> toFloatList(
            float[] vector) {

        return java.util.stream.IntStream
                .range(0, vector.length)
                .mapToObj(i -> vector[i])
                .toList();
    }
}