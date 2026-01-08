package com.graduation.project.announcement.repository;

import com.graduation.project.announcement.dto.SearchActiveAnnouncementRequest;
import com.graduation.project.announcement.entity.Announcement;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, UUID> {

  @Query("SELECT a FROM Announcement a")
  Page<Announcement> getAllAnnouncement(Pageable pageable);

  Page<Announcement> findAll(Specification<Announcement> specification, Pageable pageable);

  boolean existsBySourceUrl(String detailUrl);

  @Query(
"""
    SELECT a FROM Announcement a
    WHERE (:#{#req.announcementProvider} IS NULL
           OR a.announcementProvider = :#{#req.announcementProvider})
      AND (:#{#req.title} IS NULL
           OR a.title LIKE CONCAT('%', :#{#req.title}, '%'))
      AND a.announcementStatus = TRUE
""")
  Page<Announcement> findAllActiveAnnouncements(
      @Param("req") SearchActiveAnnouncementRequest req, Pageable pageable);

  List<Announcement> findTop10ByOrderByCreatedDateDesc();
}
