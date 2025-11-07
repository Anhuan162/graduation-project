package com.graduation.project.user.controller;

import com.graduation.project.auth.dto.response.ApiResponse;
import com.graduation.project.user.dto.AnnouncementResponse;
import com.graduation.project.user.service.AnnouncementService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/user/announcements")
public class AnnouncementController {

  private final AnnouncementService announcementService;

  @GetMapping
  public ApiResponse<List<AnnouncementResponse>> getAnnouncements() {
    return ApiResponse.<List<AnnouncementResponse>>builder()
        .result(announcementService.getAllAnnouncements())
        .build();
  }
}
