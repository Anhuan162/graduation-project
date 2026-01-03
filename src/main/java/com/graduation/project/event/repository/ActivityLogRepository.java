package com.graduation.project.event.repository;

import com.graduation.project.event.entity.ActivityLog;

import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface ActivityLogRepository
    extends JpaRepository<ActivityLog, UUID>, JpaSpecificationExecutor<ActivityLog> {

  Page<ActivityLog> findAll(Specification<ActivityLog> spec, Pageable pageable);

  @Query("""
        SELECT al
        FROM ActivityLog al
        LEFT JOIN FETCH al.user
        WHERE al.id = :id
      """)
  Optional<ActivityLog> findDetailById(@Param("id") UUID id);

}
