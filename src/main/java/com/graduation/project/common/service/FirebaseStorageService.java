package com.graduation.project.common.service;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.firebase.cloud.StorageClient;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Primary
@Slf4j
public class FirebaseStorageService implements FileStorageService {

    @Override
    public String store(MultipartFile file, String subDir) {
        try {
            String originalFilename = file.getOriginalFilename();
            String extension = "unknown";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
            }

            String newFilename = UUID.randomUUID().toString() + "." + extension;
            String filePath = subDir + "/" + newFilename;

            // Normalize path for Storage (remove leading slashes if any)
            if (filePath.startsWith("/")) {
                filePath = filePath.substring(1);
            }

            Bucket bucket = StorageClient.getInstance().bucket();
            Blob blob = bucket.create(filePath, file.getInputStream(), file.getContentType());

            // Construct Public URL
            // Format:
            // https://firebasestorage.googleapis.com/v0/b/<bucket>/o/<encoded-path>?alt=media
            String bucketName = bucket.getName();
            String encodedPath = URLEncoder.encode(filePath, StandardCharsets.UTF_8).replace("+", "%20");

            String publicUrl = String.format("https://firebasestorage.googleapis.com/v0/b/%s/o/%s?alt=media",
                    bucketName, encodedPath);

            log.info("Uploaded file to Firebase: {}", publicUrl);
            return publicUrl;

        } catch (IOException e) {
            log.error("Failed to upload file to Firebase", e);
            throw new RuntimeException("Failed to upload file to Firebase", e);
        }
    }

    @Override
    public String store(java.io.File file, String subDir) {
        try (java.io.InputStream inputStream = new java.io.FileInputStream(file)) {
            String originalFilename = file.getName();
            String extension = "unknown";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
            }

            String newFilename = UUID.randomUUID().toString() + "." + extension;
            String filePath = subDir + "/" + newFilename;

            // Normalize path for Storage (remove leading slashes if any)
            if (filePath.startsWith("/")) {
                filePath = filePath.substring(1);
            }

            Bucket bucket = StorageClient.getInstance().bucket();
            // Infer content type or default
            String contentType = "application/octet-stream";
            if (extension.equalsIgnoreCase("jpg") || extension.equalsIgnoreCase("jpeg"))
                contentType = "image/jpeg";
            else if (extension.equalsIgnoreCase("png"))
                contentType = "image/png";
            else if (extension.equalsIgnoreCase("pdf"))
                contentType = "application/pdf";

            Blob blob = bucket.create(filePath, inputStream, contentType);

            // Construct Public URL
            String bucketName = bucket.getName();
            String encodedPath = URLEncoder.encode(filePath, StandardCharsets.UTF_8).replace("+", "%20");

            String publicUrl = String.format("https://firebasestorage.googleapis.com/v0/b/%s/o/%s?alt=media",
                    bucketName, encodedPath);

            log.info("Uploaded file to Firebase: {}", publicUrl);
            return publicUrl;

        } catch (IOException e) {
            log.error("Failed to upload file to Firebase", e);
            throw new RuntimeException("Failed to upload file to Firebase", e);
        }
    }
}
