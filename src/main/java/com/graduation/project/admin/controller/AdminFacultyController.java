package com.graduation.project.admin.controller;

import com.graduation.project.admin.dto.*;
import com.graduation.project.admin.service.AdminFacultyService;
import com.graduation.project.auth.dto.response.ApiResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/faculty")
@RequiredArgsConstructor
public class AdminFacultyController {
  private final AdminFacultyService adminFacultyService;

  @PostMapping
  public ApiResponse<FacultyResponse> createFaculty(@RequestBody CreatedFacultyRequest request) {
    return ApiResponse.<FacultyResponse>builder()
        .result(adminFacultyService.createFaculty(request))
        .build();
  }

  @PutMapping("/{facultyId}")
  public ApiResponse<FacultyResponse> updateFaculty(
      @PathVariable String facultyId, @RequestBody UpdatedFacultyRequest request) {

    return ApiResponse.<FacultyResponse>builder()
        .result(adminFacultyService.updateFaculty(facultyId, request))
        .build();
  }

  @GetMapping
  public ApiResponse<List<FacultyResponse>> getAllFaculties() {
    return ApiResponse.<List<FacultyResponse>>builder()
        .result(adminFacultyService.getAllFaculties())
        .build();
  }

  @DeleteMapping("/{facultyId}")
  public ApiResponse<Void> deleteFaculty(@PathVariable String facultyId) {
    adminFacultyService.deleteFaculty(facultyId);
    return ApiResponse.<Void>builder().message("Deleted successfully").build();
  }
}
