package com.graduation.project.library.service;

import com.graduation.project.common.entity.User;
import com.graduation.project.common.service.FirebaseService;
import com.graduation.project.library.constant.DocumentStatus;
import com.graduation.project.library.constant.DocumentType;
import com.graduation.project.library.dto.DocumentRequest;
import com.graduation.project.library.dto.DocumentResponse;
import com.graduation.project.library.entity.Document;
import com.graduation.project.library.entity.Subject;
import com.graduation.project.library.repository.DocumentRepository;
import com.graduation.project.library.repository.SubjectRepository;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.graduation.project.security.exception.AppException;
import com.graduation.project.security.exception.ErrorCode;
import com.graduation.project.security.ultilities.SecurityUtils;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class DocumentService {
  private final FirebaseService firebaseService;
  private final DocumentRepository documentRepository;
  private final SubjectRepository subjectRepository;

  private final String FOLDER_DOCUMENT = "documents";
  private final String FOLDER_IMAGE = "images";

  public DocumentResponse uploadDocument(
      MultipartFile document, MultipartFile image, DocumentRequest documentRequest)
      throws IOException {

    Optional<Subject> subject = subjectRepository.findById(documentRequest.getSubjectId());
    if (subject.isEmpty()) {
      throw new RuntimeException("subject not found!!!");
    }

    List<Document> checkOldDocument = documentRepository.findByTitle(documentRequest.getTitle());
    if (checkOldDocument.size() > 0) {
      throw new RuntimeException("document name already exist!!!");
    }
    String filePath = "";
    String imageUrl = "";

    if (!document.isEmpty()) {
      String documentName = firebaseService.uploadFile(document, FOLDER_DOCUMENT);
      filePath = firebaseService.getPublicUrl(FOLDER_DOCUMENT + documentName);
    }

    if (!image.isEmpty()) {
      String imageName = firebaseService.uploadFile(image, FOLDER_IMAGE);
      imageUrl = firebaseService.getPublicUrl(FOLDER_IMAGE + imageName);
    }

    Document documentEntity = Document.builder()
        .title(documentRequest.getTitle())
        .description(documentRequest.getDescription())
        .documentType(documentRequest.getDocumentType())
        .filePath(filePath)
        .imageUrl(imageUrl)
        .subject(subject.get())
        .documentStatus(DocumentStatus.PENDING)
        .uploadedBy(User.builder().id(SecurityUtils.getCurrentUserId()).build())
        .createdAt(LocalDateTime.now())
        .build();
    documentRepository.save(documentEntity);
    return documentEntity.toDocumentResponse();
  }

  public DocumentResponse updateDocumentInfo(UUID documentId, DocumentRequest request) {
    Document document = documentRepository.findById(documentId)
        .orElseThrow(() -> new AppException(ErrorCode.DOCUMENT_NOT_FOUND));

    UUID currentUserId = SecurityUtils.getCurrentUserId();
    if (!document.getUploadedBy().getId().equals(currentUserId)) {
      throw new AccessDeniedException("You are not the owner of this document");
    }

    if (request.getTitle() != null)
      document.setTitle(request.getTitle());
    if (request.getDescription() != null)
      document.setDescription(request.getDescription());

    return documentRepository.save(document).toDocumentResponse();
  }

  public DocumentResponse updateDocument(MultipartFile document, MultipartFile image, DocumentRequest documentRequest)
      throws IOException {
    // Legacy update implementation - Keeping for now if needed by other components,
    // but usage discouraged
    Optional<Document> documentOptional = documentRepository.findById(documentRequest.getId());
    // ... logic same as before but ensure owner check if strictly needed, though
    // 'updateDocumentInfo' is the preferred way for metadata
    // For brevity, using the existing code block but generally we should restrict
    // this too.
    // Letting original code stay or modifying slightly.
    // However, the original code didn't check for ownership! Adding check.

    if (documentOptional.isEmpty()) {
      throw new AppException(ErrorCode.DOCUMENT_NOT_FOUND);
    }
    Document oldDoc = documentOptional.get();

    if (!oldDoc.getUploadedBy().getId().equals(SecurityUtils.getCurrentUserId())) {
      throw new AccessDeniedException("You are not the owner");
    }

    String filePath = "";
    String imageUrl = "";

    if (document != null && !document.isEmpty()) {
      if (oldDoc.getFilePath() != null && !oldDoc.getFilePath().isEmpty()) {
        firebaseService.deleteFileByUrl(oldDoc.getFilePath());
      }
      String documentName = firebaseService.uploadFile(document, FOLDER_DOCUMENT);
      filePath = firebaseService.getPublicUrl(FOLDER_DOCUMENT + documentName);
    }
    if (image != null && !image.isEmpty()) {
      if (oldDoc.getImageUrl() != null && !oldDoc.getImageUrl().isEmpty()) {
        firebaseService.deleteFileByUrl(oldDoc.getImageUrl());
      }
      String imageName = firebaseService.uploadFile(image, FOLDER_IMAGE);
      imageUrl = firebaseService.getPublicUrl(FOLDER_IMAGE + imageName);
    }

    if (filePath.length() > 0)
      oldDoc.setFilePath(filePath);
    if (imageUrl.length() > 0)
      oldDoc.setImageUrl(imageUrl);
    if (documentRequest.getDocumentType() != null)
      oldDoc.setDocumentType(documentRequest.getDocumentType());
    if (documentRequest.getTitle() != null)
      oldDoc.setTitle(documentRequest.getTitle());
    if (documentRequest.getDescription() != null)
      oldDoc.setDescription(documentRequest.getDescription());
    if (documentRequest.getSubjectId() != null) {
      Optional<Subject> subject = subjectRepository.findById(documentRequest.getSubjectId());
      if (subject.isPresent()) {
        oldDoc.setSubject(subject.get());
      }
    }
    oldDoc.setDocumentStatus(DocumentStatus.PENDING); // Reset to PENDING on update
    documentRepository.save(oldDoc);
    return oldDoc.toDocumentResponse();
  }

  public void deleteDocument(UUID documentId) {
    Document document = documentRepository.findById(documentId)
        .orElseThrow(() -> new AppException(ErrorCode.DOCUMENT_NOT_FOUND));

    String currentUserId = SecurityUtils.getCurrentUserIdString();
    boolean isAdmin = SecurityUtils.isAdmin();

    if (!isAdmin
        && (document.getUploadedBy() == null || !document.getUploadedBy().getId().toString().equals(currentUserId))) {
      throw new AccessDeniedException("You do not have permission to delete this document");
    }

    String filePath = document.getFilePath();
    String imageUrl = document.getImageUrl();
    if (filePath != null && !filePath.isEmpty())
      firebaseService.deleteFileByUrl(filePath);
    if (imageUrl != null && !imageUrl.isEmpty())
      firebaseService.deleteFileByUrl(imageUrl);
    documentRepository.delete(document);
  }

  public Page<DocumentResponse> searchDocuments(
      UUID subjectId, String title, DocumentType documentType, DocumentStatus status, Pageable pageable) {
    if (title == null) {
      title = "";
    }
    Page<Document> documentOptionals = documentRepository.findByTitleAndDocumentTypeAndSubjectIdAndDocumentStatus(
        title, documentType, subjectId, status, pageable);
    return documentOptionals.map(Document::toDocumentResponse);
  }

  public Page<DocumentResponse> getMyDocuments(Pageable pageable) {
    UUID userId = SecurityUtils.getCurrentUserId();
    return documentRepository.findByUploadedBy_Id(userId, pageable)
        .map(Document::toDocumentResponse);
  }

  public DocumentResponse getDocumentById(UUID id) {
    Document document = documentRepository.findById(id)
        .orElseThrow(() -> new AppException(ErrorCode.DOCUMENT_NOT_FOUND));

    // If document is not APPROVED, only Owner or Admin can view
    if (document.getDocumentStatus() != DocumentStatus.APPROVED) {
      // Allow simple check: if not approved, throw Not Found to hide it,
      // OR check permission. For Public PublicController, we might want strict
      // filtering.
      // But since Service is shared, we'll check permission here or let Controller
      // handle it.
      // "Public Read-Only" requirement says: Only return if status == APPROVED.
      // However, for "My Documents" we might need to fetch by ID too?
      // Actually `getDocumentById` is likely used for the Detail View.
      // we will implement logic: If not APPROVED, check if current user is owner or
      // admin.
      UUID currentUserId = SecurityUtils.getCurrentUserId(); // Might be null
      boolean isAdmin = SecurityUtils.isAdmin(); // Assuming this utility exists or similar
      boolean isOwner = document.getUploadedBy() != null && document.getUploadedBy().getId().equals(currentUserId);

      if (!isOwner && !isAdmin) {
        throw new AppException(ErrorCode.DOCUMENT_NOT_FOUND); // Hide it completely
      }
    }
    return document.toDocumentResponse();
  }

  public void approveDocument(UUID id) {
    Document document = documentRepository.findById(id)
        .orElseThrow(() -> new AppException(ErrorCode.DOCUMENT_NOT_FOUND));
    document.setDocumentStatus(DocumentStatus.APPROVED);
    document.setApprovedBy(User.builder().id(SecurityUtils.getCurrentUserId()).build());
    document.setApprovedAt(LocalDateTime.now());
    documentRepository.save(document);
  }

  public void rejectDocument(UUID id) {
    Document document = documentRepository.findById(id)
        .orElseThrow(() -> new AppException(ErrorCode.DOCUMENT_NOT_FOUND));
    document.setDocumentStatus(DocumentStatus.REJECTED);
    // document.setRejectionReason(reason); // If DB has this field
    documentRepository.save(document);
  }
}
