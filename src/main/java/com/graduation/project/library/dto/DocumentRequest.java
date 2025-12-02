package com.graduation.project.library.dto;

import com.graduation.project.library.constant.DocumentType;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class DocumentRequest {
    private String title;
    private String description;
    private DocumentType documentType;
    private UUID subjectId;
}
