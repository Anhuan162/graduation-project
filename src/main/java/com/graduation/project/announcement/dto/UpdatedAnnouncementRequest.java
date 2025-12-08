package com.graduation.project.announcement.dto;

import com.graduation.project.announcement.entity.Announcement;
import com.graduation.project.common.entity.User;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.graduation.project.cpa.constant.CohortCode;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UpdatedAnnouncementRequest {
  String title;
  Boolean announcementStatus;
  String content;
  String announcementType;
  List<UUID> facultyIds;
  List<String> classCodes;
  List<CohortCode> schoolYearCodes;

  public static Announcement toAnnouncement(UpdatedAnnouncementRequest request, User user) {
    return Announcement.builder()
        .id(UUID.randomUUID())
        .title(request.getTitle())
        .content(request.getContent())
        .createdBy(user)
        .createdDate(LocalDate.now())
        .build();
  }
}
