package com.graduation.project.library.entity;

import com.graduation.project.common.converter.StringListConverter;
import com.graduation.project.common.entity.User;
import com.graduation.project.library.constant.DocumentStatus;
import com.graduation.project.library.constant.DocumentType;
import com.graduation.project.library.constant.VisibilityStatus;
import com.graduation.project.library.dto.DocumentResponse;
import com.graduation.project.library.dto.SubjectResponse;
import com.graduation.project.auth.dto.response.UserProfileResponse;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "documents")
@EntityListeners(AuditingEntityListener.class)
public class Document {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @NotNull
  private String title;

  @Column(columnDefinition = "TEXT")
  private String description;

  private String filePath;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "subject_id")
  @ToString.Exclude
  private Subject subject;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "uploaded_by")
  @ToString.Exclude
  private User uploadedBy;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "approved_by")
  @ToString.Exclude
  private User approvedBy;

  @Enumerated(EnumType.STRING)
  @NotNull
  private DocumentStatus documentStatus;

  @Enumerated(EnumType.STRING)
  private DocumentType documentType;

  // File attributes
  private long size;

  @Positive
  private Integer pageCount;

  @Column(columnDefinition = "TEXT")
  private String thumbnailUrl;

  @Builder.Default
  private Boolean isPremium = false;

  @Convert(converter = StringListConverter.class)
  @Column(columnDefinition = "TEXT")
  private List<String> previewImages;

  private String originalFilename;
  private String storageProvider;

  // Định dạng tệp
  private String mimeType;

  // Hash code - giúp xác định tệp sai hay trùng lặp không
  private String checksum;

  // authentication
  private VisibilityStatus visibility;

  @Builder.Default
  private int downloadCount = 0;

  @CreatedDate
  private LocalDateTime createdAt;

  @LastModifiedDate
  private LocalDateTime updatedAt;

  private LocalDateTime approvedAt;
  private LocalDateTime deletedAt;

  @Column(columnDefinition = "TEXT")
  private String rejectionReason;

  // Only use this if you really need a separate image URL outside of thumbnail
  // But redundant with thumbnailUrl usually
  private String imageUrl;

  public DocumentResponse toDocumentResponse() {
    SubjectResponse subjectResponse = null;
    if (this.subject != null) {
      // Assuming Subject has toSubjectResponse method or we build manually
      subjectResponse = SubjectResponse.builder()
          .id(this.subject.getId())
          .subjectName(this.subject.getSubjectName())
          .subjectCode(this.subject.getSubjectCode())
          .build();
    }

    UserProfileResponse authorResponse = null;
    if (this.uploadedBy != null) {
      authorResponse = UserProfileResponse.builder()
          .email(this.uploadedBy.getEmail())
          .fullName(this.uploadedBy.getFullName())
          .avatarUrl(this.uploadedBy.getAvatarUrl())
          .build();
    }

    return DocumentResponse.builder()
        .id(this.id)
        .title(this.title)
        .description(this.description)
        .documentType(this.documentType)
        .documentStatus(this.documentStatus)
        .urlDoc(this.filePath)
        .thumbnailUrl(this.thumbnailUrl)
        .previewImages(this.previewImages)
        .uploadedBy(authorResponse)
        .subject(subjectResponse)
        .pageCount(this.pageCount)
        .isPremium(this.isPremium)
        .fileSize(this.size)
        .createdAt(this.createdAt)
        .rejectionReason(this.rejectionReason)
        .build();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    Document document = (Document) o;
    return Objects.equals(id, document.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
