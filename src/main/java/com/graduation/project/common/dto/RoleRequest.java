package com.graduation.project.common.dto;

import com.graduation.project.common.entity.Permission;
import com.graduation.project.common.entity.Role;
import java.util.Set;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoleRequest {
  String name;
  Set<String> permissions;

  public static Role toRole(RoleRequest request, Set<Permission> permissions) {
    return Role.builder().name(request.getName()).permissions(permissions).build();
  }
}
