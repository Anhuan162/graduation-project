package com.graduation.project.forum.repository;

import com.graduation.project.forum.constant.ReportStatus;
import com.graduation.project.forum.constant.TargetType;
import com.graduation.project.forum.entity.Report;
import io.lettuce.core.dynamic.annotation.Param;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportRepository extends JpaRepository<Report, UUID> {

    boolean existsByPostIdAndReporterId(UUID postId, UUID reporterId);

    boolean existsByCommentIdAndReporterId(UUID commentId, UUID reporterId);

    boolean existsByTopicIdAndReporterId(UUID topicId, UUID reporterId);

    @Query("SELECT r FROM Report r WHERE "
            + "(:status IS NULL OR r.status = :status) AND "
            + "(:type IS NULL OR r.targetType = :type)")
    Page<Report> findAllByFilters(
            @Param("status") ReportStatus status, @Param("type") TargetType type, Pageable pageable);

    @Query("SELECT r FROM Report r "
            + "LEFT JOIN r.post p "
            + "LEFT JOIN r.comment c "
            + "LEFT JOIN c.post cp "
            + "LEFT JOIN r.topic t "
            + "WHERE p.topic.id = :topicId OR cp.topic.id = :topicId OR t.id = :topicId")
    Page<Report> findAllReportsByTopic(@Param("topicId") UUID topicId, Pageable pageable);
}
