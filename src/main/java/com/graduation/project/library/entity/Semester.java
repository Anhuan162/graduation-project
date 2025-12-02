package com.graduation.project.library.entity;

import com.graduation.project.library.dto.SemesterResponse;
import com.graduation.project.library.constant.SemesterType;
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
  @Id private Integer id;

  @Enumerated(EnumType.STRING)
  private SemesterType semesterType;

  private int schoolYear;

  @OneToMany(mappedBy = "semester", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<SubjectReference> subjectReferences;

//  ThieuNN
  public SemesterResponse toResponse() {
    return SemesterResponse.builder()
            .id(this.id)
            .schoolYear(this.schoolYear)
            .semesterType(this.semesterType.toString())
            .build();

  }
}
