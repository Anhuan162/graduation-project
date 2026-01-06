package com.graduation.project.forum.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;

@Data
public class CommentWithReplyCountResponse {

  public CommentWithReplyCountResponse(
      UUID id,
      String content,
      UUID authorId,
      String authorName,
      String authorAvatarUrl,
      LocalDateTime createdDateTime,
      Long repliesCount,
      String url,
      UUID parentId) {
    this.id = id;
    this.content = content;
    this.authorId = authorId;
    this.authorName = authorName;
    this.authorAvatarUrl = authorAvatarUrl;
    this.createdDateTime = createdDateTime;
    this.repliesCount = repliesCount;
    this.url = url;
    this.parentId = parentId;
  }

  private UUID id;
  private String content;
  private UUID authorId;
  private String authorName;
  private String authorAvatarUrl;
  private LocalDateTime createdDateTime;
  private Long repliesCount;
  private String url;
  private UUID parentId; // Added field
  private boolean isCommentCreator;
  private boolean canSoftDeletePost;
  private Boolean isLiked;
}
