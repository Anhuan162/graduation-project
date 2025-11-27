package com.graduation.project.common.dto;

import com.graduation.project.common.entity.DocumentType;
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
