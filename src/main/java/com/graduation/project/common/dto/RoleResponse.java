package com.graduation.project.common.dto;

import com.graduation.project.common.entity.Role;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoleResponse {
  String name;
  Set<PermissionResponse> permissions;

  public static RoleResponse from(Role role) {
    return RoleResponse.builder()
        .name(role.getName())
        .permissions(PermissionResponse.from(role.getPermissions()))
        .build();
  }

  public static Set<RoleResponse> from(Set<Role> roles) {
    return roles.stream().map(RoleResponse::from).collect(Collectors.toSet());
  }
}
