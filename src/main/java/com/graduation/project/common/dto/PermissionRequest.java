package com.graduation.project.common.dto;

import com.graduation.project.common.entity.Permission;
import com.graduation.project.common.entity.PermissionType;
import com.graduation.project.common.entity.ResouceType;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PermissionRequest {
  String name;
  String resourceType;
  String permissionType;

  public static Permission toPermission(PermissionRequest permissionRequest) {
    return Permission.builder()
        .name(permissionRequest.getName())
        .permissionType(PermissionType.valueOf(permissionRequest.getPermissionType()))
        .resouceType(ResouceType.valueOf(permissionRequest.getPermissionType()))
        .build();
  }
}
