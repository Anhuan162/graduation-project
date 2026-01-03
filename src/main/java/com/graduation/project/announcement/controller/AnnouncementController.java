package com.graduation.project.announcement.controller;

import com.graduation.project.announcement.constant.AnnouncementType;
import com.graduation.project.announcement.dto.AnnouncementResponse;
import com.graduation.project.announcement.service.AnnouncementService;
import com.graduation.project.auth.dto.response.ApiResponse;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/announcements")
public class AnnouncementController {

  private final AnnouncementService announcementService;

  @GetMapping
  public ApiResponse<Page<AnnouncementResponse>> search(Pageable pageable,
      @RequestParam(required = false) AnnouncementType type,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) Boolean status,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
    return ApiResponse.ok(announcementService.search(pageable, type, keyword, status, fromDate, toDate));
  }

  @GetMapping("/{announcementId}")
  public ApiResponse<AnnouncementResponse> getDetail(@PathVariable String announcementId) {
    return ApiResponse.ok(announcementService.getDetail(announcementId));
  }
}
