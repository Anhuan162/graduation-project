package com.graduation.project.auth.repository;

import com.graduation.project.common.entity.PasswordResetSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PasswordResetSessionRepository extends JpaRepository<PasswordResetSession, UUID> {
    PasswordResetSession findPasswordResetSessionByEmailAndOtp(String email, String otp);
}
