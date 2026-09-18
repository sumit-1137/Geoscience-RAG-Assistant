package org.example.geoscienceragassistant.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class PdfService {

    /**
     * Extract text from an uploaded PDF file.
     */
    public String extractText(MultipartFile file) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "PDF file cannot be empty"
            );
        }

        try (PDDocument document =
                     Loader.loadPDF(file.getBytes())) {

            PDFTextStripper stripper =
                    new PDFTextStripper();

            return stripper.getText(document);
        }
    }

    /**
     * Extract text from PDF bytes.
     */
    public String extractText(byte[] pdfBytes)
            throws IOException {

        if (pdfBytes == null || pdfBytes.length == 0) {
            throw new IllegalArgumentException(
                    "PDF data cannot be empty"
            );
        }

        try (PDDocument document =
                     Loader.loadPDF(pdfBytes)) {

            PDFTextStripper stripper =
                    new PDFTextStripper();

            return stripper.getText(document);
        }
    }
}