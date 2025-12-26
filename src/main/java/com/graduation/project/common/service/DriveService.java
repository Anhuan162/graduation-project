package com.graduation.project.common.service;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.InputStreamContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.Permission;
import com.graduation.project.common.dto.FileResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DriveService {

  @Autowired private Drive googleDrive;

  // Thay ID thư mục trên Drive của bạn vào đây (Cách 1 đã hướng dẫn)
  private static final String FOLDER_ID = "1mJwLQlKalK7PDoD4Vn3cz2GI3dLLBCsO";

  public FileResponse uploadFile(MultipartFile file) throws IOException {
    if (file.isEmpty()) {
      throw new IOException("File không được rỗng");
    }

    // 1. Cấu hình thông tin file
    File fileMetadata = new File();
    fileMetadata.setName(file.getOriginalFilename());
    fileMetadata.setParents(Collections.singletonList(FOLDER_ID));

    // 2. Upload file
    InputStreamContent mediaContent =
        new InputStreamContent(file.getContentType(), file.getInputStream());

    // Convert sang Google Docs
    fileMetadata.setMimeType("application/vnd.google-apps.document");

    // Upload và yêu cầu trả về các trường cần thiết
    File uploadedFile =
        googleDrive
            .files()
            .create(fileMetadata, mediaContent)
            .setFields("id, name, webContentLink, webViewLink")
                .setSupportsAllDrives(true)
                .setOcrLanguage("vi")   // nếu là ảnh/PDF → OCR sang text
            .execute();

    // 3. (QUAN TRỌNG) Cấp quyền "Anyone with link" để xem được file công khai
    // Nếu bạn muốn bảo mật chỉ mình Service Account xem thì bỏ bước này.
    try {
      Permission permission = new Permission().setType("anyone").setRole("reader");
      googleDrive.permissions().create(uploadedFile.getId(), permission).execute();
    } catch (Exception e) {
      System.err.println("Không thể set quyền public: " + e.getMessage());
    }

    // 4. Trả về kết quả
    return new FileResponse(
        uploadedFile.getId(),
        uploadedFile.getName(),
        uploadedFile.getWebContentLink(),
        uploadedFile.getWebViewLink(),
        file.getContentType());
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


//  upload text lên drive
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
    ByteArrayContent mediaContent =
            new ByteArrayContent("text/plain", content.getBytes(StandardCharsets.UTF_8));

    // 3. Upload file
    File uploadedFile = googleDrive
            .files()
            .create(fileMetadata, mediaContent)
            .setFields("id, name, webContentLink, webViewLink")
            .setSupportsAllDrives(true)
            .setOcrLanguage("vi")  // OCR nếu ảnh/PDF
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
            "text/plain"
    );
  }

  public FileResponse uploadFile(byte[] fileBytes, String fileName, String contentType) throws IOException {
    if (fileBytes == null || fileBytes.length == 0) {
      throw new IOException("File bytes không được rỗng");
    }

    // 1. Cấu hình metadata của file
    File fileMetadata = new File();
    fileMetadata.setName(fileName);
    fileMetadata.setParents(Collections.singletonList(FOLDER_ID));

    // Convert sang Google Docs
    fileMetadata.setMimeType("application/vnd.google-apps.document");

    // 2. Chuẩn bị nội dung file
    ByteArrayContent mediaContent = new ByteArrayContent(contentType, fileBytes);

    // 3. Upload file
    File uploadedFile =
            googleDrive
                    .files()
                    .create(fileMetadata, mediaContent)
                    .setFields("id, name, webContentLink, webViewLink")
                    .setSupportsAllDrives(true)
                    .setOcrLanguage("vi")  // OCR tiếng Việt cho PDF ảnh/ảnh
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
            contentType
    );
  }


}
