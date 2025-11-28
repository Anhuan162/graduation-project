package com.graduation.project.event.dto;

import com.graduation.project.common.entity.ResourceType;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ActivityLogDTO {
  private UUID id;
  private UUID userId;
  private String action;
  private String module;
  private String description;
  private UUID targetId;
  private ResourceType targetType;
  private String metadata;
  private String ipAddress;
  private String userAgent;
  private LocalDateTime createdAt;

  public static ActivityLogDTO from(
      UUID userId,
      String action,
      String module,
      ResourceType targetType,
      UUID targetId,
      String description,
      String ipAddress) {
    ActivityLogDTO activityLogDTO = new ActivityLogDTO();
    activityLogDTO.setId(UUID.randomUUID());
    activityLogDTO.setUserId(userId);
    activityLogDTO.setAction(action);
    activityLogDTO.setModule(module);
    activityLogDTO.setTargetType(targetType);
    activityLogDTO.setTargetId(targetId);
    activityLogDTO.setDescription(description);
    activityLogDTO.setIpAddress(ipAddress);
    activityLogDTO.setCreatedAt(LocalDateTime.now());
    return activityLogDTO;
  }
}
