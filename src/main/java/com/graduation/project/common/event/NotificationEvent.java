package com.graduation.project.common.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NotificationEvent {
    private String recipientId;
    private String senderId;
    private String title;
    private String content;
    private NotificationType type; // ENUM: POST_APPROVED, DRIVE_UPLOAD...
    private String referenceId; // ID bài viết/tài liệu
    private String referenceType; // POST, DOCUMENT
}
