package com.graduation.project.common.entity;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cpa_profiles")
public class CpaProfile {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "name_cpa_profile")
  private String nameCpaProfile;

  @Column(name = "cpa_profile_code")
  private String cpaProfileCode;

  private double letterCpaScore;
  private double numberCpaScore;

}
