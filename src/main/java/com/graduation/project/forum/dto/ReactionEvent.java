package com.graduation.project.forum.dto;

import com.graduation.project.common.entity.User;
import com.graduation.project.forum.constant.ReactionType;
import com.graduation.project.forum.constant.TargetType;
import com.graduation.project.forum.entity.Reaction;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReactionEvent {
  private UUID targetId; // ID bài viết/comment
  private TargetType targetType; // POST hoặc COMMENT
  private ReactionType type; // LIKE, LOVE...
  private UUID referenceId;
  private UUID senderId;
  private String senderName;
  private UUID receiverId;

  public static ReactionEvent from(Reaction newReaction, User user, UUID receiverId) {
    return ReactionEvent.builder()
        .targetId(newReaction.getTargetId())
        .targetType(newReaction.getTargetType())
        .type(newReaction.getType())
        .referenceId(newReaction.getId())
        .senderId(user.getId())
        .senderName(user.getEmail())
        .receiverId(receiverId)
        .build();
  }
}
