package com.graduation.project.common.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Builder
@Table(name = "password_reset_session")
public class PasswordResetSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String email;
    private String otp;

    @Column(name = "is_verified")
    private Boolean isVerified;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    private Boolean used;

    @Column(name = "attempt_count")
    private Integer AttemptCount;


}
