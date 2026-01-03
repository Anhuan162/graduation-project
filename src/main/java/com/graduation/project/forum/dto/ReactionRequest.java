package com.graduation.project.forum.dto;

import com.graduation.project.forum.constant.ReactionType;
import com.graduation.project.forum.constant.TargetType;
import java.util.UUID;
import lombok.Data;

@Data
public class ReactionRequest {
  private UUID targetId;
  private TargetType targetType;
  private ReactionType reactionType;
}
