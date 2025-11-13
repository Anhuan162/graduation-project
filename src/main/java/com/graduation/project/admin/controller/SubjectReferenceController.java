package com.graduation.project.admin.controller;

import com.graduation.project.auth.dto.response.ApiResponse;
import com.graduation.project.admin.dto.SubjectReferenceRequest;
import com.graduation.project.admin.dto.SubjectReferenceResponse;
import com.graduation.project.admin.service.SubjectReferenceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/admin/subject-references")
public class SubjectReferenceController {
  private final SubjectReferenceService subjectReferenceService;

  @PostMapping
  public ApiResponse<SubjectReferenceResponse> createSemester(
      @RequestBody @Valid SubjectReferenceRequest subjectReferenceRequest) {
    return ApiResponse.<SubjectReferenceResponse>builder()
        .result(subjectReferenceService.createSubjectReference(subjectReferenceRequest))
        .build();
  }

  @GetMapping("/{subjectReferenceId}")
  public ApiResponse<SubjectReferenceResponse> getSemester(
      @PathVariable String subjectReferenceId) {
    return ApiResponse.<SubjectReferenceResponse>builder()
        .result(subjectReferenceService.getSubjectReference(subjectReferenceId))
        .build();
  }
}
