package com.graduation.project.common.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "permissions")
public class Permission {
  @Id String name;

  @Enumerated(EnumType.STRING)
  ResouceType resouceType;

  @Enumerated(EnumType.STRING)
  PermissionType permissionType;
}
