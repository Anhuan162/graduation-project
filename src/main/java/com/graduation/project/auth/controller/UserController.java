package com.graduation.project.auth.controller;

import com.graduation.project.auth.dto.VerifyUserDto;
import com.graduation.project.auth.dto.request.SignupRequest;
import com.graduation.project.auth.dto.response.ApiResponse;
import com.graduation.project.auth.dto.response.SignupResponse;
import com.graduation.project.auth.dto.response.UserResponse;
import com.graduation.project.auth.service.UserService;
import java.util.List;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @PostMapping("/register")
  public ApiResponse<SignupResponse> register(@Valid @RequestBody SignupRequest request) {
    return ApiResponse.<SignupResponse>builder()
        .result(userService.register(request))
        .build();
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

  @GetMapping
  ApiResponse<List<UserResponse>> getUsers() {
    return ApiResponse.<List<UserResponse>>builder().result(userService.getUsers()).build();
  }

  @GetMapping("/{userId}")
  ApiResponse<UserResponse> getUser(@PathVariable("userId") String userId) {
    return ApiResponse.<UserResponse>builder().result(userService.getUser(userId)).build();
  }

  @DeleteMapping("/{userId}")
  ApiResponse<String> deleteUser(@PathVariable String userId) {
    userService.deleteUser(userId);
    return ApiResponse.<String>builder().result("User has been deleted").build();
  }
}
