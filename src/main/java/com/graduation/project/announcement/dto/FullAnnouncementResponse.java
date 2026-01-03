package com.graduation.project.announcement.dto;

import com.graduation.project.announcement.constant.AnnouncementType;
import com.graduation.project.common.dto.FileMetadataResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FullAnnouncementResponse {
  UUID id;
  String title;
  String content;

  String createdByFullName;
  String modifiedByFullName;

  LocalDate createdDate;
  LocalDate modifiedDate;
  Boolean announcementStatus;
  AnnouncementType announcementType;

  List<AnnouncementTargetResponse> announcementTargetResponses;
  List<FileMetadataResponse> attachments;
}
