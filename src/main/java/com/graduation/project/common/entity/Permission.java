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
@Table(
    name = "permissions",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"resourceType", "permissionType"})})
public class Permission {
  @Id String name;

  @Enumerated(EnumType.STRING)
  ResourceType resourceType;

  @Enumerated(EnumType.STRING)
  PermissionType permissionType;
}
