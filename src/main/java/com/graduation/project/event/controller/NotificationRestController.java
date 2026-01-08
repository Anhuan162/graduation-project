package com.graduation.project.event.controller;

import com.graduation.project.auth.dto.response.ApiResponse;
import com.graduation.project.event.dto.UserNotificationResponse;
import com.graduation.project.event.service.NotificationService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationRestController {
    private final NotificationService notificationService;

    @GetMapping
    public ApiResponse<Page<UserNotificationResponse>> getMyNotifications(
            @PageableDefault(sort = "deliveredAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.<Page<UserNotificationResponse>>builder()
                .result(notificationService.getMyNotifications(pageable))
                .build();
    }

    @GetMapping("/unread-count")
    public ApiResponse<Long> getUnreadCount() {
        return ApiResponse.<Long>builder()
                .result(notificationService.getUnreadCount())
                .build();
    }

    @PutMapping("/{id}/read")
    public ApiResponse<Void> markAsRead(@PathVariable UUID id) {
        notificationService.markAsRead(id);
        return ApiResponse.<Void>builder().message("Marked as read").build();
    }

    @PutMapping("/read-all")
    public ApiResponse<Void> markAllAsRead() {
        notificationService.markAllAsRead();
        return ApiResponse.<Void>builder().message("Marked all as read").build();
    }
}
