package com.graduation.project.auth.service;

import com.graduation.project.auth.dto.request.AuthenticationRequest;
import com.graduation.project.auth.dto.request.IntrospectRequest;
import com.graduation.project.auth.dto.response.*;
import com.graduation.project.auth.exception.AppException;
import com.graduation.project.auth.exception.ErrorCode;
import com.graduation.project.auth.repository.UserRepository;
import com.graduation.project.common.dto.PermissionResponse;
import com.graduation.project.common.entity.*;
import com.graduation.project.common.repository.InvalidatedTokenRepository;
import com.nimbusds.jose.*;
import jakarta.transaction.Transactional;
import java.text.ParseException;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Transactional
@RequiredArgsConstructor
@Log4j2
@Service
public class AuthService {
  private final UserRepository userRepository;
  private final TokenService tokenService;
  private final InvalidatedTokenRepository invalidatedTokenRepository;

  public AuthenticationResponse login(AuthenticationRequest request) {
    PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
    User user =
        userRepository
            .findByEmail(request.getEmail())
            .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

    boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());

    if (!user.getEnabled()) {
      throw new AppException(ErrorCode.ACCOUNT_NOT_VERIFIED);
    }
    if (!authenticated) {
      throw new AppException(ErrorCode.UNAUTHENTICATED);
    }

    String accessToken = tokenService.generateToken(user, false);
    String refreshToken = tokenService.generateToken(user, true);

    TokenResponse tokenResponse = new TokenResponse(accessToken, refreshToken);
    List<PermissionResponse> permissionResponses = new ArrayList<>();
    user.getRoles()
        .forEach(
            role -> {
              role.getPermissions()
                  .forEach(
                      permission -> {
                        permissionResponses.add(
                            new PermissionResponse(
                                permission.getName(),
                                permission.getResouceType().toString(),
                                permission.getPermissionType().toString()));
                      });
            });
    UserResponse userResponse = UserResponse.from(user);

    return AuthenticationResponse.builder()
        .permissionResponse(permissionResponses)
        .tokenResponse(tokenResponse)
        .userResponse(userResponse)
        .build();
  }

  public RefreshTokenResponse refreshToken(String refreshToken)
      throws ParseException, JOSEException {
    var signedJWT = tokenService.verifyToken(refreshToken, true);

    var jit = signedJWT.getJWTClaimsSet().getJWTID();
    var expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
    var issuedAt = signedJWT.getJWTClaimsSet().getIssueTime();

    InvalidatedToken invalidatedToken =
        InvalidatedToken.builder()
            .id(UUID.randomUUID())
            .jit(jit)
            .issuedAt(issuedAt)
            .expiryTime(expirationTime)
            .build();
    invalidatedTokenRepository.save(invalidatedToken);

    String email = signedJWT.getJWTClaimsSet().getSubject();
    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

    String newRefreshToken = tokenService.generateToken(user, true);
    String newAccessToken = tokenService.generateToken(user, false);
    return RefreshTokenResponse.builder()
        .accessToken(newAccessToken)
        .refreshToken(newRefreshToken)
        .build();
  }

  public void logout(String refreshToken) throws ParseException, JOSEException {
    try {
      var signToken = tokenService.verifyToken(refreshToken, true);

      String jit = signToken.getJWTClaimsSet().getJWTID();
      Date expiryTime = signToken.getJWTClaimsSet().getExpirationTime();
      Date issuedAt = signToken.getJWTClaimsSet().getIssueTime();
      String email = signToken.getJWTClaimsSet().getSubject();
      User user =
          userRepository
              .findByEmail(email)
              .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

      InvalidatedToken invalidatedToken =
          InvalidatedToken.builder()
              .id(UUID.randomUUID())
              .jit(jit)
              .issuedAt(issuedAt)
              .expiryTime(expiryTime)
              .user(user)
              .build();
      invalidatedTokenRepository.save(invalidatedToken);
    } catch (AppException exception) {
      log.info("Token already expired");
    }
  }

  public IntrospectResponse introspect(IntrospectRequest request)
      throws ParseException, JOSEException {
    var token = request.getToken();
    boolean isValid = true;

    try {
      tokenService.verifyToken(token, false);
    } catch (AppException e) {
      isValid = false;
    }

    return IntrospectResponse.builder().valid(isValid).build();
  }
}
