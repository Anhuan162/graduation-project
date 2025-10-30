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
@Table(name = "subject_references")
public class SubjectReference {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne
  @JoinColumn(name = "subject_id")
  private Subject subject;

  @ManyToOne
  @JoinColumn(name = "faculty_id")
  private Faculty faculty;

  private SemesterType semesterType;
}
