package com.graduation.project.announcement.entity;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "classrooms")
public class Classroom {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "class_name")
  private String className;

  @Column(name = "class_code")
  private String classCode;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "faculty_id")
  private Faculty faculty;

  @Column(name = "started_year")
  private int startedYear;

  @Column(name = "ended_year")
  private int endedYear;

  @Column(name = "school_year_code")
  private String schoolYearCode;
}
