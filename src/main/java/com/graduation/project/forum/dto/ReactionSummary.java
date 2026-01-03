package com.graduation.project.forum.dto;

import com.graduation.project.forum.constant.ReactionType;
import java.util.Map;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReactionSummary {
  private UUID targetId;
  private Map<ReactionType, Long> counts;
  private Long totalReactions;
  private ReactionType userReaction;
}
