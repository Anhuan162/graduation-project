package com.graduation.project.user.dto;

import com.graduation.project.common.entity.AnnouncementType;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AnnouncementRequest {
  String title;
  String content;

  String createdByFullName;
  String modifiedByFullName;

  LocalDate createdDate;
  LocalDate modifiedDate;
  Boolean announcementStatus;
  AnnouncementType announcementType;
}
