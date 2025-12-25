package com.graduation.project.auth.service;

import com.graduation.project.security.exception.AppException;
import com.graduation.project.security.exception.ErrorCode;
import com.graduation.project.auth.repository.UserRepository;
import com.graduation.project.auth.security.UserPrincipal;
import com.graduation.project.common.entity.User;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

  private final UserRepository userRepository;

  public UserPrincipal getCurrentUserPrincipal() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      throw new AppException(ErrorCode.UNAUTHENTICATED);
    }

    Object principal = authentication.getPrincipal();
    if (!(principal instanceof UserPrincipal userPrincipal)) {
      throw new AppException(ErrorCode.UNAUTHENTICATED);
    }
    return userPrincipal;
  }

  public String getCurrentUserEmail() {
    return getCurrentUserPrincipal().getEmail();
  }

  public UUID getCurrentUserId() {
    return getCurrentUserPrincipal().getId();
  }

  public User getCurrentUserEntity() {
    String email = getCurrentUserEmail();
    return userRepository.findByEmail(email)
        .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
  }

  public boolean isCurrentUserAdmin() {
    return getCurrentUserPrincipal().getAuthorities().stream()
        .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
  }
}
