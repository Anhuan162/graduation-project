package com.graduation.project.library.controller;

import com.graduation.project.auth.dto.response.ApiResponse;
import com.graduation.project.library.constant.DocumentStatus;
import com.graduation.project.library.constant.DocumentType;
import com.graduation.project.library.dto.DocumentResponse;
import com.graduation.project.library.service.DocumentService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/documents")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminDocumentController {

    private final DocumentService documentService;

    @GetMapping("/search")
    public ApiResponse<Page<DocumentResponse>> searchDocuments(
            @RequestParam(required = false) String subjectId,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String documentType,
            @RequestParam(required = false) String documentStatus,
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        UUID uuidSubjectId = null;
        if (subjectId != null && !subjectId.isEmpty()) {
            try {
                uuidSubjectId = UUID.fromString(subjectId);
            } catch (IllegalArgumentException e) {
                // ignore or handle invalid format
            }
        }

        DocumentType type = null;
        if (documentType != null && !documentType.isEmpty()) {
            try {
                type = DocumentType.valueOf(documentType);
            } catch (Exception e) {
            }
        }

        DocumentStatus status = null;
        if (documentStatus != null && !documentStatus.isEmpty()) {
            try {
                status = DocumentStatus.valueOf(documentStatus);
            } catch (Exception e) {
            }
        }

        return ApiResponse.<Page<DocumentResponse>>builder()
                .result(documentService.searchDocuments(uuidSubjectId, title, type, status, null, pageable))
                .build();
    }

    @PutMapping("/{id}/approve")
    public ApiResponse<Void> approveDocument(@PathVariable UUID id) {
        documentService.approveDocument(id);
        return ApiResponse.<Void>builder().build();
    }

    @PutMapping("/{id}/reject")
    public ApiResponse<Void> rejectDocument(@PathVariable UUID id) {
        documentService.rejectDocument(id);
        return ApiResponse.<Void>builder().build();
    }
}
