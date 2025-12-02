package com.graduation.project.event.repository;

import com.graduation.project.event.entity.NotificationEvent;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationEventRepository extends JpaRepository<NotificationEvent, UUID> {}
