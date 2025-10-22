package com.graduation.project.auth.security;

import org.springframework.security.core.Authentication;

public interface EntityPermissionHandler<T> {
  boolean supports(Class<?> clazz);

  boolean hasPermission(Authentication authentication, T target, String permission);
}
