package com.graduation.project.announcement.controller;

import com.graduation.project.announcement.dto.AnnouncementResponse;
import com.graduation.project.announcement.dto.CreatedAnnonucementResponse;
import com.graduation.project.announcement.dto.CreatedAnnouncementRequest;
import com.graduation.project.announcement.dto.UpdatedAnnouncementRequest;
import com.graduation.project.announcement.service.AdminAnnouncementService;
import com.graduation.project.auth.dto.response.ApiResponse;
import com.graduation.project.auth.repository.UserRepository;
import com.graduation.project.auth.security.UserPrincipal;
import com.graduation.project.common.entity.User;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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

//  ThieuNN
 @GetMapping("/all")
 public  ApiResponse<Page<AnnouncementResponse>> getAllAnnouncements(
         @RequestParam Integer page,
         @RequestParam Integer size
 )
 {
     Page<AnnouncementResponse> announcementResponses = adminAnnouncementService.getAnnouncements(page, size);
     return ApiResponse.<Page<AnnouncementResponse>>builder()
             .result(announcementResponses)
             .build();
 }

//  @GetMapping
//  public List<AnnouncementResponse> getAnnouncements() {
//    return adminAnnouncementService.getAnnouncements();
//  }
}
