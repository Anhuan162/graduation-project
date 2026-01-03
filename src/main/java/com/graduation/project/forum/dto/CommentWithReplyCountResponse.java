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
      LocalDateTime createdDateTime,
      long repliesCount,
      String url) {
    this.id = id;
    this.content = content;
    this.authorId = authorId;
    this.createdDateTime = createdDateTime;
    this.repliesCount = repliesCount;
    this.url = url;
  }

  private UUID id;
  private String content;
  private UUID authorId;
  private LocalDateTime createdDateTime;
  private long repliesCount;
  private String url;
  private boolean isCommentCreator;
  private boolean canSoftDeletePost;
}
