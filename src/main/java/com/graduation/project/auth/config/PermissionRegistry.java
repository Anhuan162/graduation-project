package com.graduation.project.auth.config;

import com.graduation.project.auth.security.EntityPermissionHandler;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PermissionRegistry {
  private final List<EntityPermissionHandler<?>> handlers;

  @SuppressWarnings("unchecked")
  public <T> boolean hasPermission(Authentication auth, T target, String permission) {
    for (var handler : handlers) {
      if (handler.supports(target.getClass())) {
        return ((EntityPermissionHandler<T>) handler).hasPermission(auth, target, permission);
      }
    }
    return false;
  }
}
