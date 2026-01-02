package com.graduation.project.auth.repository;

import com.graduation.project.common.entity.User;
import java.util.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository
    extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {

  Optional<User> findByEmail(String email);

  @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :role")
  List<User> findAllByRoleName(String role);

  @Query("SELECT u.id FROM User u WHERE u.classCode IN :classCodes")
  List<UUID> findUserIdsByClassCodes(@Param("classCodes") Set<String> classCodes);

  Page<User> findAll(Specification<User> spec, Pageable pageable);

  User findUserByEmail(String email);

  List<User> findByStudentCodeIsNotNull(Pageable pageable);

  @Query("SELECT new com.graduation.project.auth.dto.response.UserStatsProjection(" +
      "(SELECT COUNT(p) FROM Post p WHERE p.author.id = :userId), " +
      "(SELECT COUNT(d) FROM Document d WHERE d.uploadedBy.id = :userId)) ")
  com.graduation.project.auth.dto.response.UserStatsProjection getUserStats(@Param("userId") UUID userId);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("UPDATE User u SET u.followerCount = u.followerCount + 1 WHERE u.id = :userId")
  int incrementFollowerCount(@Param("userId") UUID userId);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("UPDATE User u SET u.followerCount = u.followerCount - 1 WHERE u.id = :userId AND u.followerCount > 0")
  int decrementFollowerCount(@Param("userId") UUID userId);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("UPDATE User u SET u.followingCount = u.followingCount + 1 WHERE u.id = :userId")
  int incrementFollowingCount(@Param("userId") UUID userId);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("UPDATE User u SET u.followingCount = u.followingCount - 1 WHERE u.id = :userId AND u.followingCount > 0")
  int decrementFollowingCount(@Param("userId") UUID userId);
}