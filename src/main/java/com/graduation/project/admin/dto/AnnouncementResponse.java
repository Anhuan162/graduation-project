package com.graduation.project.admin.dto;

import com.graduation.project.common.entity.Announcement;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AnnouncementResponse {
  UUID id;
  String title;
  String content;
  String announcementType;
  Boolean announcementStatus;
  String modifiedBy;
  LocalDate modifiedDate;

  public static AnnouncementResponse from(Announcement announcement) {
    return AnnouncementResponse.builder()
        .id(announcement.getId())
        .title(announcement.getTitle())
        .content(announcement.getContent())
        .modifiedBy(
            Objects.nonNull(announcement.getModifiedBy())
                ? announcement.getModifiedBy().getFullName()
                : "")
        .modifiedDate(announcement.getModifiedDate())
        .announcementStatus(announcement.getAnnouncementStatus())
        .build();
  }

  public static List<AnnouncementResponse> from(List<Announcement> announcements) {
    return announcements.stream().map(AnnouncementResponse::from).toList();
  }
}
