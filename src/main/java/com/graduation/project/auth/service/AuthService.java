package com.graduation.project.auth.service;

import com.graduation.project.auth.dto.request.AuthenticationRequest;
import com.graduation.project.auth.dto.request.LoginRequest;
import com.graduation.project.auth.dto.response.AuthenticationResponse;
import com.graduation.project.auth.dto.response.LoginResponse;
import com.graduation.project.auth.exception.AppException;
import com.graduation.project.auth.exception.ErrorCode;
import com.graduation.project.auth.repository.UserRepository;
import com.graduation.project.auth.repository.VerificationTokenRepository;
import com.graduation.project.auth.security.CustomUserDetails;
import com.graduation.project.auth.security.JwtUtils;
import com.graduation.project.common.entity.Provider;
import com.graduation.project.common.entity.Role;
import com.graduation.project.common.entity.User;
import com.graduation.project.common.entity.VerificationToken;
import com.graduation.project.common.service.EmailService;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Transactional
@RequiredArgsConstructor
@Log4j2
@Service
public class AuthService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final JwtUtils jwtUtils;
  private final RefreshTokenService refreshTokenService;
  private final EmailService emailService;
  private final VerificationTokenRepository verificationTokenRepository;

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
    user.setEnabled(false);
    userRepository.save(user);
    String token = generateVerificationCode();
    storeVerifyToken(user, token);

    sendVerificationEmail(user, token);
    return AuthenticationResponse.from(user);
  }

  private void storeVerifyToken(User user, String token) {
    VerificationToken verificationToken = new VerificationToken();
    verificationToken.setToken(token);
    verificationToken.setUser(user);
    verificationToken.setExpiryDate(LocalDateTime.now().plusMinutes(10)); // valid for 10m
    verificationTokenRepository.save(verificationToken);
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

    if (!user.getEnabled()) {
      throw new AppException(ErrorCode.ACCOUNT_NOT_VERIFIED);
    }

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

  public void verifyEmail(String token) {
    VerificationToken verificationToken =
        verificationTokenRepository
            .findByToken(token)
            .orElseThrow(() -> new AppException(ErrorCode.INVALID_TOKEN));

    if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
      throw new AppException(ErrorCode.TOKEN_EXPIRED);
    }

    User user = verificationToken.getUser();
    user.setEnabled(true);
    userRepository.save(user);

    // delete token to prevent reuse
    verificationTokenRepository.delete(verificationToken);
  }

  private void sendVerificationEmail(User user, String token) { // TODO: Update with company logo
    String subject = "Account Verification";
    String verificationCode = "VERIFICATION CODE " + token;
    String htmlMessage =
        "<html>"
            + "<body style=\"font-family: Arial, sans-serif;\">"
            + "<div style=\"background-color: #f5f5f5; padding: 20px;\">"
            + "<h2 style=\"color: #333;\">Welcome to our app!</h2>"
            + "<p style=\"font-size: 16px;\">Please enter the verification code below to continue:</p>"
            + "<div style=\"background-color: #fff; padding: 20px; border-radius: 5px; box-shadow: 0 0 10px rgba(0,0,0,0.1);\">"
            + "<h3 style=\"color: #333;\">Verification Code:</h3>"
            + "<p style=\"font-size: 18px; font-weight: bold; color: #007bff;\">"
            + verificationCode
            + "</p>"
            + "</div>"
            + "</div>"
            + "</body>"
            + "</html>";

    emailService.sendVerificationEmail(user.getEmail(), subject, htmlMessage);
  }

  private String generateVerificationCode() {
    Random random = new Random();
    int code = random.nextInt(900000) + 100000;
    return String.valueOf(code);
  }

  public void resendVerificationCode(String email) {
    Optional<User> optionalUser = userRepository.findByEmail(email);
    if (optionalUser.isPresent()) {
      User user = optionalUser.get();
      if (user.getEnabled()) {
        throw new RuntimeException("Account is already verified");
      }
      verificationTokenRepository.deleteByUserId(user.getId());
      verificationTokenRepository.flush();

      String token = generateVerificationCode();
      storeVerifyToken(user, token);
      sendVerificationEmail(user, token);
    } else {
      throw new RuntimeException("User not found");
    }
  }
}
