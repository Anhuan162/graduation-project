package com.graduation.project.forum.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentResponse {
  private UUID id;
  private String content;
  private UUID authorId;
  private UUID postId;
  private UUID parentId;
  private LocalDateTime createdDateTime;
  private String url;
  private Boolean deleted;
  private Long reactionCount;

  private PostAuthorResponse author;
  private CommentStatsResponse stats;
  private CommentUserStateResponse userState;
  private CommentPermissionsResponse permissions;
  private List<AttachmentResponse> attachments;
}
