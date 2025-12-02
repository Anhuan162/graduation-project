package com.graduation.project.library.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.graduation.project.cpa.constant.CohortCode;
import com.graduation.project.announcement.entity.Faculty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "subject_references")
public class SubjectReference {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @NotNull
  @JsonIgnore
  @ManyToOne
  @JoinColumn(name = "subject_id")
  private Subject subject;

  @NotNull
  @JsonIgnore
  @ManyToOne
  @JoinColumn(name = "faculty_id")
  private Faculty faculty;

  @NotNull
  @JsonIgnore
  @ManyToOne
  @JoinColumn(name = "semester_id")
  private Semester semester;

  @NotNull
  @Enumerated(EnumType.STRING)
  private CohortCode cohortCode;
}
