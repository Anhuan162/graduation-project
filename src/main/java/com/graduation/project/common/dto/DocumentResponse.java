package com.graduation.project.common.dto;

import com.graduation.project.common.entity.DocumentType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DocumentResponse {
    private String title;
    private String description;
    private DocumentType documentType;
    private String urlDoc;
    private String urlImage;
}
