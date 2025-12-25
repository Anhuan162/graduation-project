package com.graduation.project.forum.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostStatsResponse {
    private Long viewCount;
    private Long commentCount;
    private Long reactionCount;
}
