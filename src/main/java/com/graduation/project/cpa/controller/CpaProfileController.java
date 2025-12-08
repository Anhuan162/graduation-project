package com.graduation.project.cpa.controller;

import com.graduation.project.auth.dto.response.ApiResponse;
import com.graduation.project.cpa.dto.CpaProfileRequest;
import com.graduation.project.cpa.dto.CpaProfileResponse;
import com.graduation.project.cpa.service.CpaProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/cpa-profiles")
public class CpaProfileController {

  public final CpaProfileService cpaProfileService;

  @PostMapping
  public ApiResponse<CpaProfileResponse> initializeCpaProfile() {
    return ApiResponse.<CpaProfileResponse>builder()
        .result(cpaProfileService.initializeCpaProfile())
        .build();
  }

  @PutMapping("/add-gpa-profile/{cpaProfileId}")
  public ApiResponse<CpaProfileResponse> addGpaProfileForCpaProfile(
      @PathVariable String cpaProfileId) {
    return ApiResponse.<CpaProfileResponse>builder()
        .result(cpaProfileService.addGpaProfileForCpaProfile(cpaProfileId))
        .build();
  }

  @PutMapping("/{cpaProfileId}/gpa-profile/{gpaProfileId}")
  public ApiResponse<CpaProfileResponse> deleteGpaProfileInCpaProfile(
      @PathVariable String cpaProfileId, @PathVariable String gpaProfileId) {
    return ApiResponse.<CpaProfileResponse>builder()
        .result(cpaProfileService.deleteGpaProfileInCpaProfile(cpaProfileId, gpaProfileId))
        .build();
  }

  @PutMapping("/calculate-cpa-score/{cpaProfileId}")
  public ApiResponse<CpaProfileResponse> addScoreForGpaProfile(
      @PathVariable String cpaProfileId, @RequestBody CpaProfileRequest cpaProfileRequest) {
    return ApiResponse.<CpaProfileResponse>builder()
        .result(cpaProfileService.calculateCpaScore(cpaProfileId, cpaProfileRequest))
        .build();
  }

  @DeleteMapping("/{cpaProfileId}")
  public ApiResponse<Void> deleteCpaProfile(@PathVariable String cpaProfileId) {
    cpaProfileService.deleteCpaProfile(cpaProfileId);
    return ApiResponse.<Void>builder().result(null).build();
  }

  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ApiResponse<Page<CpaProfileResponse>> getCpaProfiles(
      @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
          Pageable pageable) {
    return ApiResponse.<Page<CpaProfileResponse>>builder()
        .result(cpaProfileService.getCpaProfiles(pageable))
        .build();
  }

  @GetMapping("/{cpaProfileId}")
  public ApiResponse<CpaProfileResponse> getCpaProfile(@PathVariable String cpaProfileId) {
    return ApiResponse.<CpaProfileResponse>builder()
        .result(cpaProfileService.getCpaProfile(cpaProfileId))
        .build();
  }
}
