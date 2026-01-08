package com.graduation.project.common.service;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.InputStreamContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.Permission;
import com.graduation.project.common.dto.FileResponse;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DriveService {

  @Autowired
  private Drive googleDrive;

  // Thay ID thư mục trên Drive của bạn vào đây (Cách 1 đã hướng dẫn)
  private static final String FOLDER_ID = "1mJwLQlKalK7PDoD4Vn3cz2GI3dLLBCsO";

  /**
   * Upload file from InputStream (Memory Efficient)
   */
  public FileResponse uploadFile(InputStream inputStream, String fileName, String contentType) throws IOException {
    if (inputStream == null) {
      throw new IOException("InputStream cannot be null");
    }

    // 1. Configure file metadata
    File fileMetadata = new File();
    fileMetadata.setName(fileName);
    fileMetadata.setParents(Collections.singletonList(FOLDER_ID));

    // SMART MAPPING: Convert based on Source MimeType
    String targetMimeType = getTargetMimeType(contentType);
    if (targetMimeType != null) {
      fileMetadata.setMimeType(targetMimeType);
    }

    // 2. Prepare content
    InputStreamContent mediaContent = new InputStreamContent(contentType, inputStream);

    // 3. Upload
    File uploadedFile = googleDrive.files().create(fileMetadata, mediaContent)
        .setFields("id, name, webContentLink, webViewLink")
        .setSupportsAllDrives(true)
        .execute();

    // 4. Set Permission (Public)
    try {
      Permission permission = new Permission().setType("anyone").setRole("reader");
      googleDrive.permissions().create(uploadedFile.getId(), permission).execute();
    } catch (Exception e) {
      System.err.println("Cannot set public permission: " + e.getMessage());
    }

    return new FileResponse(
        uploadedFile.getId(),
        uploadedFile.getName(),
        uploadedFile.getWebContentLink(),
        uploadedFile.getWebViewLink(),
        contentType);
  }

  // Deprecated: byte[] method (Keep for compatibility if needed, else remove)
  public FileResponse uploadFile(MultipartFile file) throws IOException {
    return uploadFile(file.getInputStream(), file.getOriginalFilename(), file.getContentType());
  }

  // Xóa file
  public void deleteFile(String fileId) throws IOException {
    googleDrive.files().delete(fileId).execute();
  }

  // Lấy danh sách file trong folder (Optional)
  public List<File> listFiles() throws IOException {
    String query = "'" + FOLDER_ID + "' in parents and trashed = false";
    return googleDrive
        .files()
        .list()
        .setQ(query)
        .setFields("files(id, name, webViewLink)")
        .execute()
        .getFiles();
  }

  // upload text lên drive
  public FileResponse uploadTextToDrive(String fileName, String content) throws IOException {

    if (content == null || content.isEmpty()) {
      throw new IOException("Nội dung file không được rỗng");
    }

    // 1. Cấu hình metadata file
    File fileMetadata = new File();
    fileMetadata.setName(fileName);
    fileMetadata.setParents(Collections.singletonList(FOLDER_ID));

    // Convert sang Google Docs
    fileMetadata.setMimeType("application/vnd.google-apps.document");

    // 2. Chuyển nội dung text thành InputStreamContent
    ByteArrayContent mediaContent = new ByteArrayContent("text/plain", content.getBytes(StandardCharsets.UTF_8));

    // 3. Upload file
    File uploadedFile = googleDrive
        .files()
        .create(fileMetadata, mediaContent)
        .setFields("id, name, webContentLink, webViewLink")
        .setSupportsAllDrives(true)
        .setOcrLanguage("vi") // OCR nếu ảnh/PDF
        .execute();

    // 4. Set quyền công khai (optional)
    try {
      Permission permission = new Permission().setType("anyone").setRole("reader");
      googleDrive.permissions().create(uploadedFile.getId(), permission).execute();
    } catch (Exception e) {
      System.err.println("Không thể set quyền public: " + e.getMessage());
    }

    // 5. Trả về kết quả
    return new FileResponse(
        uploadedFile.getId(),
        uploadedFile.getName(),
        uploadedFile.getWebContentLink(),
        uploadedFile.getWebViewLink(),
        "text/plain");
  }

  public FileResponse uploadFile(byte[] fileBytes, String fileName, String contentType) throws IOException {
    if (fileBytes == null || fileBytes.length == 0) {
      throw new IOException("File bytes không được rỗng");
    }

    // 1. Cấu hình metadata của file
    File fileMetadata = new File();
    fileMetadata.setName(fileName);
    fileMetadata.setParents(Collections.singletonList(FOLDER_ID));

    // SMART MAPPING: Convert based on Source MimeType
    String targetMimeType = getTargetMimeType(contentType);
    if (targetMimeType != null) {
      fileMetadata.setMimeType(targetMimeType);
    }

    // 2. Chuẩn bị nội dung file
    ByteArrayContent mediaContent = new ByteArrayContent(contentType, fileBytes);

    // 3. Upload file
    File uploadedFile = googleDrive
        .files()
        .create(fileMetadata, mediaContent)
        .setFields("id, name, webContentLink, webViewLink")
        .setSupportsAllDrives(true)
        .setOcrLanguage("vi") // OCR tiếng Việt cho PDF ảnh/ảnh
        .execute();

    // 4. Set quyền public cho file
    try {
      Permission permission = new Permission()
          .setType("anyone")
          .setRole("reader");
      googleDrive.permissions().create(uploadedFile.getId(), permission).execute();
    } catch (Exception e) {
      System.err.println("Không thể set quyền public: " + e.getMessage());
    }

    // 5. Trả response
    return new FileResponse(
        uploadedFile.getId(),
        uploadedFile.getName(),
        uploadedFile.getWebContentLink(),
        uploadedFile.getWebViewLink(),
        contentType);
  }

  // Helper method xác định Target MimeType
  private String getTargetMimeType(String sourceMimeType) {
    if (sourceMimeType == null)
      return null;

    switch (sourceMimeType) {
      case "application/msword":
      case "application/vnd.openxmlformats-officedocument.wordprocessingml.document":
      case "text/plain":
      case "text/html":
      case "application/pdf": // PDF convert sang Doc để lấy text (OCR)
        return "application/vnd.google-apps.document";

      case "application/vnd.ms-excel":
      case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet":
      case "text/csv":
        return "application/vnd.google-apps.spreadsheet"; // Excel phải sang Sheet

      case "application/vnd.ms-powerpoint":
      case "application/vnd.openxmlformats-officedocument.presentationml.presentation":
        return "application/vnd.google-apps.presentation"; // PPT sang Slide

      default:
        return null; // Giữ nguyên gốc (Ví dụ: Ảnh, Zip, Video)
    }
  }
}
