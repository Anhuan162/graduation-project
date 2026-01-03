package com.graduation.project.auth.service;

import com.graduation.project.announcement.entity.Faculty;
import com.graduation.project.announcement.repository.FacultyRepository;
import com.graduation.project.auth.constant.PredefinedRole;
import com.graduation.project.auth.dto.VerifyUserDto;
import com.graduation.project.auth.dto.request.SearchUserRequest;
import com.graduation.project.auth.dto.request.SignupRequest;
import com.graduation.project.auth.dto.request.UserProfileUpdateRequest;
import com.graduation.project.auth.dto.response.SignupResponse;
import com.graduation.project.auth.dto.response.UserAuthResponse;
import com.graduation.project.auth.dto.response.UserProfileResponse;
import com.graduation.project.auth.dto.response.UserResponse;
import com.graduation.project.auth.dto.response.PublicUserProfileResponse;
import com.graduation.project.auth.repository.PasswordResetSessionRepository;
import com.graduation.project.auth.repository.RoleRepository;
import com.graduation.project.auth.repository.UserRepository;
import com.graduation.project.auth.repository.VerificationTokenRepository;
import com.graduation.project.auth.security.UserPrincipal;
import com.graduation.project.common.constant.Provider;
import com.graduation.project.common.entity.PasswordResetSession;
import com.graduation.project.common.entity.Role;
import com.graduation.project.common.entity.User;
import com.graduation.project.common.entity.VerificationToken;
import com.graduation.project.common.service.FirebaseService;
import com.graduation.project.security.exception.AppException;
import com.graduation.project.security.exception.ErrorCode;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
  private final PasswordResetSessionRepository passwordResetSessionRepository;
  private final FirebaseService firebaseService;
  private final FacultyRepository facultyRepository;
  private final Validator validator;

  private String AVATAR_FOLDER = "avatars";

  public UserProfileResponse updateProfileInfo(UserProfileUpdateRequest request) {
    Set<ConstraintViolation<UserProfileUpdateRequest>> violations = validator.validate(request);
    if (!violations.isEmpty()) {
      StringBuilder message = new StringBuilder();
      for (ConstraintViolation<UserProfileUpdateRequest> violation : violations) {
        message.append(violation.getMessage()).append("; ");
      }
      throw new AppException(ErrorCode.INVALID_REQUEST, message.toString());
    }

    User user = getCurrentUser();
    if (user == null) {
      throw new AppException(ErrorCode.USER_NOT_FOUND);
    }

    String facultiesName = "";

    if (request.getClassCode() != null && !request.getClassCode().isBlank()
        && request.getStudentCode() != null && !request.getStudentCode().isBlank()) {

      facultiesName = getAndValidateFacultiesCode(request.getStudentCode(), request.getClassCode());

      user.setStudentCode(request.getStudentCode());
      user.setClassCode(request.getClassCode());
    }

    if (request.getFullName() != null && !request.getFullName().isBlank()) {
      user.setFullName(request.getFullName().trim());
    }

    if (request.getPhone() != null && !request.getPhone().isBlank()) {
      user.setPhone(request.getPhone().trim());
    }

    userRepository.save(user);

    UserProfileResponse response = user.toUserProfileResponse();
    if (!facultiesName.isBlank()) {
      response.setFacultyName(facultiesName);
    } else if (user.getStudentCode() != null && user.getClassCode() != null) {
      response.setFacultyName(getAndValidateFacultiesCode(user.getStudentCode(), user.getClassCode()));
    }
    return response;
  }

  public UserProfileResponse updateAvatar(MultipartFile image) {
    if (image == null || image.isEmpty()) {
      throw new AppException(ErrorCode.INVALID_REQUEST, "Avatar image is required");
    }

    User user = getCurrentUser();
    if (user == null) {
      throw new RuntimeException("User not authenticated or not found");
    }

    try {
      String oldAvatarUrl = user.getAvatarUrl();
      String newAvatarUrl = firebaseService.uploadFile(image, AVATAR_FOLDER);
      user.setAvatarUrl(newAvatarUrl);
      userRepository.save(user);
      if (oldAvatarUrl != null && !oldAvatarUrl.isEmpty()) {
        try {
          firebaseService.deleteFile(oldAvatarUrl);
        } catch (Exception e) {
          log.warn("Failed to delete old avatar: {}", oldAvatarUrl, e);
        }
      }
    } catch (IOException e) {
      throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
    }

    UserProfileResponse response = user.toUserProfileResponse();
    if (user.getStudentCode() != null && user.getClassCode() != null) {
      response.setFacultyName(getAndValidateFacultiesCode(user.getStudentCode(), user.getClassCode()));
    }
    return response;
  }

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
    user.setRegistrationDate(LocalDateTime.now());
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
    VerificationToken verificationToken = verificationTokenRepository
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
    String htmlMessage = "<html>"
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

  public Page<UserResponse> searchUsers(SearchUserRequest searchUserRequest, Pageable pageable) {
    log.info("In method get Users");

    Specification<User> spec = (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();

      if (searchUserRequest.getEmail() != null && !searchUserRequest.getEmail().trim().isEmpty()) {
        predicates.add(
            cb.like(
                cb.lower(root.get("email")),
                "%" + searchUserRequest.getEmail().toLowerCase() + "%"));
      }

      if (searchUserRequest.getFullName() != null && !searchUserRequest.getFullName().trim().isEmpty()) {
        predicates.add(
            cb.like(
                cb.lower(root.get("fullName")),
                "%" + searchUserRequest.getFullName().toLowerCase() + "%"));
      }

      if (searchUserRequest.getStudentCode() != null && !searchUserRequest.getStudentCode().trim().isEmpty()) {
        predicates.add(
            cb.like(
                cb.lower(root.get("studentCode")),
                "%" + searchUserRequest.getStudentCode().toLowerCase() + "%"));
      }

      if (searchUserRequest.getClassCode() != null && !searchUserRequest.getClassCode().trim().isEmpty()) {
        predicates.add(
            cb.like(
                cb.lower(root.get("classCode")),
                "%" + searchUserRequest.getClassCode().toLowerCase() + "%"));
      }

      if (searchUserRequest.getEnable() != null) {
        predicates.add(cb.equal(root.get("enabled"), searchUserRequest.getEnable()));
      }

      return cb.and(predicates.toArray(new Predicate[0]));
    };

    return userRepository.findAll(spec, pageable).map(UserResponse::from);
  }

  public UserResponse getUser(String id) {
    return UserResponse.from(
        userRepository
            .findById(UUID.fromString(id))
            .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND)));
  }

  public void deleteUser(String userId) {
    userRepository.deleteById(UUID.fromString(userId));
  }

  public String sendOtpToUserToResetPassword(String email) {
    User user = userRepository.findUserByEmail(email);
    if (user == null) {
      throw new AppException(ErrorCode.EMAIL_NOT_FOUND);
    }

    String otp = generateVerificationCode();
    try {
      sendVerificationEmail(user, otp);
    } catch (Exception e) {
      throw new AppException(ErrorCode.CAN_NOT_SEND_EMAIL);
    }

    PasswordResetSession passwordResetSession = passwordResetSessionRepository.findByEmailAndNotUsed(email);
    if (passwordResetSession == null) {
      PasswordResetSession newPasswordResetSession = new PasswordResetSession();
      newPasswordResetSession.setEmail(email);
      newPasswordResetSession.setOtp(otp);
      newPasswordResetSession.setExpiresAt(LocalDateTime.now().plusMinutes(5));
      passwordResetSessionRepository.save(newPasswordResetSession);

    } else if (passwordResetSession.getUsed() == null || !passwordResetSession.getUsed()) {
      passwordResetSession.setOtp(otp);
      passwordResetSession.setExpiresAt(LocalDateTime.now().plusMinutes(5));
      passwordResetSessionRepository.save(passwordResetSession);
    }
    return email;
  }

  public String verifyOtp(String otp, String email) {
    PasswordResetSession passwordResetSession = passwordResetSessionRepository
        .findPasswordResetSessionByEmailAndOtp(email, otp);
    if (passwordResetSession == null) {
      throw new AppException(ErrorCode.INVALID_TOKEN);
    }
    if (passwordResetSession.getExpiresAt().isBefore(LocalDateTime.now())) {
      throw new AppException(ErrorCode.TOKEN_EXPIRED);
    }
    return passwordResetSession.getId().toString();
  }

  @Transactional
  public String changePassword(String passwordResetSessionId, String newPassword) {
    UUID sessionId = null;
    try {
      sessionId = UUID.fromString(passwordResetSessionId);
    } catch (Exception e) {
      throw new AppException(ErrorCode.UUID_IS_INVALID);
    }

    PasswordResetSession passwordResetSession = passwordResetSessionRepository.findById(sessionId)
        .orElseThrow(() -> new AppException(ErrorCode.SESSION_REST_PASSWORD_NOT_FOUND));

    if (passwordResetSession.getUsed() != null && passwordResetSession.getUsed()) {
      throw new AppException(ErrorCode.SESSION_REST_PASSWORD_HAS_USED);
    }
    if (passwordResetSession.getExpiresAt().isBefore(LocalDateTime.now())) {
      throw new AppException(ErrorCode.TOKEN_EXPIRED);
    }

    User user = userRepository.findUserByEmail(passwordResetSession.getEmail());
    user.setPassword(passwordEncoder.encode(newPassword));
    userRepository.save(user);

    passwordResetSession.setUsed(true);
    passwordResetSessionRepository.save(passwordResetSession);
    return "success";
  }

  @Transactional
  public String resetPasswordWithOtp(String email, String otp, String newPassword) {
    PasswordResetSession passwordResetSession = passwordResetSessionRepository
        .findPasswordResetSessionByEmailAndOtp(email, otp);
    if (passwordResetSession == null) {
      throw new AppException(ErrorCode.INVALID_TOKEN);
    }
    if (passwordResetSession.getUsed() != null && passwordResetSession.getUsed()) {
      throw new AppException(ErrorCode.SESSION_REST_PASSWORD_HAS_USED);
    }
    if (passwordResetSession.getExpiresAt().isBefore(LocalDateTime.now())) {
      throw new AppException(ErrorCode.TOKEN_EXPIRED);
    }

    User user = userRepository.findUserByEmail(passwordResetSession.getEmail());
    user.setPassword(passwordEncoder.encode(newPassword));
    userRepository.save(user);

    passwordResetSession.setUsed(true);
    passwordResetSessionRepository.save(passwordResetSession);
    return "success";
  }

  @Transactional
  public String changePasswordAuthenticated(String oldPassword, String newPassword) {
    User user = getCurrentUser();
    if (user == null) {
      throw new AppException(ErrorCode.USER_NOT_FOUND);
    }
    if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
      throw new AppException(ErrorCode.INVALID_PASSWORD);
    }

    user.setPassword(passwordEncoder.encode(newPassword));
    userRepository.save(user);
    return "success";
  }

  public PublicUserProfileResponse getPublicProfile(String userId) {
    UUID id;
    try {
      id = UUID.fromString(userId);
    } catch (IllegalArgumentException e) {
      throw new AppException(ErrorCode.UUID_IS_INVALID);
    }
    User user = userRepository
        .findById(id)
        .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    return PublicUserProfileResponse.from(user);
  }

  public User getCurrentUser() {
    UserPrincipal userPrincipal = getCurrentUserPrincipal();
    if (userPrincipal != null && userPrincipal.getId() != null) {
      return userRepository.findUserByEmail(userPrincipal.getUsername());
    }
    return null;
  }

  public UserPrincipal getCurrentUserPrincipal() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal)) {
      throw new RuntimeException("User not authenticated");
    }
    return (UserPrincipal) auth.getPrincipal();
  }

  public UserProfileResponse getUserProfile() {
    User user = getCurrentUser();
    if (user == null) {
      throw new RuntimeException("User not authenticated or not found");
    }
    UserProfileResponse userProfileResponse = user.toUserProfileResponse();
    if (user.getStudentCode() != null && user.getClassCode() != null)
      userProfileResponse.setFacultyName(
          getAndValidateFacultiesCode(user.getStudentCode(), user.getClassCode()));
    return userProfileResponse;
  }

  public UserAuthResponse getAuthInfo() {
    UserPrincipal userPrincipal = getCurrentUserPrincipal();
    User user = getCurrentUser();
    String facultyName = null;
    if (user != null && user.getStudentCode() != null && user.getClassCode() != null) {
      facultyName = getAndValidateFacultiesCode(user.getStudentCode(), user.getClassCode());
    }
    return UserAuthResponse.builder()
        .id(userPrincipal.getId().toString())
        .email(userPrincipal.getEmail())
        .fullName(userPrincipal.getFullName())
        .avatar(user != null ? user.getAvatarUrl() : userPrincipal.getAvatar())
        .permissions(getUserPermissionsFromPrincipal(userPrincipal))
        .phone(user != null ? user.getPhone() : null)
        .studentCode(user != null ? user.getStudentCode() : null)
        .classCode(user != null ? user.getClassCode() : null)
        .facultyName(facultyName)
        .build();
  }

  private Set<String> getUserPermissionsFromPrincipal(UserPrincipal principal) {
    Set<String> permissions = new HashSet<>();
    for (var auth : principal.getAuthorities()) {
      permissions.add(auth.getAuthority());
    }
    return permissions;
  }

  public List<String> getPermissionOfCurrentUser() {
    UserPrincipal userPrincipal = getCurrentUserPrincipal();
    return new ArrayList<>(getUserPermissionsFromPrincipal(userPrincipal));
  }

  public String getAndValidateFacultiesCode(String studentCode, String classCode) {
    if (studentCode != null && studentCode.length() != 10)
      throw new AppException(ErrorCode.INVALID_STUDENT_CODE);
    if (classCode != null && classCode.length() < 10)
      throw new RuntimeException("class code is invalid");
    String facultiesCodeFromStudent = studentCode.trim().toUpperCase().substring(5, 7);
    String facultiesCodeFromClass = classCode.trim().toUpperCase().substring(5, 7);
    if (!facultiesCodeFromStudent.equals(facultiesCodeFromClass)) {
      throw new RuntimeException("faculties code is invalid");
    }
    Optional<Faculty> faculty = facultyRepository.findByFacultyCode(facultiesCodeFromClass);
    if (faculty.isEmpty())
      throw new AppException(ErrorCode.FACULTY_NOT_FOUND);
    return faculty.get().getFacultyName();
  }
}
