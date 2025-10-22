package com.graduation.project.auth.permission_handler;

import com.graduation.project.auth.security.EntityPermissionHandler;
import com.graduation.project.auth.security.UserPrincipal;
import com.graduation.project.common.entity.Classroom;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class ClassroomPermissionHandler implements EntityPermissionHandler<Classroom> {
  @Override
  public boolean supports(Class<?> clazz) {
    return Classroom.class.isAssignableFrom(clazz);
  }

  @Override
  public boolean hasPermission(Authentication auth, Classroom classroom, String permission) {
    var principal = (UserPrincipal) auth.getPrincipal();
    return classroom.getId().equals(principal.getId()) || principal.hasRole(permission);
  }
}
