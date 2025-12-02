package com.graduation.project.announcement.controller;

import com.graduation.project.auth.dto.response.ApiResponse;
import com.graduation.project.announcement.dto.FullAnnouncementResponse;
import com.graduation.project.announcement.service.AnnouncementService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/announcements")
public class AnnouncementController {

  private final AnnouncementService announcementService;

  @GetMapping
  public ApiResponse<List<FullAnnouncementResponse>> getAnnouncements() {
    return ApiResponse.<List<FullAnnouncementResponse>>builder()
        .result(announcementService.getAllAnnouncements())
        .build();
  }
}
