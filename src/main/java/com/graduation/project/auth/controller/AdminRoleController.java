package com.graduation.project.auth.controller;

import com.graduation.project.auth.service.RoleService;

import jakarta.validation.Valid;

import com.graduation.project.auth.dto.response.ApiResponse;
import com.graduation.project.auth.dto.request.AddPermissionRequest;
import com.graduation.project.auth.dto.request.RoleRequest;
import com.graduation.project.auth.dto.response.RoleResponse;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/roles")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AdminRoleController {

  RoleService roleService;

  @PostMapping
  public ApiResponse<RoleResponse> create(@Valid @RequestBody RoleRequest request) {
    return ApiResponse.ok(roleService.create(request));
  }

  @PutMapping("/{roleId}")
  public ApiResponse<RoleResponse> addPermissions(
      @PathVariable String roleId,
      @Valid @RequestBody AddPermissionRequest request) {
    return ApiResponse.ok(roleService.addPermissions(roleId, request));
  }

  @GetMapping
  public ApiResponse<List<RoleResponse>> getAll() {
    return ApiResponse.ok(roleService.getAll());
  }

  @DeleteMapping("/{role}")
  public ApiResponse<String> delete(@PathVariable("role") String role) {
    roleService.delete(role);
    return ApiResponse.ok("ROLE_DELETED");
  }
}
