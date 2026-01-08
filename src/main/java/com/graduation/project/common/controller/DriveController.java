package com.graduation.project.common.controller;

import com.google.api.services.drive.model.File;
import com.graduation.project.common.dto.FileResponse;
import com.graduation.project.common.service.DriveService;
import java.io.IOException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/drive")
@CrossOrigin(origins = "*") // Cho phép Frontend gọi API
public class DriveController {

  @Autowired
  private DriveService driveService;

  // 1. API Upload
  // Method: POST
  // URL: http://localhost:8080/api/drive/upload
  // Body (form-data): Key="file", Value=[Chọn file]
  @PostMapping("/upload")
  public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
    try {
      FileResponse response = driveService.uploadFile(file);
      return ResponseEntity.ok(response);
    } catch (IOException e) {
      return ResponseEntity.internalServerError().body("Lỗi upload: " + e.getMessage());
    }
  }

  // 2. API Xóa File
  // Method: DELETE
  // URL: http://localhost:8080/api/drive/delete/{fileId}
  @DeleteMapping("/{fileId}")
  public ResponseEntity<String> deleteFile(@PathVariable String fileId) {
    try {
      driveService.deleteFile(fileId);
      return ResponseEntity.ok("Đã xóa file thành công: " + fileId);
    } catch (IOException e) {
      return ResponseEntity.internalServerError().body("Lỗi xóa file: " + e.getMessage());
    }
  }

  // 3. API Lấy danh sách file
  // Method: GET
  @GetMapping("/list")
  public ResponseEntity<List<File>> listFiles() throws IOException {
    return ResponseEntity.ok(driveService.listFiles());
  }
}
