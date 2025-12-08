package com.graduation.project.library.controller;

import com.graduation.project.auth.dto.response.ApiResponse;
import com.graduation.project.cpa.constant.CohortCode;
import com.graduation.project.library.dto.SubjectReferenceRequest;
import com.graduation.project.library.dto.SubjectReferenceResponse;
import com.graduation.project.library.service.SubjectReferenceService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/admin/subject-references")
public class AdminSubjectReferenceController {
  private final SubjectReferenceService subjectReferenceService;

  @PostMapping
  public ApiResponse<SubjectReferenceResponse> createSubjectReference(
      @RequestBody @Valid SubjectReferenceRequest subjectReferenceRequest)
      throws BadRequestException {
    return ApiResponse.<SubjectReferenceResponse>builder()
        .result(subjectReferenceService.createSubjectReference(subjectReferenceRequest))
        .build();
  }

  @GetMapping("/{subjectReferenceId}")
  public ApiResponse<SubjectReferenceResponse> getSubjectReference(
      @PathVariable UUID subjectReferenceId) {
    return ApiResponse.<SubjectReferenceResponse>builder()
        .result(subjectReferenceService.getSubjectReference(subjectReferenceId))
        .build();
  }

  @GetMapping
  public ApiResponse<Page<SubjectReferenceResponse>> searchSubjectReferences(
      @RequestParam(required = false) UUID facultyId,
      @RequestParam(required = false) Integer semesterId,
      @RequestParam(required = false) CohortCode cohortCode,
      @RequestParam(required = false) UUID subjectId,
      Pageable pageable) {
    return ApiResponse.<Page<SubjectReferenceResponse>>builder()
        .result(
            subjectReferenceService.searchSubjectReferences(
                facultyId, semesterId, cohortCode, subjectId, pageable))
        .build();
  }

  @DeleteMapping("/{subjectReferenceId}")
  public ApiResponse<String> deleteSubjectReference(@PathVariable UUID subjectReferenceId) {
    subjectReferenceService.deleteSubjectReference(subjectReferenceId);
    return ApiResponse.<String>builder().result("Delete subject reference successfully").build();
  }
}
