package com.graduation.project.service;

import com.graduation.project.dto.request.AuthenticationRequest;
import com.graduation.project.dto.request.LoginRequest;
import com.graduation.project.dto.response.AuthenticationResponse;
import com.graduation.project.dto.response.LoginResponse;
import com.graduation.project.entity.Provider;
import com.graduation.project.entity.Role;
import com.graduation.project.entity.User;
import com.graduation.project.exception.AppException;
import com.graduation.project.exception.ErrorCode;
import com.graduation.project.repository.UserRepository;
import com.graduation.project.security.CustomUserDetails;
import com.graduation.project.security.JwtUtils;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Log4j2
@Service
public class AuthService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final JwtUtils jwtUtils;
  private final RefreshTokenService refreshTokenService;

  public AuthenticationResponse register(AuthenticationRequest request) {
    String email = request.getEmail();
    String rawPassword = request.getPassword();

    if (userRepository.findByEmail(email).isPresent()) {
      throw new AppException(ErrorCode.USER_EXISTED);
    }

    User user = new User();
    user.setEmail(email);
    user.setPassword(passwordEncoder.encode(rawPassword));
    user.setProvider(Provider.LOCAL);
    userRepository.save(user);

    return AuthenticationResponse.from(user);
  }

  public LoginResponse login(LoginRequest request) {
    Authentication authentication =
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

    CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
    User user =
        userRepository
            .findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

    List<String> roles = user.getRoles().stream().map(Role::getName).toList();

    String accessToken = jwtUtils.generateAccessToken(user.getEmail(), user.getId(), roles);
    String refreshToken = jwtUtils.generateRefreshToken(user.getEmail());

    refreshTokenService.createRefreshTokenForUser(user, refreshToken);

    return LoginResponse.builder().accessToken(accessToken).refreshToken(refreshToken).build();
  }

  public String refreshToken(String refreshToken) {
    if (!jwtUtils.validate(refreshToken)
        || !refreshTokenService.validateRefreshToken(refreshToken)) {
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    String email = jwtUtils.getUsername(refreshToken);
    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

    List<String> roles = user.getRoles().stream().map(Role::getName).toList();
    return jwtUtils.generateAccessToken(user.getEmail(), user.getId(), roles);
  }
}
