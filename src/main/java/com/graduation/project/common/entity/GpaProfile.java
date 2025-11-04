package com.graduation.project.common.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
@Table(name = "gpa_profiles")
public class GpaProfile {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "gpa_profile_code")
  private String gpaProfileCode;

  @Column(name = "letter_gpa_score")
  private double letterGpaScore;

  @Column(name = "number_gpa_score")
  private double numberGpaScore;

  @Column(name = "previous_number_gpa_score")
  private double previousNumberGpaScore;

  @Column(name = "passed_credits")
  private int passedCredits;

  @JsonIgnore
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "cpa_profile_id")
  private CpaProfile cpaProfile;

  @OneToMany(mappedBy = "gpaProfile", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<GradeSubjectAverageProfile> gradeSubjectAverageProfiles;
}
