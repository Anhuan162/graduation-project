package com.graduation.project.admin.controller;

import com.graduation.project.auth.dto.response.ApiResponse;
import com.graduation.project.admin.dto.SemesterRequest;
import com.graduation.project.admin.dto.SemesterResponse;
import com.graduation.project.admin.service.SemesterService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/admin/semesters")
public class SemesterController {

  private final SemesterService semesterService;

  @PostMapping
  public ApiResponse<SemesterResponse> createSemester(
      @RequestBody SemesterRequest semesterRequest) {
    return ApiResponse.<SemesterResponse>builder()
        .result(semesterService.createSemester(semesterRequest))
        .build();
  }

  @GetMapping("/{semesterId}")
  public ApiResponse<SemesterResponse> getSemester(@PathVariable String semesterId) {
    return ApiResponse.<SemesterResponse>builder()
        .result(semesterService.getSemester(semesterId))
        .build();
  }
}
