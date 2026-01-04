package com.graduation.project.library.service;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.imageio.ImageIO;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PdfProcessingService {

    private final Path rootLocation = Paths.get("uploads");

    public int getPageCount(File pdfFile) {
        // Try-with-resources to ensure PDDocument is closed!
        try (PDDocument document = PDDocument.load(pdfFile)) {
            return document.getNumberOfPages();
        } catch (IOException e) {
            log.error("Failed to count pages for file: " + pdfFile.getName(), e);
            return 0;
        }
    }

    public List<File> convertPagesToImages(File pdfFile, int maxPages) {
        List<File> imageFiles = new ArrayList<>();
        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            int pages = Math.min(document.getNumberOfPages(), maxPages);

            for (int page = 0; page < pages; ++page) {
                // Optimization: Scale 0.5f (equivalent to ~72 DPI if original is 144) or
                // directly use DPI
                // renderImageWithDPI(page, 72) is safer for consistent size
                BufferedImage bim = pdfRenderer.renderImageWithDPI(page, 72, ImageType.RGB);

                File tempFile = File.createTempFile("pdf-preview-" + UUID.randomUUID(), ".jpg");
                ImageIO.write(bim, "jpg", tempFile);
                imageFiles.add(tempFile);
            }
        } catch (IOException e) {
            log.error("Failed to convert PDF to images: " + pdfFile.getName(), e);
        }
        return imageFiles;
    }
}
