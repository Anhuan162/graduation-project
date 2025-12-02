package com.graduation.project.event.service;

import com.graduation.project.auth.repository.UserRepository;
import com.graduation.project.event.entity.ActivityLog;
import com.graduation.project.common.entity.User;
import com.graduation.project.event.repository.ActivityLogRepository;
import com.graduation.project.event.dto.ActivityLogDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityLogHandler {
  private final ActivityLogRepository activityLogRepository;
  private final UserRepository userRepository;

  public void handleActivityLog(ActivityLogDTO activityLogDTO) {
    User user = userRepository.findById(activityLogDTO.getUserId()).orElse(null);

    ActivityLog activityLog =
        ActivityLog.builder()
            .action(activityLogDTO.getAction())
            .user(user)
            .description(activityLogDTO.getDescription())
            .targetType(activityLogDTO.getTargetType())
            .targetId(activityLogDTO.getTargetId())
            .module(activityLogDTO.getModule())
            .createdAt(activityLogDTO.getCreatedAt())
            .build();

    activityLogRepository.save(activityLog);
  }
}
