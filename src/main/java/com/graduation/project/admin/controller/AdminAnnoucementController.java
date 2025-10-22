package com.graduation.project.admin.controller;

import com.graduation.project.admin.dto.CreatedAnnoucementRequest;
import com.graduation.project.admin.dto.CreatedAnnoucementResponse;
import com.graduation.project.admin.dto.UpdatedAnnoucementRequest;
import com.graduation.project.admin.dto.UpdatedAnnoucementResponse;
import com.graduation.project.admin.service.AdminAnnoucementService;
import com.graduation.project.auth.dto.response.ApiResponse;
import com.graduation.project.common.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/annoucement")
@RequiredArgsConstructor
public class AdminAnnoucementController {
  private final AdminAnnoucementService adminAnnoucementService;

  @PostMapping
  public ApiResponse<CreatedAnnoucementResponse> createAnnoucement(
      @RequestBody CreatedAnnoucementRequest request, @AuthenticationPrincipal User user) {

    return ApiResponse.<CreatedAnnoucementResponse>builder()
        .result(adminAnnoucementService.createAnnoucement(request, user))
        .build();
  }

  @PutMapping("/{annoucementId}")
  public ApiResponse<UpdatedAnnoucementResponse> updateAnnoucement(
      @PathVariable String annoucementId,
      @RequestBody UpdatedAnnoucementRequest request,
      @AuthenticationPrincipal User user) {

    return ApiResponse.<UpdatedAnnoucementResponse>builder()
        .result(adminAnnoucementService.updateAnnoucement(annoucementId, request, user))
        .build();
  }
}
