package com.graduation.project.forum.dto;

import com.graduation.project.forum.constant.ReactionType;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReactionDetailResponse {
  private UUID userId;
  private String username;
  private String avatarUrl; // Nếu User entity có avatar
  private ReactionType type;
}
