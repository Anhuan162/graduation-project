package com.graduation.project.auth.mapper;

import com.graduation.project.auth.dto.response.UserProfileResponse;
import com.graduation.project.auth.dto.response.UserStatsProjection;
import com.graduation.project.common.entity.User;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class UserAccessMapper {

    public UserProfileResponse toResponse(User targetUser, User currentUser, UserStatsProjection stats,
            String facultyName, boolean isFollowing) {
        if (targetUser == null) {
            return null; // Or throw specialized exception
        }
        boolean isOwner = currentUser != null && Objects.equals(targetUser.getId(), currentUser.getId());
        // For now, only owner can see private info. Admin check can be added later if
        // needed.

        // Base info (Public)
        UserStatsProjection populatedStats = null;
        if (stats != null) {
            populatedStats = new UserStatsProjection(
                    stats.getPostCount(),
                    stats.getDocCount(),
                    targetUser.getFollowerCount() == null ? 0 : targetUser.getFollowerCount(),
                    targetUser.getFollowingCount() == null ? 0 : targetUser.getFollowingCount());
        }

        UserProfileResponse res = UserProfileResponse.builder()
                .id(targetUser.getId().toString())
                .fullName(targetUser.getFullName())
                .avatarUrl(targetUser.getAvatarUrl())
                .bio(targetUser.getBio())
                .facultyName(facultyName)
                .stats(populatedStats)
                .isOwnProfile(isOwner)
                .isFollowing(isFollowing)
                .build();

        // Private info
        if (isOwner) {
            res.setEmail(targetUser.getEmail());
            res.setStudentCode(targetUser.getStudentCode());
            res.setClassCode(targetUser.getClassCode());
            res.setPhone(targetUser.getPhone());
        }

        return res;
    }
}
