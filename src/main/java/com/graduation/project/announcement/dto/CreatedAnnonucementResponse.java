package com.graduation.project.announcement.dto;

import com.graduation.project.announcement.entity.Announcement;
import java.time.LocalDate;
import java.util.List;
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
  List<String> urls;

  public static CreatedAnnonucementResponse from(Announcement announcement, List<String> urls) {
    return CreatedAnnonucementResponse.builder()
        .id(announcement.getId())
        .title(announcement.getTitle())
        .content(announcement.getContent())
        .announcementType(String.valueOf(announcement.getAnnouncementType()))
        .createdBy(announcement.getCreatedBy().getEmail())
        .createdDate(announcement.getCreatedDate())
        .urls(urls)
        .build();
  }
}
