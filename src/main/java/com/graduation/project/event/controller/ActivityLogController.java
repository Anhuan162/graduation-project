package com.graduation.project.event.controller;

import com.graduation.project.auth.dto.response.ApiResponse;
import com.graduation.project.auth.service.CurrentUserService;
import com.graduation.project.event.dto.ActivityLogResponse;
import com.graduation.project.event.dto.ActivityLogSearchRequest;
import com.graduation.project.event.service.ActivityLogService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/activity-logs")
public class ActivityLogController {

  private final ActivityLogService activityLogService;
  private final CurrentUserService currentUserService;

  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ApiResponse<Page<ActivityLogResponse>> getAllLogs(
      @ModelAttribute ActivityLogSearchRequest request, Pageable pageable) {
    return ApiResponse.ok(activityLogService.searchLogs(request, pageable));
  }

  @GetMapping("/me")
  public ApiResponse<Page<ActivityLogResponse>> getMyLogs(
      @ModelAttribute ActivityLogSearchRequest request, Pageable pageable) {
    request.setUserId(currentUserService.getCurrentUserId());
    return ApiResponse.ok(activityLogService.searchLogs(request, pageable));
  }

  @GetMapping("/{id}")
  public ApiResponse<ActivityLogResponse> getLogDetail(@PathVariable UUID id) {
    return ApiResponse.ok(activityLogService.getLogById(id));
  }
}
