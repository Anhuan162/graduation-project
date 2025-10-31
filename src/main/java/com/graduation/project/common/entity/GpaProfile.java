package com.graduation.project.common.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "gpa_profiles")
public class GpaProfile {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;


}
