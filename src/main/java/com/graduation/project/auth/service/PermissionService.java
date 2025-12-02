package com.graduation.project.auth.service;

import com.graduation.project.auth.dto.request.PermissionRequest;
import com.graduation.project.auth.dto.response.PermissionResponse;
import com.graduation.project.common.entity.Permission;
import com.graduation.project.auth.repository.PermissionRepository;
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
public class PermissionService {
  PermissionRepository permissionRepository;

  @PreAuthorize("hasRole('ADMIN')")
  public PermissionResponse create(PermissionRequest request) {
    Permission permission = PermissionRequest.toPermission(request);
    permission = permissionRepository.save(permission);
    return PermissionResponse.from(permission);
  }

  @PreAuthorize("hasRole('ADMIN')")
  public List<PermissionResponse> getAll() {
    var permissions = permissionRepository.findAll();
    return permissions.stream().map(PermissionResponse::from).toList();
  }

  public void delete(String permission) {
    permissionRepository.deleteById(permission);
  }
}
