package com.graduation.project.common.service;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.firebase.cloud.StorageClient;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FirebaseService {
  /**
   * Upload a file to Firebase Cloud Storage
   *
   * @param file MultipartFile from client
   * @param folderName folder in storage (optional, e.g., "images/")
   * @return uploaded file name
   * @throws IOException
   */
  public String uploadFile(MultipartFile file, String folderName) throws IOException {
    String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
    Bucket bucket = StorageClient.getInstance().bucket();

    try (InputStream inputStream = file.getInputStream()) {
      bucket.create(folderName + fileName, inputStream, file.getContentType());
    }

    return fileName;
  }

  public String getPublicUrl(String fileName) {
    Bucket bucket = StorageClient.getInstance().bucket();
    Blob blob = bucket.get(fileName);

    if (blob == null) return null;

    // Lấy metadata token hiện tại
    String token =
        blob.getMetadata() != null ? blob.getMetadata().get("firebaseStorageDownloadTokens") : null;

    // Nếu chưa có token, tạo mới
    if (token == null || token.isEmpty()) {
      token = UUID.randomUUID().toString();
      blob.toBuilder()
          .setMetadata(Collections.singletonMap("firebaseStorageDownloadTokens", token))
          .build()
          .update();
    }

    return String.format(
        "https://firebasestorage.googleapis.com/v0/b/%s/o/%s?alt=media&token=%s",
        bucket.getName(), fileName.replace("/", "%2F"), token);
  }

  /**
   * Get public URL of uploaded file
   *
   * @param fileName name of file in storage
   * @return public URL
   */
  public String getSignedFileUrl(String fileName, long durationMinutes) {
    Bucket bucket = StorageClient.getInstance().bucket();
    Blob blob = bucket.get(fileName);
    if (blob == null) return null;

    URL signedUrl = blob.signUrl(durationMinutes, TimeUnit.MINUTES);
    return signedUrl.toString();
  }

  public void deleteFile(String fileName) {
    Bucket bucket = StorageClient.getInstance().bucket();
    Blob blob = bucket.get(fileName);
    if (blob != null) {
      blob.delete();
    }
  }

  public void deleteFileByUrl(String fileUrl) {
    try {
      // Lấy phần sau /o/
      String encodedPath = fileUrl.substring(fileUrl.indexOf("/o/") + 3);

      // Bỏ query params (?alt=media&token=...)
      int queryIndex = encodedPath.indexOf("?");
      if (queryIndex != -1) {
        encodedPath = encodedPath.substring(0, queryIndex);
      }

      // Decode %2F, %20 ...
      String filePath = URLDecoder.decode(encodedPath, StandardCharsets.UTF_8);

      // Xoá file
      deleteFile(filePath);

    } catch (Exception e) {
      throw new RuntimeException("Cannot delete Firebase file from URL", e);
    }
  }


}
