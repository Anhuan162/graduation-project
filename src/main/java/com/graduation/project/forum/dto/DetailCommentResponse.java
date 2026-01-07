package com.graduation.project.forum.dto;

import com.graduation.project.common.dto.UserSummaryDto;
import com.graduation.project.forum.entity.Comment;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetailCommentResponse {
  private UUID id;
  private String content;
  private UserSummaryDto author;
  private UUID postId;
  private UUID parentId;
  private LocalDateTime createdDateTime;
  private String url;
  private Boolean deleted;
  private Permissions permissions;
  private Long reactionCount;
  private boolean isCommentCreator;
  private boolean canSoftDeletePost;
  private Boolean isLiked;

  @Data
  public static class Permissions {
    private boolean canEdit;
    private boolean canDelete;
    private boolean canReport;
  }

  public static DetailCommentResponse toResponse(
      Comment comment, String url, Boolean isCommentCreator, Boolean canSoftDeletePost, Boolean isLiked) {

    Permissions permissions = new Permissions();
    permissions.setCanEdit(isCommentCreator);
    permissions.setCanDelete(isCommentCreator || canSoftDeletePost);
    permissions.setCanReport(!isCommentCreator);

    return DetailCommentResponse.builder()
        .id(comment.getId())
        .content(comment.getContent())
        .author(
            UserSummaryDto.builder()
                .id(comment.getAuthor().getId())
                .fullName(comment.getAuthor().getFullName())
                .avatarUrl(comment.getAuthor().getAvatarUrl())
                .build())
        .postId(comment.getPost().getId())
        .parentId(Objects.nonNull(comment.getParent()) ? comment.getParent().getId() : null)
        .createdDateTime(comment.getCreatedDateTime())
        .deleted(comment.getDeleted())
        .url(url)
        .reactionCount(comment.getReactionCount())
        .permissions(permissions)
        .isCommentCreator(isCommentCreator)
        .canSoftDeletePost(canSoftDeletePost)
        .isLiked(isLiked)
        .build();
  }
}
