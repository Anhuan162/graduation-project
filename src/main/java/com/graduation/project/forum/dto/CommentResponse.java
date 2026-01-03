package com.graduation.project.forum.dto;

import java.time.Instant;
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

  private Instant createdDateTime;

  private boolean deleted;
  private Long reactionCount;

  private UUID rootCommentId;

  private PostAuthorResponse replyToUser;

  private PostAuthorResponse author;
  private CommentStatsResponse stats;
  private CommentUserStateResponse userState;
  private CommentPermissionsResponse permissions;

  private List<AttachmentResponse> attachments;
}