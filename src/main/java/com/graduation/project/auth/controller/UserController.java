package com.graduation.project.auth.controller;

import com.graduation.project.auth.dto.VerifyUserDto;
import com.graduation.project.auth.dto.request.*;
import com.graduation.project.auth.dto.response.*;
import com.graduation.project.auth.security.UserPrincipal;
import com.graduation.project.auth.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;
  private final com.graduation.project.auth.service.FollowService followService;

  @PreAuthorize("isAuthenticated()")
  @PostMapping("/{userId}/follow")
  public ApiResponse<String> followUser(@AuthenticationPrincipal UserPrincipal currentUser, @PathVariable UUID userId) {
    if (currentUser.getId().equals(userId)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot follow yourself");
    }
    followService.follow(currentUser.getId(), userId);
    return ApiResponse.<String>builder().result("Followed successfully").build();
  }

  @PreAuthorize("isAuthenticated()")
  @DeleteMapping("/{userId}/follow")
  public ApiResponse<String> unfollowUser(@PathVariable UUID userId) {
    var currentUser = (com.graduation.project.auth.security.UserPrincipal) org.springframework.security.core.context.SecurityContextHolder
        .getContext().getAuthentication().getPrincipal();
    followService.unfollow(currentUser.getId(), userId);
    return ApiResponse.<String>builder().result("Unfollowed successfully").build();
  }

  @PostMapping("/register")
  public ApiResponse<SignupResponse> register(@Valid @RequestBody SignupRequest request) {
    return ApiResponse.ok(userService.register(request));
  }

  @PostMapping("/verify")
  public ApiResponse<Void> verifyEmail(@RequestBody VerifyUserDto request) {
    userService.verifyEmail(request);
    return ApiResponse.ok(null);
  }

  @PostMapping("/resend")
  public ApiResponse<Void> resendVerificationCode(@RequestParam String email) {
    userService.resendVerificationCode(email);
    return ApiResponse.ok(null);
  }

  @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
  @GetMapping("/{userId}")
  ApiResponse<UserResponse> getUserDetail(@PathVariable("userId") String userId) {
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

  @PutMapping("/password/reset-confirm")
  public ApiResponse<String> confirmResetPassword(@Valid @RequestBody ResetPasswordConfirmRequest request) {
    return ApiResponse.<String>builder()
        .result(userService.resetPasswordWithOtp(request.getEmail(), request.getOtp(), request.getNewPassword()))
        .build();
  }

  @PreAuthorize("isAuthenticated()")
  @PutMapping("/change-password")
  public ApiResponse<String> changePassword(@Valid @RequestBody ChangePasswordAuthenticatedRequest request) {
    return ApiResponse.<String>builder()
        .result(userService.changePasswordAuthenticated(request.getOldPassword(), request.getNewPassword()))
        .build();
  }

  @GetMapping("/public/{userId}")
  public ApiResponse<PublicUserProfileResponse> getPublicProfile(@PathVariable("userId") String userId) {
    return ApiResponse.<PublicUserProfileResponse>builder().result(userService.getPublicProfile(userId)).build();
  }

  @PreAuthorize("isAuthenticated()")
  @GetMapping("/me")
  public ApiResponse<UserAuthResponse> getAuthInfo() {
    return ApiResponse.<UserAuthResponse>builder().result(userService.getAuthInfo()).build();
  }

  @GetMapping("/{userId}/profile")
  public ApiResponse<UserProfileResponse> getUserProfile(@PathVariable("userId") String userId) {
    return ApiResponse.<UserProfileResponse>builder()
        .result(userService.getUserProfile(userId))
        .build();
  }

  @PreAuthorize("isAuthenticated()")
  @GetMapping("/profile")
  public ApiResponse<UserProfileResponse> getMyProfile() {
    return ApiResponse.<UserProfileResponse>builder().result(userService.getUserProfile()).build();
  }

  @PreAuthorize("isAuthenticated()")
  @PatchMapping(value = "/profile", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ApiResponse<UserProfileResponse> updateProfileInfo(
      @Valid @RequestBody UserProfileUpdateRequest request) {
    return ApiResponse.<UserProfileResponse>builder()
        .result(userService.updateProfileInfo(request))
        .build();
  }

  @PreAuthorize("isAuthenticated()")
  @PutMapping(value = "/profile/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ApiResponse<UserProfileResponse> updateAvatar(
      @RequestParam(value = "image") MultipartFile image) {
    return ApiResponse.<UserProfileResponse>builder()
        .result(userService.updateAvatar(image))
        .build();
  }
}
