package com.graduation.project.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.graduation.project.common.constant.ResourceType;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEventDTO {
  private UUID referenceId;
  private ResourceType type;
  private UUID parentReferenceId;
  private UUID relatedId;
  private String title;
  private String content;
  private UUID senderId;
  private String senderName;
  private Set<UUID> receiverIds;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
  private Instant createdAt;
}
