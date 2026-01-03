package com.graduation.project.forum.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentStatsResponse {
    private Long reactionCount;
    private Long replyCount;
}
