package com.graduation.project.common.service;

import com.graduation.project.auth.repository.FileMetadataRepository;
import com.graduation.project.auth.service.CurrentUserService;
import com.graduation.project.common.constant.AccessType;
import com.graduation.project.common.constant.ResourceType;
import com.graduation.project.common.dto.FileMetadataResponse;
import com.graduation.project.common.entity.FileMetadata;
import com.graduation.project.common.entity.User;
import com.graduation.project.common.mapper.FileMetadataMapper;
import com.graduation.project.security.exception.AppException;
import com.graduation.project.security.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileService {

  private final FirebaseService firebaseService;
  private final FileMetadataRepository fileRepository;
  private final CurrentUserService currentUserService;
  private final FileMetadataMapper fileMapper;

  @Transactional
  public FileMetadataResponse uploadAndSaveFile(MultipartFile file, String folderName, AccessType accessType,
      String resourceTypeStr, String resourceIdStr) throws IOException {
    if (file == null) {
      throw new AppException(ErrorCode.INVALID_REQUEST);
    }
    User currentUser = currentUserService.getCurrentUserEntity();

    FirebaseService.FileUploadResult result = firebaseService.uploadFile(file, folderName);

    // 2. Build Entity
    FileMetadata metadata = FileMetadata.builder()
        .fileName(file.getOriginalFilename())
        .folder(folderName)
        .url(result.url()) // Lưu URL vĩnh viễn vào DB
        .storagePath(result.storagePath()) // Lưu storage path để xóa sau này
        .contentType(file.getContentType())
        .size(file.getSize()) // Không cần ép kiểu
        .accessType(accessType)
        .user(currentUser)
        .build();

    // 3. Map Resource (Post, Comment, etc.)
    if (resourceTypeStr != null && resourceIdStr != null) {
      try {
        metadata.setResourceType(ResourceType.valueOf(resourceTypeStr.toUpperCase()));
        metadata.setResourceId(UUID.fromString(resourceIdStr));
      } catch (IllegalArgumentException e) {
        throw new AppException(ErrorCode.INVALID_REQUEST);
      }
    }

    // 4. Save DB
    FileMetadata saved = fileRepository.save(metadata);

    return fileMapper.toFileMetadataResponse(saved);
  }

  public List<FileMetadataResponse> uploadMultipleFiles(List<MultipartFile> files, String folderName) {
    if (files == null || files.isEmpty()) {
      return new ArrayList<>();
    }

    List<FirebaseService.FileUploadResult> uploadResults = new ArrayList<>();
    List<String> storagePathsToDelete = new ArrayList<>();

    try {
      for (MultipartFile file : files) {
        FirebaseService.FileUploadResult result = firebaseService.uploadFile(file, folderName);
        uploadResults.add(result);
        storagePathsToDelete.add(result.storagePath());
      }
      // All uploads succeeded, now save to DB
      return saveMultipleFiles(uploadResults, files, folderName);
    } catch (Exception e) {
      // Compensation logic: delete all successfully uploaded files to prevent
      // orphaned files
      for (String path : storagePathsToDelete) {
        try {
          firebaseService.deleteFile(path);
        } catch (Exception deleteEx) {
          log.error("Failed to delete file during compensation: {}", path, deleteEx);
        }
      }
      throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
    }
  }

  @Transactional
  private List<FileMetadataResponse> saveMultipleFiles(List<FirebaseService.FileUploadResult> results,
      List<MultipartFile> files, String folderName) {
    User currentUser = currentUserService.getCurrentUserEntity();
    List<FileMetadata> metadatas = new ArrayList<>();
    for (int i = 0; i < files.size(); i++) {
      MultipartFile file = files.get(i);
      FirebaseService.FileUploadResult result = results.get(i);
      FileMetadata metadata = FileMetadata.builder()
          .fileName(file.getOriginalFilename())
          .folder(folderName)
          .url(result.url())
          .storagePath(result.storagePath())
          .contentType(file.getContentType())
          .size(file.getSize())
          .accessType(AccessType.PUBLIC)
          .user(currentUser)
          .build();
      metadatas.add(metadata);
    }
    List<FileMetadata> saved = fileRepository.saveAll(metadatas);
    return saved.stream().map(fileMapper::toFileMetadataResponse).toList();
  }

  public List<FileMetadataResponse> findAllByUserId(UUID userId) {
    List<FileMetadata> files = fileRepository.findAllByUserId(userId);
    return files.stream().map(fileMapper::toFileMetadataResponse).toList();
  }

  public List<FileMetadataResponse> getFilesByResource(UUID resourceId, ResourceType resourceType) {
    if (resourceId == null || resourceType == null) {
      return new ArrayList<>();
    }
    List<FileMetadata> files = fileRepository.findAllByResourceIdAndResourceType(resourceId, resourceType);
    return files.stream().map(fileMapper::toFileMetadataResponse).toList();
  }

  @Transactional
  public FileMetadataResponse replaceFile(UUID fileId, MultipartFile newFile) throws IOException {
    FileMetadata oldFile = fileRepository.findById(fileId)
        .orElseThrow(() -> new AppException(ErrorCode.FILE_NOT_FOUND));

    User currentUser = currentUserService.getCurrentUserEntity();
    if (!oldFile.getUser().getId().equals(currentUser.getId())) {
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    // Lưu lại path cũ để xóa sau
    String oldStoragePath = oldFile.getStoragePath();

    // 1. Upload file MỚI trước (Nếu lỗi ở đây -> DB chưa sửa -> File cũ vẫn sống)
    FirebaseService.FileUploadResult newResult = firebaseService.uploadFile(newFile, oldFile.getFolder());

    // 2. Update DB
    oldFile.setUrl(newResult.url());
    oldFile.setStoragePath(newResult.storagePath());
    oldFile.setSize(newFile.getSize());
    oldFile.setContentType(newFile.getContentType());
    oldFile.setFileName(newFile.getOriginalFilename());

    FileMetadata saved = fileRepository.save(oldFile);

    // 3. Xóa file CŨ sau cùng (Cleanup)
    // Nếu bước này lỗi -> Chỉ tốn chút dung lượng rác trên Cloud, nhưng dữ liệu
    // User AN TOÀN.
    try {
      if (oldStoragePath != null) {
        firebaseService.deleteFile(oldStoragePath);
      }
    } catch (Exception e) {
      log.warn("Failed to clean up old file: {}", oldStoragePath, e);
      // Không throw exception ở đây để tránh rollback transaction đã thành công
    }

    return fileMapper.toFileMetadataResponse(saved);
  }

  @Transactional
  public void deleteFile(UUID fileId) {
    FileMetadata file = fileRepository.findById(fileId)
        .orElseThrow(() -> new AppException(ErrorCode.FILE_NOT_FOUND));

    // Check permission - ensure current user owns the file
    User currentUser = currentUserService.getCurrentUserEntity();
    if (!file.getUser().getId().equals(currentUser.getId())) {
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    // Delete from Firebase first
    try {
      firebaseService.deleteFile(file.getStoragePath());
    } catch (Exception e) {
      log.error("Failed to delete file from Firebase: {}", file.getStoragePath(), e);
      // Continue with DB deletion even if Firebase deletion fails
      // This prevents orphaned database records
    }

    // Delete from database
    fileRepository.delete(file);
  }

  @Transactional
  public void deleteAllFiles(List<String> ids) {
    if (ids == null || ids.isEmpty()) {
      throw new AppException(ErrorCode.INVALID_REQUEST);
    }
    List<UUID> uuidList = new ArrayList<>();
    for (String id : ids) {
      try {
        uuidList.add(UUID.fromString(id));
      } catch (IllegalArgumentException e) {
        throw new AppException(ErrorCode.INVALID_REQUEST);
      }
    }
    List<FileMetadata> files = fileRepository.findAllById(uuidList);

    // Check permissions - ensure current user owns all files
    User currentUser = currentUserService.getCurrentUserEntity();
    for (FileMetadata file : files) {
      if (!file.getUser().getId().equals(currentUser.getId())) {
        throw new AppException(ErrorCode.UNAUTHORIZED);
      }
    }

    // Delete from Firebase first (batch operation)
    for (FileMetadata file : files) {
      try {
        firebaseService.deleteFile(file.getStoragePath());
      } catch (Exception e) {
        log.error("Failed to delete file from Firebase: {}", file.getStoragePath(), e);
        // Continue with other deletions even if one fails
        // This prevents one failed deletion from blocking the entire batch
      }
    }

    // Delete from database
    fileRepository.deleteAll(files);
  }

  @Transactional
  public void deleteFileByResourceId(UUID resourceId, ResourceType resourceType) {
    List<FileMetadata> files = fileRepository.findAllByResourceIdAndResourceType(resourceId, resourceType);

    // Check permissions - ensure current user owns all files
    User currentUser = currentUserService.getCurrentUserEntity();
    for (FileMetadata file : files) {
      if (!file.getUser().getId().equals(currentUser.getId())) {
        throw new AppException(ErrorCode.UNAUTHORIZED);
      }
    }

    // Delete from Firebase
    for (FileMetadata file : files) {
      try {
        firebaseService.deleteFile(file.getStoragePath());
      } catch (Exception e) {
        log.error("Failed to delete file from Firebase: {}", file.getStoragePath(), e);
      }
    }

    // Delete from database
    fileRepository.deleteAll(files);
  }

  @Transactional
  public List<FileMetadata> updateFileMetadataList(List<UUID> fileMetadataIds, UUID resourceId,
      ResourceType resourceType) {
    User currentUser = currentUserService.getCurrentUserEntity();

    // Find existing attachments for this resource
    List<FileMetadata> existingFiles = fileRepository.findAllByResourceIdAndResourceType(resourceId, resourceType);

    if (fileMetadataIds == null || fileMetadataIds.isEmpty()) {
      // If no files provided, delete all existing attachments
      deleteAttachments(existingFiles);
      return new ArrayList<>();
    }

    List<FileMetadata> newFiles = fileRepository.findAllById(fileMetadataIds);

    // Check permissions - ensure current user owns all new files
    for (FileMetadata file : newFiles) {
      if (!file.getUser().getId().equals(currentUser.getId())) {
        throw new AppException(ErrorCode.UNAUTHORIZED);
      }
    }

    // Find files to remove (existing files not in the new list)
    List<FileMetadata> filesToRemove = existingFiles.stream()
        .filter(existing -> !fileMetadataIds.contains(existing.getId()))
        .toList();

    // Check permissions for files to remove
    for (FileMetadata file : filesToRemove) {
      if (!file.getUser().getId().equals(currentUser.getId())) {
        throw new AppException(ErrorCode.UNAUTHORIZED);
      }
    }

    // Delete removed attachments
    deleteAttachments(filesToRemove);

    // Update resource information for new/remaining files
    for (FileMetadata file : newFiles) {
      file.setResourceId(resourceId);
      file.setResourceType(resourceType);
    }

    return fileRepository.saveAll(newFiles);
  }

  private void deleteAttachments(List<FileMetadata> files) {
    if (files.isEmpty()) {
      return;
    }

    // Delete from Firebase first
    for (FileMetadata file : files) {
      try {
        firebaseService.deleteFile(file.getStoragePath());
      } catch (Exception e) {
        log.error("Failed to delete file from Firebase: {}", file.getStoragePath(), e);
        // Continue with other deletions even if one fails
      }
    }

    // Delete from database
    fileRepository.deleteAll(files);
  }

  @Transactional
  public void updateResourceTarget(UUID resourceId, ResourceType resourceType, FileMetadata fileMetadata) {
    // Check permission - ensure current user owns the file
    User currentUser = currentUserService.getCurrentUserEntity();
    if (!fileMetadata.getUser().getId().equals(currentUser.getId())) {
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    fileMetadata.setResourceId(resourceId);
    fileMetadata.setResourceType(resourceType);
    fileRepository.save(fileMetadata);
  }

  @Transactional
  public int fixExistingAttachmentUrls(int page, int size) {
    List<FileMetadata> files = fileRepository.findAll(PageRequest.of(page, size)).getContent();
    String bucketName = firebaseService.getBucketName();
    int processedCount = 0;

    for (FileMetadata file : files) {
      if (file.getStoragePath() != null && !file.getStoragePath().isEmpty()) {
        // Extract token from current URL
        String currentUrl = file.getUrl();
        String token = extractTokenFromUrl(currentUrl);

        if (token != null) {
          // Re-generate URL with proper encoding
          String fixedUrl = firebaseService.generateFirebaseUrl(bucketName, file.getStoragePath(), token);
          if (!fixedUrl.equals(currentUrl)) {
            file.setUrl(fixedUrl);
            log.info("Fixed URL for file {}: {} -> {}", file.getId(), currentUrl, fixedUrl);
          }
        }
      }
      processedCount++;
    }

    fileRepository.saveAll(files);
    return processedCount;
  }

  private String extractTokenFromUrl(String url) {
    if (url == null || !url.contains("token=")) {
      return null;
    }
    int tokenIndex = url.indexOf("token=");
    return url.substring(tokenIndex + 6); // 6 is length of "token="
  }

  @Transactional
  public FileMetadata attachFileToResource(UUID fileId, UUID resourceId, ResourceType resourceType) {
    if (fileId == null)
      return null;
    FileMetadata fileMetadata = fileRepository.findById(fileId)
        .orElseThrow(() -> new AppException(ErrorCode.FILE_NOT_FOUND));
    updateResourceTarget(resourceId, resourceType, fileMetadata);
    return fileRepository.save(fileMetadata);
  }

  @Transactional
  public FileMetadata syncFileAttachment(UUID newFileId, UUID resourceId, ResourceType resourceType) {
    // 1. Get existing files
    List<FileMetadata> existingFiles = fileRepository.findAllByResourceIdAndResourceType(resourceId, resourceType);

    // 2. If no file provided/changing -> remove old files
    boolean isRemovingOrChanging = newFileId == null ||
        existingFiles.stream().noneMatch(f -> f.getId().equals(newFileId));

    if (isRemovingOrChanging && !existingFiles.isEmpty()) {
      deleteAttachments(existingFiles);
    }

    // 3. Attach new file if provided
    if (newFileId != null) {
      boolean alreadyAttached = existingFiles.stream().anyMatch(f -> f.getId().equals(newFileId));
      if (!alreadyAttached) {
        return attachFileToResource(newFileId, resourceId, resourceType);
      }
      return existingFiles.stream().filter(f -> f.getId().equals(newFileId)).findFirst().orElse(null);
    }
    return null;
  }

  @Transactional(readOnly = true)
  public java.util.Map<UUID, FileMetadata> getFileMapByResourceIds(List<UUID> resourceIds, ResourceType resourceType) {
    if (resourceIds == null || resourceIds.isEmpty()) {
      return java.util.Collections.emptyMap();
    }
    List<FileMetadata> files = fileRepository.findAllByResourceIdInAndResourceType(resourceIds, resourceType);
    return files.stream().collect(
        java.util.stream.Collectors.toMap(FileMetadata::getResourceId, java.util.function.Function.identity(),
            (existing, replacement) -> existing));
  }
}
