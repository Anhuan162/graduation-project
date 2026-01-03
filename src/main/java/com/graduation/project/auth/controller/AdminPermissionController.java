package com.graduation.project.auth.controller;

import com.graduation.project.auth.dto.request.PermissionRequest;
import com.graduation.project.auth.dto.response.ApiResponse;
import com.graduation.project.auth.dto.response.PermissionResponse;
import com.graduation.project.auth.service.PermissionService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/permissions")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AdminPermissionController {

  PermissionService permissionService;

  @PostMapping
  public ApiResponse<PermissionResponse> create(@RequestBody @Valid PermissionRequest request) {
    return ApiResponse.ok(permissionService.create(request));
  }

  @GetMapping
  public ApiResponse<Page<PermissionResponse>> getAll(
      @RequestParam(required = false) String name,
      Pageable pageable) {
    return ApiResponse.ok(permissionService.getAll(name, pageable));
  }

  @DeleteMapping("/{permission}")
  public ApiResponse<String> delete(@PathVariable String permission) {
    permissionService.delete(permission);
    return ApiResponse.ok("Deleted successfully");
  }
}
