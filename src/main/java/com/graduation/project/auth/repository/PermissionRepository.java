package com.graduation.project.auth.repository;

import com.graduation.project.common.entity.Permission;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, String> {
  List<Permission> findAllByNameIn(Set<String> permissions);

  @Query(
      "SELECT p FROM Permission p "
          + "WHERE :name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', CAST(:name AS string), '%'))")
  Page<Permission> findAll(@Param("name") String name, Pageable pageable);
}
