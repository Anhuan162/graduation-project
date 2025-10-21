package com.graduation.project.common.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.util.Date;
import java.util.UUID;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class InvalidatedToken {
  @Id private UUID id;

  private Date expiryTime;
  private String jit;
  private Date issuedAt;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;
}
