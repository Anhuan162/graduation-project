package com.graduation.project.admin.dto;

import com.graduation.project.common.entity.Announcement;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CreatedAnnonucementResponse {
  UUID id;
  String title;
  String content;
  String announcementType;
  String createdBy;
  LocalDate createdDate;

  public static CreatedAnnonucementResponse from(Announcement announcement) {
    return CreatedAnnonucementResponse.builder()
        .id(announcement.getId())
        .title(announcement.getTitle())
        .content(announcement.getContent())
        .createdBy(announcement.getCreatedBy().getFullName())
        .createdDate(announcement.getCreatedDate())
        .build();
  }
}
