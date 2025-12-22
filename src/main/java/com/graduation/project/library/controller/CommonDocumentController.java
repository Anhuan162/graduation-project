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

  @PreAuthorize("hasAuthority('CREATE_ANY_FILES') or hasAuthority('CREATE_ALL_FILES')")
  @PostMapping("/upload")
  public ApiResponse<DocumentResponse> createDocument(
      @RequestParam("document") MultipartFile document,
      @RequestParam("image") MultipartFile image,
      @RequestParam() UUID subjectId,
      @RequestParam() String title,
      @RequestParam(required = false, defaultValue = "") String description,
      @RequestParam(required = false) DocumentType documentType)
      throws IOException {

    if (document.isEmpty()) {
      throw new BadRequestException("Document cannot be empty");
    }
    if (image.isEmpty()) {
      throw new BadRequestException("Image cannot be empty");
    }

    // Chỉ cho phép ảnh
    String contentType = image.getContentType();
    if (contentType == null ||
            (!contentType.equals("image/jpeg")
                    && !contentType.equals("image/png"))) {
      throw new BadRequestException("Invalid image type. Only JPG, PNG allowed");
    }

    DocumentRequest documentRequest =
        DocumentRequest.builder()
            .title(title)
            .documentType(documentType)
            .description(description)
            .subjectId(subjectId)
            .build();
    DocumentResponse res = documentService.uploadDocument(document, image, documentRequest);
    return ApiResponse.<DocumentResponse>builder().result(res).build();
  }

  @GetMapping("/search")
  public ApiResponse<Page<DocumentResponse>> searchDocumentBySubjectIdAndTitleAndDocumentType(
      @RequestParam(required = false) String subjectId,
      @RequestParam(required = false) String title,
      @RequestParam(required = false) String documentType,
      @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
          Pageable pageable) {
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
    Page<DocumentResponse> res =
        documentService.searchDocuments(UUIDSubjectId, title, edocumentType, pageable);
    return ApiResponse.<Page<DocumentResponse>>builder().result(res).build();
  }

  @PreAuthorize("hasAuthority('UPDATE_ALL_PERMISSIONS')")
  @PutMapping("/{id}")
  public ApiResponse<DocumentResponse> updateDocument(
          @PathVariable UUID id,
          @RequestParam(name = "document", required = false) MultipartFile document,
          @RequestParam(name = "image", required = false) MultipartFile image,
          @RequestParam(required = false) UUID subjectId,
          @RequestParam(required = false) String title,
          @RequestParam(required = false, defaultValue = "") String description,
          @RequestParam(required = false) DocumentType documentType
  ) throws IOException {
    if (document.isEmpty() && image.isEmpty()
            && title == null && description == null
            && subjectId == null && documentType == null) {
      throw new BadRequestException("No data to update");
    }
    if (!image.isEmpty()) {
      String contentType = image.getContentType();
      if (contentType == null ||
              (!contentType.equals("image/jpeg")
                      && !contentType.equals("image/png"))) {
        throw new BadRequestException("Invalid image type. Only JPG, PNG allowed");
      }
    }

    DocumentRequest documentRequest =
            DocumentRequest.builder()
                    .title(title)
                    .documentType(documentType)
                    .description(description)
                    .subjectId(subjectId)
                    .id(id)
                    .build();
    DocumentResponse res = documentService.updateDocument(document, image, documentRequest);
    return ApiResponse.<DocumentResponse>builder().result(res).build();
  }

  @PreAuthorize("hasAuthority('DELETE_ALL_PERMISSIONS')")
  @DeleteMapping("/{id}")
  public ApiResponse<String> deleteDocument(@PathVariable UUID id) {
    documentService.deleteDocument(id);
    return ApiResponse.<String>builder().result("deleted").build();
  }
}
