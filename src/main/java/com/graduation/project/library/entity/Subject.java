package com.graduation.project.library.entity;

import com.graduation.project.library.dto.SubjectResponse;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "subjects")
public class Subject {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  private String subjectName;
  private String subjectCode;
  private int credit;
  private String description;
  private LocalDateTime createdDate;
  private LocalDateTime lastModifiedDate;

  @OneToMany(mappedBy = "subject", cascade = CascadeType.ALL, orphanRemoval = true)
  List<SubjectReference> subjectReferences;

  @OneToMany(mappedBy = "subject", cascade = CascadeType.ALL, orphanRemoval = true)
  List<Document> documents;

//  ThieuNN
  public SubjectResponse toSubjectResponse() {
    return SubjectResponse.builder()
            .id(this.id)
            .subjectName(this.subjectName)
            .subjectCode(this.subjectCode)
            .description(this.description)
            .createdDate(this.createdDate)
            .lastModifiedDate(this.lastModifiedDate).build();
  }
}
