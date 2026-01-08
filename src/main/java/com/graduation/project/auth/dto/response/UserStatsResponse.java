package com.graduation.project.auth.dto.response;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserStatsResponse {
    private long postCount;
    private long documentCount;
    private long commentCount;
    private long reputation; // For now, maybe mock or sum of something
}
