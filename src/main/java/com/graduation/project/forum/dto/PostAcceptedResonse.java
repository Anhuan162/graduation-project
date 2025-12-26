package com.graduation.project.forum.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class PostAcceptedResonse {
    private UUID postId;
    private String title;
    private String content;
    private List<CommentAcceptedResponse> comments;
}
