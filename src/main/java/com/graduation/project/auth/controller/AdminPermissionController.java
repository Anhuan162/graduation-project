package com.graduation.project.auth.controller;

import com.graduation.project.auth.dto.response.ApiResponse;
import com.graduation.project.auth.dto.request.PermissionRequest;
import com.graduation.project.auth.dto.response.PermissionResponse;
import com.graduation.project.auth.service.PermissionService;
import java.util.List;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/admin/permissions")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AdminPermissionController {
  PermissionService permissionService;

  @PostMapping
  ApiResponse<PermissionResponse> create(@RequestBody @Valid PermissionRequest request) {
    return ApiResponse.<PermissionResponse>builder()
        .result(permissionService.create(request))
        .build();
  }

  @GetMapping
  ApiResponse<List<PermissionResponse>> getAll() {
    return ApiResponse.<List<PermissionResponse>>builder()
        .result(permissionService.getAll())
        .build();
  }

  @DeleteMapping("/{permission}")
  ApiResponse<Void> delete(@PathVariable String permission) {
    permissionService.delete(permission);
    return ApiResponse.<Void>builder().build();
  }
}
