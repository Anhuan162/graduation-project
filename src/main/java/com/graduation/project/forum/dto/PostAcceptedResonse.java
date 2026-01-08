package com.graduation.project.forum.dto;

import com.graduation.project.forum.constant.SyncStatus;
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
    private String authorName;
    private List<CommentAcceptedResponse> comments;
    private SyncStatus syncStatus;
}
