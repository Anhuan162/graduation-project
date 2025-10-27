package com.graduation.project.common.repository;

import com.graduation.project.common.entity.UserNotification;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserNotificationRepository extends JpaRepository<UserNotification, UUID> {}
