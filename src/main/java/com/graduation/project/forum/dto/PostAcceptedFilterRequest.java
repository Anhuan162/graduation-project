package com.graduation.project.forum.dto;

import com.graduation.project.forum.constant.SyncStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class PostAcceptedFilterRequest {
    private LocalDateTime timeBegin;
    private LocalDateTime timeEnd;
    private String title;
    private UUID topicId;
    private Integer reactionCount;
    private SyncStatus syncStatus;
}
