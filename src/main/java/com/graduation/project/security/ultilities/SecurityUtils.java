package com.graduation.project.security.ultilities;

import java.util.Optional;
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
}
