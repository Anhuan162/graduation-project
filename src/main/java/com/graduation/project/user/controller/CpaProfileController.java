package com.graduation.project.user.controller;

import com.graduation.project.auth.dto.response.ApiResponse;
import com.graduation.project.user.dto.CpaProfileResponse;
import com.graduation.project.user.service.CpaProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class CpaProfileController {

  public final CpaProfileService cpaProfileService;

  public ApiResponse<CpaProfileResponse> initializeCpaProfile(@RequestParam String cpaProfileName) {
    return ApiResponse.<CpaProfileResponse>builder()
        .result(cpaProfileService.initializeCpaProfile(cpaProfileName))
        .build();
  }
}
