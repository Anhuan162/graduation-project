package com.graduation.project.auth.service;

import com.graduation.project.announcement.entity.Faculty;
import com.graduation.project.announcement.repository.FacultyRepository;
import com.graduation.project.auth.constant.PredefinedRole;
import com.graduation.project.auth.dto.VerifyUserDto;
import com.graduation.project.auth.dto.request.SearchUserRequest;
import com.graduation.project.auth.dto.request.SignupRequest;
import com.graduation.project.auth.dto.response.*;
import com.graduation.project.auth.repository.PasswordResetSessionRepository;
import com.graduation.project.auth.repository.RoleRepository;
import com.graduation.project.auth.repository.UserRepository;
import com.graduation.project.auth.repository.VerificationTokenRepository;
import com.graduation.project.auth.repository.InvalidatedTokenRepository;
import com.graduation.project.auth.security.UserPrincipal;
import com.graduation.project.common.constant.Provider;
import com.graduation.project.common.entity.PasswordResetSession;
import com.graduation.project.common.entity.Role;
import com.graduation.project.common.entity.User;
import com.graduation.project.common.entity.VerificationToken;
import com.graduation.project.common.entity.InvalidatedToken;
import com.graduation.project.common.service.FirebaseService;
import com.graduation.project.security.exception.AppException;
import com.graduation.project.security.exception.ErrorCode;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
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
  private final InvalidatedTokenRepository invalidatedTokenRepository;
  private final FirebaseService firebaseService;
  private final FacultyRepository facultyRepository;
  private final UserRegistrationService userRegistrationService;
  private final UserProfileService userProfileService;

  private String AVATAR_FOLDER = "avatars";

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

      if (searchUserRequest.getEmail() != null
          && !searchUserRequest.getEmail().trim().isEmpty()) {
        predicates.add(
            cb.like(
                cb.lower(root.get("email")),
                "%" + searchUserRequest.getEmail().toLowerCase() + "%"));
      }

      if (searchUserRequest.getFullName() != null
          && !searchUserRequest.getFullName().trim().isEmpty()) {
        predicates.add(
            cb.like(
                cb.lower(root.get("fullName")),
                "%" + searchUserRequest.getFullName().toLowerCase() + "%"));
      }

      if (searchUserRequest.getStudentCode() != null
          && !searchUserRequest.getStudentCode().trim().isEmpty()) {
        predicates.add(
            cb.like(
                cb.lower(root.get("studentCode")),
                "%" + searchUserRequest.getStudentCode().toLowerCase() + "%"));
      }

      if (searchUserRequest.getClassCode() != null
          && !searchUserRequest.getClassCode().trim().isEmpty()) {
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
      log.info("Reset password requested for non-existent email: {}", email);
      return email; // Return 200 OK to prevent enumeration
    }

    String otp = generateVerificationCode();
    try {
      sendVerificationEmail(user, otp);
    } catch (Exception e) {
      log.error("Failed to send email to user {}", email, e);
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
    Optional<PasswordResetSession> passwordResetSession = passwordResetSessionRepository.findById(sessionId);
    if (passwordResetSession == null || passwordResetSession.isEmpty()) {
      throw new AppException(ErrorCode.SESSION_REST_PASSWORD_NOT_FOUND);
    }
    if (passwordResetSession.get().getUsed() != null && passwordResetSession.get().getUsed()) {
      throw new AppException(ErrorCode.SESSION_REST_PASSWORD_HAS_USED);
    }
    if (passwordResetSession.get().getExpiresAt().plusMinutes(5).isBefore(LocalDateTime.now())) {
      throw new AppException(ErrorCode.TOKEN_EXPIRED);
    }
    User user = userRepository.findUserByEmail(passwordResetSession.get().getEmail());
    user.setPassword(passwordEncoder.encode(newPassword));
    userRepository.save(user);

    passwordResetSession.get().setUsed(true);
    passwordResetSessionRepository.save(passwordResetSession.get());

    // Invalidate all sessions for security
    invalidateUserSessions(user);

    return "success";
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

  public UserProfileResponse getMyProfile() {
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

  public UserProfileResponse getUserProfile(String userId) {
    User user = userRepository
        .findById(UUID.fromString(userId))
        .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

    UserProfileResponse userProfileResponse = user.toUserProfileResponse();
    if (user.getStudentCode() != null && user.getClassCode() != null)
      userProfileResponse.setFacultyName(
          getAndValidateFacultiesCode(user.getStudentCode(), user.getClassCode()));
    return userProfileResponse;
  }

  public UserAuthResponse getAuthInfo() {
    return userProfileService.getAuthInfo();
  }

  public List<String> getPermissionOfCurrentUser() {
    UserPrincipal userPrincipal = getCurrentUserPrincipal();
    return userPrincipal.getAuthorities().stream()
        .map(
            auth -> {
              return auth.getAuthority();
            })
        .toList();
  }

  public String getAndValidateFacultiesCode(String studentCode, String classCode) {
    if (studentCode != null && studentCode.length() != 10)
      throw new AppException(ErrorCode.INVALID_STUDENT_CODE);
    if (classCode != null && classCode.length() < 10)
      throw new AppException(ErrorCode.INVALID_CLASS_CODE);
    String facultiesCodeFromStudent = studentCode.trim().toUpperCase().substring(5, 7);
    String facultiesCodeFromClass = classCode.trim().toUpperCase().substring(5, 7);
    if (!facultiesCodeFromStudent.equals(facultiesCodeFromClass)) {
      throw new AppException(ErrorCode.INVALID_FACULTY_CODE);
    }
    Optional<Faculty> faculty = facultyRepository.findByFacultyCode(facultiesCodeFromClass);
    if (faculty.isEmpty())
      throw new AppException(ErrorCode.FACULTY_NOT_FOUND);
    return faculty.get().getFacultyName();
  }

  @Transactional
  public UserProfileResponse updateUserProfile(
      com.graduation.project.auth.dto.UserProfileUpdateDto profileData,
      org.springframework.web.multipart.MultipartFile avatarFile) {
    User user = getCurrentUser();
    if (user == null) {
      throw new AppException(ErrorCode.USER_NOT_FOUND);
    }

    // Track uploaded file for rollback if needed
    String uploadedFileName = null;

    try {
      // Handle avatar upload first
      if (avatarFile != null && !avatarFile.isEmpty()) {
        uploadedFileName = firebaseService.uploadFile(avatarFile, AVATAR_FOLDER);
        // uploadFile returns just fileName, but stores at "folderName + fileName"
        // getPublicUrl needs the full path
        String fullPath = AVATAR_FOLDER + uploadedFileName;
        String newAvatarUrl = firebaseService.getPublicUrl(fullPath);
        user.setAvatarUrl(newAvatarUrl);
      }

      // Update text fields
      user.setFullName(profileData.getFullName());

      if (profileData.getPhone() != null && !profileData.getPhone().isEmpty()) {
        user.setPhone(profileData.getPhone());
      }

      // Handle student/class code and faculty validation
      String facultiesName = "";
      if (profileData.getClassCode() != null && !profileData.getClassCode().isEmpty()
          && profileData.getStudentCode() != null && !profileData.getStudentCode().isEmpty()) {
        facultiesName = getAndValidateFacultiesCode(
            profileData.getStudentCode(), profileData.getClassCode());
        user.setStudentCode(profileData.getStudentCode());
        user.setClassCode(profileData.getClassCode());
      }

      // Save to database (this is the critical point)
      userRepository.save(user);

      // Build response
      UserProfileResponse userProfileResponse = user.toUserProfileResponse();
      userProfileResponse.setFacultyName(facultiesName);
      return userProfileResponse;

    } catch (Exception e) {
      // Manual rollback: delete uploaded file if DB save failed
      if (uploadedFileName != null) {
        try {
          firebaseService.deleteFile(AVATAR_FOLDER + "/" + uploadedFileName);
          log.warn("Rolled back uploaded file: {}/{} due to error: {}",
              AVATAR_FOLDER, uploadedFileName, e.getMessage());
        } catch (Exception deleteException) {
          log.error("Failed to delete orphan file: {}/{}",
              AVATAR_FOLDER, uploadedFileName, deleteException);
        }
      }

      // Re-throw the original exception
      if (e instanceof AppException) {
        throw (AppException) e;
      }
      throw new AppException(ErrorCode.UPLOAD_FILE_FAILED);
    }
  }

  private void invalidateUserSessions(User user) {
    // Logic to invalidate sessions would go here.
    // Since we don't have access to all active tokens in a stateless system without
    // a registry,
    // we log this event. A real implementation might use a "credentialsChangedAt"
    // timestamp on the User entity
    // or a blacklist mechanism if we knew the tokens.
    log.info("User {} changed password. All active sessions should be considered invalid.", user.getEmail());
  }
}
