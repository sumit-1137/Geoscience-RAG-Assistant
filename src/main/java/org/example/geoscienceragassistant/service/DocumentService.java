package org.example.geoscienceragassistant.service;

import org.example.geoscienceragassistant.model.Document;
import org.example.geoscienceragassistant.repository.DocumentChunkRepository;
import org.example.geoscienceragassistant.repository.DocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentChunkRepository chunkRepository;

    public DocumentService(
            DocumentRepository documentRepository,
            DocumentChunkRepository chunkRepository) {

        this.documentRepository = documentRepository;
        this.chunkRepository = chunkRepository;
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

        // First check whether document exists
        Document document =
                documentRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Document not found with id: " + id
                                )
                        );

        // Delete all child chunks first
        chunkRepository.deleteAll(
                chunkRepository.findByDocumentId(id)
        );

        // Then delete parent document
        documentRepository.delete(document);
    }
}