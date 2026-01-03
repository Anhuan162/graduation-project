package com.graduation.project.common.service;

import com.graduation.project.security.exception.AppException;
import com.graduation.project.security.exception.ErrorCode;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
public class LocalFileStorageService {

    private final Path rootLocation = Paths.get("uploads");

    public LocalFileStorageService() {
        try {
            if (!Files.exists(rootLocation)) {
                Files.createDirectories(rootLocation);
            }
            Files.createDirectories(rootLocation.resolve("documents"));
            Files.createDirectories(rootLocation.resolve("images"));
        } catch (IOException e) {
            log.error("Could not initialize storage", e);
            throw new RuntimeException("Could not initialize storage", e);
        }
    }

    public String store(MultipartFile file, String subDir) {
        if (file.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        String contentType = file.getContentType();
        // Validate Content Type (Only allow PDF for documents)
        if (subDir.equals("documents")) {
            if (contentType == null || !contentType.equals("application/pdf")) {
                log.warn("Invalid file type uploaded: {}", contentType);
                // In a real scenario, use more robust check (Tika), but strict check is fine
                // for now
                throw new RuntimeException("Only PDF files are allowed");
            }
        }

        try {
            String originalFilename = file.getOriginalFilename();
            String extension = "pdf";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
            }

            // UUID Renaming
            String newFilename = UUID.randomUUID().toString() + "." + extension;

            Path destinationFile = this.rootLocation.resolve(subDir).resolve(newFilename)
                    .normalize().toAbsolutePath();

            // Security Check: prevent directory traversal
            if (!destinationFile.getParent().equals(this.rootLocation.resolve(subDir).normalize().toAbsolutePath())) {
                throw new RuntimeException("Cannot store file outside current directory.");
            }

            Files.copy(file.getInputStream(), destinationFile);

            // Return relative path for DB
            return subDir + "/" + newFilename;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file.", e);
        }
    }
}
