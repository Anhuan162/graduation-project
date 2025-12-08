package com.graduation.project.announcement.dto;

import com.graduation.project.announcement.entity.Announcement;
import java.util.Set;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AnnouncementCreatedEvent {
  private UUID announcementId;
  private String title;
  private String content;
  private UUID actorId;
  private String actorEmail;
  private Set<String> allClassroomCodes;

  public static AnnouncementCreatedEvent from(
      Announcement announcement, Set<String> allClassroomCodes) {
    return AnnouncementCreatedEvent.builder()
        .announcementId(announcement.getId())
        .title(announcement.getTitle())
        .content(announcement.getContent())
        .actorEmail(announcement.getCreatedBy().getEmail())
        .actorId(announcement.getCreatedBy().getId())
        .allClassroomCodes(allClassroomCodes)
        .build();
  }
}
