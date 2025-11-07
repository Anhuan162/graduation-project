package com.graduation.project.user.controller;

import com.graduation.project.auth.dto.response.ApiResponse;
import com.graduation.project.user.dto.CpaProfileRequest;
import com.graduation.project.user.dto.CpaProfileResponse;
import com.graduation.project.user.service.CpaProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/user/cpa-profile")
public class CpaProfileController {

  public final CpaProfileService cpaProfileService;

  @PostMapping
  public ApiResponse<CpaProfileResponse> initializeCpaProfile(@RequestParam String cpaProfileName) {
    return ApiResponse.<CpaProfileResponse>builder()
        .result(cpaProfileService.initializeCpaProfile(cpaProfileName))
        .build();
  }

  @PutMapping("/add-gpa-profile/{cpaProfileId}")
  public ApiResponse<CpaProfileResponse> addGpaProfileForCpaProfile(
      @PathVariable String cpaProfileId) {
    return ApiResponse.<CpaProfileResponse>builder()
        .result(cpaProfileService.addGpaProfileForCpaProfile(cpaProfileId))
        .build();
  }

  @PutMapping("/delete-gpa-profile/{cpaProfileId}")
  public ApiResponse<CpaProfileResponse> deleteGpaProfileInCpaProfile(
      @PathVariable String cpaProfileId, @RequestParam String gpaProfileId) {
    return ApiResponse.<CpaProfileResponse>builder()
        .result(cpaProfileService.deleteGpaProfileInCpaProfile(cpaProfileId, gpaProfileId))
        .build();
  }

  @PutMapping("/{cpaProfileId}}/add-score")
  public ApiResponse<CpaProfileResponse> addScoreForGpaProfile(
      @PathVariable String cpaProfileId, @RequestBody CpaProfileRequest cpaProfileRequest) {
    return ApiResponse.<CpaProfileResponse>builder()
        .result(cpaProfileService.calculateAverageScore(cpaProfileId, cpaProfileRequest))
        .build();
  }
}
