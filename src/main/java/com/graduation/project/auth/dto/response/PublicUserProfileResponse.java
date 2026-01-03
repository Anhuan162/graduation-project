package com.graduation.project.auth.dto.response;

import com.graduation.project.common.entity.User;
import java.util.UUID;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PublicUserProfileResponse {
    UUID id;
    String fullName;
    String avatarUrl;
    String facultyName;

    public static PublicUserProfileResponse from(User user) {
        return PublicUserProfileResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }
}
