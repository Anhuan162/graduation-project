package com.graduation.project.forum.dto;

import com.graduation.project.common.dto.UserSummaryDto;
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
      UUID parentId,
      Long reactionCount) {
    this.id = id;
    this.content = content;
    this.author = UserSummaryDto.builder()
        .id(authorId)
        .fullName(authorName)
        .avatarUrl(authorAvatarUrl)
        .build();
    this.createdDateTime = createdDateTime;
    this.repliesCount = repliesCount;
    this.url = url;
    this.parentId = parentId;
    this.reactionCount = reactionCount;
  }

  private UUID id;
  private String content;
  private UserSummaryDto author;
  private LocalDateTime createdDateTime;
  private Long repliesCount;
  private String url;
  private UUID parentId;
  private Long reactionCount;
  private Permissions permissions;
  private boolean isCommentCreator;
  private boolean canSoftDeletePost;
  private Boolean isLiked;

  @Data
  public static class Permissions {
    private boolean canEdit;
    private boolean canDelete;
    private boolean canReport;
  }
}
