package com.graduation.project.auth.dto.response;

import com.graduation.project.common.constant.Provider;
import com.graduation.project.common.entity.User;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
  UUID id;
  String email;
  String password;
  String fullName;
  Boolean enabled;
  String avatar_url;
  String phone;
  Provider provider;
  String studentCode;
  LocalDateTime registrationDate;
  Set<RoleResponse> roles;

  public static UserResponse from(User user) {
    return UserResponse.builder()
        .id(user.getId())
        .email(user.getEmail())
        .password(user.getPassword())
        .fullName(user.getFullName())
        .enabled(user.getEnabled())
        .avatar_url(user.getAvatarUrl())
        .phone(user.getPhone())
        .studentCode(user.getStudentCode())
        .roles(RoleResponse.from(user.getRoles()))
        .build();
  }
}
