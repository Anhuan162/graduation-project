package com.graduation.project.common.controller;

import com.graduation.project.auth.dto.response.ApiResponse;
import com.graduation.project.common.dto.PermissionRequest;
import com.graduation.project.common.dto.PermissionResponse;
import com.graduation.project.common.service.PermissionService;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/permissions")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PermissionController {
  PermissionService permissionService;

  @PostMapping
  ApiResponse<PermissionResponse> create(@RequestBody PermissionRequest request) {
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
