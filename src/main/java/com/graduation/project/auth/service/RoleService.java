package com.graduation.project.auth.service;

import com.graduation.project.security.exception.AppException;
import com.graduation.project.security.exception.ErrorCode;
import com.graduation.project.auth.dto.request.AddPermissionRequest;
import com.graduation.project.auth.dto.request.RoleRequest;
import com.graduation.project.auth.dto.response.RoleResponse;
import com.graduation.project.common.entity.Permission;
import com.graduation.project.common.entity.Role;
import com.graduation.project.auth.repository.PermissionRepository;
import com.graduation.project.auth.repository.RoleRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleService {
  RoleRepository roleRepository;
  PermissionRepository permissionRepository;

  @PreAuthorize("hasRole('ADMIN')")
  public RoleResponse create(RoleRequest request) {
    var role = RoleRequest.toRole(request);

    role = roleRepository.save(role);
    return RoleResponse.from(role);
  }

  public RoleResponse addPermissions(String roleId, AddPermissionRequest request) {
    Role role =
        roleRepository
            .findById(roleId)
            .orElseThrow(() -> new AppException(ErrorCode.ANNOUNCEMENT_NOT_FOUND));

    Set<Permission> allPermissions = new HashSet<>(role.getPermissions());
    var permissions = permissionRepository.findAllByNameIn(request.getPermissions());
    allPermissions.addAll(permissions);
    role.setPermissions(allPermissions);
    roleRepository.save(role);
    return RoleResponse.from(role);
  }

  public List<RoleResponse> getAll() {
    return roleRepository.findAll().stream().map(RoleResponse::from).toList();
  }

  public RoleResponse getById(String roleId) {
    Role role =
        roleRepository
            .findById(roleId)
            .orElseThrow(() -> new AppException(ErrorCode.ANNOUNCEMENT_NOT_FOUND));
    return RoleResponse.from(role);
  }

  public void delete(String role) {
    roleRepository.deleteById(role);
  }
}
