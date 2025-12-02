package com.graduation.project.library.controller;

import com.graduation.project.auth.dto.response.ApiResponse;
import com.graduation.project.library.dto.DocumentRequest;
import com.graduation.project.library.dto.DocumentResponse;
import com.graduation.project.library.constant.DocumentType;
import com.graduation.project.library.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

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
            @RequestParam(required = false) DocumentType documentType
            ) throws IOException {
        DocumentRequest documentRequest = DocumentRequest.builder().title(title)
                .documentType(documentType)
                .description(description)
                .subjectId(subjectId)
                .build();
        DocumentResponse res = documentService.uploadDocument(document, image, documentRequest);
        return  ApiResponse.<DocumentResponse>builder().result(res).build();
    }

    @GetMapping("/search")
    public ApiResponse<Page<DocumentResponse>> searchDocumentBySubjectIdAndTitleAndDocumentType(
            @RequestParam() String subjectId,
            @RequestParam() String title,
            @RequestParam() String documentType,
            @RequestParam() Integer pageNumber,
            @RequestParam() Integer pageSize
            ) {
        UUID UUIDSubjectId = null;
        DocumentType EdocumentType = null;
        try {
            UUIDSubjectId = UUID.fromString(subjectId);
        } catch (Exception e){
        }
        try {
            EdocumentType = DocumentType.valueOf(documentType);
        } catch (Exception e){
        }
        Page<DocumentResponse> res = documentService.searchDocuments(UUIDSubjectId, title, EdocumentType, pageNumber, pageSize);
        return ApiResponse.<Page<DocumentResponse>>builder().result(res).build();
    }
}
