package com.graduation.project.common.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "documents")
public class Document {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  private String title;
  private String description;
  private String filePath;

  @ManyToOne
  @JoinColumn(name = "subject_id")
  private Subject subject;

  @ManyToOne
  @JoinColumn(name = "uploaded_by")
  private User uploadedBy;

  @ManyToOne
  @JoinColumn(name = "approved_by")
  private User approvedBy;

  private DocumentStatus documentStatus;
  private DocumentType documentType;
  private int size;

  private String originalFilename;
  private String storageProvider;

  // Định dạng tệp
  private String mimeType;

  // Hash code - giúp xác định tệp sai hay trùng lặp không
  private String checksum;
  // authentication
  private VisibilityStatus visibility;

  private int downloadCount;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private LocalDateTime approvedAt;
  private LocalDateTime deletedAt;
}
