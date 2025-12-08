package com.graduation.project.forum.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentResponse {
  private UUID id;
  private String content;
  private UUID authorId;
  private LocalDateTime createdDateTime;
  private String url;
  private Long reactionCount;
}
