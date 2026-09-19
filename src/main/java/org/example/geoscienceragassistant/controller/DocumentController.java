package org.example.geoscienceragassistant.controller;

import org.example.geoscienceragassistant.model.Document;
import org.example.geoscienceragassistant.model.DocumentChunk;
import org.example.geoscienceragassistant.service.ChunkService;
import org.example.geoscienceragassistant.service.DocumentService;
import org.example.geoscienceragassistant.service.PdfService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;
    private final ChunkService chunkService;
    private final PdfService pdfService;

    // =========================
    // CONSTRUCTOR
    // =========================

    public DocumentController(
            DocumentService documentService,
            ChunkService chunkService,
            PdfService pdfService) {

        this.documentService = documentService;
        this.chunkService = chunkService;
        this.pdfService = pdfService;
    }

    // =========================
    // ADD DOCUMENT
    // =========================

    @PostMapping
    public Document addDocument(@RequestBody Document document) {

        return documentService.addDocument(document);
    }

    // =========================
    // GET ALL DOCUMENTS
    // =========================

    @GetMapping
    public List<Document> getAllDocuments() {

        return documentService.getAllDocuments();
    }

    // =========================
    // GET DOCUMENT BY ID
    // =========================

    @GetMapping("/{id}")
    public Document getDocumentById(
            @PathVariable Long id) {

        return documentService.getDocumentById(id);
    }

    // =========================
    // DELETE DOCUMENT
    // =========================

    @DeleteMapping("/{id}")
    public String deleteDocument(
            @PathVariable Long id) {

        documentService.deleteDocument(id);

        return "Document deleted successfully";
    }

    // =========================
    // UPDATE DOCUMENT
    // =========================

    @PutMapping("/{id}")
    public Document updateDocument(
            @PathVariable Long id,
            @RequestBody Document document) {

        return documentService.updateDocument(id, document);
    }

    // =========================
    // UPLOAD PDF
    // =========================

    @PostMapping(
            value = "/upload",
            consumes = "multipart/form-data"
    )
    public Document uploadPdf(

            @RequestParam("file")
            MultipartFile file,

            @RequestParam("title")
            String title,

            @RequestParam(
                    value = "description",
                    required = false
            )
            String description

    ) throws IOException {

        // 1. Extract text from PDF
        String extractedText =
                pdfService.extractText(file);

        // 2. Create Document object
        Document document = new Document();

        document.setTitle(title);
        document.setFileName(file.getOriginalFilename());
        document.setDescription(description);
        document.setContent(extractedText);

        // 3. Save document in database
        Document savedDocument =
                documentService.addDocument(document);

        // 4. Create chunks
        chunkService.createChunks(savedDocument);

        // 5. Return saved document
        return savedDocument;
    }

    // =========================
    // GET CHUNKS BY DOCUMENT
    // =========================

    @GetMapping("/{id}/chunks")
    public List<DocumentChunk> getChunks(
            @PathVariable Long id) {

        return chunkService.getChunksByDocument(id);
    }

    // =========================
    // SEARCH CHUNKS
    // =========================

    @GetMapping("/chunks/search")
    public List<DocumentChunk> searchChunks(
            @RequestParam String keyword) {

        return chunkService.searchChunks(keyword);
    }
}