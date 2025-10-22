package com.graduation.project.auth.controller;

import com.graduation.project.auth.dto.request.AuthenticationRequest;
import com.graduation.project.auth.dto.request.IntrospectRequest;
import com.graduation.project.auth.dto.response.ApiResponse;
import com.graduation.project.auth.dto.response.AuthenticationResponse;
import com.graduation.project.auth.dto.response.IntrospectResponse;
import com.graduation.project.auth.dto.response.RefreshTokenResponse;
import com.graduation.project.auth.service.AuthService;
import com.nimbusds.jose.JOSEException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  @PostMapping("/login")
  public ApiResponse<AuthenticationResponse> login(@RequestBody AuthenticationRequest request) {
    return ApiResponse.<AuthenticationResponse>builder().result(authService.login(request)).build();
  }

  //  @PostMapping("/refresh")
  //  public ApiResponse<AuthenticationResponse> refresh(@RequestParam String refreshToken)
  //      throws ParseException, JOSEException {
  //    return ApiResponse.<AuthenticationResponse>builder()
  //        .result(authService.refreshToken(refreshToken))
  //        .build();
  //  }

  @PostMapping("/refresh")
  public ApiResponse<RefreshTokenResponse> refresh(HttpServletRequest request)
      throws ParseException, JOSEException {

    String refreshToken = null;
    if (request.getCookies() != null) {
      for (Cookie cookie : request.getCookies()) {
        if ("refreshToken".equals(cookie.getName())) {
          refreshToken = cookie.getValue();
        }
      }
    }

    return ApiResponse.<RefreshTokenResponse>builder()
        .result(authService.refreshToken(refreshToken))
        .build();
  }

  @PostMapping("/logout")
  public ApiResponse<Void> logout(@RequestBody Map<String, String> body)
      throws ParseException, JOSEException {
    String refreshToken = body.get("refreshToken");
    authService.logout(refreshToken);
    return ApiResponse.<Void>builder().result(null).build();
  }

  @PostMapping("/introspect")
  ApiResponse<IntrospectResponse> authenticate(@RequestBody IntrospectRequest request)
      throws ParseException, JOSEException {
    var result = authService.introspect(request);
    return ApiResponse.<IntrospectResponse>builder().result(result).build();
  }
}
