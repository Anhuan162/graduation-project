package com.graduation.project.controller;

import com.graduation.project.dto.request.AuthenticationRequest;
import com.graduation.project.dto.request.LoginRequest;
import com.graduation.project.dto.response.ApiResponse;
import com.graduation.project.dto.response.AuthenticationResponse;
import com.graduation.project.dto.response.LoginResponse;
import com.graduation.project.service.AuthService;
import com.graduation.project.service.RefreshTokenService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;
  private final RefreshTokenService refreshTokenService;

  @PostMapping("/register")
  public ApiResponse<AuthenticationResponse> register(@RequestBody AuthenticationRequest request) {
    return ApiResponse.<AuthenticationResponse>builder()
        .result(authService.register(request))
        .build();
  }

  @PostMapping("/login")
  public ApiResponse<LoginResponse> login(@RequestBody LoginRequest request) {
    return ApiResponse.<LoginResponse>builder().result(authService.login(request)).build();
  }

  @PostMapping("/refresh")
  public ApiResponse<String> refresh(@RequestParam String refreshToken) {
    return ApiResponse.<String>builder().result(authService.refreshToken(refreshToken)).build();
  }

  @PostMapping("/logout")
  public ResponseEntity<String> logout(@RequestBody Map<String, String> body) {
    String refreshToken = body.get("refreshToken");
    refreshTokenService.revokeRefreshToken(refreshToken);
    return ResponseEntity.ok("Logged out successfully");
  }
}
