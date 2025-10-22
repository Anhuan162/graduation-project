package com.graduation.project.common.entity;

import jakarta.persistence.*;
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
}
