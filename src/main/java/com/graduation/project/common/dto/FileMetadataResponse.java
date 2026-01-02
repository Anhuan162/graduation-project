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
  UUID id;

  String fileName;
  String folder;
  String url;
  String contentType;

  long size;

  String storagePath;

  String accessType;
  String resourceType;
  UUID resourceId;

  @Builder.Default
  LocalDateTime createdAt = LocalDateTime.now();

  String userId;
}
