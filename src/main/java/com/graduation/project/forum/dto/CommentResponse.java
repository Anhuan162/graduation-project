package com.graduation.project.forum.dto;

import com.graduation.project.common.dto.UserSummaryDto;
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
  private UserSummaryDto author;
  private UUID postId;
  private UUID parentId;
  private LocalDateTime createdDateTime;
  private String url;
  private Boolean deleted;
  private Long reactionCount;
  private Boolean isLiked;
}
