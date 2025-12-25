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
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ReactionRepository extends JpaRepository<Reaction, UUID> {

    // Tìm xem user này đã thả reaction vào target này chưa
    Optional<Reaction> findByUserIdAndTargetIdAndTargetType(
            UUID userId, UUID targetId, TargetType targetType);

    // Đếm số reaction theo loại (VD: bao nhiêu TIM)
    long countByTargetIdAndTargetTypeAndType(
            UUID targetId, TargetType targetType, ReactionType type);

    // Đếm tổng reaction của target
    long countByTargetIdAndTargetType(UUID targetId, TargetType targetType);

    // Lấy danh sách reaction (paging)
    Page<Reaction> findAllByTargetIdAndTargetType(
            UUID targetId, TargetType targetType, Pageable pageable);

    // Lấy danh sách reaction theo loại
    Page<Reaction> findAllByTargetIdAndTargetTypeAndType(
            UUID targetId, TargetType targetType, ReactionType type, Pageable pageable);

    // Group by reaction type (TIM, LIKE, WOW...)
    @Query("""
            SELECT r.type as type, COUNT(r) as count
            FROM Reaction r
            WHERE r.targetId = :targetId AND r.targetType = :targetType
            GROUP BY r.type
            """)
    List<ReactionCountProjection> countReactionsByTarget(
            UUID targetId, TargetType targetType);

    /**
     * CHECK NHANH: user đã reaction vào target chưa?
     * Dùng cho Post Detail
     */
    boolean existsByUserIdAndTargetIdAndTargetType(
            UUID userId, UUID targetId, TargetType targetType);

    /**
     * Trả về danh sách targetId mà user đã reaction
     */
    @Query("""
            SELECT r.targetId
            FROM Reaction r
            WHERE r.user.id = :userId
              AND r.targetType = :targetType
              AND r.targetId IN :targetIds
            """)
    List<UUID> findReactedTargetIdsByUser(
            UUID userId,
            TargetType targetType,
            List<UUID> targetIds);

}
