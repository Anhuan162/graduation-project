package com.graduation.project.forum.repository;

import com.graduation.project.forum.constant.ReactionType;
import com.graduation.project.forum.constant.TargetType;
import com.graduation.project.forum.entity.Reaction;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ReactionRepository extends JpaRepository<Reaction, UUID> {
        @Query("SELECT r FROM Reaction r WHERE r.user.id = :userId AND r.targetId = :targetId AND r.targetType = :targetType")
        Optional<Reaction> findByUserIdAndTargetIdAndTargetType(
                        @Param("userId") UUID userId,
                        @Param("targetId") UUID targetId,
                        @Param("targetType") TargetType targetType);

        long countByTargetIdAndTargetTypeAndType(
                        UUID targetId, TargetType targetType, ReactionType type);

        long countByTargetIdAndTargetType(UUID targetId, TargetType targetType);

        Page<Reaction> findAllByTargetIdAndTargetType(
                        UUID targetId, TargetType targetType, Pageable pageable);

        Page<Reaction> findAllByTargetIdAndTargetTypeAndType(
                        UUID targetId, TargetType targetType, ReactionType type, Pageable pageable);

        @Query("""
                        SELECT r.type as type, COUNT(r) as count
                        FROM Reaction r
                        WHERE r.targetId = :targetId AND r.targetType = :targetType
                        GROUP BY r.type
                        """)
        List<ReactionCountProjection> countReactionsByTarget(
                        @Param("targetId") UUID targetId, @Param("targetType") TargetType targetType);

        @Query("""
                        SELECT r.targetId
                        FROM Reaction r
                        WHERE r.user.id = :userId
                          AND r.targetType = :targetType
                          AND r.targetId IN :targetIds
                        """)
        List<UUID> findReactedTargetIdsByUser(
                        @Param("userId") UUID userId,
                        @Param("targetType") TargetType targetType,
                        @Param("targetIds") List<UUID> targetIds);

        boolean existsByUserIdAndTargetIdAndTargetType(UUID userId, UUID targetId, TargetType targetType);

        @Modifying
        @Transactional
        @Query("""
                        delete from Reaction r
                        where r.user.id = :userId
                          and r.targetId = :targetId
                          and r.targetType = :targetType
                        """)
        int deleteByUserIdAndTargetIdAndTargetType(
                        @Param("userId") UUID userId,
                        @Param("targetId") UUID targetId,
                        @Param("targetType") TargetType targetType);

        @Modifying
        @Transactional
        @Query("DELETE FROM Reaction r WHERE r.id = :id")
        void deleteByIdExplicit(@Param("id") UUID id);
}
