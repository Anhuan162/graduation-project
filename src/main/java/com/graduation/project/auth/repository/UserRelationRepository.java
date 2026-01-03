package com.graduation.project.auth.repository;

import com.graduation.project.auth.entity.UserRelation;
import com.graduation.project.auth.entity.UserRelation.UserRelationId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserRelationRepository extends JpaRepository<UserRelation, UserRelationId> {

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(ur) FROM UserRelation ur WHERE ur.id.followerId = :followerId AND ur.id.followingId = :followingId")
    long countByFollowerIdAndFollowingId(
            @org.springframework.data.repository.query.Param("followerId") UUID followerId,
            @org.springframework.data.repository.query.Param("followingId") UUID followingId);

    long countByFollowerId(UUID followerId);

    long countByFollowingId(UUID followingId);
}
