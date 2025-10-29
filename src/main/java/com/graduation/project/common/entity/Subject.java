package com.graduation.project.common.entity;

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
  List<Document> documents;
}
