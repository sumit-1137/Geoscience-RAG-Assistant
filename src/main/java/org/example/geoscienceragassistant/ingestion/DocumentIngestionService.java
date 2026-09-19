package org.example.geoscienceragassistant.ingestion;
import io.qdrant.client.grpc.JsonWithInt.Value;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.grpc.Points.PointStruct;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.example.geoscienceragassistant.embedding.EmbeddingService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.qdrant.client.PointIdFactory.id;
import static io.qdrant.client.ValueFactory.value;
import static io.qdrant.client.VectorsFactory.vectors;

@Service
public class DocumentIngestionService {

    private static final String COLLECTION_NAME =
            "geoscience_chunks";

    private final EmbeddingService embeddingService;
    private final QdrantClient qdrantClient;

    public DocumentIngestionService(
            EmbeddingService embeddingService,
            QdrantClient qdrantClient) {

        this.embeddingService = embeddingService;
        this.qdrantClient = qdrantClient;
    }

    /**
     * Reads a PDF, extracts its text,
     * creates chunks, generates embeddings
     * and stores them in Qdrant.
     */
    public int ingestPdf(
            MultipartFile file,
            String title,
            String description) throws Exception {

        // ------------------------------------------
        // 1. Validate uploaded file
        // ------------------------------------------

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "PDF file cannot be empty."
            );
        }

        // ------------------------------------------
        // 2. Extract text from PDF
        // ------------------------------------------

        byte[] pdfBytes = file.getBytes();

        String text;

        try (PDDocument document =
                     Loader.loadPDF(pdfBytes)) {

            PDFTextStripper stripper =
                    new PDFTextStripper();

            text = stripper.getText(document);
        }

        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException(
                    "PDF does not contain readable text."
            );
        }

        System.out.println(
                "PDF text extracted successfully."
        );

        // ------------------------------------------
        // 3. Split PDF text into chunks
        // ------------------------------------------

        List<String> chunks =
                splitIntoChunks(
                        text,
                        800,
                        100
                );

        System.out.println(
                "Total chunks created: "
                        + chunks.size()
        );

        // ------------------------------------------
        // 4. Generate embeddings
        // ------------------------------------------

        List<PointStruct> points =
                new ArrayList<>();

        for (int i = 0;
             i < chunks.size();
             i++) {

            String chunk =
                    chunks.get(i);

            System.out.println(
                    "Generating embedding for chunk "
                            + (i + 1)
                            + "/"
                            + chunks.size()
            );

            float[] embedding =
                    embeddingService
                            .generateEmbedding(chunk);

            // --------------------------------------
            // 5. Create unique Qdrant point ID
            // --------------------------------------

            UUID pointId =
                    UUID.randomUUID();

            // --------------------------------------
            // 6. Create payload
            // --------------------------------------

            Map<String, Value> payload =
                    Map.of(
                            "text",
                            value(chunk),

                            "title",
                            value(
                                    title != null
                                            ? title
                                            : ""
                            ),

                            "description",
                            value(
                                    description != null
                                            ? description
                                            : ""
                            ),

                            "filename",
                            value(
                                    file.getOriginalFilename() != null
                                            ? file.getOriginalFilename()
                                            : ""
                            ),

                            "chunk_index",
                            value(i)
                    );

            // --------------------------------------
            // 7. Create Qdrant point
            // --------------------------------------

            PointStruct point =
                    PointStruct.newBuilder()

                            .setId(
                                    id(pointId)
                            )

                            .setVectors(
                                    vectors(embedding)
                            )

                            .putAllPayload(
                                    payload
                            )

                            .build();

            points.add(point);
        }

        // ------------------------------------------
        // 8. Store all vectors in Qdrant
        // ------------------------------------------

        qdrantClient
                .upsertAsync(
                        COLLECTION_NAME,
                        points
                )
                .get();

        System.out.println(
                "Successfully stored "
                        + points.size()
                        + " chunks in Qdrant."
        );

        return points.size();
    }

    /**
     * Splits extracted PDF text into overlapping chunks.
     *
     * Example:
     *
     * chunk size = 800 characters
     * overlap    = 100 characters
     */
    private List<String> splitIntoChunks(
            String text,
            int chunkSize,
            int overlap) {

        List<String> chunks =
                new ArrayList<>();

        // Clean unnecessary whitespace
        text = text
                .replaceAll("\\s+", " ")
                .trim();

        if (text.isBlank()) {
            return chunks;
        }

        int start = 0;

        while (start < text.length()) {

            int end =
                    Math.min(
                            start + chunkSize,
                            text.length()
                    );

            String chunk =
                    text.substring(
                            start,
                            end
                    ).trim();

            if (!chunk.isBlank()) {
                chunks.add(chunk);
            }

            // Stop when we reach the end
            if (end == text.length()) {
                break;
            }

            // Move forward while keeping overlap
            start =
                    end - overlap;
        }

        return chunks;
    }
}