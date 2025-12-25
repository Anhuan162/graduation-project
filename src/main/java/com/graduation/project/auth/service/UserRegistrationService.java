package com.graduation.project.auth.service;

import com.graduation.project.auth.constant.PredefinedRole;
import com.graduation.project.auth.dto.VerifyUserDto;
import com.graduation.project.auth.dto.request.SignupRequest;
import com.graduation.project.auth.dto.response.SignupResponse;
import com.graduation.project.auth.repository.RoleRepository;
import com.graduation.project.auth.repository.UserRepository;
import com.graduation.project.auth.repository.VerificationTokenRepository;
import com.graduation.project.common.constant.Provider;
import com.graduation.project.common.entity.Role;
import com.graduation.project.common.entity.User;
import com.graduation.project.common.entity.VerificationToken;
import com.graduation.project.security.exception.AppException;
import com.graduation.project.security.exception.ErrorCode;

import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.security.SecureRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class UserRegistrationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final VerificationTokenRepository verificationTokenRepository;
    private final RoleRepository roleRepository;

    private static final SecureRandom secureRandom = new SecureRandom();

    // ===== REGISTER =====
    @Transactional
    public SignupResponse register(SignupRequest request) {
        String email = request.getEmail();
        String rawPassword = request.getPassword();

        if (userRepository.findByEmail(email).isPresent()) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        HashSet<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findById(PredefinedRole.USER_ROLE)
                .orElseThrow(() -> new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION));
        roles.add(userRole);

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

    // ===== VERIFY EMAIL =====
    @Transactional
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

        // prevent reuse
        verificationTokenRepository.delete(verificationToken);
    }

    // ===== RESEND CODE =====
    @Transactional
    public void resendVerificationCode(String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }

        User user = optionalUser.get();
        if (Boolean.TRUE.equals(user.getEnabled())) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Account already verified");
        }

        verificationTokenRepository.deleteByUserId(user.getId());
        verificationTokenRepository.flush();

        String token = generateVerificationCode();
        storeVerifyToken(user, token);
        sendVerificationEmail(user, token);
    }

    // ===== INTERNALS =====
    private void storeVerifyToken(User user, String token) {
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(user);
        verificationToken.setExpiryDate(LocalDateTime.now().plusMinutes(10));
        verificationTokenRepository.save(verificationToken);
    }

    private void sendVerificationEmail(User user, String token) {
        String subject = "Account Verification";
        String verificationCode = token;

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
        int code = secureRandom.nextInt(900000) + 100000;
        return String.valueOf(code);
    }
}
