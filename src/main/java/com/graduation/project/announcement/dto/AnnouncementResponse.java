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
public class AnnouncementResponse {
  UUID id;
  String title;
  String content;
  String announcementType;
  Boolean announcementStatus;
  String createdBy;
  LocalDate createdDate;
  String modifiedBy;
  Boolean onDrive;
  LocalDate modifiedDate;

  public static AnnouncementResponse from(Announcement announcement) {
    return AnnouncementResponse.builder()
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
        .onDrive(announcement.getOnDrive())
        .modifiedDate(announcement.getModifiedDate())
        .build();
  }

  public static List<AnnouncementResponse> from(List<Announcement> announcements) {
    return announcements.stream().map(AnnouncementResponse::from).toList();
  }
}
