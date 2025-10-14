package com.graduation.project.auth.controller;

import com.graduation.project.auth.dto.request.AuthenticationRequest;
import com.graduation.project.auth.dto.request.LoginRequest;
import com.graduation.project.auth.dto.response.ApiResponse;
import com.graduation.project.auth.dto.response.AuthenticationResponse;
import com.graduation.project.auth.dto.response.LoginResponse;
import com.graduation.project.auth.service.AuthService;
import com.graduation.project.auth.service.RefreshTokenService;
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

  @PostMapping("/verify")
  public ApiResponse<Void> verify(@RequestBody Map<String, String> request) {
    String token = request.get("token");
    authService.verifyEmail(token);
    return ApiResponse.<Void>builder().result(null).build();
  }

    @PostMapping("/resend")
    public ResponseEntity<?> resendVerificationCode(@RequestParam String email) {
        try {
            authService.resendVerificationCode(email);
            return ResponseEntity.ok("Verification code sent");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
