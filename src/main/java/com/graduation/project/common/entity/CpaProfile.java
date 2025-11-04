package com.graduation.project.common.entity;

import jakarta.persistence.*;
import java.util.List;
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

  @Column(name = "cpa_profile_name")
  private String cpaProfileName;

  @Column(name = "cpa_profile_code")
  private String cpaProfileCode;

  @Column(name = "letter_cpa_score")
  private double letterCpaScore;

  @Column(name = "number_cpa_score")
  private double numberCpaScore;

  @Column(name = "previous_number_cpa_score")
  private double previousNumberCpaScore;

  @Column(name = "accumulated_credits")
  private int accumulatedCredits;

  @OneToMany(mappedBy = "cpaProfile", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<GpaProfile> gpaProfiles;
}
