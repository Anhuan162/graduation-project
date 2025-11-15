package com.graduation.project.common.service;

import com.graduation.project.auth.exception.AppException;
import com.graduation.project.auth.exception.ErrorCode;
import com.graduation.project.auth.permission_handler.FileMetadataPermissionHandler;
import com.graduation.project.auth.repository.FileMetadataRepository;
import com.graduation.project.auth.service.CurrentUserService;
import com.graduation.project.auth.service.FirebaseService;
import com.graduation.project.common.dto.FileMetadataResponse;
import com.graduation.project.common.entity.AccessType;
import com.graduation.project.common.entity.FileMetadata;
import com.graduation.project.common.entity.ResourceType;
import com.graduation.project.common.entity.User;
import com.graduation.project.common.mapper.FileMetadataMapper;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileService {

  private final FirebaseService firebaseService;
  private final FileMetadataRepository fileRepo;
  private final CurrentUserService currentUserService;
  private final FileMetadataMapper fileMetadataMapper;
  private final FileMetadataPermissionHandler fileMetadataPermissionHandler;

  public FileService(
      FirebaseService firebaseService,
      FileMetadataRepository fileRepo,
      CurrentUserService currentUserService,
      FileMetadataMapper fileMetadataMapper,
      FileMetadataPermissionHandler fileMetadataPermissionHandler) {
    this.firebaseService = firebaseService;
    this.fileRepo = fileRepo;
    this.currentUserService = currentUserService;
    this.fileMetadataMapper = fileMetadataMapper;
    this.fileMetadataPermissionHandler = fileMetadataPermissionHandler;
  }

  public FileMetadataResponse uploadAndSaveFile(
      MultipartFile file,
      String folderName,
      AccessType accessType,
      String resourceType,
      String resourceId)
      throws IOException {
    // Upload file lên Firebase Storage
    String fileName = firebaseService.uploadFile(file, folderName);
    User currentUser = currentUserService.getCurrentUserEntity();

    String url =
        AccessType.PUBLIC.equals(accessType)
            ? firebaseService.getPublicUrl(folderName + fileName)
            : null;

    // Lưu metadata vào DB
    FileMetadata metadata =
        FileMetadata.builder()
            .user(currentUser)
            .fileName(fileName)
            .folder(folderName)
            .url(url)
            .contentType(file.getContentType())
            .accessType(accessType)
            .resourceType(Objects.isNull(resourceType) ? null : ResourceType.valueOf(resourceType))
            .resourceId(Objects.isNull(resourceId) ? null : UUID.fromString(resourceId))
            .createdAt(LocalDateTime.now())
            .build();
    fileRepo.save(metadata);
    return fileMetadataMapper.toFileMetadataResponse(metadata);
  }

  // Lấy signed URL từ metadata
  public String getSignedUrl(FileMetadata metadata, long durationMinutes) {
    return null;
  }

  public void deleteFile(UUID fileId) {
    FileMetadata metadata =
        fileRepo.findById(fileId).orElseThrow(() -> new RuntimeException("File not found"));
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (!fileMetadataPermissionHandler.hasPermission(auth, metadata, "DELETE")) {
      throw new AccessDeniedException("No permission to delete this file");
    }
    User current = currentUserService.getCurrentUserEntity();

    if (!metadata.getUser().getId().equals(current.getId())) {
      throw new AppException(ErrorCode.UNAUTHENTICATED);
    }

    firebaseService.deleteFile(metadata.getFolder() + metadata.getFileName());

    fileRepo.delete(metadata);
  }

  public FileMetadataResponse replaceFile(UUID fileId, MultipartFile newFile) throws IOException {
    FileMetadata oldMetadata =
        fileRepo.findById(fileId).orElseThrow(() -> new RuntimeException("File not found"));
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (!fileMetadataPermissionHandler.hasPermission(auth, oldMetadata, "UPDATE")) {
      throw new AccessDeniedException("No permission to update this file");
    }

    User current = currentUserService.getCurrentUserEntity();

    if (!oldMetadata.getUser().getId().equals(current.getId())) {
      throw new AppException(ErrorCode.UNAUTHENTICATED);
    }

    firebaseService.deleteFile(oldMetadata.getFileName());
    String newFileName = firebaseService.uploadFile(newFile, oldMetadata.getFolder());

    String newUrl =
        oldMetadata.getAccessType() == AccessType.PUBLIC
            ? firebaseService.getPublicUrl(oldMetadata.getFolder() + newFileName)
            : null;

    oldMetadata.setFileName(newFileName);
    oldMetadata.setUrl(newUrl);
    oldMetadata.setContentType(newFile.getContentType());
    oldMetadata.setCreatedAt(LocalDateTime.now());
    fileRepo.save(oldMetadata);
    return fileMetadataMapper.toFileMetadataResponse(oldMetadata);
  }

  public List<FileMetadataResponse> findAllByUserId(UUID userId) {
    List<FileMetadata> files = fileRepo.findAllByUserId(userId);

    return files.stream().map(fileMetadataMapper::toFileMetadataResponse).toList();
  }
}
