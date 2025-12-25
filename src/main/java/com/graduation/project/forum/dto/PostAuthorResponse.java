package com.graduation.project.forum.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostAuthorResponse {
    private String id;
    private String fullName;
    private String avatarUrl;
    private String badge;
    private String faculty;
}
