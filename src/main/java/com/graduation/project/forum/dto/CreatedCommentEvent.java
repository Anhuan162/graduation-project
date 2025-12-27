package com.graduation.project.forum.dto;

import com.graduation.project.forum.entity.Comment;
import java.time.Instant;
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

  private Instant createdDateTime;

  public static CreatedCommentEvent from(Comment comment) {
    UUID parentOwnerId = null;
    UUID parentId = null;
    if (comment.getParent() != null) {
      parentId = comment.getParent().getId();
      if (comment.getParent().getAuthor() != null) {
        parentOwnerId = comment.getParent().getAuthor().getId();
      }
    }

    UUID postOwnerId = (comment.getPost() != null && comment.getPost().getAuthor() != null)
        ? comment.getPost().getAuthor().getId()
        : null;

    String senderName = (comment.getAuthor() != null)
        ? comment.getAuthor().getFullName()
        : "Unknown User";

    UUID authorId = (comment.getAuthor() != null)
        ? comment.getAuthor().getId()
        : null;

    if (comment.getPost() == null) {
      throw new IllegalStateException("Comment must have an associated post");
    }

    return CreatedCommentEvent.builder()
        .id(comment.getId())
        .parentCommentOwnerId(parentOwnerId)
        .parentCommentId(parentId)
        .postOwnerId(postOwnerId)
        .postId(comment.getPost().getId())
        .content(comment.getContent())
        .authorId(authorId)
        .senderName(senderName)
        .createdDateTime(comment.getCreatedDateTime())
        .build();
  }
}
