package com.graduation.project.forum.controller;

import com.graduation.project.auth.dto.response.ApiResponse;
import com.graduation.project.forum.constant.ReportStatus;
import com.graduation.project.forum.constant.TargetType;
import com.graduation.project.forum.dto.ProcessReportRequest;
import com.graduation.project.forum.dto.ReportRequest;
import com.graduation.project.forum.dto.ReportResponse;
import com.graduation.project.forum.service.ReportService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

  private final ReportService reportService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse<String> createReport(@Valid @RequestBody ReportRequest request) {
    reportService.createReport(request);
    return ApiResponse.<String>builder()
        .result("Create report for " + request.getTargetType() + "and" + request.getTargetId())
        .build();
  }

  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ApiResponse<Page<ReportResponse>> getReports(
      @RequestParam(required = false)
          ReportStatus status, // Lọc theo trạng thái (PENDING/APPROVED...)
      @RequestParam(required = false) TargetType type, // Lọc theo loại (POST/COMMENT)
      @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
    return ApiResponse.<Page<ReportResponse>>builder()
        .result(reportService.searchReportsForAdmin(status, type, pageable))
        .build();
  }

  @GetMapping("topic/{topicId}")
  public ApiResponse<Page<ReportResponse>> searchReportsByTopic(
      @PathVariable UUID topicId,
      @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
    return ApiResponse.<Page<ReportResponse>>builder()
        .result(reportService.searchReportsByTopic(topicId, pageable))
        .build();
  }

  // 2. Xem chi tiết báo cáo
  @GetMapping("/{id}")
  // @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
  public ApiResponse<ReportResponse> getReportDetail(@PathVariable UUID id) {
    return ApiResponse.<ReportResponse>builder().result(reportService.getReportDetail(id)).build();
  }

  // 3. Xử lý báo cáo (Duyệt/Từ chối + Xóa nội dung vi phạm)
  @PatchMapping("/{id}/process")
  @ResponseStatus(HttpStatus.NO_CONTENT) // 204 No Content
  // @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
  public ApiResponse<ReportResponse> processReport(
      @PathVariable UUID id, @Valid @RequestBody ProcessReportRequest request) {
    return ApiResponse.<ReportResponse>builder()
        .result(reportService.processReport(id, request))
        .build();
  }
}
