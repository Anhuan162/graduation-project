package com.graduation.project.security.ultilities;

import com.graduation.project.auth.security.UserPrincipal;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public class SecurityUtils {
  public static Optional<String> getCurrentUserLogin() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null && authentication.isAuthenticated()) {
      Object principal = authentication.getPrincipal();
      if (principal instanceof UserDetails) {
        return Optional.of(((UserDetails) principal).getUsername());
      } else {
        return Optional.of(principal.toString());
      }
    }
    return Optional.empty();
  }

  public static UUID getCurrentUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
      return ((UserPrincipal) authentication.getPrincipal()).getId();
    }
    return null; // Or throw exception if strict
  }

  public static String getCurrentUserIdString() {
    UUID id = getCurrentUserId();
    return id != null ? id.toString() : null;
  }

  public static boolean isAdmin() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
      return ((UserPrincipal) authentication.getPrincipal()).hasRole("ROLE_ADMIN");
    }
    return false;
  }
}
