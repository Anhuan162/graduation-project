package com.graduation.project.announcement.controller;

import com.graduation.project.announcement.dto.*;
import com.graduation.project.announcement.service.AnnouncementService;
import com.graduation.project.auth.dto.response.ApiResponse;
import com.graduation.project.auth.repository.UserRepository;
import com.graduation.project.auth.security.UserPrincipal;
import com.graduation.project.common.dto.FileResponse;
import com.graduation.project.common.entity.User;

import java.io.IOException;
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
  private final AnnouncementService announcementService;
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
        .result(announcementService.createAnnouncement(request, user))
        .build();
  }

  @PostMapping("release-announcement/{announcementId}")
  public ApiResponse<String> releaseAnnouncement(
      @PathVariable UUID announcementId, @RequestBody ReleaseAnnouncementRequest request) {
    announcementService.releaseAnnouncement(announcementId, request);
    return ApiResponse.<String>builder().result("Release announcement successfully").build();
  }

  @PutMapping("/{announcementId}")
  public ApiResponse<AnnouncementResponse> updateAnnouncement(
      @PathVariable String announcementId,
      @RequestBody UpdatedAnnouncementRequest request,
      @AuthenticationPrincipal UserPrincipal userPrincipal) {
    User user = userRepository
        .findByEmail(userPrincipal.getEmail())
        .orElseThrow(() -> new RuntimeException("User not found"));
    return ApiResponse.<AnnouncementResponse>builder()
        .result(announcementService.updateAnnouncement(announcementId, request, user))
        .build();
  }

  @GetMapping("/{announcementId}")
  public ApiResponse<DetailedAnnouncementResponse> getAnnouncement(
      @PathVariable UUID announcementId) {
    return ApiResponse.<DetailedAnnouncementResponse>builder()
        .result(announcementService.getAnnouncement(announcementId))
        .build();
  }

  @GetMapping("/all")
  public ApiResponse<Page<AnnouncementResponse>> searchAnnouncements(
      @ModelAttribute SearchAnnouncementRequest request, Pageable pageable) {
    Page<AnnouncementResponse> announcementResponses = announcementService.searchAnnouncement(request, pageable);
    return ApiResponse.<Page<AnnouncementResponse>>builder().result(announcementResponses).build();
  }

  @PostMapping("/add-to-drive/{announcementId}")
  public ApiResponse<FileResponse> addToDrive(
      @PathVariable String announcementId) throws IOException {
    return ApiResponse.<FileResponse>builder().result(announcementService.addAnnouncementToDrive(announcementId))
        .build();
  }

  @DeleteMapping("/{announcementId}")
  public ApiResponse<String> deleteAnnouncement(@PathVariable String announcementId) {
    announcementService.deleteAnnouncement(announcementId);
    return ApiResponse.<String>builder().result("Deleted announcement successfully").build();
  }
}
