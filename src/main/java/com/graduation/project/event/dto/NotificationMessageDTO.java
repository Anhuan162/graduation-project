package com.graduation.project.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.graduation.project.common.constant.ResourceType;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMessageDTO {
  private UUID relatedId; // Liên kết đến Announcement/Post
  private ResourceType type; // Loại thông báo
  private String title;
  private String content;
  private UUID senderId;
  private String senderName;
  private Set<UUID> receiverIds; // Danh sách user nhận thông báo

  // ✅ format rõ ràng để Redis JSON serializer đọc được
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
  private LocalDateTime createdAt;
}
