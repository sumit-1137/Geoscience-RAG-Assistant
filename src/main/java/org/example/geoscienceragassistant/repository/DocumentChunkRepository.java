package org.example.geoscienceragassistant.repository;

import org.example.geoscienceragassistant.model.DocumentChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentChunkRepository
        extends JpaRepository<DocumentChunk, Long> {

    List<DocumentChunk> findByDocumentId(Long documentId);

    List<DocumentChunk> findByContentContainingIgnoreCase(String keyword);
}