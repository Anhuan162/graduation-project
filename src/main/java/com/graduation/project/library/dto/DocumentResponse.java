package com.graduation.project.library.dto;

import com.graduation.project.library.constant.DocumentType;
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
