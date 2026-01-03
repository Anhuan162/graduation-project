package com.graduation.project.forum.dto;

import com.graduation.project.forum.constant.ReactionType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReactionToggleResponse {
    private boolean reacted;
    private ReactionType type;
    private long totalReactions;
}