package com.graduation.project.common.service;

import com.graduation.project.auth.exception.AppException;
import com.graduation.project.auth.exception.ErrorCode;
import com.graduation.project.auth.permission_handler.FileMetadataPermissionHandler;
import com.graduation.project.auth.repository.FileMetadataRepository;
import com.graduation.project.auth.service.CurrentUserService;
import com.graduation.project.auth.service.FirebaseService;
import com.graduation.project.common.dto.FileMetadataResponse;
import com.graduation.project.common.entity.*;
import com.graduation.project.common.mapper.FileMetadataMapper;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
  private final FileMetadataRepository fileMetadataRepository;
  private final CurrentUserService currentUserService;
  private final FileMetadataMapper fileMetadataMapper;
  private final FileMetadataPermissionHandler fileMetadataPermissionHandler;

  public FileService(
      FirebaseService firebaseService,
      FileMetadataRepository fileMetadataRepository,
      CurrentUserService currentUserService,
      FileMetadataMapper fileMetadataMapper,
      FileMetadataPermissionHandler fileMetadataPermissionHandler) {
    this.firebaseService = firebaseService;
    this.fileMetadataRepository = fileMetadataRepository;
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
    fileMetadataRepository.save(metadata);
    return fileMetadataMapper.toFileMetadataResponse(metadata);
  }

  // Lấy signed URL từ metadata
  public String getSignedUrl(FileMetadata metadata, long durationMinutes) {
    return null;
  }

  public FileMetadataResponse replaceFile(UUID fileId, MultipartFile newFile) throws IOException {
    FileMetadata oldMetadata =
        fileMetadataRepository
            .findById(fileId)
            .orElseThrow(() -> new RuntimeException("File not found"));
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
    fileMetadataRepository.save(oldMetadata);
    return fileMetadataMapper.toFileMetadataResponse(oldMetadata);
  }

  public List<FileMetadataResponse> findAllByUserId(UUID userId) {
    List<FileMetadata> files = fileMetadataRepository.findAllByUserId(userId);

    return files.stream().map(fileMetadataMapper::toFileMetadataResponse).toList();
  }

  public List<FileMetadataResponse> uploadMultipleFiles(
      List<MultipartFile> files, String folderName) throws IOException {
    List<FileMetadataResponse> responses = new ArrayList<>();
    for (MultipartFile file : files) {
      FileMetadataResponse res = uploadAndSaveFile(file, folderName, AccessType.PUBLIC, null, null);
      responses.add(res);
    }
    return responses;
  }

  public List<String> getFileMetadataIds(UUID resourceId, ResourceType resourceType) {
    return fileMetadataRepository
        .findAllByResourceIdAndResourceType(resourceId, resourceType)
        .stream()
        .map(FileMetadata::getUrl)
        .toList();
  }

  public List<FileMetadata> updateFileMetadataList(
      List<UUID> fileMetadataIds, Post save, UUID userId) {
    List<FileMetadata> fileMetadataList = fileMetadataRepository.findAllByIdIn(fileMetadataIds);

    fileMetadataList.forEach(
        fileMetadata -> {
          if (!fileMetadata.getUser().getId().equals(userId)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
          }
          fileMetadata.setResourceType(ResourceType.POST);
          fileMetadata.setResourceId(save.getId());
        });
    return fileMetadataRepository.saveAll(fileMetadataList);
  }

  public void deleteFile(UUID fileId) {
    FileMetadata metadata =
        fileMetadataRepository
            .findById(fileId)
            .orElseThrow(() -> new RuntimeException("File not found"));
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (!fileMetadataPermissionHandler.hasPermission(auth, metadata, "DELETE")) {
      throw new AccessDeniedException("No permission to delete this file");
    }
    User current = currentUserService.getCurrentUserEntity();

    deleteFileFromDbAndStorage(metadata, current);
  }

  private void deleteFileFromDbAndStorage(FileMetadata metadata, User current) {
    if (!metadata.getUser().getId().equals(current.getId())) {
      throw new AppException(ErrorCode.UNAUTHENTICATED);
    }

    firebaseService.deleteFile(metadata.getFolder() + metadata.getFileName());

    fileMetadataRepository.delete(metadata);
  }

  public void deleteAllFiles(List<String> fileIds) {
    List<UUID> fileMetadataIds = fileIds.stream().map(UUID::fromString).toList();
    List<FileMetadata> fileMetadataList = fileMetadataRepository.findAllByIdIn(fileMetadataIds);
    fileMetadataList.forEach(
        fileMetadata -> {
          Authentication auth = SecurityContextHolder.getContext().getAuthentication();
          if (!fileMetadataPermissionHandler.hasPermission(auth, fileMetadata, "DELETE")) {
            throw new AccessDeniedException("No permission to delete this file");
          }
          deleteFileFromDbAndStorage(fileMetadata, currentUserService.getCurrentUserEntity());
        });
  }
}
