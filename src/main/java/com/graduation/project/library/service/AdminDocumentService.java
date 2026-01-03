package com.graduation.project.library.service;

import com.graduation.project.common.entity.User;
import com.graduation.project.library.constant.DocumentStatus;
import com.graduation.project.library.constant.DocumentType;
import com.graduation.project.library.dto.DocumentResponse;
import com.graduation.project.library.entity.Document;
import com.graduation.project.library.repository.DocumentRepository;
import com.graduation.project.library.repository.DocumentSpecification;
import com.graduation.project.security.exception.AppException;
import com.graduation.project.security.exception.ErrorCode;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminDocumentService {

    private final DocumentRepository documentRepository;

    public Page<DocumentResponse> searchDocuments(
            UUID subjectId, String title, DocumentType documentType, DocumentStatus status, Pageable pageable) {

        // Admin can see ALL documents, filterable by any status
        Specification<Document> spec = Specification.where(DocumentSpecification.containsTitle(title))
                .and(DocumentSpecification.hasType(documentType))
                .and(DocumentSpecification.hasStatus(status))
                .and(DocumentSpecification.hasSubjectId(subjectId));

        Page<Document> documents = documentRepository.findAll(spec, pageable);
        return documents.map(Document::toDocumentResponse);
    }

    @Transactional
    public void approveDocument(UUID documentId, User approver) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new AppException(ErrorCode.FILE_NOT_FOUND));

        // Logic: Only PENDING documents can be approved? Or REJECTED too?
        // User didn't specify, but typically PENDING -> PUBLISHED

        document.setDocumentStatus(DocumentStatus.PUBLISHED);
        document.setApprovedBy(approver);
        document.setApprovedAt(LocalDateTime.now());
        document.setRejectionReason(null); // Clear rejection reason if approved (e.g. re-evaluated)

        documentRepository.save(document);
        log.info("Document {} approved by {}", documentId, approver.getId());

        // Future: Add points, notification, indexing
    }

    @Transactional
    public void rejectDocument(UUID documentId, User rejector, String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Rejection reason cannot be empty");
        }

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new AppException(ErrorCode.FILE_NOT_FOUND));

        document.setDocumentStatus(DocumentStatus.REJECTED);
        document.setRejectionReason(reason);

        // Ensure approvedBy is cleared if it was previously approved?
        document.setApprovedBy(null);
        document.setApprovedAt(null);

        documentRepository.save(document);
        log.info("Document {} rejected by {}. Reason: {}", documentId, rejector.getId(), reason);
    }
}
