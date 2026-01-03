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

    public List<String> convertPagesToImages(File pdfFile, int maxPages) {
        List<String> imagePaths = new ArrayList<>();
        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            int pages = Math.min(document.getNumberOfPages(), maxPages);

            for (int page = 0; page < pages; ++page) {
                // Optimization: Scale 0.5f (equivalent to ~72 DPI if original is 144) or
                // directly use DPI
                // renderImageWithDPI(page, 72) is safer for consistent size
                BufferedImage bim = pdfRenderer.renderImageWithDPI(page, 72, ImageType.RGB);

                String imageName = UUID.randomUUID().toString() + "_p" + (page + 1) + ".jpg";
                Path imagePath = rootLocation.resolve("images").resolve(imageName);
                File outputFile = imagePath.toFile();

                // Ensure parent exists
                outputFile.getParentFile().mkdirs();

                ImageIO.write(bim, "jpg", outputFile);
                imagePaths.add("images/" + imageName);
            }
        } catch (IOException e) {
            log.error("Failed to convert PDF to images: " + pdfFile.getName(), e);
        }
        return imagePaths;
    }
}
