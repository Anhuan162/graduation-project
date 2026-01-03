package com.graduation.project.common.service;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.firebase.cloud.StorageClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class FirebaseService {

  public record FileUploadResult(String url, String storagePath) {
  }

  /**
   * Upload file và trả về Full URL và storage path
   */
  public FileUploadResult uploadFile(MultipartFile file, String folderName) throws IOException {
    String originalFilename = file.getOriginalFilename();
    String extension = "";
    if (originalFilename != null && originalFilename.contains(".")) {
      extension = originalFilename.substring(originalFilename.lastIndexOf("."));
    }

    // Tạo unique file name để tránh trùng đè
    String fileName = UUID.randomUUID().toString() + extension;

    // Normalize folder name
    String objectName;
    if (folderName == null || folderName.isEmpty()) {
      objectName = fileName;
    } else {
      String normalizedFolder = folderName.endsWith("/") ? folderName.substring(0, folderName.length() - 1)
          : folderName;
      objectName = normalizedFolder + "/" + fileName;
    }

    Storage storage = StorageClient.getInstance().bucket().getStorage();
    String bucketName = getBucketName();

    // 1. Tạo Token ngay tại đây (Client-side generation)
    String token = UUID.randomUUID().toString();

    // 2. Prepare Metadata
    Map<String, String> map = new HashMap<>();
    map.put("firebaseStorageDownloadTokens", token); // Key bắt buộc của Firebase

    BlobId blobId = BlobId.of(bucketName, objectName);
    String contentType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";
    BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
        .setContentType(contentType)
        .setMetadata(map) // Gắn token vào ngay lúc tạo
        .build();

    // 3. Upload
    storage.create(blobInfo, file.getBytes());

    // 4. Return Full URL và storage path
    String publicUrl = generateFirebaseUrl(bucketName, objectName, token);
    log.debug("Uploaded file '{}' to Firebase Storage", objectName);
    return new FileUploadResult(publicUrl, objectName);
  }

  public void deleteFile(String storagePath) {
    if (storagePath == null || storagePath.isEmpty())
      return;
    Bucket bucket = StorageClient.getInstance().bucket();
    Blob blob = bucket.get(storagePath);
    if (blob != null) {
      blob.delete();
    }
  }

  public String generateFirebaseUrl(String bucketName, String objectName, String token) {
    if (bucketName == null || bucketName.isEmpty()) {
      throw new IllegalArgumentException("Bucket name cannot be null or empty");
    }
    if (objectName == null || objectName.isEmpty()) {
      throw new IllegalArgumentException("Object name cannot be null or empty");
    }
    if (token == null || token.isEmpty()) {
      throw new IllegalArgumentException("Token cannot be null or empty");
    }

    // Encode object name to handle paths and special characters
    String encodedName = URLEncoder.encode(objectName, StandardCharsets.UTF_8).replace("+", "%20");
    if (bucketName == null || bucketName.isEmpty()) {
      throw new IllegalArgumentException("Bucket name cannot be null or empty");
    }

    // Construct URL explicitly using concatenation to avoid any String.format
    // issues
    String finalUrl = "https://firebasestorage.googleapis.com/v0/b/" + bucketName + "/o/" + encodedName
        + "?alt=media&token=" + token;

    log.debug("Generated Firebase URL for bucket={}, name={}", bucketName, encodedName);
    return finalUrl;
  }

  public String getBucketName() {
    return StorageClient.getInstance().bucket().getName();
  }
}
