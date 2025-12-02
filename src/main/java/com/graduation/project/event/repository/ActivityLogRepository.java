package com.graduation.project.event.repository;

import com.graduation.project.event.entity.ActivityLog;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, UUID> {}
