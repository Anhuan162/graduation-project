package com.graduation.project.forum.dto;

import com.graduation.project.forum.entity.Comment;

import java.time.Instant;
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
  private UUID authorId;
  private UUID postId;
  private UUID parentId;
  private Instant createdDateTime;
  private String url;
  private Boolean deleted;
  private Long reactionCount;
  private boolean isCommentCreator;
  private boolean canSoftDeletePost;

  public static DetailCommentResponse toResponse(
      Comment comment, String url, Boolean isCommentCreator, Boolean canSoftDeletePost) {
    return DetailCommentResponse.builder()
        .id(comment.getId())
        .content(comment.getContent())
        .authorId(comment.getAuthor().getId())
        .postId(comment.getPost().getId())
        .parentId(Objects.nonNull(comment.getParent()) ? comment.getParent().getId() : null)
        .createdDateTime(comment.getCreatedDateTime())
        .deleted(comment.isDeleted())
        .url(url)
        .reactionCount(comment.getReactionCount())
        .isCommentCreator(isCommentCreator)
        .canSoftDeletePost(canSoftDeletePost)
        .build();
  }
}
