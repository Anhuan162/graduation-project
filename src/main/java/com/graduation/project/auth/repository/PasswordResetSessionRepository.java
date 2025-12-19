package com.graduation.project.auth.repository;

import com.graduation.project.common.entity.PasswordResetSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface PasswordResetSessionRepository extends JpaRepository<PasswordResetSession, UUID> {
    PasswordResetSession findPasswordResetSessionByEmailAndOtp(String email, String otp);

    @Query("SELECT p from PasswordResetSession as p " +
            " where p.email = :email " +
            " and (p.used is null or p.used = false ) " +
            " order by p.expiresAt DESC ")
    PasswordResetSession findByEmailAndNotUsed(
            @Param("email") String email);
}
