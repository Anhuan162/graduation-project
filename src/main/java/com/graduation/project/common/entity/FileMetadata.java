package com.graduation.project.common.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.graduation.project.common.constant.AccessType;
import com.graduation.project.common.constant.ResourceType;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "file_metadata")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileMetadata {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  private String fileName;

  private String folder;

  private String url;

  private String contentType;

  private int size;

  @Enumerated(EnumType.STRING)
  private AccessType accessType;

  @Enumerated(EnumType.STRING)
  private ResourceType resourceType;

  private UUID resourceId;

  private LocalDateTime createdAt = LocalDateTime.now();

  @Column(name = "is_on_drive")
  private Boolean isOnDrive;

  @JsonIgnore
  @ManyToOne
  @JoinColumn(name = "user_id")
  private User user;
}
