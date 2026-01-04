package com.graduation.project.library.service;

import com.graduation.project.library.repository.DocumentSpecification;
import org.springframework.data.jpa.domain.Specification;
import com.graduation.project.common.entity.User;
import com.graduation.project.common.service.FileStorageService;
import com.graduation.project.library.constant.DocumentStatus;
import com.graduation.project.library.constant.DocumentType;
import com.graduation.project.library.dto.DocumentRequest;
import com.graduation.project.library.dto.DocumentResponse;
import com.graduation.project.library.entity.Document;
import com.graduation.project.library.entity.Subject;
import com.graduation.project.library.repository.DocumentRepository;
import com.graduation.project.library.repository.SubjectRepository;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService {

  private final DocumentRepository documentRepository;
  private final SubjectRepository subjectRepository;
  private final FileStorageService fileStorageService;
  private final DocumentAsyncService documentAsyncService;

  @Transactional
  public DocumentResponse uploadDocument(MultipartFile file, User user, DocumentRequest request) {
    // 1. Validate Subject
    Subject subject = subjectRepository.findById(request.getSubjectId())
        .orElseThrow(() -> new RuntimeException("Subject not found"));

    // 2. Store File Locally (Rename to UUID)
    String filePath = fileStorageService.store(file, "documents");

    // 3. Create Entity (Status: PROCESSING)
    Document document = Document.builder()
        .title(request.getTitle())
        .description(request.getDescription())
        .documentType(request.getDocumentType())
        .subject(subject)
        .uploadedBy(user)
        .documentStatus(DocumentStatus.PROCESSING) // Async processing will start
        .filePath(filePath)
        .originalFilename(file.getOriginalFilename())
        .mimeType(file.getContentType())
        .build();

    // 4. Save & Flush (Critical: Ensure DB commit before Async reads it)
    Document savedDoc = documentRepository.saveAndFlush(document);

    // 5. Trigger Async Job
    documentAsyncService.processDocumentBackground(savedDoc.getId(), filePath);

    // 6. Return Response immediately
    return savedDoc.toDocumentResponse();
  }

  public Page<DocumentResponse> searchPublicDocuments(
      UUID subjectId, String title, DocumentType documentType, DocumentStatus documentStatus, Pageable pageable) {

    // Default to PUBLISHED if null.
    // If frontend requests something else (e.g. PENDING), we might want to restrict
    // that
    // depending on business logic, but for now we trust the filter or default to
    // PUBLISHED.
    // Ideally public search should ONLY return PUBLISHED.
    DocumentStatus statusFilter = (documentStatus != null) ? documentStatus : DocumentStatus.PUBLISHED;

    Specification<Document> spec = Specification.where(DocumentSpecification.hasStatus(statusFilter))
        .and(DocumentSpecification.containsTitle(title))
        .and(DocumentSpecification.hasType(documentType))
        .and(DocumentSpecification.hasSubjectId(subjectId));

    Page<Document> documents = documentRepository.findAll(spec, pageable);
    return documents.map(Document::toDocumentResponse);
  }

  public Page<DocumentResponse> searchMyDocuments(
      User user, String title, DocumentType documentType, DocumentStatus status, Pageable pageable) {

    // Filter: Created by Current User AND dynamic filters
    Specification<Document> spec = Specification.where(DocumentSpecification.createdBy(user.getId()))
        .and(DocumentSpecification.containsTitle(title))
        .and(DocumentSpecification.hasType(documentType))
        .and(DocumentSpecification.hasStatus(status)); // User can search their own docs by status (e.g. show REJECTED)

    Page<Document> documents = documentRepository.findAll(spec, pageable);
    return documents.map(Document::toDocumentResponse);
  }

  public DocumentResponse getDocumentById(UUID id) {
    Document document = documentRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Document not found"));

    // For public access, we might want to restrict to PUBLISHED status
    // But since the controller is where permissions are often checked, or here.
    // Let's check status.
    if (document.getDocumentStatus() != DocumentStatus.PUBLISHED) {
      // Option: Allow if current user is owner (needs user context)
      // For now, strict public rule:
      throw new RuntimeException("Document is not available (Status: " + document.getDocumentStatus() + ")");
    }

    return document.toDocumentResponse();
  }
}
