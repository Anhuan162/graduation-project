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

  // Tìm xem user này đã thả tim vào đối tượng này chưa
  Optional<Reaction> findByUserIdAndTargetIdAndTargetType(
      UUID userId, UUID targetId, TargetType targetType);

  // Đếm số lượng cảm xúc (Ví dụ: Bài post này có bao nhiêu TIM?)
  long countByTargetIdAndTargetTypeAndType(UUID targetId, TargetType targetType, ReactionType type);

  // Đếm tổng số cảm xúc của bài viết
  long countByTargetIdAndTargetType(UUID targetId, TargetType targetType);

  // Lấy danh sách reaction của 1 bài viết/comment (để hiển thị ai đã like)
  Page<Reaction> findAllByTargetIdAndTargetType(
      UUID targetId, TargetType targetType, Pageable pageable);

  // Lấy danh sách reaction theo loại cụ thể (VD: Chỉ xem ai thả tim)
  Page<Reaction> findAllByTargetIdAndTargetTypeAndType(
      UUID targetId, TargetType targetType, ReactionType type, Pageable pageable);

  // [QUAN TRỌNG] Đếm số lượng từng loại reaction (Group By)
  // Trả về dạng Interface Projection bên dưới
  @Query(
      "SELECT r.type as type, COUNT(r) as count "
          + "FROM Reaction r "
          + "WHERE r.targetId = :targetId AND r.targetType = :targetType "
          + "GROUP BY r.type")
  List<ReactionCountProjection> countReactionsByTarget(UUID targetId, TargetType targetType);
}
