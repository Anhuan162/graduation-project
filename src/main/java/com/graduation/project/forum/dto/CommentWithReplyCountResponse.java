package com.graduation.project.forum.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class CommentWithReplyCountResponse {
  private UUID id;
  private String content;
  private UUID authorId;
  private LocalDateTime createdDateTime;
  private long repliesCount;
  private String url;
}
