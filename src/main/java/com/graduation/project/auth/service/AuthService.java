package com.graduation.project.auth.service;

import com.graduation.project.auth.dto.request.AuthenticationRequest;
import com.graduation.project.auth.dto.request.IntrospectRequest;
import com.graduation.project.auth.dto.response.*;
import com.graduation.project.auth.dto.response.PermissionResponse;
import com.graduation.project.auth.repository.InvalidatedTokenRepository;
import com.graduation.project.auth.repository.UserRepository;
import com.graduation.project.common.entity.*;
import com.graduation.project.common.entity.User;
import com.graduation.project.security.exception.AppException;
import com.graduation.project.security.exception.ErrorCode;
import com.nimbusds.jose.*;
import com.nimbusds.jwt.SignedJWT;
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
    User user = userRepository
        .findByEmail(request.getEmail())
        .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

    boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());

    if (Boolean.FALSE.equals(user.getEnabled())) {
      throw new AppException(ErrorCode.ACCOUNT_NOT_VERIFIED);
    }
    if (!authenticated) {
      throw new AppException(ErrorCode.UNAUTHENTICATED);
    }

    String accessToken = tokenService.generateToken(user, false);
    String refreshToken = tokenService.generateToken(user, true);

    return AuthenticationResponse.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .user(UserResponse.from(user))
        .build();
  }

  public RefreshTokenResponse refreshToken(String refreshToken) throws ParseException {
    var signedJWT = tokenService.verifyToken(refreshToken, true);
    User user = invalidateValidToken(signedJWT);

    String newRefreshToken = tokenService.generateToken(user, true);
    String newAccessToken = tokenService.generateToken(user, false);
    return RefreshTokenResponse.builder()
        .accessToken(newAccessToken)
        .refreshToken(newRefreshToken)
        .build();
  }

  private User invalidateValidToken(SignedJWT signedJWT) throws ParseException {
    var jit = signedJWT.getJWTClaimsSet().getJWTID();
    var expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
    var issuedAt = signedJWT.getJWTClaimsSet().getIssueTime();
    String email = signedJWT.getJWTClaimsSet().getSubject();
    User user = userRepository
        .findByEmail(email)
        .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

    InvalidatedToken invalidatedToken = InvalidatedToken.builder()
        .id(UUID.randomUUID())
        .jit(jit)
        .issuedAt(issuedAt)
        .expiryTime(expirationTime)
        .user(user)
        .build();

    invalidatedTokenRepository.save(invalidatedToken);
    log.debug("Invalidated token for user {}", email);

    return user;
  }

  public void logout(String refreshToken) throws ParseException {
    try {
      var signToken = tokenService.verifyToken(refreshToken, true);

      invalidateValidToken(signToken);
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
