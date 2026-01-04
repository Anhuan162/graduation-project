package com.graduation.project.auth.service;

import com.graduation.project.auth.repository.InvalidatedTokenRepository;
import com.graduation.project.auth.repository.PasswordResetSessionRepository;
import com.graduation.project.auth.repository.UserRepository;
import com.graduation.project.auth.validation.StrongPasswordValidator;
import com.graduation.project.common.entity.InvalidatedToken;
import com.graduation.project.common.entity.PasswordResetSession;
import com.graduation.project.common.entity.User;
import com.graduation.project.security.exception.AppException;
import com.graduation.project.security.exception.ErrorCode;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetSessionRepository passwordResetSessionRepository;
    private final InvalidatedTokenRepository invalidatedTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final CurrentUserService currentUserService;
    private final StrongPasswordValidator passwordValidator = new StrongPasswordValidator();
    private final SecureRandom random = new SecureRandom();

    // ===== REQUEST OTP =====
    @Transactional
    public String sendOtpToUserToResetPassword(String email) {
        User user = userRepository.findUserByEmail(email);
        if (user == null) {
            log.info("OTP requested for non-existent email");
            return "success";
        }

        String otp = generateOtp();
        String hashedOtp = passwordEncoder.encode(otp);
        PasswordResetSession session = passwordResetSessionRepository.findByEmailAndNotUsed(email);

        if (session == null) {
            PasswordResetSession newSession = new PasswordResetSession();
            newSession.setEmail(email);
            newSession.setOtp(hashedOtp);
            newSession.setExpiresAt(LocalDateTime.now().plusMinutes(5));
            try {
                passwordResetSessionRepository.save(newSession);
            } catch (Exception e) {
                log.error("Failed to persist password reset session for user {}", user.getId(), e);
                throw e;
            }
        } else {
            session.setOtp(hashedOtp);
            session.setExpiresAt(LocalDateTime.now().plusMinutes(5));
            try {
                passwordResetSessionRepository.save(session);
            } catch (Exception e) {
                log.error("Failed to persist password reset session for user {}", user.getId(), e);
                throw e;
            }
        }

        try {
            sendOtpEmail(user, otp);
        } catch (Exception e) {
            log.warn("Send email failed for user {}", user.getId(), e);
            throw new AppException(ErrorCode.CAN_NOT_SEND_EMAIL);
        }

        return email;
    }

    // ===== VERIFY OTP =====
    public String verifyOtp(String otp, String email) {
        PasswordResetSession session = passwordResetSessionRepository.findPasswordResetSessionByEmailAndOtp(email, otp);

        if (session == null) {
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }
        if (session.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.TOKEN_EXPIRED);
        }
        return session.getId().toString();
    }

    // ===== RESET WITH OTP =====
    @Transactional
    public String resetPasswordWithOtp(String email, String otp, String newPassword) {
        if (newPassword == null || newPassword.length() < 8) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Password must be at least 8 characters");
        }

        PasswordResetSession session = passwordResetSessionRepository.findPasswordResetSessionByEmailAndOtp(email, otp);

        if (session == null) {
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }
        if (Boolean.TRUE.equals(session.getUsed())) {
            throw new AppException(ErrorCode.SESSION_RESET_PASSWORD_HAS_USED);
        }
        if (session.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.TOKEN_EXPIRED);
        }
        User user = userRepository.findUserByEmail(session.getEmail());
        if (user == null) {
            throw new AppException(ErrorCode.EMAIL_NOT_FOUND);
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        session.setUsed(true);
        passwordResetSessionRepository.save(session);
        return "success";
    }

    // ===== CHANGE PASSWORD WHEN AUTHENTICATED =====
    @Transactional
    public String changePasswordAuthenticated(String oldPassword, String newPassword) {
        User user = currentUserService.getCurrentUserEntity();

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Old password is incorrect");
        }

        // Validate new password strength
        if (!passwordValidator.isValid(newPassword, null)) {
            throw new AppException(ErrorCode.BAD_REQUEST,
                    "Password must be 8–32 chars, include upper, lower, digit, and special character, and contain no spaces.");
        }

        // Prevent reusing current password
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new AppException(ErrorCode.BAD_REQUEST, "New password cannot be the same as the current password.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Invalidate all other active sessions/tokens for this user
        invalidateOtherUserSessions(user);

        return "success";
    }

    // ===== INTERNALS =====
    private String generateOtp() {
        int code = random.nextInt(900000) + 100000;
        return String.valueOf(code);
    }

    private void sendOtpEmail(User user, String otp) {
        String subject = "Reset password OTP";
        String html = "<html><body>"
                + "<p>Your OTP to reset password is: <b>"
                + otp
                + "</b></p>"
                + "<p>This OTP will expire in 5 minutes.</p>"
                + "</body></html>";
        emailService.sendVerificationEmail(user.getEmail(), subject, html);
    }

    private void invalidateOtherUserSessions(User user) {
        // Get the current JWT token
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getCredentials() instanceof Jwt jwt) {
            String currentJit = jwt.getId();
            Date issuedAt = Date.from(jwt.getIssuedAt());
            Date expiryTime = Date.from(jwt.getExpiresAt());

            // Invalidate all other tokens for this user by creating invalidation records
            // Since JWTs are stateless, we can't directly invalidate all active tokens,
            // but we can invalidate all previously invalidated tokens and the current one
            List<InvalidatedToken> existingInvalidatedTokens = invalidatedTokenRepository.findByUser(user);

            // Invalidate the current token
            InvalidatedToken currentTokenInvalidation = InvalidatedToken.builder()
                    .id(UUID.randomUUID())
                    .jit(currentJit)
                    .issuedAt(issuedAt)
                    .expiryTime(expiryTime)
                    .user(user)
                    .build();
            invalidatedTokenRepository.save(currentTokenInvalidation);

            // Note: In a complete implementation, you would need to modify the token
            // verification
            // logic to check if tokens were issued before the password change time.
            // For now, we invalidate the current token and log the action.
            log.info("Invalidated current session token for user {} after password change", user.getId());
        } else {
            log.warn("Could not retrieve current JWT token for session invalidation");
        }
    }
}
