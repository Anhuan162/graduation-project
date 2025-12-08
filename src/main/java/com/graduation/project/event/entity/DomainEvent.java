package com.graduation.project.event.entity;

import com.graduation.project.common.constant.ResourceType;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@Builder
public class DomainEvent {

  // --- 1. META DATA (Chung) ---
  private LocalDateTime localDateTime;
  private UUID actorId;
  private String actorName;

  // --- 3. WHAT (Action & Context) ---
  // Mapping: ActivityLog.action, ActivityLog.module, Noti.type (logic suy ra từ action)
  private String action; // VD: CREATE, UPDATE, COMMENT_REPLY
  private String module; // VD: FORUM, USER, AUTH

  // --- 4. WHICH RESOURCE (Đối tượng bị tác động) ---
  // Mapping: ActivityLog.targetId, ActivityLog.targetType, Noti.relatedId
  private UUID resourceId;
  private ResourceType resourceType;
  private String title;
  private String content;

  // --- 5. TO WHOM (Đối tượng nhận thông báo) ---
  // Mapping: Noti.receiverIds.
  // Quan trọng: Log ko cần cái này, nhưng Noti bắt buộc phải biết gửi cho ai.
  private Set<UUID> recipientIds;

  // --- 6. PAYLOAD / DETAILS ---
  // Mapping: ActivityLog.description/metadata, Noti.content
  private String message; // Nội dung tóm tắt (VD: "đã bình luận bài viết")
  private Map<String, Object> metadata; // Dữ liệu chi tiết dạng Map (VD: nội dung comment cũ/mới)

  // --- 7. TECHNICAL INFO (Chỉ dành cho Log) ---
  // Mapping: ActivityLog.ipAddress, ActivityLog.userAgent
  private String ipAddress;
  private String userAgent;
}
