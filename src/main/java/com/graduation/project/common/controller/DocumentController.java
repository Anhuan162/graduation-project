package com.graduation.project.common.controller;

import com.graduation.project.auth.dto.response.ApiResponse;
import com.graduation.project.common.dto.DocumentRequest;
import com.graduation.project.common.dto.DocumentResponse;
import com.graduation.project.common.dto.FileMetadataResponse;
import com.graduation.project.common.entity.AccessType;
import com.graduation.project.common.entity.DocumentType;
import com.graduation.project.common.service.DocumentService;
import com.graduation.project.common.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/document")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;
    @PreAuthorize("hasAuthority('CREATE_ANY_FILES') or hasAuthority('CREATE_ALL_FILES')")
    @PostMapping("/upload")
    public ApiResponse<DocumentResponse> createDocument(
            @RequestParam("document") MultipartFile document,
            @RequestParam("image") MultipartFile image,
            @RequestParam() DocumentRequest documentRequest
            ) throws IOException {
        DocumentResponse res = documentService.uploadDocument(document, image, documentRequest);
        return  ApiResponse.<DocumentResponse>builder().result(res).build();
    }

    @GetMapping("/search")
    public ApiResponse<Page<DocumentResponse>> searchDocumentBySubjectIdAndTitleAndDocumentType(
            @RequestParam() UUID subjectId,
            @RequestParam() String title,
            @RequestParam() DocumentType documentType,
            @RequestParam() Integer pageNumber,
            @RequestParam() Integer pageSize
            ) {
        Page<DocumentResponse> res = documentService.searchDocuments(subjectId, title, documentType, pageNumber, pageSize);
        return ApiResponse.<Page<DocumentResponse>>builder().result(res).build();
    }
}
