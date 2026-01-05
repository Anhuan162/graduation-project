package com.graduation.project.announcement.dto;

import com.graduation.project.announcement.entity.Announcement;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class DetailedAnnouncementResponse {
  UUID id;
  String title;
  String content;
  String announcementType;
  Boolean announcementStatus;
  String createdBy;
  LocalDate createdDate;
  String modifiedBy;
  LocalDate modifiedDate;
  List<AnnouncementFileResponse> attachments;

  public static DetailedAnnouncementResponse from(
      Announcement announcement, List<AnnouncementFileResponse> attachments) {
    return DetailedAnnouncementResponse.builder()
        .id(announcement.getId())
        .title(announcement.getTitle())
        .content(announcement.getContent())
        .announcementStatus(announcement.getAnnouncementStatus())
        .announcementType(String.valueOf(announcement.getAnnouncementType()))
        .createdBy(
            Objects.nonNull(announcement.getCreatedBy())
                ? announcement.getCreatedBy().getEmail()
                : "")
        .createdDate(announcement.getCreatedDate())
        .modifiedBy(
            Objects.nonNull(announcement.getModifiedBy())
                ? announcement.getModifiedBy().getFullName()
                : "")
        .modifiedDate(announcement.getModifiedDate())
        .attachments(attachments)
        .build();
  }
}
