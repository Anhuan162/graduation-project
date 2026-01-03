package com.graduation.project.announcement.entity;

import com.graduation.project.announcement.constant.AnnouncementType;
import com.graduation.project.common.entity.User;
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
@Table(name = "announcements")
public class Announcement {
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
  private Boolean announcementStatus;

  @Enumerated(EnumType.STRING)
  private AnnouncementType announcementType;

  @OneToMany(mappedBy = "announcement", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  @Builder.Default
  private List<AnnouncementTarget> targets = new ArrayList<>();
}
