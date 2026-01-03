package com.graduation.project.library.dto;

import com.graduation.project.auth.dto.response.UserProfileResponse;
import com.graduation.project.library.constant.DocumentStatus;
import com.graduation.project.library.constant.DocumentType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class DocumentResponse {
    private UUID id;
    private String title;
    private String description;
    private DocumentType documentType;
    private DocumentStatus documentStatus;
    private String urlDoc;
    private String thumbnailUrl;
    private List<String> previewImages;
    private Integer pageCount;
    private Boolean isPremium;
    private Long fileSize;

    private String rejectionReason;

    private LocalDateTime createdAt;

    // Relations nested DTOs
    private UserProfileResponse uploadedBy;
    private SubjectResponse subject;
}
