package com.graduation.project.library.entity;

import com.graduation.project.common.entity.User;
import com.graduation.project.library.constant.DocumentStatus;
import com.graduation.project.library.constant.DocumentType;
import com.graduation.project.library.constant.VisibilityStatus;
import com.graduation.project.library.dto.DocumentResponse;
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

  @Enumerated(EnumType.STRING)
  private DocumentStatus documentStatus;

  @Enumerated(EnumType.STRING)
  private DocumentType documentType;

  private int size;

  private String originalFilename;
  private String storageProvider;

  // Định dạng tệp
  private String mimeType;

  // Hash code - giúp xác định tệp sai hay trùng lặp không
  private String checksum;
  // authentication
  @Enumerated(EnumType.STRING)
  private VisibilityStatus visibility;

  private int downloadCount;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private LocalDateTime approvedAt;
  private LocalDateTime deletedAt;
  private String imageUrl;

  public DocumentResponse toDocumentResponse() {
    return DocumentResponse.builder()
        .title(this.title)
        .description(this.description)
        .documentType(this.documentType)
        .urlDoc(this.filePath)
        .urlImage(this.imageUrl)
        .subjectId(this.id)
        .id(this.id.toString())
        .build();
  }
}
