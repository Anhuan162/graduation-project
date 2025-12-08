package com.graduation.project.announcement.controller;

import com.graduation.project.announcement.dto.*;
import com.graduation.project.announcement.service.AdminAnnouncementService;
import com.graduation.project.auth.dto.response.ApiResponse;
import com.graduation.project.auth.repository.UserRepository;
import com.graduation.project.auth.security.UserPrincipal;
import com.graduation.project.common.entity.User;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/admin/announcements")
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

  @PostMapping("release-announcement/{announcementId}")
  public ApiResponse<String> releaseAnnouncement(
      @PathVariable UUID announcementId, @RequestBody ReleaseAnnouncementRequest request) {
    adminAnnouncementService.releaseAnnouncement(announcementId, request);
    return ApiResponse.<String>builder().result("Release announcement successfully").build();
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

  //  ThieuNN
  @GetMapping("/all")
  public ApiResponse<Page<AnnouncementResponse>> searchAnnouncements(
      @ModelAttribute SearchAnnouncementRequest request, Pageable pageable) {
    Page<AnnouncementResponse> announcementResponses =
        adminAnnouncementService.searchAnnouncement(request, pageable);
    return ApiResponse.<Page<AnnouncementResponse>>builder().result(announcementResponses).build();
  }

  @DeleteMapping("/{announcementId}")
  public ApiResponse<String> deleteAnnouncement(@PathVariable String announcementId) {
    adminAnnouncementService.deleteAnnouncement(announcementId);
    return ApiResponse.<String>builder().result("Deleted announcement successfully").build();
  }
}
