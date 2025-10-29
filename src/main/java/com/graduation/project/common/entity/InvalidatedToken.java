package com.graduation.project.common.entity;

import jakarta.persistence.*;

import java.util.Date;
import java.util.UUID;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "invalidated_tokens")
public class InvalidatedToken {
  @Id private UUID id;

  private Date expiryTime;
  private String jit;
  private Date issuedAt;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;
}
