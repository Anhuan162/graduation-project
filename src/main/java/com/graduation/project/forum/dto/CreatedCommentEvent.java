package com.graduation.project.forum.dto;

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
public class CreatedCommentEvent {
  private UUID id;
  private UUID parentCommentOwnerId;
  private UUID postOwnerId;
  private String content;
  private UUID authorId;
  private String senderName;
  private UUID parentCommentId;
  private UUID postId;
  private LocalDateTime createdDateTime;

  public static CreatedCommentEvent from(Comment comment) {
    return CreatedCommentEvent.builder()
        .id(comment.getId())
        .parentCommentOwnerId(
            Objects.nonNull(comment.getParent()) ? comment.getParent().getAuthor().getId() : null)
        .parentCommentId(Objects.nonNull(comment.getParent()) ? comment.getParent().getId() : null)
        .postOwnerId(comment.getPost().getAuthor().getId())
        .postId(comment.getPost().getId())
        .content(comment.getContent())
        .authorId(comment.getAuthor().getId())
        .senderName(comment.getAuthor().getFullName())
        .createdDateTime(comment.getCreatedDateTime())
        .build();
  }
}
