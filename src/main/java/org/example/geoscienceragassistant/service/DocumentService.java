package org.example.geoscienceragassistant.service;

import org.example.geoscienceragassistant.model.Document;
import org.example.geoscienceragassistant.model.DocumentChunk;
import org.example.geoscienceragassistant.repository.DocumentChunkRepository;
import org.example.geoscienceragassistant.repository.DocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentChunkRepository chunkRepository;
    private final QdrantService qdrantService;

    public DocumentService(
            DocumentRepository documentRepository,
            DocumentChunkRepository chunkRepository,
            QdrantService qdrantService) {

        this.documentRepository = documentRepository;
        this.chunkRepository = chunkRepository;
        this.qdrantService = qdrantService;
    }

    // =========================
    // GET ALL DOCUMENTS
    // =========================

    public List<Document> getAllDocuments() {

        return documentRepository.findAll();
    }

    // =========================
    // GET DOCUMENT BY ID
    // =========================

    public Document getDocumentById(Long id) {

        return documentRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Document not found with id: " + id
                        )
                );
    }

    // =========================
    // ADD DOCUMENT
    // =========================

    public Document addDocument(Document document) {

        return documentRepository.save(document);
    }

    // =========================
    // UPDATE DOCUMENT
    // =========================

    public Document updateDocument(
            Long id,
            Document updatedDocument) {

        return documentRepository.findById(id)
                .map(existing -> {

                    existing.setTitle(
                            updatedDocument.getTitle()
                    );

                    existing.setDescription(
                            updatedDocument.getDescription()
                    );

                    existing.setFileName(
                            updatedDocument.getFileName()
                    );

                    existing.setContent(
                            updatedDocument.getContent()
                    );

                    return documentRepository.save(existing);
                })
                .orElseThrow(() ->
                        new RuntimeException(
                                "Document not found with id: " + id
                        )
                );
    }

    // =========================
    // DELETE DOCUMENT
    // =========================

    @Transactional
    public void deleteDocument(Long id) {

        // 1. Check whether document exists
        Document document =
                documentRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Document not found with id: " + id
                                )
                        );

        // 2. Get all chunks belonging to this document
        List<DocumentChunk> chunks =
                chunkRepository.findByDocumentId(id);

        System.out.println(
                "Found "
                        + chunks.size()
                        + " chunks for document: "
                        + id
        );

        // 3. Delete corresponding vectors from Qdrant
        qdrantService.deleteChunks(chunks);

        // 4. Delete chunks from PostgreSQL
        if (!chunks.isEmpty()) {

            chunkRepository.deleteAll(chunks);

            System.out.println(
                    "Deleted "
                            + chunks.size()
                            + " chunks from PostgreSQL."
            );
        }

        // 5. Delete the document from PostgreSQL
        documentRepository.delete(document);

        System.out.println(
                "Document deleted successfully: "
                        + id
        );
    }
}