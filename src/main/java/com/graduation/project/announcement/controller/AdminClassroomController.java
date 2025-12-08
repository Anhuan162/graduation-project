package com.graduation.project.announcement.controller;

import com.graduation.project.announcement.dto.ClassroomResponse;
import com.graduation.project.announcement.dto.CreatedClassroomRequest;
import com.graduation.project.announcement.dto.FilterClassroomResponse;
import com.graduation.project.announcement.dto.UpdatedClassroomRequest;
import com.graduation.project.announcement.service.AdminClassroomService;
import com.graduation.project.auth.dto.response.ApiResponse;
import com.graduation.project.cpa.constant.CohortCode;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/admin/classrooms")
@RequiredArgsConstructor
public class AdminClassroomController {
  private final AdminClassroomService adminClassroomService;

  @PostMapping("/{facultyId}")
  public ApiResponse<ClassroomResponse> createClassroom(
      @PathVariable UUID facultyId, @RequestBody CreatedClassroomRequest request) {
    return ApiResponse.<ClassroomResponse>builder()
        .result(adminClassroomService.createClassroom(request, facultyId))
        .build();
  }

  @PutMapping("/{classroomId}")
  public ApiResponse<ClassroomResponse> updateClassroom(
      @PathVariable String classroomId, @RequestBody UpdatedClassroomRequest request) {

    return ApiResponse.<ClassroomResponse>builder()
        .result(adminClassroomService.updateClassroom(classroomId, request))
        .build();
  }

  @GetMapping
  public ApiResponse<Page<ClassroomResponse>> searchAllClassrooms(
      @RequestParam(required = false) String classCode,
      @RequestParam(required = false) UUID facultyId,
      @RequestParam(required = false) CohortCode schoolYearCode,
      Pageable pageable) {
    return ApiResponse.<Page<ClassroomResponse>>builder()
        .result(
            adminClassroomService.searchAllClassrooms(
                classCode, facultyId, schoolYearCode, pageable))
        .build();
  }

  @GetMapping("/classroom-codes")
  public ApiResponse<List<FilterClassroomResponse>> getClassroomsByFacultyIdAndSchoolYearCode(
      @RequestParam(required = false) UUID facultyId,
      @RequestParam(required = false) CohortCode schoolYearCode) {
    return ApiResponse.<List<FilterClassroomResponse>>builder()
        .result(adminClassroomService.getClassroomsForFilter(facultyId, schoolYearCode))
        .build();
  }

  @DeleteMapping("/{classroomId}")
  public ApiResponse<Void> deleteClassroom(@PathVariable String classroomId) {
    adminClassroomService.deleteClassroom(classroomId);
    return ApiResponse.<Void>builder().message("Deleted successfully").build();
  }

  //  @GetMapping("/search")
  //  public ResponseEntity<?> search(
  //      @RequestParam Map<String, String> params,
  //      Pageable pageable,
  //      @AuthenticationPrincipal User user) {
  //    Page<Classroom> page = adminClassroomService.search(params, pageable, user);
  //    return ResponseEntity.ok(page);
  //  }
}
