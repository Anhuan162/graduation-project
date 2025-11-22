package com.graduation.project.user.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CommentWithReplyCountResponse {
  private UUID id;
  private String content;
  private UUID authorId;
  private LocalDateTime createdDateTime;
  private long repliesCount;
}
