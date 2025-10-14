package com.graduation.project.common.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "verification_tokens")
public class VerificationToken {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  private String token;

  @OneToOne
  @JoinColumn(name = "user_id")
  private User user;

  private LocalDateTime expiryDate;
}
