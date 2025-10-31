package com.graduation.project.common.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "semesters")
public class Semester {
  @Id private String id;

  @Enumerated(EnumType.STRING)
  private SemesterType semesterType;

  private int schoolYear;

  @OneToMany(mappedBy = "semester", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<SubjectReference> subjectReferences;
}
