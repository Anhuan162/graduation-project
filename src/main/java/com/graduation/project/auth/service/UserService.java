package com.graduation.project.auth.service;

import com.graduation.project.auth.dto.VerifyUserDto;
import com.graduation.project.auth.dto.request.*;
import com.graduation.project.auth.dto.response.*;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRegistrationService userRegistrationService;
  private final UserProfileService userProfileService;
  private final PasswordResetService passwordResetService;
  private final UserAdminService userAdminService;

  // ===== Registration =====
  public SignupResponse register(SignupRequest request) {
    return userRegistrationService.register(request);
  }

  public void verifyEmail(VerifyUserDto request) {
    userRegistrationService.verifyEmail(request);
  }

  public void resendVerificationCode(String email) {
    userRegistrationService.resendVerificationCode(email);
  }

  // ===== Admin =====
  public Page<UserResponse> searchUsers(SearchUserRequest req, Pageable pageable) {
    return userAdminService.searchUsers(req, pageable);
  }

  public void deleteUser(String userId) {
    userAdminService.deleteUser(userId);
  }

  // ===== Query/profile =====
  public UserResponse getUser(String id) {
    return userProfileService.getUser(id);
  }

  public PublicUserProfileResponse getPublicProfile(String userId) {
    // Legacy support or just map it
    UserProfileResponse response = userProfileService.getUserProfile(userId);
    return PublicUserProfileResponse.builder()
        .id(UUID.fromString(response.getId()))
        .fullName(response.getFullName())
        .avatarUrl(response.getAvatarUrl())
        .facultyName(response.getFacultyName())
        .build();
  }

  public UserAuthResponse getAuthInfo() {
    return userProfileService.getAuthInfo();
  }

  public UserProfileResponse getUserProfile() {
    return userProfileService.getMyProfile();
  }

  public UserProfileResponse getUserProfile(String userId) {
    return userProfileService.getUserProfile(userId);
  }

  public UserProfileResponse updateProfileInfo(UserProfileUpdateRequest request) {
    return userProfileService.updateProfileInfo(request);
  }

  public UserProfileResponse updateAvatar(MultipartFile image) {
    return userProfileService.updateAvatar(image);
  }

  public java.util.List<String> getPermissionOfCurrentUser() {
    return userProfileService.getPermissionOfCurrentUser();
  }

  // ===== Password =====
  public String sendOtpToUserToResetPassword(String email) {
    return passwordResetService.sendOtpToUserToResetPassword(email);
  }

  public String verifyOtp(String otp, String email) {
    return passwordResetService.verifyOtp(otp, email);
  }

  public String resetPasswordWithOtp(String email, String otp, String newPassword) {
    return passwordResetService.resetPasswordWithOtp(email, otp, newPassword);
  }

  public String changePasswordAuthenticated(String oldPassword, String newPassword) {
    return passwordResetService.changePasswordAuthenticated(oldPassword, newPassword);
  }
}
