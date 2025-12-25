package com.graduation.project.forum.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttachmentResponse {
    private String id;
    private String url;
    private String name;
    private String type;
    private Long size;
}
