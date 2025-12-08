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
  private Map<ReactionType, Long> counts; // VD: LIKE: 10, LOVE: 5
  private Long totalReactions; // Tổng: 15
  private ReactionType userReaction; // User hiện tại đang thả gì (null nếu chưa thả)
}
