package com.graduation.project.auth.controller;

import com.graduation.project.auth.dto.UserProfileUpdateDto;
import com.graduation.project.auth.dto.VerifyUserDto;
import com.graduation.project.auth.dto.request.*;
import com.graduation.project.auth.dto.response.*;
import com.graduation.project.auth.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("api/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @PostMapping("/register")
  public ApiResponse<SignupResponse> register(@Valid @RequestBody SignupRequest request) {
    return ApiResponse.<SignupResponse>builder().result(userService.register(request)).build();
  }

  @PostMapping("/verify")
  public ApiResponse<Void> verifyEmail(@RequestBody VerifyUserDto request) {
    userService.verifyEmail(request);
    return ApiResponse.<Void>builder().result(null).build();
  }

  @PostMapping("/resend")
  public ApiResponse<Void> resendVerificationCode(@RequestParam String email) {
    userService.resendVerificationCode(email);
    return ApiResponse.<Void>builder().result(null).build();
  }

  @GetMapping("/{userId}")
  ApiResponse<UserResponse> getUser(@PathVariable("userId") String userId) {
    return ApiResponse.<UserResponse>builder().result(userService.getUser(userId)).build();
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping
  ApiResponse<Page<UserResponse>> searchUsers(
      @ModelAttribute SearchUserRequest searchUserRequest,
      @PageableDefault(page = 0, size = 10, sort = "registrationDate", direction = Sort.Direction.DESC) Pageable pageable) {
    return ApiResponse.<Page<UserResponse>>builder()
        .result(userService.searchUsers(searchUserRequest, pageable))
        .build();
  }

  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("/{userId}")
  ApiResponse<String> deleteUser(@PathVariable String userId) {
    userService.deleteUser(userId);
    return ApiResponse.<String>builder().result("User has been deleted").build();
  }

  @PostMapping("/password/reset")
  public ApiResponse<String> requestResetPassword(@RequestBody ResetPasswordRequest request) {
    return ApiResponse.<String>builder()
        .result(userService.sendOtpToUserToResetPassword(request.getEmail()))
        .build();
  }

  @PostMapping("/otp")
  public ApiResponse<String> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
    return ApiResponse.<String>builder()
        .result(userService.verifyOtp(request.getOtp(), request.getEmail()))
        .build();
  }

  @PutMapping("/change-password")
  public ApiResponse<String> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
    return ApiResponse.<String>builder()
        .result(
            userService.changePassword(request.getPasswordSessionId(), request.getNewPassword()))
        .build();
  }

  @PreAuthorize("isAuthenticated()")
  @GetMapping("/me")
  public ApiResponse<UserAuthResponse> getAuthInfo() {
    return ApiResponse.<UserAuthResponse>builder().result(userService.getAuthInfo()).build();
  }

  @PreAuthorize("isAuthenticated()")
  @GetMapping("/profile")
  public ApiResponse<UserProfileResponse> getMyProfile() {
    return ApiResponse.<UserProfileResponse>builder().result(userService.getMyProfile()).build();
  }

  @PreAuthorize("isAuthenticated()")
  @GetMapping("/{userId}/profile")
  public ApiResponse<UserProfileResponse> getUserProfile(@PathVariable String userId) {
    return ApiResponse.<UserProfileResponse>builder().result(userService.getUserProfile(userId)).build();
  }

  @PreAuthorize("isAuthenticated()")
  @PutMapping(value = "/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ApiResponse<UserProfileResponse> updateUserProfile(
      @Valid @RequestPart("data") UserProfileUpdateDto profileData,
      @RequestPart(value = "avatar", required = false) MultipartFile avatarFile) {
    return ApiResponse.<UserProfileResponse>builder()
        .result(userService.updateUserProfile(profileData, avatarFile))
        .build();
  }

}
