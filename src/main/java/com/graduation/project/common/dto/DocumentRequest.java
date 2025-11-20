package com.graduation.project.common.dto;

import com.graduation.project.common.entity.DocumentType;
import lombok.Data;

@Data
public class DocumentRequest {
    private String title;
    private String description;
    private DocumentType documentType;
}
