package com.graduation.project.library.controller;

import com.graduation.project.auth.dto.response.ApiResponse;
import com.graduation.project.library.constant.DocumentType;
import com.graduation.project.library.dto.DocumentRequest;
import com.graduation.project.library.dto.DocumentResponse;
import com.graduation.project.library.service.DocumentService;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class CommonDocumentController {

  private final DocumentService documentService;

  @GetMapping("/{id}")
  public ApiResponse<DocumentResponse> getDocumentById(@PathVariable UUID id) {
    // Rely on service to check permissions or throw 404 if not found/hidden
    return ApiResponse.<DocumentResponse>builder()
        .result(documentService.getDocumentById(id))
        .build();
  }

  @GetMapping("/search")
  public ApiResponse<Page<DocumentResponse>> searchDocumentBySubjectIdAndTitleAndDocumentType(
      @RequestParam(required = false) String subjectId,
      @RequestParam(required = false) String title,
      @RequestParam(required = false) String documentType,
      @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
    UUID UUIDSubjectId = null;
    DocumentType edocumentType = null;
    try {
      UUIDSubjectId = UUID.fromString(subjectId);
    } catch (Exception e) {
    }
    try {
      edocumentType = DocumentType.valueOf(documentType);
    } catch (Exception e) {
    }
    // Enforce APPROVED status for public search
    Page<DocumentResponse> res = documentService.searchDocuments(UUIDSubjectId, title, edocumentType,
        com.graduation.project.library.constant.DocumentStatus.APPROVED, pageable);
    return ApiResponse.<Page<DocumentResponse>>builder().result(res).build();
  }
}
