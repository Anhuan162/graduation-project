package com.graduation.project.event.repository;

import com.graduation.project.event.entity.UserNotification;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserNotificationRepository extends JpaRepository<UserNotification, UUID> {}
