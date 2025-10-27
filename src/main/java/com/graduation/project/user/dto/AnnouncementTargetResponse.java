package com.graduation.project.user.dto;

import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AnnouncementTargetResponse {
  UUID id;
  String classroomCode;
}
