package com.graduation.project.common.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.UUID;
import lombok.*;

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

  @Column(name = "letter_current_score")
  private String letterCurrentScore;

  @Column(name = "letter_improvement_score")
  private String letterImprovementScore;

  @Column(name = "current_score")
  private Double currentScore;

  @Column(name = "improvement_score")
  private Double improvementScore;

  @JsonIgnore
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "gpa_profile_id")
  private GpaProfile gpaProfile;

  @JsonIgnore
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "subject_reference_id")
  private SubjectReference subjectReference;
}
