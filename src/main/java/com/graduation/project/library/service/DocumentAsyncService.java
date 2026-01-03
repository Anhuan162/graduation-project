package com.graduation.project.library.service;

import com.graduation.project.library.constant.DocumentStatus;
import com.graduation.project.library.entity.Document;
import com.graduation.project.library.repository.DocumentRepository;
import java.io.File;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentAsyncService {

    private final DocumentRepository documentRepository;
    private final PdfProcessingService pdfProcessingService;

    @Async("taskExecutor")
    @Transactional
    public void processDocumentBackground(UUID documentId, String filePath) {
        log.info("Starting background processing for document: {}", documentId);
        try {
            // 1. Simulate delay if needed (optional)
            // Thread.sleep(1000);

            // 2. Fetch Document (Must exist because Sync method committed)
            Document document = documentRepository.findById(documentId)
                    .orElseThrow(() -> new RuntimeException("Document not found in async job: " + documentId));

            // 3. Process PDF
            // Construct absolute path properly.
            // Assuming LocalFileStorageService saves relative to project root 'uploads'
            // But filePath stored is relative "documents/uuid.pdf".
            File pdfFile = new File("uploads/" + filePath);

            if (!pdfFile.exists()) {
                log.error("File not found on disk: {}", pdfFile.getAbsolutePath());
                document.setDocumentStatus(DocumentStatus.REJECTED);
                documentRepository.save(document);
                return;
            }

            int pageCount = pdfProcessingService.getPageCount(pdfFile);
            List<String> previewImages = pdfProcessingService.convertPagesToImages(pdfFile, 3);

            String thumbnailUrl = null;
            if (!previewImages.isEmpty()) {
                thumbnailUrl = previewImages.get(0);
            }

            // 4. Update Entity
            document.setPageCount(pageCount);
            document.setPreviewImages(previewImages);
            document.setThumbnailUrl(thumbnailUrl);
            document.setSize(pdfFile.length());

            // Update status to PENDING (Waiting for Admin approval)
            document.setDocumentStatus(DocumentStatus.PENDING);

            documentRepository.save(document);
            log.info("Successfully processed document: {}", documentId);

        } catch (Exception e) {
            log.error("Error processing document: {}", documentId, e);
            // Handle failure status
            try {
                Document document = documentRepository.findById(documentId).orElse(null);
                if (document != null) {
                    document.setDocumentStatus(DocumentStatus.REJECTED); // Or ERROR
                    documentRepository.save(document);
                }
            } catch (Exception ex) {
                log.error("Failed to update error status", ex);
            }
        }
    }
}
