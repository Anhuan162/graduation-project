package com.graduation.project.announcement.dto;

import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AnnouncementTargetResponse {
  UUID id;
  String classroomCode;
}
