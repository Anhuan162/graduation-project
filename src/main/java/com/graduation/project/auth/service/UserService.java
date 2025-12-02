package com.graduation.project.auth.service;

import com.graduation.project.auth.constant.PredefinedRole;
import com.graduation.project.auth.dto.VerifyUserDto;
import com.graduation.project.auth.dto.request.SignupRequest;
import com.graduation.project.auth.dto.response.SignupResponse;
import com.graduation.project.auth.dto.response.UserResponse;
import com.graduation.project.auth.exception.AppException;
import com.graduation.project.auth.exception.ErrorCode;
import com.graduation.project.auth.repository.UserRepository;
import com.graduation.project.auth.repository.VerificationTokenRepository;
import com.graduation.project.common.constant.Provider;
import com.graduation.project.common.entity.Role;
import com.graduation.project.common.entity.User;
import com.graduation.project.common.entity.VerificationToken;
import com.graduation.project.common.repository.RoleRepository;
import com.graduation.project.common.service.EmailService;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Transactional
@RequiredArgsConstructor
@Log4j2
@Service
public class UserService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final EmailService emailService;
  private final VerificationTokenRepository verificationTokenRepository;
  private final RoleRepository roleRepository;

  public SignupResponse register(SignupRequest request) {
    String email = request.getEmail();
    String rawPassword = request.getPassword();

    if (userRepository.findByEmail(email).isPresent()) {
      throw new AppException(ErrorCode.USER_EXISTED);
    }

    HashSet<Role> roles = new HashSet<>();
    roleRepository.findById(PredefinedRole.USER_ROLE).ifPresent(roles::add);

    User user = new User();
    user.setEmail(email);
    user.setPassword(passwordEncoder.encode(rawPassword));
    user.setProvider(Provider.LOCAL);
    user.setEnabled(false);
    user.setRoles(roles);
    userRepository.save(user);
    String token = generateVerificationCode();
    storeVerifyToken(user, token);

    sendVerificationEmail(user, token);
    return SignupResponse.from(user);
  }

  private void storeVerifyToken(User user, String token) {
    VerificationToken verificationToken = new VerificationToken();
    verificationToken.setToken(token);
    verificationToken.setUser(user);
    verificationToken.setExpiryDate(LocalDateTime.now().plusMinutes(10)); // valid for 10m
    verificationTokenRepository.save(verificationToken);
  }

  public void verifyEmail(VerifyUserDto request) {
    VerificationToken verificationToken =
        verificationTokenRepository
            .findByToken(request.getVerificationCode())
            .orElseThrow(() -> new AppException(ErrorCode.INVALID_TOKEN));

    if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
      throw new AppException(ErrorCode.TOKEN_EXPIRED);
    }

    User user = verificationToken.getUser();
    if (!user.getEmail().equals(request.getEmail())) {
      throw new AppException(ErrorCode.INVALID_TOKEN);
    }
    user.setEnabled(true);
    userRepository.save(user);

    // delete token to prevent reuse
    verificationTokenRepository.delete(verificationToken);
  }

  public void resendVerificationCode(String email) {
    Optional<User> optionalUser = userRepository.findByEmail(email);
    if (optionalUser.isPresent()) {
      User user = optionalUser.get();
      if (user.getEnabled()) {
        throw new AppException(ErrorCode.ACCOUNT_VERIFIED);
      }
      verificationTokenRepository.deleteByUserId(user.getId());
      verificationTokenRepository.flush();

      String token = generateVerificationCode();
      storeVerifyToken(user, token);
      sendVerificationEmail(user, token);
    } else {
      throw new AppException(ErrorCode.USER_NOT_FOUND);
    }
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

  @PreAuthorize("hasRole('ADMIN')")
  public void deleteUser(String userId) {
    userRepository.deleteById(UUID.fromString(userId));
  }

  @PreAuthorize("hasRole('ADMIN')")
  public List<UserResponse> getUsers() {
    log.info("In method get Users");
    return userRepository.findAll().stream().map(UserResponse::from).toList();
  }

  @PreAuthorize("hasRole('ADMIN')")
  public UserResponse getUser(String id) {
    return UserResponse.from(
        userRepository
            .findById(UUID.fromString(id))
            .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND)));
  }
}
