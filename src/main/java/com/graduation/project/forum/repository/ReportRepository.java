package com.graduation.project.forum.repository;

import com.graduation.project.forum.constant.ReportStatus;
import com.graduation.project.forum.entity.Report;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportRepository extends JpaRepository<Report, UUID> {

  boolean existsByPostIdAndReporterId(UUID postId, UUID reporterId);

  boolean existsByCommentIdAndReporterId(UUID commentId, UUID reporterId);

  Page<Report> findByStatusOrderByCreatedAtDesc(ReportStatus status, Pageable pageable);
}
