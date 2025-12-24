package com.graduation.project.announcement.entity;

import com.graduation.project.announcement.dto.FacultyResponse;
import com.graduation.project.library.entity.SubjectReference;
import jakarta.persistence.*;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "faculties")
public class Faculty {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "faculty_name")
  private String facultyName;

  @Column(name = "faculty_code")
  private String facultyCode;

  private String description;

  @OneToMany(mappedBy = "faculty", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<SubjectReference> subjectReferences;

  // ThieuNN
  public FacultyResponse toFacultyResponse() {
    return FacultyResponse.builder()
        .id(this.id.toString())
        .facultyName(this.facultyName)
        .facultyCode(this.facultyCode)
        .description(this.description)
        .build();
  }
}
