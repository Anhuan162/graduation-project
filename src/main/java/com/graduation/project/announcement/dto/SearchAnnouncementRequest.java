package com.graduation.project.announcement.dto;

import java.time.LocalDate;
import lombok.Data;

@Data
public class SearchAnnouncementRequest {
  String title;
  String announcementType;
  Boolean announcementStatus;
  String announcementProvider;
  LocalDate fromDate;
  LocalDate toDate;
}
