package com.graduation.project.announcement.dto;

import com.graduation.project.announcement.constant.AnnouncementType;
import java.time.LocalDate;
import lombok.Data;

@Data
public class SearchAnnouncementRequest {
  private String title;
  private AnnouncementType announcementType;
  private Boolean announcementStatus;
  private LocalDate fromDate;
  private LocalDate toDate;
}
