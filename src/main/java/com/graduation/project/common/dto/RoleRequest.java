package com.graduation.project.common.dto;

import com.graduation.project.common.entity.Role;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoleRequest {
  String name;

  public static Role toRole(RoleRequest request) {
    return Role.builder().name(request.getName()).build();
  }
}
