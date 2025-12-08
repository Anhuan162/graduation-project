package com.graduation.project.event.controller;

import com.graduation.project.event.dto.ActivityLogResponse;
import com.graduation.project.event.dto.ActivityLogSearchRequest;
import com.graduation.project.event.service.ActivityLogService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/activity-logs")
public class ActivityLogController {

  private final ActivityLogService activityLogService;

  // API 1: Dành cho Admin - Xem toàn bộ log hệ thống
  // GET /api/activity-logs?module=POST&keyword=spam&page=0&size=20
  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Page<ActivityLogResponse>> getAllLogs(
      @ModelAttribute ActivityLogSearchRequest request, Pageable pageable) {
    return ResponseEntity.ok(activityLogService.searchLogs(request, pageable));
  }

  // API 2: Dành cho User - Xem lịch sử của chính mình
  // GET /api/v1/activity-logs/me
  @GetMapping("/me")
  public ResponseEntity<Page<ActivityLogResponse>> getMyLogs(
      @ModelAttribute ActivityLogSearchRequest request, Pageable pageable) {
    return ResponseEntity.ok(activityLogService.searchLogs(request, pageable));
  }

  // API 3: Xem chi tiết 1 log (nếu cần xem metadata dạng raw JSON)
  @GetMapping("/{id}")
  public ResponseEntity<ActivityLogResponse> getLogDetail(@PathVariable UUID id) {
    // Logic findById...
    return ResponseEntity.ok(null); // implement later
  }
}
