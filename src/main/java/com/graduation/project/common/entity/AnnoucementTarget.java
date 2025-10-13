package com.graduation.project.common.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "annoucement_targets")
public class AnnoucementTarget {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String classroomCode;
    private String facultyCode;
}
