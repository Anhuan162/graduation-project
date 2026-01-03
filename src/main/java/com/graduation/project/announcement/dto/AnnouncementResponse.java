package com.graduation.project.announcement.dto;

import com.graduation.project.announcement.constant.AnnouncementType;
import com.graduation.project.announcement.entity.Announcement;

import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnnouncementResponse {

  private UUID id;

  private String title;

  private String content;

  private AnnouncementType announcementType;

  private Boolean announcementStatus;

  private String createdBy;

  private LocalDate createdDate;

  private String modifiedBy;

  private LocalDate modifiedDate;

  public static AnnouncementResponse from(Announcement a) {
    if (a == null)
      return null;
    return AnnouncementResponse.builder()
        .id(a.getId())
        .title(a.getTitle())
        .content(a.getContent())
        .announcementType(a.getAnnouncementType())
        .announcementStatus(a.getAnnouncementStatus())
        .createdDate(a.getCreatedDate())
        .modifiedDate(a.getModifiedDate())
        .createdBy(a.getCreatedBy() != null ? a.getCreatedBy().getEmail() : null)
        .modifiedBy(a.getModifiedBy() != null ? a.getModifiedBy().getEmail() : null)
        .build();
  }
}
