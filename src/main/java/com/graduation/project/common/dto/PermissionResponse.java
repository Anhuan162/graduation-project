package com.graduation.project.common.dto;

import com.graduation.project.common.entity.Permission;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PermissionResponse {
  String name;
  String resourceType;
  String permissionType;

  public static PermissionResponse from(Permission permission) {
    return PermissionResponse.builder()
        .name(permission.getName())
        .permissionType(String.valueOf(permission.getPermissionType()))
        .resourceType(String.valueOf(permission.getPermissionType()))
        .build();
  }

  public static Set<PermissionResponse> from(Set<Permission> permissions) {
    return permissions.stream().map(PermissionResponse::from).collect(Collectors.toSet());
  }
}
