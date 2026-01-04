package com.graduation.project.library.controller;

import com.graduation.project.auth.dto.response.ApiResponse;
import com.graduation.project.auth.service.CurrentUserService;
import com.graduation.project.common.entity.User;
import com.graduation.project.library.constant.DocumentStatus;
import com.graduation.project.library.constant.DocumentType;
import com.graduation.project.library.dto.DocumentResponse;
import com.graduation.project.library.service.AdminDocumentService;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/documents")
@RequiredArgsConstructor
public class AdminDocumentController {

    private final AdminDocumentService adminDocumentService;
    private final CurrentUserService currentUserService;

    @PreAuthorize("hasAuthority('MANAGE_ALL_FILES')") // Assuming ADMIN role has this authority
    @GetMapping("/search")
    public ApiResponse<Page<DocumentResponse>> search(
            @RequestParam(required = false) UUID subjectId,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) DocumentType documentType,
            @RequestParam(required = false) DocumentStatus documentStatus,
            Pageable pageable) {
        return ApiResponse.ok(
                adminDocumentService.searchDocuments(subjectId, title, documentType, documentStatus, pageable));
    }

    @PreAuthorize("hasAuthority('MANAGE_ALL_FILES')")
    @PutMapping("/{id}/approve")
    public ApiResponse<Void> approveDocument(@PathVariable UUID id) {
        User currentUser = currentUserService.getCurrentUserEntity();
        adminDocumentService.approveDocument(id, currentUser);
        return ApiResponse.ok(null);
    }

    @PreAuthorize("hasAuthority('MANAGE_ALL_FILES')")
    @PutMapping("/{id}/reject")
    public ApiResponse<Void> rejectDocument(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        User currentUser = currentUserService.getCurrentUserEntity();
        String reason = body.get("reason");
        adminDocumentService.rejectDocument(id, currentUser, reason);
        return ApiResponse.ok(null);
    }
}
