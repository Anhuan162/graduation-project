package com.graduation.project.auth.repository;

import com.graduation.project.common.entity.User;
import io.lettuce.core.dynamic.annotation.Param;
import java.util.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, UUID> {
  Optional<User> findByEmail(String email);

  @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :role")
  List<User> findAllByRoleName(String role);

  @Query("SELECT u.id FROM User u WHERE u.classCode IN :classCodes")
  List<UUID> findUserIdsByClassCodes(@Param("classCodes") Set<String> classCodes);

  Page<User> findAll(Specification<User> spec, Pageable pageable);

  User findUserByEmail(String email);
}
