package com.graduation.project.common.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FileMetadataResponse {
  private UUID id;

  private String fileName;

  private String folder;

  private String url;

  private String contentType;

  private int size;

  private String accessType;

  private String resourceType;

  private UUID resourceId;

  private LocalDateTime createdAt = LocalDateTime.now();

  private String userId;
}
