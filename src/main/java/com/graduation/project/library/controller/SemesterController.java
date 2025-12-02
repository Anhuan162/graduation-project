package com.graduation.project.library.controller;

import com.graduation.project.auth.dto.response.ApiResponse;
import com.graduation.project.library.dto.SemesterRequest;
import com.graduation.project.library.dto.SemesterResponse;
import com.graduation.project.library.service.SemesterService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/admin/semesters")
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
