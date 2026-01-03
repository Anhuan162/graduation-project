package com.graduation.project.common.controller;

import com.graduation.project.auth.dto.response.ApiResponse;
import com.graduation.project.common.constant.AccessType;
import com.graduation.project.common.dto.FileMetadataResponse;
import com.graduation.project.common.service.FileService;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
public class FileController {

  private final FileService fileService;

  public FileController(FileService fileService) {
    this.fileService = fileService;
  }

  @PreAuthorize("hasAuthority('CREATE_OWN_FILE') or hasAuthority('MANAGE_ALL_FILE')")
  @PostMapping("/upload")
  public ApiResponse<FileMetadataResponse> uploadFile(
      @RequestParam("file") MultipartFile file,
      @RequestParam String folderName,
      @RequestParam(defaultValue = "PUBLIC") AccessType accessType,
      @RequestParam(required = false) String resourceType,
      @RequestParam(required = false) String resourceId)
      throws IOException {
    FileMetadataResponse metadata = fileService.uploadAndSaveFile(file, folderName, accessType, resourceType,
        resourceId);

    return ApiResponse.<FileMetadataResponse>builder().result(metadata).build();
  }

  @PostMapping("/upload-multiple-files")
  public ApiResponse<List<FileMetadataResponse>> uploadMultipleFiles(
      @RequestParam("files") List<MultipartFile> files, @RequestParam String folderName)
      throws IOException {

    List<FileMetadataResponse> responses = fileService.uploadMultipleFiles(files, folderName);
    return ApiResponse.<List<FileMetadataResponse>>builder().result(responses).build();
  }

  @PutMapping("/{fileId}/replace")
  public ApiResponse<FileMetadataResponse> replaceFile(
      @PathVariable UUID fileId, @RequestParam("file") MultipartFile newFile) throws IOException {

    FileMetadataResponse updated = fileService.replaceFile(fileId, newFile);

    return ApiResponse.<FileMetadataResponse>builder().result(updated).build();
  }

  @GetMapping("/user/{userId}")
  public ApiResponse<List<FileMetadataResponse>> getUserFiles(@PathVariable UUID userId) {
    List<FileMetadataResponse> result = fileService.findAllByUserId(userId);

    return ApiResponse.<List<FileMetadataResponse>>builder().result(result).build();
  }

  public record DeleteFilesRequest(List<UUID> fileIds) {
  }

  @PreAuthorize("hasAuthority('DELETE_FILES') or hasAuthority('DELETE_OWN_FILES')")
  @DeleteMapping("/{fileId}")
  public ApiResponse<String> deleteFile(@PathVariable UUID fileId) {
    fileService.deleteFile(fileId);
    return ApiResponse.ok("Deleted successfully");
  }

  @PreAuthorize("hasAuthority('DELETE_FILES') or hasAuthority('DELETE_ALL_FILES')")
  @PostMapping("/delete-all-files")
  public ApiResponse<String> deleteAllFiles(@RequestBody DeleteFilesRequest request) {
    fileService.deleteAllFiles(request.fileIds().stream().map(UUID::toString).toList());
    return ApiResponse.ok("Deleted successfully");
  }
}
