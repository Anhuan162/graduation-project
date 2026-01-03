package com.graduation.project.event.dto;

import com.graduation.project.common.constant.ResourceType;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;

@Data
public class ActivityLogResponse {
    private UUID id;
    private UUID userId;
    private String username; // Chỉ lấy tên hiển thị
    private String userAvatar; // Optional
    private String action;
    private String module;
    private String description;
    private UUID targetId;
    private ResourceType targetType;
    private String metadata; // Trả về dạng JSON String hoặc Map<String, Object>
    private String ipAddress;
    private LocalDateTime createdAt;

}