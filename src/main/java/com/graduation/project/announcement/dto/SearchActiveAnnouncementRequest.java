package com.graduation.project.announcement.dto;

import com.graduation.project.announcement.constant.AnnouncementProvider;
import lombok.Data;

@Data
public class SearchActiveAnnouncementRequest {
  String title;
  AnnouncementProvider announcementProvider;
}
