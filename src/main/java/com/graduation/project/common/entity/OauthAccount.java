package com.graduation.project.common.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "oauth_accounts")
public class OauthAccount {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  private Provider provider; // GOOGLE, FACEBOOK

  @Column(nullable = false)
  private String providerUserId; // Google/FB ID

  private String accessToken;
  private String refreshToken;
  private LocalDateTime expiresAt;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  // getters/setters
}
