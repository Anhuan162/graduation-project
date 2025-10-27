package com.graduation.project.admin.controller;

import com.graduation.project.admin.dto.AnnouncementResponse;
import com.graduation.project.admin.dto.CreatedAnnonucementResponse;
import com.graduation.project.admin.dto.CreatedAnnouncementRequest;
import com.graduation.project.admin.dto.UpdatedAnnouncementRequest;
import com.graduation.project.admin.service.AdminAnnouncementService;
import com.graduation.project.auth.dto.response.ApiResponse;
import com.graduation.project.auth.repository.UserRepository;
import com.graduation.project.auth.security.UserPrincipal;
import com.graduation.project.common.entity.User;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/announcement")
@RequiredArgsConstructor
public class AdminAnnouncementController {
  private final AdminAnnouncementService adminAnnouncementService;
  private final UserRepository userRepository;

  @PostMapping
  public ApiResponse<CreatedAnnonucementResponse> createAnnouncement(
      @RequestBody CreatedAnnouncementRequest request,
      @AuthenticationPrincipal UserPrincipal userPrincipal) {

    User user =
        userRepository
            .findByEmail(userPrincipal.getEmail())
            .orElseThrow(() -> new RuntimeException("User not found"));
    return ApiResponse.<CreatedAnnonucementResponse>builder()
        .result(adminAnnouncementService.createAnnouncement(request, user))
        .build();
  }

  @PutMapping("/{announcementId}")
  public ApiResponse<AnnouncementResponse> updateAnnouncement(
      @PathVariable String announcementId,
      @RequestBody UpdatedAnnouncementRequest request,
      @AuthenticationPrincipal UserPrincipal userPrincipal) {
    User user =
        userRepository
            .findByEmail(userPrincipal.getEmail())
            .orElseThrow(() -> new RuntimeException("User not found"));
    return ApiResponse.<AnnouncementResponse>builder()
        .result(adminAnnouncementService.updateAnnouncement(announcementId, request, user))
        .build();
  }

  @GetMapping("/{announcementId}")
  public ApiResponse<AnnouncementResponse> getAnnouncement(@PathVariable String announcementId) {
    return ApiResponse.<AnnouncementResponse>builder()
        .result(adminAnnouncementService.getAnnouncement(announcementId))
        .build();
  }

  @GetMapping
  public List<AnnouncementResponse> getAnnouncements() {
    return adminAnnouncementService.getAnnouncements();
  }
}
