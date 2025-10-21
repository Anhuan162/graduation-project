package com.graduation.project.common.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "annoucements")
public class Annoucement {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  private String title;
  private String content;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "created_by")
  private User createdBy;

  private LocalDate createdDate;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "modified_by")
  private User modifiedBy;

  private LocalDate modifiedDate;
  private Boolean annoucementStatus;

  @Enumerated(EnumType.STRING)
  private AnnoucementType annoucementType;

  @OneToMany(
      mappedBy = "annoucement",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  private List<AnnoucementTarget> targets = new ArrayList<>();
}
