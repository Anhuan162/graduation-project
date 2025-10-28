package com.graduation.project.common.repository;

import com.graduation.project.common.entity.Permission;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, String> {
  List<Permission> findAllByNameIn(Set<String> permissions);
}
