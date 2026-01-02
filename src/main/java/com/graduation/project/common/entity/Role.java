package com.graduation.project.common.entity;

import jakarta.persistence.*;
import java.util.Set;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "roles")
public class Role {
  @Id
  String name;

  String description;

  @ManyToMany
  Set<Permission> permissions;

  public String getName() {
    return name;
  }

  public Set<Permission> getPermissions() {
    return permissions;
  }
}
