package com.graduation.project.common.repository;

import com.graduation.project.common.entity.NotificationEvent;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationEventRepository extends JpaRepository<NotificationEvent, UUID> {}
