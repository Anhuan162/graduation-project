package com.graduation.project.common.service;

import com.graduation.project.auth.repository.FileMetadataRepository;
import com.graduation.project.auth.service.CurrentUserService;
import com.graduation.project.common.constant.AccessType;
import com.graduation.project.common.constant.ResourceType;
import com.graduation.project.common.dto.FileMetadataResponse;
import com.graduation.project.common.dto.FileMetadataSelected;
import com.graduation.project.common.dto.FileResponse;
import com.graduation.project.common.dto.SearchFileRequest;
import com.graduation.project.common.entity.*;
import com.graduation.project.common.entity.User;
import com.graduation.project.common.mapper.FileMetadataMapper;
import com.graduation.project.common.permission_handler.FileMetadataPermissionHandler;
import com.graduation.project.security.exception.AppException;
import com.graduation.project.security.exception.ErrorCode;
import java.io.IOException;
import java.net.URLConnection;
import java.time.LocalDateTime;
import java.util.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileService {

  private final FirebaseService firebaseService;
  private final FileMetadataRepository fileMetadataRepository;
  private final CurrentUserService currentUserService;
  private final FileMetadataMapper fileMetadataMapper;
  private final FileMetadataPermissionHandler fileMetadataPermissionHandler;
  private final DriveService driveService;

  public FileService(
          FirebaseService firebaseService,
          FileMetadataRepository fileMetadataRepository,
          CurrentUserService currentUserService,
          FileMetadataMapper fileMetadataMapper,
          FileMetadataPermissionHandler fileMetadataPermissionHandler, DriveService driveService) {
    this.firebaseService = firebaseService;
    this.fileMetadataRepository = fileMetadataRepository;
    this.currentUserService = currentUserService;
    this.fileMetadataMapper = fileMetadataMapper;
    this.fileMetadataPermissionHandler = fileMetadataPermissionHandler;
    this.driveService = driveService;
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
      List<UUID> fileMetadataIds, UUID resourceId, ResourceType resourceType, UUID userId) {
    List<FileMetadata> fileMetadataList = fileMetadataRepository.findAllByIdIn(fileMetadataIds);

    fileMetadataList.forEach(
        fileMetadata -> {
          updateResourceTarget(resourceId, resourceType, userId, fileMetadata);
        });
    return fileMetadataRepository.saveAll(fileMetadataList);
  }

  public void updateResourceTarget(
      UUID resourceId, ResourceType resourceType, UUID userId, FileMetadata fileMetadata) {
    if (!fileMetadata.getUser().getId().equals(userId)) {
      throw new AppException(ErrorCode.UNAUTHENTICATED);
    }
    fileMetadata.setResourceType(resourceType);
    fileMetadata.setResourceId(resourceId);
  }

  public void deleteFile(UUID fileId) {
    FileMetadata metadata =
        fileMetadataRepository
            .findById(fileId)
            .orElseThrow(() -> new RuntimeException("File not found"));
    User user = currentUserService.getCurrentUserEntity();
    if (!user.getId().equals(metadata.getUser().getId())) {
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
    User user = currentUserService.getCurrentUserEntity();

    fileMetadataList.forEach(
        fileMetadata -> {
          if (!user.getId().equals(fileMetadata.getUser().getId())) {
            throw new AccessDeniedException("No permission to delete this file");
          }
          deleteFileFromDbAndStorage(fileMetadata, currentUserService.getCurrentUserEntity());
        });
  }

  public List<FileMetadata> findFileMetadataByResourceTarget(
      UUID resourceId, ResourceType resourceType) {
    return fileMetadataRepository.findAllByResourceIdAndResourceType(resourceId, resourceType);
  }

  public Page<FileMetadataResponse> searchFiles(
      SearchFileRequest searchFileRequest, Pageable pageable) {
    Page<FileMetadata> fileMetadataPage =
        fileMetadataRepository.searchFiles(searchFileRequest, pageable);
    return fileMetadataPage.map(fileMetadataMapper::toFileMetadataResponse);
  }

  public FileResponse uploadToDrive(FileMetadataSelected fileMetadataSelected) throws IOException {


    UUID fileId = UUID.fromString(fileMetadataSelected.getFileId());

    Optional<FileMetadata> fileMetadata =  fileMetadataRepository.findById(fileId);
    if (fileMetadata.isEmpty()){
      throw new AppException(ErrorCode.FILE_NOT_FOUND);
    }
    fileMetadata.get().setIsOnDrive(true);
    fileMetadataRepository.save(fileMetadata.get());

    String fileUrl = fileMetadataSelected.getUrl();
    // 1. Download file từ Firebase Storage URL
    RestTemplate rest = new RestTemplate();
    ResponseEntity<byte[]> response = rest.getForEntity(fileUrl, byte[].class);

    if (!response.getStatusCode().is2xxSuccessful()) {
      throw new IOException("Không tải được file từ Firebase URL");
    }

    byte[] fileBytes = response.getBody();
    String fileName = extractFileName(fileUrl);
    String contentType = URLConnection.guessContentTypeFromName(fileName);


    return driveService.uploadFile(fileBytes, fileName, contentType);
  }

  private String extractFileName(String url) {
    return url.substring(url.lastIndexOf('/') + 1).split("\\?")[0];
  }
}
