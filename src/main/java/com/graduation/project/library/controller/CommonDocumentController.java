package com.graduation.project.library.controller;

import com.graduation.project.auth.dto.response.ApiResponse;
import com.graduation.project.auth.service.CurrentUserService;
import com.graduation.project.common.entity.User;
import com.graduation.project.library.constant.DocumentStatus;
import com.graduation.project.library.constant.DocumentType;
import com.graduation.project.library.dto.DocumentRequest;
import com.graduation.project.library.dto.DocumentResponse;
import com.graduation.project.library.service.DocumentService;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class CommonDocumentController {

  private final DocumentService documentService;
  private final CurrentUserService currentUserService;

  @PreAuthorize("hasAuthority('CREATE_OWN_FILE') or hasAuthority('MANAGE_ALL_FILES')")
  @PostMapping("/upload")
  public ApiResponse<DocumentResponse> createDocument(
      @RequestParam("document") MultipartFile document,
      @RequestParam() UUID subjectId,
      @RequestParam() String title,
      @RequestParam(required = false, defaultValue = "") String description,
      @RequestParam(required = false) DocumentType documentType)
      throws IOException {

    User currentUser = currentUserService.getCurrentUserEntity();

    DocumentRequest documentRequest = DocumentRequest.builder()
        .title(title)
        .documentType(documentType)
        .description(description)
        .subjectId(subjectId)
        .build();

    // Note: Image upload is removed as it's now auto-generated
    DocumentResponse res = documentService.uploadDocument(document, currentUser, documentRequest);
    return ApiResponse.<DocumentResponse>builder().result(res).build();
  }

  @GetMapping("/search")
  public ApiResponse<Page<DocumentResponse>> search(
      @RequestParam(required = false) UUID subjectId,
      @RequestParam(required = false) String title,
      @RequestParam(required = false) DocumentType documentType,
      @RequestParam(required = false) DocumentStatus documentStatus,
      Pageable pageable) {
    return ApiResponse.ok(
        documentService.searchPublicDocuments(subjectId, title, documentType, documentStatus, pageable));
  }

  @GetMapping("/me")
  public ApiResponse<Page<DocumentResponse>> getMyDocuments(
      @RequestParam(required = false) String title,
      @RequestParam(required = false) DocumentType documentType,
      @RequestParam(required = false) DocumentStatus status,
      Pageable pageable) {

    User currentUser = currentUserService.getCurrentUserEntity();
    return ApiResponse.ok(
        documentService.searchMyDocuments(currentUser, title, documentType, status, pageable));
  }
}
