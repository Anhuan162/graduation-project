package com.graduation.project.library.dto;

import com.graduation.project.library.constant.DocumentType;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class DocumentResponse {
    private String id;
    private String title;
    private String description;
    private DocumentType documentType;
    private String urlDoc;
    private String urlImage;
    private UUID subjectId;
    private String subjectName;
    private String subjectCode;

    private String documentStatus;
    private UserSummaryDto uploadedBy;
    private boolean isPremium;
    private int viewCount;
    private int downloadCount;
    private int pageCount;
    private String createdAt;

    @Data
    @Builder
    public static class UserSummaryDto {
        private String id;
        private String fullName;
        private String avatarUrl;
    }
}
