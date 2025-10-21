package com.graduation.project.admin.controller;

import com.graduation.project.admin.dto.*;
import com.graduation.project.admin.service.AdminClassroomService;
import com.graduation.project.auth.dto.response.ApiResponse;
import com.graduation.project.common.entity.Classroom;
import com.graduation.project.common.entity.User;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/classroom")
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
  public ApiResponse<List<ClassroomResponse>> getAllClassrooms() {
    return ApiResponse.<List<ClassroomResponse>>builder()
        .result(adminClassroomService.getAllClassrooms())
        .build();
  }

  @DeleteMapping("/{classroomId}")
  public ApiResponse<Void> deleteClassroom(@PathVariable String classroomId) {
    adminClassroomService.deleteClassroom(classroomId);
    return ApiResponse.<Void>builder().message("Deleted successfully").build();
  }

  @GetMapping("/search")
  public ResponseEntity<?> search(
      @RequestParam Map<String, String> params,
      Pageable pageable,
      @AuthenticationPrincipal User user) {
    Page<Classroom> page = adminClassroomService.search(params, pageable, user);
    return ResponseEntity.ok(page);
  }
}
