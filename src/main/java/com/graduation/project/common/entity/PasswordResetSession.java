package com.graduation.project.common.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Data
@Entity
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
}
