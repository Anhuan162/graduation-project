package com.graduation.project.cpa.entity;

import com.graduation.project.common.entity.User;
import jakarta.persistence.*;
import java.util.ArrayList;
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
  private String letterCpaScore;

  @Column(name = "number_cpa_score")
  private Double numberCpaScore;

  @Column(name = "previous_number_cpa_score")
  private Double previousNumberCpaScore;

  @Column(name = "total_accumulated_score")
  private Double totalAccumulatedScore;

  @Column(name = "accumulated_credits")
  private int accumulatedCredits;

  @Builder.Default
  @OneToMany(mappedBy = "cpaProfile", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<GpaProfile> gpaProfiles = new ArrayList<>();

  @OneToOne
  @JoinColumn(name = "user_id")
  private User user;
}
