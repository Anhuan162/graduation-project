package com.graduation.project.common.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "grade_subject_average_profiles")
public class GradeSubjectAverageProfile {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @JsonIgnore
  @ManyToOne
  @JoinColumn(name = "cpa_profile_id")
  private CpaProfile cpaProfile;
}
