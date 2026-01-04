package com.graduation.project.library.service;

import com.graduation.project.common.service.FileStorageService;
import com.graduation.project.library.constant.DocumentStatus;
import com.graduation.project.library.entity.Document;
import com.graduation.project.library.repository.DocumentRepository;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
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
    private final FileStorageService fileStorageService;

    @Async("taskExecutor")
    @Transactional
    public void processDocumentBackground(UUID documentId, String filePath) {
        log.info("Starting background processing for document: {}", documentId);
        File pdfFile = null;
        List<File> tempImageFiles = new ArrayList<>();

        try {
            // 1. Fetch Document
            Document document = documentRepository.findById(documentId)
                    .orElseThrow(() -> new RuntimeException("Document not found in async job: " + documentId));

            // 2. Resolve PDF File (Local or Remote)
            if (filePath.startsWith("http")) {
                pdfFile = downloadToTemp(filePath);
            } else {
                pdfFile = new File("uploads/" + filePath);
                if (!pdfFile.exists()) {
                    throw new IOException("File not found on disk: " + pdfFile.getAbsolutePath());
                }
            }

            // 3. Process PDF -> Temp Images
            int pageCount = pdfProcessingService.getPageCount(pdfFile);
            tempImageFiles = pdfProcessingService.convertPagesToImages(pdfFile, 3);

            // 4. Upload Images to Storage -> Public URLs
            List<String> previewImageUrls = new ArrayList<>();
            for (File imgFile : tempImageFiles) {
                String imageUrl = fileStorageService.store(imgFile, "images");
                previewImageUrls.add(imageUrl);
            }

            String thumbnailUrl = null;
            if (!previewImageUrls.isEmpty()) {
                thumbnailUrl = previewImageUrls.get(0);
            }

            // 5. Update Entity
            document.setPageCount(pageCount);
            document.setPreviewImages(previewImageUrls);
            document.setThumbnailUrl(thumbnailUrl);
            document.setSize(pdfFile.length()); // Note: if remote, this is temp file size

            document.setDocumentStatus(DocumentStatus.PENDING);
            documentRepository.save(document);

            log.info("Successfully processed document: {}", documentId);

        } catch (Exception e) {
            log.error("Error processing document: {}", documentId, e);
            try {
                Document document = documentRepository.findById(documentId).orElse(null);
                if (document != null) {
                    document.setDocumentStatus(DocumentStatus.REJECTED);
                    documentRepository.save(document);
                }
            } catch (Exception ex) {
                log.error("Failed to update error status", ex);
            }
        } finally {
            // 6. Cleanup Temp Files
            if (filePath.startsWith("http") && pdfFile != null && pdfFile.exists()) {
                pdfFile.delete();
            }
            if (tempImageFiles != null) {
                for (File f : tempImageFiles) {
                    if (f.exists())
                        f.delete();
                }
            }
        }
    }

    private File downloadToTemp(String urlStr) throws IOException {
        URL url = new URL(urlStr);
        File temp = File.createTempFile("downloaded-" + UUID.randomUUID(), ".pdf");
        try (InputStream in = url.openStream()) {
            Files.copy(in, temp.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        return temp;
    }
}
