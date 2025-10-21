package com.graduation.project.common.service;

import com.graduation.project.common.dto.RoleRequest;
import com.graduation.project.common.dto.RoleResponse;
import com.graduation.project.common.repository.PermissionRepository;
import com.graduation.project.common.repository.RoleRepository;
import java.util.HashSet;
import java.util.List;
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
    var permissions = permissionRepository.findAllById(request.getPermissions());

    var role = RoleRequest.toRole(request, (new HashSet<>(permissions)));

    role = roleRepository.save(role);
    return RoleResponse.from(role);
  }

  public List<RoleResponse> getAll() {
    return roleRepository.findAll().stream().map(RoleResponse::from).toList();
  }

  public void delete(String role) {
    roleRepository.deleteById(role);
  }
}
