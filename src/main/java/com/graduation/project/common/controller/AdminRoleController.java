package com.graduation.project.common.controller;

import com.graduation.project.common.service.RoleService;
import com.graduation.project.auth.dto.response.ApiResponse;
import com.graduation.project.common.dto.AddPermissionRequest;
import com.graduation.project.common.dto.RoleRequest;
import com.graduation.project.common.dto.RoleResponse;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("admin/role")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AdminRoleController {
  RoleService roleService;

  @PostMapping
  ApiResponse<RoleResponse> create(@RequestBody RoleRequest request) {
    return ApiResponse.<RoleResponse>builder().result(roleService.create(request)).build();
  }

  @PutMapping("/{roleId}")
  ApiResponse<RoleResponse> addPermissions(
      @PathVariable String roleId, @RequestBody AddPermissionRequest request) {
    return ApiResponse.<RoleResponse>builder()
        .result(roleService.addPermissions(roleId, request))
        .build();
  }

  @GetMapping
  ApiResponse<List<RoleResponse>> getAll() {
    return ApiResponse.<List<RoleResponse>>builder().result(roleService.getAll()).build();
  }

  @DeleteMapping("/{role}")
  ApiResponse<Void> delete(@PathVariable String role) {
    roleService.delete(role);
    return ApiResponse.<Void>builder().build();
  }
}
