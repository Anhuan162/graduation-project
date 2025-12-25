package com.graduation.project.auth.service;

import com.graduation.project.auth.repository.PasswordResetSessionRepository;
import com.graduation.project.auth.repository.UserRepository;
import com.graduation.project.common.entity.PasswordResetSession;
import com.graduation.project.common.entity.User;
import com.graduation.project.security.exception.AppException;
import com.graduation.project.security.exception.ErrorCode;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetSessionRepository passwordResetSessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final CurrentUserService currentUserService;
    private final SecureRandom random = new SecureRandom();

    // ===== REQUEST OTP =====
    @Transactional
    public String sendOtpToUserToResetPassword(String email) {
        User user = userRepository.findUserByEmail(email);
        if (user == null) {
            throw new AppException(ErrorCode.EMAIL_NOT_FOUND);
        }

        String otp = generateOtp();

        PasswordResetSession session = passwordResetSessionRepository.findByEmailAndNotUsed(email);

        if (session == null) {
            PasswordResetSession newSession = new PasswordResetSession();
            newSession.setEmail(email);
            newSession.setOtp(otp);
            newSession.setExpiresAt(LocalDateTime.now().plusMinutes(5));
            try {
                passwordResetSessionRepository.save(newSession);
            } catch (Exception e) {
                log.error("Failed to persist password reset session for {}", email, e);
                throw e;
            }
        } else {
            session.setOtp(otp);
            session.setExpiresAt(LocalDateTime.now().plusMinutes(5));
            try {
                passwordResetSessionRepository.save(session);
            } catch (Exception e) {
                log.error("Failed to persist password reset session for {}", email, e);
                throw e;
            }
        }

        try {
            sendOtpEmail(user, otp);
        } catch (Exception e) {
            log.warn("Send email failed for {}", email, e);
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

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
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
}
