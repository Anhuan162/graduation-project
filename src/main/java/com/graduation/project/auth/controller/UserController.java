package com.graduation.project.auth.controller;

import com.graduation.project.auth.dto.VerifyUserDto;
import com.graduation.project.auth.dto.request.SearchUserRequest;
import com.graduation.project.auth.dto.request.SignupRequest;
import com.graduation.project.auth.dto.response.*;
import com.graduation.project.auth.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
      @PageableDefault(
              page = 0,
              size = 10,
              sort = "registrationDate",
              direction = Sort.Direction.DESC)
          Pageable pageable) {
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


  @PostMapping("/reset-password")
  public ApiResponse<String> resetPassword (
          @RequestParam String email
  ) {
    return ApiResponse.<String>builder().result(userService.sendOtpToUserToResetPassword(email)).build();
  }

  @PostMapping("/otp")
  public ApiResponse<String> otp (
          @RequestParam String otp,
          @RequestParam String email
  ) {
    return ApiResponse.<String>builder().result( userService.verifyOtp(otp, email)).build();
  }

  @PutMapping("/change-password")
  public ApiResponse<String> changePassword (
          @RequestParam String PasswordSessionId,
          @RequestParam String newPassword
  ) {
    return ApiResponse.<String>builder().result( userService.changePassword(PasswordSessionId, newPassword)).build();
  }

  @GetMapping("/profiles")
  public ApiResponse<UserProfileResponse> getUserProfile () {
    return ApiResponse.<UserProfileResponse>builder().result(userService.getUserProfile()).build();
  }


  @PutMapping("/profiles")
  public ApiResponse<UserProfileResponse> updateUserProfile (
          @RequestParam(value = "image") MultipartFile avatarFile,
          @RequestParam("fullName") String fullName,
          @RequestParam("phone") String phone,
          @RequestParam("studentCode") String studentCode,
          @RequestParam("classCode") String classCode
  ) {

    UserProfileRequest userProfileRequest =
            UserProfileRequest.builder()
                    .phone(phone)
                    .classCode(classCode)
                    .studentCode(studentCode)
                    .avatarFile(avatarFile)
                    .fullName(fullName).build();
    return ApiResponse.<UserProfileResponse>builder().result(userService.updateUserProfile(userProfileRequest)).build();
  }
}
