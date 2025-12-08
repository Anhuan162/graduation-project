package com.graduation.project.library.controller;

import com.graduation.project.auth.dto.response.ApiResponse;
import com.graduation.project.cpa.constant.CohortCode;
import com.graduation.project.library.dto.SubjectRequest;
import com.graduation.project.library.dto.SubjectResponse;
import com.graduation.project.library.service.SubjectService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/admin/subjects")
public class AdminSubjectController {

  private final SubjectService subjectService;

  @PostMapping
  public ApiResponse<SubjectResponse> createSubject(@RequestBody SubjectRequest request) {
    return ApiResponse.<SubjectResponse>builder()
        .result(subjectService.createSubject(request))
        .build();
  }

  @GetMapping("/{subjectId}")
  public ApiResponse<SubjectResponse> getSubject(@PathVariable UUID subjectId) {
    return ApiResponse.<SubjectResponse>builder()
        .result(subjectService.getSubject(subjectId))
        .build();
  }

  @GetMapping
  public ApiResponse<List<SubjectResponse>> getAllSubjects() {
    return ApiResponse.<List<SubjectResponse>>builder()
        .result(subjectService.getAllSubjects())
        .build();
  }

  @GetMapping("/search")
  public ApiResponse<Page<SubjectResponse>> search(
      @RequestParam(required = false) UUID facultyId,
      @RequestParam(required = false) Integer semesterId,
      @RequestParam(required = false) CohortCode cohortCode,
      @RequestParam(required = false) String subjectName,
      @PageableDefault(page = 0, size = 10, sort = "createdDate", direction = Sort.Direction.DESC)
          Pageable pageable) {
    return ApiResponse.<Page<SubjectResponse>>builder()
        .result(
            subjectService.searchSubjects(facultyId, semesterId, cohortCode, subjectName, pageable))
        .build();
  }

  @PutMapping("/{subjectId}")
  public ApiResponse<SubjectResponse> updateSubject(
      @PathVariable UUID subjectId, @RequestBody SubjectRequest request) {
    return ApiResponse.<SubjectResponse>builder()
        .result(subjectService.updateSubject(subjectId, request))
        .build();
  }

  @DeleteMapping("/{subjectId}")
  public ApiResponse<String> delete(@PathVariable UUID subjectId) {
    subjectService.deleteSubject(subjectId);
    return ApiResponse.<String>builder().result("Deleted successfully").build();
  }
}
