package com.graduation.project.common.service;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.firebase.cloud.StorageClient;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
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

            Storage storage = StorageClient.getInstance().bucket().getStorage();
            String bucketName = StorageClient.getInstance().bucket().getName();

            // 1. Generate Token
            String token = UUID.randomUUID().toString();

            // 2. Prepare Metadata
            Map<String, String> map = new HashMap<>();
            map.put("firebaseStorageDownloadTokens", token);

            BlobId blobId = BlobId.of(bucketName, filePath);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType(file.getContentType())
                    .setMetadata(map)
                    .build();

            // 3. Upload
            storage.create(blobInfo, file.getBytes());

            // 4. Construct Public URL with Token
            String encodedPath = URLEncoder.encode(filePath, StandardCharsets.UTF_8).replace("+", "%20");
            String publicUrl = String.format("https://firebasestorage.googleapis.com/v0/b/%s/o/%s?alt=media&token=%s",
                    bucketName, encodedPath, token);

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

            Storage storage = StorageClient.getInstance().bucket().getStorage();
            String bucketName = StorageClient.getInstance().bucket().getName();

            // 1. Generate Token
            String token = UUID.randomUUID().toString();

            // 2. Prepare Metadata
            Map<String, String> map = new HashMap<>();
            map.put("firebaseStorageDownloadTokens", token);

            // Infer content type
            String contentType = "application/octet-stream";
            if (extension.equalsIgnoreCase("jpg") || extension.equalsIgnoreCase("jpeg"))
                contentType = "image/jpeg";
            else if (extension.equalsIgnoreCase("png"))
                contentType = "image/png";
            else if (extension.equalsIgnoreCase("pdf"))
                contentType = "application/pdf";

            BlobId blobId = BlobId.of(bucketName, filePath);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType(contentType)
                    .setMetadata(map)
                    .build();

            // 3. Upload
            storage.create(blobInfo, java.nio.file.Files.readAllBytes(file.toPath()));

            // 4. Construct Public URL with Token
            String encodedPath = URLEncoder.encode(filePath, StandardCharsets.UTF_8).replace("+", "%20");
            String publicUrl = String.format("https://firebasestorage.googleapis.com/v0/b/%s/o/%s?alt=media&token=%s",
                    bucketName, encodedPath, token);

            log.info("Uploaded file to Firebase: {}", publicUrl);
            return publicUrl;

        } catch (IOException e) {
            log.error("Failed to upload file to Firebase", e);
            throw new RuntimeException("Failed to upload file to Firebase", e);
        }
    }

    @javax.annotation.PostConstruct
    public void init() {
        try {
            Bucket bucket = StorageClient.getInstance().bucket();

            // Configure CORS to allow any origin (for development)
            // In production, restrict this to your domain.
            com.google.cloud.storage.Cors cors = com.google.cloud.storage.Cors.newBuilder()
                    .setOrigins(java.util.Collections.singletonList(com.google.cloud.storage.Cors.Origin.of("*")))
                    .setMethods(java.util.List.of(com.google.cloud.storage.HttpMethod.GET,
                            com.google.cloud.storage.HttpMethod.HEAD, com.google.cloud.storage.HttpMethod.POST))
                    .setResponseHeaders(java.util.List.of("Content-Type", "Access-Control-Allow-Origin",
                            "x-goog-meta-firebasestoragedownloadtokens"))
                    .setMaxAgeSeconds(3600)
                    .build();

            bucket.toBuilder().setCors(java.util.Collections.singletonList(cors)).build().update();
            log.info("Successfully updated Firebase Storage CORS configuration");
        } catch (Exception e) {
            log.error("Failed to update Firebase Storage CORS configuration", e);
            // Don't throw exception to avoid stopping app startup if this fails (e.g.
            // permission issues)
        }
    }
}
