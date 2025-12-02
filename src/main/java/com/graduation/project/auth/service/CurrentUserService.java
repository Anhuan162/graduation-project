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

  /** Lấy đối tượng UserPrincipal hiện tại từ SecurityContext */
  public UserPrincipal getCurrentUserPrincipal() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal)) {
      throw new IllegalStateException("Không có user đăng nhập hoặc principal không hợp lệ");
    }
    return (UserPrincipal) authentication.getPrincipal();
  }

  /** Lấy email người dùng hiện tại */
  public String getCurrentUserEmail() {
    return getCurrentUserPrincipal().getEmail();
  }

  /** Lấy ID người dùng hiện tại (nếu UserPrincipal có trường id) */
  public UUID getCurrentUserId() {
    return getCurrentUserPrincipal().getId();
  }

  /** Lấy entity User từ database dựa theo email trong principal */
  public User getCurrentUserEntity() {
    String email = getCurrentUserEmail();
    return userRepository
        .findByEmail(email)
        .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
  }

  /** Kiểm tra role hiện tại có phải ADMIN không */
  public boolean isCurrentUserAdmin() {
    return getCurrentUserPrincipal().getAuthorities().stream()
        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
  }
}
