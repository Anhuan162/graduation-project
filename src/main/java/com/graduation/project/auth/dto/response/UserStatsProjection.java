package com.graduation.project.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserStatsProjection {
    private long postCount;
    private long docCount;
    private long followerCount;
    private long followingCount;

    public UserStatsProjection(long postCount, long docCount) {
        this.postCount = postCount;
        this.docCount = docCount;
    }
}
