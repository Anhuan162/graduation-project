package com.graduation.project.announcement.controller;

import com.graduation.project.announcement.dto.AnnouncementResponse;
import com.graduation.project.announcement.dto.DetailedAnnouncementResponse;
import com.graduation.project.announcement.service.AnnouncementService;
import com.graduation.project.auth.dto.response.ApiResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/announcements")
public class AnnouncementController {

  private final AnnouncementService announcementService;

  @GetMapping
  public ApiResponse<Page<AnnouncementResponse>> searchAnnouncements(
      @PageableDefault(page = 0, size = 10, sort = "createdDate", direction = Sort.Direction.DESC)
          Pageable pageable) {
    return ApiResponse.<Page<AnnouncementResponse>>builder()
        .result(announcementService.searchActiveAnnouncements(pageable))
        .build();
  }

  @GetMapping("/{announcementId}")
  public ApiResponse<DetailedAnnouncementResponse> getAnnouncement(
      @PathVariable UUID announcementId) {
    return ApiResponse.<DetailedAnnouncementResponse>builder()
        .result(announcementService.getAnnouncement(announcementId))
        .build();
  }
}
