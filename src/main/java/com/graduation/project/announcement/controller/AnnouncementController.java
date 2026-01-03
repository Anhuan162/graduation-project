package com.graduation.project.announcement.controller;

import com.graduation.project.announcement.constant.AnnouncementType;
import com.graduation.project.announcement.dto.AnnouncementResponse;
import com.graduation.project.announcement.dto.FullAnnouncementResponse;
import com.graduation.project.announcement.service.AnnouncementService;
import com.graduation.project.auth.dto.response.ApiResponse;

import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/announcements")
@RequiredArgsConstructor
public class AnnouncementController {

  private final AnnouncementService announcementService;

  @GetMapping
  public ApiResponse<Page<AnnouncementResponse>> searchAnnouncements(
      @PageableDefault(sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable,
      @RequestParam(required = false) AnnouncementType type,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) Boolean status,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
    return ApiResponse.ok(
        announcementService.searchAnnouncements(pageable, type, keyword, status, fromDate, toDate));
  }

  @GetMapping("/{announcementId}")
  public ApiResponse<FullAnnouncementResponse> getDetail(@PathVariable UUID announcementId) {
    return ApiResponse.ok(announcementService.getAnnouncementDetail(announcementId));
  }

}
