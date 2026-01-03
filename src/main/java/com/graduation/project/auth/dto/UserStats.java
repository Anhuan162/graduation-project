package com.graduation.project.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserStats {
    private long followerCount;
    private long followingCount;
    private long postCount;
    private long docCount;

    // Additional constructor to handle potential Integer/Long mismatch from JPQL if
    // needed,
    // but Lombok AllArgsConstructor usually suffices if types are compatible.
    // However, Hibernate might pass Integer for SIZE.
    // Let's define one explicitly just in case Lombok one is strict.
    public UserStats(Number followerCount, Number followingCount, Number postCount, Number docCount) {
        this.followerCount = followerCount != null ? followerCount.longValue() : 0;
        this.followingCount = followingCount != null ? followingCount.longValue() : 0;
        this.postCount = postCount != null ? postCount.longValue() : 0;
        this.docCount = docCount != null ? docCount.longValue() : 0;
    }
}
