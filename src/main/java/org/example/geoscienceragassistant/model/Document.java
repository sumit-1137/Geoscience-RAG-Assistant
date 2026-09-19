package org.example.geoscienceragassistant.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "documents")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;

    private String fileName;

    @Column(columnDefinition = "TEXT")
    private String content;

    @OneToMany(
            mappedBy = "document",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @JsonIgnore
    private List<DocumentChunk> chunks = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<DocumentChunk> getChunks() {
        return chunks;
    }

    public void setChunks(List<DocumentChunk> chunks) {
        this.chunks = chunks;
    }

    public void addChunk(DocumentChunk chunk) {
        chunks.add(chunk);
        chunk.setDocument(this);
    }

    public void removeChunk(DocumentChunk chunk) {
        chunks.remove(chunk);
        chunk.setDocument(null);
    }
}